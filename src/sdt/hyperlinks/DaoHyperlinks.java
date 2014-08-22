package sdt.hyperlinks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import sdt.SDTPlugin;
import sdt.core._;

@SuppressWarnings("restriction")
public class DaoHyperlinks extends AbstractHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {

		IDocument document = textViewer.getDocument();
		if (document == null)
			return null;

		int offset = region.getOffset();
		IRegion lineInfo;
		String line;
		try {
			lineInfo = document.getLineInformationOfOffset(offset);
			line = document.get(lineInfo.getOffset(), lineInfo.getLength());
		} catch (BadLocationException ex) {
			return null;
		}

		int offsetInLine = offset - lineInfo.getOffset();

		StringBuffer pre = new StringBuffer();
		StringBuffer key = new StringBuffer();

		int start = -1;
		int end = -1;
		boolean hasKey = false;
		for (int i = offsetInLine; i > 0; i--) {
			char c = line.charAt(i);
			if (!hasKey && !Character.isLetterOrDigit(c) && c != '.' && c != '"' && c != '(') // 前半部分格式
				return null;

			if (c == '"') {
				if (line.charAt(i - 1) != '(')// key 分割点
					return null;
				i--;
				hasKey = true;
				start = i + 2;
				continue;
			}
			if (hasKey) {
				if (!Character.isLetterOrDigit(c)) // key 格式
					break;

				key.append(c);
			} else {
				pre.append(c);
			}
		}
		key.reverse();
		pre.reverse();

		if (!key.toString().equals("insert") && !key.toString().equals("queryForList")) {
			return null;
		}

		StringBuffer pos = new StringBuffer();
		for (int i = offsetInLine + 1; i < line.length(); i++) {
			char c = line.charAt(i);
			if (!Character.isLetterOrDigit(c) && c != '.') { // 后半部分格式
				end = i;
				break;
			}

			pos.append(c);
		}

		int fstart = lineInfo.getOffset() + start;
		int fend = lineInfo.getOffset() + end;
		IRegion fregion = new Region(fstart, fend - fstart);

		String name = pre.append(pos).toString();

		// TOOD

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IWorkbenchPart part = page.getActiveEditor();

		IProject proj = null;
		IEditorInput ei = null;
		IFile editingFile = null;
		if (part instanceof CompilationUnitEditor) {
			ei = ((CompilationUnitEditor) part).getEditorInput();
			if (ei instanceof FileEditorInput) {
				editingFile = ((FileEditorInput) ei).getFile();
				proj = editingFile.getProject();
			}
		}

		if (proj == null)
			return null;

		List<IHyperlink> result = new ArrayList<IHyperlink>();

		int p1 = name.indexOf(".");
		int p2 = name.lastIndexOf(".");
		String fileName = name.substring(p1 + 1, p2);

		IFolder folder = proj.getFolder(SDTPlugin.D_RES);
		List<File> files = new ArrayList<File>();
		Map<String, String> param = new HashMap<String, String>();

		param.put("fileName", fileName + "-sqlmap-mapping.xml");

		File file = new File(folder.getLocationURI());
		findFiles(file, files, param);

		if (!files.isEmpty()) {
			File xmlFile = files.get(0);
			String xmlFileName = xmlFile.getAbsolutePath();
			int keyPosition = xmlFileName.indexOf("/src/");
			if (keyPosition == -1) {
				keyPosition = xmlFileName.indexOf("\\src\\");
			}
			if (keyPosition == -1) {
				return null;
			}
			String content = _.readFromFile(xmlFile);
			int contentPos = content.indexOf(name);

			IFile fFile = proj.getFile(xmlFileName.substring(keyPosition));
			result.add(new ResourceHyperlink(fregion, fFile, contentPos, name));

		}

		if (result.isEmpty()) {
			return null;
		}
		return result.toArray(new IHyperlink[] {});
	}

	private void findFiles(File file, Collection<File> result, Map<String, String> param) {
		if (file.isDirectory() && result.isEmpty()) {
			for (File subFile : file.listFiles()) {
				findFiles(subFile, result, param);
			}

		} else if (file.isFile()) {
			if (file.getName().equals(param.get("fileName"))) {
				result.add(file);
			}
		}
	}

	private class ResourceHyperlink implements IHyperlink {
		private final IRegion fRegion;
		private final IFile fFile;
		private final int fPosition;
		private final String fName;

		public ResourceHyperlink(IRegion region, IFile fFile, int position, String name) {
			this.fRegion = region;
			this.fFile = fFile;
			this.fPosition = position;
			this.fName = name;
		}

		@Override
		public void open() {
			if (fFile == null)
				return;

			try {
				IEditorPart ep = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage(), fFile);
				if (ep == null)
					return;

				ITextEditor te = null;
				if (ep instanceof ITextEditor) {
					te = (ITextEditor) ep;
				} else {
					Object o = ep.getAdapter(ITextEditor.class);
					if (o == null)
						return;
					te = (ITextEditor) o;
				}
				te.selectAndReveal(fPosition, fName.length());
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}

		@Override
		public String getHyperlinkText() {
			if (fFile == null)
				return null;

			return fFile.getProject().getName() + "/" + fFile.getName();
		}

		@Override
		public IRegion getHyperlinkRegion() {
			return this.fRegion;
		}

		@Override
		public String getTypeLabel() {
			return "java";
		}
	}

}

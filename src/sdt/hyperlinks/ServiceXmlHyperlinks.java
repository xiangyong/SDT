package sdt.hyperlinks;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

//
// org.eclipse.ui.workbench.texteditor.hyperlinkDetectors
// org.eclipse.ui.workbench.texteditor.hyperlinkDetectorTargets
//

public class ServiceXmlHyperlinks extends AbstractHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {

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
			if (!hasKey && !Character.isLetterOrDigit(c) && c != '.'
					&& c != '"' && c != '=')
				return null;

			if (c == '"') {
				if (line.charAt(i - 1) != '=')
					return null;
				i--;
				hasKey = true;
				start = i + 2;
				continue;
			}
			if (hasKey) {
				if (!Character.isLetterOrDigit(c) && c != '.')
					break;

				key.append(c);
			} else {
				pre.append(c);
			}
		}
		key.reverse();
		if (!key.toString().equals("interface")
				&& !key.toString().equals("class")) {
			return null;
		}

		pre.reverse();

		StringBuffer pos = new StringBuffer();
		for (int i = offsetInLine + 1; i < line.length(); i++) {
			char c = line.charAt(i);
			if (!Character.isLetterOrDigit(c) && c != '.') {
				end = i;
				break;
			}

			pos.append(c);
		}

		int fstart = lineInfo.getOffset() + start;
		int fend = lineInfo.getOffset() + end;
		IRegion fregion = new Region(fstart, fend - fstart);

		String name = pre.append(pos).toString();
		IWorkbenchPart part = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IProject proj = null;
		if (part instanceof MultiPageEditorPart) {
			IEditorInput ei = ((MultiPageEditorPart) part).getEditorInput();
			if (ei instanceof FileEditorInput) {
				proj = ((FileEditorInput) ei).getFile().getProject();
			}
		}

		if (proj == null)
			return null;

		IJavaProject jpro = JavaCore.create(proj);
		List<IHyperlink> result = new ArrayList<IHyperlink>();
		try {
			IType type = jpro.findType(name);
			if (type != null) {
				result.add(new XmlIHyperlink(fregion, type));
			}
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}

		if (result.isEmpty()) {
			// TODO 方案二 result.add(new XmlIHyperlink(fregion, null));
			return null;
		}
		return result.toArray(new IHyperlink[] {});
	}

	private class XmlIHyperlink implements IHyperlink {
		private final IRegion fRegion;
		private final IType fType;

		public XmlIHyperlink(IRegion region, IType type) {

			this.fRegion = region;
			this.fType = type;
		}

		@Override
		public void open() {
			// TODO class 文件打开
			if (fType == null)
				return;

			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(
					fType.getPath());
			try {
				IDE.openEditor(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage(), file);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}

		@Override
		public String getHyperlinkText() {
			if (fType == null)
				return null;

			return this.fType.getJavaProject().getElementName() + //
					"/" + this.fType.getFullyQualifiedName();
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

package sdt.hyperlinks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import sdt.SDTPlugin;

//
// org.eclipse.ui.workbench.texteditor.hyperlinkDetectors
// org.eclipse.ui.workbench.texteditor.hyperlinkDetectorTargets
//

public class ServiceXmlHyperlinks extends AbstractHyperlinkDetector {

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
			if (!hasKey && !Character.isLetterOrDigit(c) && c != '.' && c != '"' && c != '=' && c != '/'
					&& c != '-')
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
				if (!Character.isLetterOrDigit(c))
					break;

				key.append(c);
			} else {
				pre.append(c);
			}
		}
		key.reverse();
		pre.reverse();

		if (!key.toString().equals("interface") && !key.toString().equals("class") && !key.toString().equals("ref")
				&& !key.toString().equals("bean") && !key.toString().equals("resource")) {
			return null;
		}

		StringBuffer pos = new StringBuffer();
		for (int i = offsetInLine + 1; i < line.length(); i++) {
			char c = line.charAt(i);
			if (!Character.isLetterOrDigit(c) && c != '.' && c != '/' && c != '-') {
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
		if (part instanceof MultiPageEditorPart) {
			ei = ((MultiPageEditorPart) part).getEditorInput();
			if (ei instanceof FileEditorInput) {
				editingFile = ((FileEditorInput) ei).getFile();
				proj = editingFile.getProject();
			}
		}

		if (proj == null)
			return null;

		IJavaProject jpro = JavaCore.create(proj);
		List<IHyperlink> result = new ArrayList<IHyperlink>();
		try {

			Set<String> cache = new HashSet<String>();
			if (key.toString().equals("interface") || key.toString().equals("class")) {
				IType type = jpro.findType(name);
				if (type != null) {
					result.add(new JavaHyperlink(fregion, type));
				}
			} else if (key.toString().equals("resource")) {
				IFile file = proj.getFile(SDTPlugin.D_RES + "/" + name);
				if (file != null && file.exists()) {
					result.add(new ResourceHyperlink(fregion, file));
				}
			} else if (key.toString().equals("ref")) {
				result.addAll(getCurrentPageLink(cache, fregion, document, name, "id", "name"));
			} else if (key.toString().equals("bean")) {
				result.addAll(getCurrentPageLink(cache, fregion, document, name, "id"));
			}

		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}

		if (result.isEmpty()) {
			// TODO ·½°¸¶þ result.add(new XmlIHyperlink(fregion, null));
			return null;
		}
		return result.toArray(new IHyperlink[] {});
	}

	private Collection<BeanHyperlink> getCurrentPageLink(Set<String> cache, IRegion fregion, IDocument document,
			String name, String... keys) {
		Collection<BeanHyperlink> f = new ArrayList<BeanHyperlink>();
		String doc = document.get();

		for (String k : keys) {
			String toString = k + "=\"" + name + "\"";
			if (!cache.contains(toString) && doc.contains(toString)) {
				cache.add(toString);
				int toOffset, l = 0;
				while ((toOffset = doc.indexOf(toString, l)) > 0) {
					l = toOffset + toString.length();
					int lineNo = 0;
					try {
						lineNo = document.getNumberOfLines(0, l);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
					f.add(new BeanHyperlink(fregion, toString, toOffset, lineNo));
				}
			}
		}
		return f;
	}

	private class BeanHyperlink implements IHyperlink {
		private final IRegion fRegion;
		private final String fString;
		private final int fOffset;
		private final int lineNo;

		public BeanHyperlink(IRegion region, String key, int offset, int lineNo) {
			this.fRegion = region;
			this.fString = key;
			this.fOffset = offset;
			this.lineNo = lineNo;
		}

		@Override
		public void open() {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			Object a = page.getActiveEditor().getAdapter(ITextEditor.class);
			if (!(a instanceof ITextEditor))
				return;
			ITextEditor te = (ITextEditor) a;
			try {
				te.selectAndReveal(this.fOffset, fString.length());
				page.activate(te);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public IRegion getHyperlinkRegion() {
			return fRegion;
		}

		@Override
		public String getHyperlinkText() {
			return lineNo + ": " + fString;
		}

		@Override
		public String getTypeLabel() {
			return "xml";
		}

	}

	private class ResourceHyperlink implements IHyperlink {
		private final IRegion fRegion;
		private final IFile fFile;

		public ResourceHyperlink(IRegion region, IFile fFile) {
			this.fRegion = region;
			this.fFile = fFile;
		}

		@Override
		public void open() {
			if (fFile == null)
				return;

			try {
				IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), fFile);
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

	private class JavaHyperlink implements IHyperlink {
		private final IRegion fRegion;
		private final IType fType;

		public JavaHyperlink(IRegion region, IType type) {
			this.fRegion = region;
			this.fType = type;
		}

		@Override
		public void open() {
			if (fType == null)
				return;

			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(fType.getPath());
			try {
				IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
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

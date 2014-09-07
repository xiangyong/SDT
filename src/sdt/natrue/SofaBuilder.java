package sdt.natrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import sdt.SDTPlugin;

public class SofaBuilder extends IncrementalProjectBuilder {

	// TODO SampleDeltaVisitor
	class SampleDeltaVisitor implements IResourceDeltaVisitor {

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource.getType() != IResource.FILE)
				return true;

			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				checkResource(resource);
				break;
			case IResourceDelta.REMOVED:
				checkResource(resource);
				break;
			case IResourceDelta.CHANGED:
				if (!resource.getName().endsWith(".xml"))
					break;
				checkResource(resource);
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	// TODO SampleResourceVisitor
	class SampleResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			if (resource.getType() != IResource.FILE)
				return true;
			checkResource(resource);
			return true;
		}
	}

	private void checkResource(IResource resource) {
		if (resource.getName().endsWith(".xml")) {
			checkXML(resource);
		} else if (resource.getName().endsWith(".java")) {
			IFile file = (IFile) resource;
			IProject p = file.getProject();
			IFolder folder = p.getFolder(SDTPlugin.D_RES);
			Set<IFile> xmls = new HashSet<IFile>();
			findXmls(folder, xmls);

			for (IFile xmlFile : xmls) {
				checkXML(xmlFile);
			}
		}
	}

	private void findXmls(IResource res, Set<IFile> xmls) {
		if (res instanceof IFolder) {
			IFolder folder = (IFolder) res;
			try {
				for (IResource ires : folder.members()) {
					findXmls(ires, xmls);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		} else if (res instanceof IFile) {
			IFile file = (IFile) res;
			if (file.getName().endsWith(".xml")) {
				xmls.add(file);
			}
		}
	}

	// TODO XMLErrorHandler
	class XMLErrorHandler extends DefaultHandler {

		private IFile file;

		public XMLErrorHandler(IFile file) {
			this.file = file;
		}

		private void addMarker(SAXParseException e, int severity) {
			SofaBuilder.this.addMarker(file, e.getMessage(), e.getLineNumber(), severity);
		}

		public void error(SAXParseException exception) throws SAXException {
			addMarker(exception, IMarker.SEVERITY_ERROR);
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			addMarker(exception, IMarker.SEVERITY_ERROR);
		}

		public void warning(SAXParseException exception) throws SAXException {
			addMarker(exception, IMarker.SEVERITY_WARNING);
		}
	}

	public static final String BUILDER_ID = "SDT.SofaBuilder";

	private static final String MARKER_TYPE = "SDT.XmlProblem";

	private void addMarker(IFile file, String message, int lineNumber, int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}

	// TODO build
	@SuppressWarnings("unchecked")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		// 6 FULL_BUILD; 9 AUTO_BUILD, 10 INCREMENTAL_BUILD, 15 CLEAN_BUILD
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	// TODO clean
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

	void checkXML(IResource resource) {
		IFile file = (IFile) resource;
		deleteMarkers(file);
		XMLErrorHandler handler = new XMLErrorHandler(file);
		try {
			parse(file, handler);
		} catch (Exception e1) {
		}
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			getProject().accept(new SampleResourceVisitor());
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		delta.accept(new SampleDeltaVisitor());
	}

	private void parse(IFile file, DefaultHandler handler) throws SAXException, CoreException {

		InputStream is = file.getContents();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		List<String> content = new ArrayList<String>();
		String line = "";
		try {
			while ((line = br.readLine()) != null) {
				content.add(line);
			}
		} catch (IOException e) {
		}
		if (content.isEmpty())
			return;

		String[] keyClasses = new String[] { "class=\"", "interface=\"" };
		for (int i = 0; i < content.size(); i++) {
			String s = content.get(i);
			for (String keyClass : keyClasses)
				if (s.contains(keyClass)) {
					int beginIndex = s.indexOf(keyClass) + keyClass.length();
					int endIndex = s.indexOf('"', beginIndex);
					String klass = s.substring(beginIndex, endIndex);

					IProject p = file.getProject();
					IJavaProject jp = JavaCore.create(p);
					IType type = jp.findType(klass);
					if (type == null) {
						SAXParseException e = new SAXParseException("Class Not Found:" + klass, "", "", i + 1,
								beginIndex);
						handler.error(e);
					}
				}
		}

	}

}
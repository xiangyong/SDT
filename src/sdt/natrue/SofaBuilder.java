package sdt.natrue;

import static org.eclipse.core.resources.IResourceDelta.ADDED;
import static org.eclipse.core.resources.IResourceDelta.REMOVED;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
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

	// TODO IResourceDeltaVisitor
	class SofaDeltaVisitor implements IResourceDeltaVisitor {
		private IProgressMonitor monitor;

		public SofaDeltaVisitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			if (monitor.isCanceled())
				return false;

			IResource resource = delta.getResource();

			if (resource.getType() != IResource.FILE)
				return true;

			if (resource.toString().contains("/target/"))
				return false;

			if (resource.getName().endsWith(".xml")) {
				checkXML(resource);
				return true;
			}

			if (resource.getName().endsWith(".java") && (delta.getKind() == ADDED || delta.getKind() == REMOVED)) {
				fullBuild(monitor);
				return true;
			}

			return true;
		}
	}

	// TODO IResourceVisitor
	class SofaResourceVisitor implements IResourceVisitor {
		private IProgressMonitor monitor;

		public SofaResourceVisitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		public boolean visit(IResource resource) {
			if (monitor.isCanceled())
				return false;

			if (resource.getType() != IResource.FILE)
				return true;

			if (!resource.getName().endsWith(".xml") || resource.toString().contains("/target/"))
				return true;

			checkXML(resource);
			return true;
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
			getProject().getFolder(SDTPlugin.D_RES).accept(new SofaResourceVisitor(monitor));
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		delta.accept(new SofaDeltaVisitor(monitor));
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

		String sofaReference = "sofa:reference";
		String[] keyClasses = new String[] { "class=\"", "interface=\"" };
		for (int i = 0; i < content.size(); i++) {
			String s = content.get(i);
			if (s.contains(sofaReference)
					|| (i > 0 && !s.contains("<") && content.get(i - 1).contains(sofaReference))) {
				continue;
			}
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
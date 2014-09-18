package sdt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import sdt.core._;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class SDTPlugin extends AbstractUIPlugin {

	public static final String D_RES = "src/main/resources";
	public static final String D_META_INF = D_RES + "/META-INF";
	public static final String D_SPRING = D_META_INF + "/spring";
	public static final String D_JAVA = "src/main/java";

	public static final String PLUGIN_ID = "SDT";

	private static SDTPlugin plugin;

	public SDTPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static SDTPlugin getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public URL getInstallURL() {
		return getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	public static String getTpl(Map ctx, String path) {
		BufferedReader reader = null;
		StringBuffer f = new StringBuffer();
		try {
			URL url = new URL("platform:/plugin/" + SDTPlugin.PLUGIN_ID + "/" + path);
			InputStream inputStream = url.openConnection().getInputStream();
			reader = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			while ((line = reader.readLine()) != null) {
				f.append(line);
				f.append("\n");
			}
			reader.close();

			return _.f(f.toString(), ctx);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

			}
		}
		return null;
	}

	public static String readFile(IFile file) {
		InputStream contents = null;
		InputStreamReader reader = null;
		try {
			file.refreshLocal(IFile.DEPTH_ZERO, new NullProgressMonitor());
			contents = file.getContents();
			String charset = file.getCharset();
			reader = new InputStreamReader(contents, charset);
			return readFile(reader);
		} catch (CoreException localCoreException) {
			localCoreException.printStackTrace();

		} catch (IOException localIOException) {
			localIOException.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
				if (contents != null) {
					contents.close();
					contents = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
		}
		return null;
	}

	public static String readFile(Reader reader) {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(reader);
			StringBuilder sb = new StringBuilder(2000);
			for (;;) {
				int c = bufferedReader.read();
				if (c == -1) {
					return sb.toString();
				}
				sb.append((char) c);
			}

		} catch (IOException localIOException1) {
			localIOException1.printStackTrace();
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static IFile getFile(String path) {
		return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
	}

	public static IFolder getFolder(String path) {
		return ResourcesPlugin.getWorkspace().getRoot().getFolder(new Path(path));
	}

	public static void openResource(final IFile resource) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}

		final IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null) {
			return;
		}

		final Display display = window.getShell().getDisplay();
		if (display == null) {
			return;
		}

		display.asyncExec(new Runnable() {
			public void run() {
				try {
					IDE.openEditor(activePage, resource, true);
				} catch (PartInitException e) {

				}
			}
		});

	}

	public static void addProject(IJavaProject from, IJavaProject to) {
		IClasspathEntry[] entries = null;
		try {
			entries = to.getRawClasspath();
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
		if (entries == null)
			return;

		int l = entries.length;
		IClasspathEntry[] f = new IClasspathEntry[l + 1];
		for (int i = 0; i < l; i++) {
			f[i] = entries[i];
			if (f[i].getPath().equals(from.getPath())) {
				return;
			}
		}
		IClasspathEntry ce = new ClasspathEntry( //
				IPackageFragmentRoot.K_SOURCE,// contentKind
				ClasspathEntry.CPE_PROJECT, // entryKind
				from.getPath(), // path
				new IPath[0], // inclusionPatterns
				new IPath[0], // exclusionPatterns
				null, // sourceAttachmentPath
				null, // sourceAttachmentRootPath
				null, // specificOutputLocation
				false, // isExported
				new IAccessRule[0], // accessRules
				true,// combineAccessRules
				new IClasspathAttribute[0] // extraAttributes
		);
		f[l] = ce;
		try {
			to.setRawClasspath(f, null);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	public static Collection<IClasspathEntry> getClasspathEntry(String name, int... entryKinds) {
		List<IClasspathEntry> f = new ArrayList<IClasspathEntry>();
		IProject project = getProject(name);
		IProjectDescription desc = null;
		try {
			desc = project.getDescription();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if (desc == null)
			return f;

		IJavaProject jp = JavaCore.create(project);
		IClasspathEntry[] ces = null;
		try {
			ces = jp.getRawClasspath();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		if (ces == null || ces.length == 0)
			return f;

		for (IClasspathEntry ce : ces) {
			for (int ek : entryKinds) {
				if (ce.getEntryKind() == ek) {
					System.err.println(ce);
					f.add(ce);
				}
			}
		}

		return f;
	}

	public static void findResource(final List<IResource> list, IFolder root, final String filter) {
		try {
			root.accept(new ResourceNameVisitor(list, filter));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static String getPreference(String name) {
		return getDefault().getPreferenceStore().getString(name);
	}

	public static IProject getProject(String name) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}

	public static IJavaProject getJavaProject(String name) {
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		IJavaProject jp = JavaCore.create(p);
		return jp;
	}

	public static IPackageFragmentRoot getPackageFragmentRoot(String javaProjectName) {
		IPackageFragmentRoot f = null;
		IJavaProject jp = getJavaProject(javaProjectName);

		try {
			f = jp.findPackageFragmentRoot(new Path("/" + javaProjectName + "/" + D_JAVA));
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return f;
	}

	public static IPackageFragment getPackageFragment(String javaProjectName, String packageName) {
		return getPackageFragmentRoot(javaProjectName).getPackageFragment(packageName);
	}

	static class ResourceNameVisitor implements IResourceVisitor {
		private List<IResource> fList;
		private String fFilter;
		private int fType;

		public ResourceNameVisitor(List<IResource> list, String filter) {
			fList = list;
			if (filter.charAt(0) == '^') {
				fType = 1;
				fFilter = filter.substring(0, filter.length() - 1);
			} else if (filter.charAt(filter.length() - 1) == '$') {
				fType = 2;
				fFilter = filter.substring(0, filter.length() - 1);
			} else {
				fFilter = filter;
			}
		}

		@Override
		public boolean visit(IResource resource) throws CoreException {
			if (resource.getType() != IFile.FILE)
				return true;
			String name = resource.getName();
			switch (fType) {
			case 0:
				if (name.contains(fFilter))
					fList.add(resource);
				break;
			case 1:
				if (name.startsWith(fFilter))
					fList.add(resource);
				break;
			case 2:
				if (name.endsWith(fFilter))
					fList.add(resource);
				break;
			}
			return false;
		}
	}
}

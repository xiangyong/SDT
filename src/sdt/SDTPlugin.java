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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
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

	public static TextFileChange createNewFileChange(IFile targetFile, String contents) {
		String fileName = targetFile.getName();
		String message;
		if (targetFile.exists()) {
			message = String.format("Replace %1$s", new Object[] { fileName });
		} else {
			message = String.format("Create %1$s", new Object[] { fileName });
		}
		TextFileChange change = new TextFileChange(message, targetFile) {
			protected IDocument acquireDocument(IProgressMonitor pm) throws CoreException {
				IDocument document = super.acquireDocument(pm);
				if (document.getLength() > 0) {
					try {
						document.replace(0, document.getLength(), "");
					} catch (BadLocationException localBadLocationException) {
						localBadLocationException.printStackTrace();
					}
				}
				return document;
			}
		};

		change.setTextType(targetFile.getFileExtension());

		MultiTextEdit rootEdit = new MultiTextEdit();
		rootEdit.addChild(new InsertEdit(0, contents));
		change.setEdit(rootEdit);

		return change;
	}

	public static TextFileChange createReplaceEdit(IFile to, String serviceXml) {
		String fragment = serviceXml;

		String currentXml = readFile(to);

		String contents = currentXml.replace("</beans>", fragment + "\n</beans>");
		// TODO
		TextFileChange change = new TextFileChange("Merge " + to.getName(), to);
		MultiTextEdit rootEdit = new MultiTextEdit();
		rootEdit.addChild(new ReplaceEdit(0, currentXml.length(), contents));
		change.setEdit(rootEdit);
		change.setTextType(to.getFileExtension());
		return change;
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

	public static void findResource(List<IResource> list, IFolder root, String filter) {
		IResource[] rs = null;
		try {
			rs = root.members();
			for (IResource r : rs) {
				if (r.getType() == IFile.FILE) {
					String name = r.getName();
					String key = filter;
					if (filter.charAt(0) == '^') {
						key = key.substring(1);
						if (name.startsWith(key))
							list.add(r);
					} else if (filter.charAt(filter.length() - 1) == '$') {
						key = key.substring(0, key.length() - 1);
						if (name.endsWith(key))
							list.add(r);
					} else {
						if (name.contains(key))
							list.add(r);
					}
				} else if (r.getType() == IFile.FOLDER) {
					findResource(list, (IFolder) r, filter);
				}
			}
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

	public static String getJavaType(String type) {
		char c = type.charAt(0);

		switch (c) {
		case Signature.C_ARRAY:
			return Signature.getElementType(type) + "[] " + type.substring(1, type.length() - 1);
		case Signature.C_RESOLVED:
			return type.substring(1, type.length() - 1);
		case Signature.C_UNRESOLVED:
			break;
		case Signature.C_TYPE_VARIABLE:
			break;
		case Signature.C_BOOLEAN:
			return "boolean ";
		case Signature.C_BYTE:
			return "byte ";
		case Signature.C_CHAR:
			return "char ";
		case Signature.C_DOUBLE:
			return "double ";
		case Signature.C_FLOAT:
			return "float ";
		case Signature.C_INT:
			return "int ";
		case Signature.C_LONG:
			return "long ";
		case Signature.C_SHORT:
			return "short ";
		case Signature.C_VOID:
			return "void ";
		case Signature.C_STAR:
		case Signature.C_SUPER:
		case Signature.C_EXTENDS:
			break;
		case Signature.C_CAPTURE:
			break;
		default:
			return null;
		}
		return null;
	}
}

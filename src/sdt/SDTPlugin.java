package sdt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
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

/**
 * The activator class controls the plug-in life cycle
 */
public class SDTPlugin extends AbstractUIPlugin {

	public static final String D_RES = "src/main/resources";
	public static final String D_META_INF = D_RES + "/META-INF";
	public static final String D_SPRING = D_META_INF + "/spring";

	public static final String D_JAVA = "src/main/java";

	public static final String F_MANIFEST_MF = D_META_INF + "/MANIFEST.MF";

	// The plug-in ID
	public static final String PLUGIN_ID = "SDT";

	// The shared instance
	private static SDTPlugin plugin;

	/**
	 * The constructor
	 */
	public SDTPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static SDTPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public URL getInstallURL() {
		return getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
	}

	@SuppressWarnings("deprecation")
	public static String getTpl(VelocityContext context, String path) {
		InputStream inputStream = null;
		StringWriter writer = null;
		try {
			URL url = new URL("platform:/plugin/" + SDTPlugin.PLUGIN_ID + "/" + path);
			inputStream = url.openConnection().getInputStream();

			writer = new StringWriter();

			Velocity.evaluate(context, writer, "VelocityTest", inputStream);
			return writer.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
					inputStream = null;
				}
				if (writer != null) {
					writer.close();
					writer = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "";
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

	public static IFile getTargetFile(String path) {
		return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
	}

	public static void addNature(IProject project, String natureId) {
		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i) {
				if (natureId.equals(natures[i])) {
					// Remove the nature
					// String[] newNatures = new String[natures.length - 1];
					// System.arraycopy(natures, 0, newNatures, 0, i);
					// System.arraycopy(natures, i + 1, newNatures, i,
					// natures.length - i - 1);
					// description.setNatureIds(newNatures);
					// project.setDescription(description, null);
					return;
				}
			}

			// Add the nature
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = natureId;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		} catch (CoreException e) {
		}
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

	public static void addFileToProject(IContainer container, Path path, InputStream contentStream,
			IProgressMonitor monitor) throws CoreException {
		final IFile file = container.getFile(path);

		if (file.exists()) {
			file.setContents(contentStream, true, true, monitor);
		} else {
			file.create(contentStream, true, monitor);
		}
	}

	public static IProject getProject(String name) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}
}

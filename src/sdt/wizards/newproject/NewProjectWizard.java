package sdt.wizards.newproject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

import sdt.SDTPlugin;
import sdt.wizards.NewPreviewWizardPage;
import sdt.wizards.NewWizard;

import com.google.common.io.Files;

public class NewProjectWizard extends NewWizard {

	private NewProjectWizardPage wizardPage;

	protected boolean performFinish(IProgressMonitor monitor) {
		NewProjectState data = (NewProjectState) this.previewPage.data;
		IProject project = SDTPlugin.getProject(data.name);

		// open project
		try {
			project.open(IResource.BACKGROUND_REFRESH, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		// add project to working set
		IWorkingSetManager wsm = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet ws = wsm.getWorkingSet(data.system);

		if (ws == null) {
			ws = wsm.createWorkingSet(data.system, new IAdaptable[0]);
			wsm.addWorkingSet(ws);
		}

		wsm.addToWorkingSets(project, new IWorkingSet[] { ws });

		boolean f = super.performFinish(monitor);

		JavaCore.create(project);

		addToMainPom();

		// # add to ace pom
		// find proj
		// -z 
		//   find tpl
		//   -n
		//     find @tpl "</artifactItem>"
		//     insert @>"</artifactItem>" proj

		// # add to test pom
		// find proj
		// -z 
		//   find "</dependencies>"
		//   insert @>"</dependencies>" proj

		// # add to test project
		// .project
		// .classpath

		// super performFinish

		return f;
	}

	private void addToMainPom() {
		NewProjectState data = (NewProjectState) this.previewPage.data;
		File pom = getMainPom(data.name);

		// read
		String txt = null;
		try {
			txt = Files.toString(pom, Charset.forName("GBK"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (txt == null || txt.contains(data.name))
			return;

		StringBuffer buff = new StringBuffer(txt);

		// init context
		Velocity.init();
		VelocityContext context = new VelocityContext();
		context.put("system", data.system);
		context.put("project", data.name);

		// dependency
		{
			context.put("type", "parent");
			String content = "\n" + SDTPlugin.getTpl(context, "tpl/proj/dependency.vm");
			String key1 = data.name;
			String key2 = data.system + "-" + data.type;
			String key3 = "</dependency>";
			insertString(buff, content, key1, key2, key3);
		}
		// module
		{
			context.put("type", "module");
			String content = "\n" + SDTPlugin.getTpl(context, "tpl/proj/dependency.vm");
			String key1 = "app/" + data.name.substring(data.name.indexOf("-") + 1).replaceFirst("-", "/");
			String key2 = "app/" + data.type.replaceFirst("-", "/");
			String key3 = "</module>";
			insertString(buff, content, key1, key2, key3);
		}

		// write
		try {
			Files.write(buff.toString().getBytes(), pom);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void insertString(StringBuffer buff, String value, String key1, String key2, String key3) {
		if (buff.indexOf(key1) > 0 || buff.indexOf(key2) < 0)
			return;

		int i = buff.indexOf(key2);
		int j = buff.indexOf(key3, i) + key3.length();
		buff.insert(j, value);
	}

	private File getMainPom(String name) {
		IProject project = SDTPlugin.getProject(name);
		URI uri = project.getRawLocationURI();
		File file = new File(uri);
		if (!file.exists())
			return null;

		int l = name.endsWith("-assembly-templage") ? 2 : 3;
		File parent = file;
		for (int i = 0; i < l; i++) {
			if (parent.getParentFile().exists()) {
				parent = parent.getParentFile();
			} else {
				return null;
			}
		}

		File[] files = parent.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File f, String name) {
				return name.equals("pom.xml");
			}
		});
		if (files.length == 0)
			return null;

		return files[0];
	}

	@Override
	public boolean performCancel() {

		try {
			NewProjectState data = (NewProjectState) this.previewPage.data;
			if (data.name != null) {
				IProject p = SDTPlugin.getProject(data.name);
				if (p != null && p.exists()) {
					p.delete(true, true, null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return super.performCancel();
	}

	@Override
	public void addPages() {
		NewProjectState data = new NewProjectState();

		wizardPage = new NewProjectWizardPage(data);
		wizardPage.setTitle("New Project");
		wizardPage.setDescription("New a Sofa Project");
		addPage(wizardPage);

		previewPage = new NewPreviewWizardPage(data);
		previewPage.setTitle("New Project Preview");
		previewPage.setDescription("Review Changes");
		addPage(previewPage);

	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == wizardPage) {
			return previewPage;
		}
		return null;
	}

}

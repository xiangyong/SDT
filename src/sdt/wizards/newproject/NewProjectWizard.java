package sdt.wizards.newproject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.net.URI;

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

		addToMainPom(project);

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

	private void addToMainPom(IProject project) {
		NewProjectState data = (NewProjectState) this.previewPage.data;
		// # add to main pom
		// find proj
		// -z 
		//   find tpl
		//   -n
		//     find @tpl "</dependency>"
		//     insert @>"</dependency>" proj
		// find proj_module
		// -z
		//   find tpl module
		//   -n
		//     insert @tpl_module prject_module

		try {
			URI uri = project.getRawLocationURI();
			File file = new File(uri);
			if (!file.exists())
				return;
			File parent = file;
			for (int i = 0; i < 3; i++) {
				if (parent.getParentFile().exists()) {
					parent = parent.getParentFile();
				}
			}
			System.err.println(parent);

			File[] files = parent.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File f, String name) {
					return name.equals("pom.xml");
				}
			});
			if (files.length == 0)
				return;

			File pom = files[0];
			if (!pom.exists())
				return;

			FileReader f = new FileReader(pom);
			int l = (int) pom.length();
			char[] cs = new char[l];
			f.read(cs);
			f.close();
			String txt = String.valueOf(cs);

			if (txt.contains(data.name))
				return;

			String tpl = data.system + "-" + data.type;
			if (!txt.contains(tpl))
				return;

			StringBuffer buff = new StringBuffer(txt);
			// dependency
			int p = buff.indexOf(tpl);
			String key = "</dependency>";
			int d = buff.indexOf(key, p) + key.length();

			Velocity.init();
			VelocityContext context = new VelocityContext();
			context.put("system", data.system);
			context.put("project", data.name);
			context.put("type", "parent");
			String content = "\n" + SDTPlugin.getTpl(context, "tpl/proj/dependency.vm");

			buff.insert(d, content);

			// module
			FileWriter w = new FileWriter(pom);
			w.write(buff.toString());
			w.flush();
			w.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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

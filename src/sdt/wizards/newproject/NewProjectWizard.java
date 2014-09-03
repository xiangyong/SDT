package sdt.wizards.newproject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

import sdt.SDTPlugin;
import sdt.core._;
import sdt.wizards.NewPreviewWizardPage;
import sdt.wizards.NewWizard;

@SuppressWarnings("restriction")
public class NewProjectWizard extends NewWizard {

	private NewProjectWizardPage wizardPage;

	protected void doBefore(IProgressMonitor monitor) {
		NewProjectState data = (NewProjectState) this.previewPage.data;
		IProject project = SDTPlugin.getProject(data.name);

		IWorkingSetManager wsm = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet ws = wsm.getWorkingSet(data.system);
		if (ws == null) {
			ws = wsm.createWorkingSet(data.system, new IAdaptable[0]);
			wsm.addWorkingSet(ws);
		}
		wsm.addToWorkingSets(project, new IWorkingSet[] { ws });

		// open project
		try {
			project.open(IResource.BACKGROUND_REFRESH, monitor);
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	protected void doAfter(IProgressMonitor monitor) {

		NewProjectState data = (NewProjectState) this.previewPage.data;
		IProject project = SDTPlugin.getProject(data.name);
		IJavaProject jp = JavaCore.create(project);

		if (data.type.equalsIgnoreCase("web-home")) {
			addHtdocs(monitor);
		}

		// init context
		Map context = new HashMap();
		context.put("system", data.system);
		context.put("project", data.name);

		addToMainPom(context);
		addToAcePom(context);
		addToTestPom(context);

		// # add to test project
		IProject testProject = SDTPlugin.getProject(data.system + "-test");
		SDTPlugin.addProject(jp, JavaCore.create(testProject));

	}

	protected void doException(IProgressMonitor monitor) {
		try {
			NewProjectState data = (NewProjectState) this.previewPage.data;
			if (data.name != null) {
				IProject p = SDTPlugin.getProject(data.name);
				if (p != null && p.exists()) {
					p.delete(true, true, monitor);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addHtdocs(IProgressMonitor monitor) {
		NewProjectState data = (NewProjectState) this.previewPage.data;
		File parent = getParent(data.name);
		if (parent == null || !parent.exists())
			return;

		File templates = new File(parent.getAbsolutePath() + "/htdocs/templates");
		if (!templates.exists())
			return;

		boolean f = false;
		String module = data.name.substring(data.name.lastIndexOf("-") + 1);
		File layoutDir = new File(templates.getAbsolutePath() + "/" + module + "/layout");
		if (!layoutDir.exists()) {
			layoutDir.mkdirs();
			f = true;
		}

		File screenDir = new File(templates.getAbsolutePath() + "/" + module + "/screen");
		if (!screenDir.exists()) {
			screenDir.mkdirs();
			f = true;
		}

		File titleDir = new File(templates.getAbsolutePath() + "/" + module + "/title");
		if (!titleDir.exists()) {
			titleDir.mkdirs();
			f = true;
		}

		File macros = new File(templates.getAbsolutePath() + "/" + module + "/macros.vm");

		try {
			if (!macros.exists()) {
				macros.createNewFile();
				f = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		File dft = new File(templates.getAbsolutePath() + "/" + module + "/layout/default.vm");
		try {
			if (!dft.exists()) {
				dft.createNewFile();
				f = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		IProject p = SDTPlugin.getProject(data.system + "-htdocs");
		if (p == null || !p.exists()) {

			IProjectDescription desc = new ProjectDescription();
			desc.setName(data.system + "-htdocs");
			File htdocs = new File(parent.getAbsolutePath() + "/htdocs");
			desc.setLocationURI(htdocs.toURI());
			try {
				p.create(desc, monitor);

				IWorkingSetManager wsm = PlatformUI.getWorkbench().getWorkingSetManager();
				IWorkingSet ws = wsm.getWorkingSet(data.system);
				if (ws == null) {
					ws = wsm.createWorkingSet(data.system, new IAdaptable[0]);
					wsm.addWorkingSet(ws);
				}
				wsm.addToWorkingSets(p, new IWorkingSet[] { ws });

				p.open(IResource.BACKGROUND_REFRESH, monitor);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			try {
				if (f)
					p.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

	}

	private void addToMainPom(Map<String, String> context) {
		NewProjectState data = (NewProjectState) this.previewPage.data;
		File pom = getPom(data.name, "pom.xml");
		if (pom == null || !pom.exists())
			return;

		// read
		String txt = null;
		txt = _.readFromFile(pom);

		if (txt == null || txt.contains(data.name))
			return;

		StringBuffer buff = new StringBuffer(txt);

		// dependency
		{
			context.put("type", "parent");
			String key1 = data.name;
			String key2 = data.system + "-" + data.type;
			String key3 = "</dependency>";
			String content = SDTPlugin.getTpl(context, "tpl/proj/dependency.vm");
			insertString(buff, content, key1, key2, key3);
		}
		// module
		{
			context.put("type", "module");
			String key1 = "app/" + data.name.substring(data.name.indexOf("-") + 1).replaceFirst("-", "/");
			String key2 = "app/" + data.type.replaceFirst("-", "/");
			String key3 = "</module>";
			context.put("module", key1);
			String content = SDTPlugin.getTpl(context, "tpl/proj/dependency.vm");
			insertString(buff, content, key1, key2, key3);
		}

		// write
		_.writeToFile(pom, buff.toString());

	}

	private void insertString(StringBuffer buff, String value, String key1, String key2, String key3) {
		if (buff.indexOf(key1) > 0 || buff.indexOf(key2) < 0)
			return;

		int i = buff.indexOf(key2);
		int j = buff.indexOf(key3, i) + key3.length() + 1;
		buff.insert(j, value);
	}

	private void addToAcePom(Map<String, String> context) {

		NewProjectState data = (NewProjectState) this.previewPage.data;
		File pom = getPom(data.name, "assembly/ace/pom.xml");
		if (pom == null || !pom.exists())
			return;

		// read
		String txt = _.readFromFile(pom);
		if (txt.contains(data.name))
			return;

		StringBuffer buff = new StringBuffer(txt);

		// artifactItem
		{
			context.put("type", "ace");
			String key1 = data.name;
			String key2 = data.system + "-" + data.type;
			String key3 = "</artifactItem>";
			String content = SDTPlugin.getTpl(context, "tpl/proj/dependency.vm");
			insertString(buff, content, key1, key2, key3);
		}

		// write
		_.writeToFile(pom, buff.toString());

	}

	private void addToTestPom(Map<String, String> context) {
		NewProjectState data = (NewProjectState) this.previewPage.data;
		File pom = getPom(data.name, "app/test/pom.xml");
		if (pom == null || !pom.exists())
			return;
		// read
		String txt = _.readFromFile(pom);
		if (txt.contains(data.name))
			return;

		StringBuffer buff = new StringBuffer(txt);

		// artifactItem
		{
			context.put("type", "test");
			String key1 = data.name;
			String key2 = data.system + "-" + data.type;
			String key3 = "</dependency>";
			String content = SDTPlugin.getTpl(context, "tpl/proj/dependency.vm");
			insertString(buff, content, key1, key2, key3);
		}

		// write
		_.writeToFile(pom, buff.toString());

	}

	private File getPom(String name, String pom) {
		File parent = getParent(name);
		if (parent == null)
			return null;

		return new File(parent.getAbsolutePath() + "/" + pom);
	}

	private File getParent(String name) {
		IProject project = SDTPlugin.getProject(name);
		URI uri = project.getRawLocationURI();
		File file = new File(uri);
		if (!file.exists())
			return null;

		File parent = file;
		for (int i = 0; i < 3; i++) {
			if (parent.getParentFile().exists()) {
				parent = parent.getParentFile();
			} else {
				return null;
			}
		}

		return parent;
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
		wizardPage.setDescription("New a Project");
		addPage(wizardPage);

		previewPage = new NewPreviewWizardPage(data);
		previewPage.setTitle("Preview");
		previewPage.setDescription("Review Changes");
		addPage(previewPage);

	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == wizardPage) {
			wizardPage.refreshData();
			return previewPage;
		}
		return null;
	}

}

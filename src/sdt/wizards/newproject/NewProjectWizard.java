package sdt.wizards.newproject;

import java.io.File;
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
import org.eclipse.jdt.core.IJavaProject;
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

	protected void doAfter(IProgressMonitor monitor) {

		NewProjectState data = (NewProjectState) this.previewPage.data;
		IProject project = SDTPlugin.getProject(data.name);
		IJavaProject jp = JavaCore.create(project);

		// init context
		Velocity.init();
		VelocityContext context = new VelocityContext();
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

	private void addToMainPom(VelocityContext context) {
		NewProjectState data = (NewProjectState) this.previewPage.data;
		File pom = getPom(data.name, "pom.xml");
		if (!pom.exists())
			return;

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
		int j = buff.indexOf(key3, i) + key3.length() + 1;
		buff.insert(j, value);
	}

	private void addToAcePom(VelocityContext context) {

		NewProjectState data = (NewProjectState) this.previewPage.data;
		File pom = getPom(data.name, "assembly/ace/pom.xml");
		if (!pom.exists())
			return;

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
		try {
			Files.write(buff.toString().getBytes(), pom);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addToTestPom(VelocityContext context) {
		NewProjectState data = (NewProjectState) this.previewPage.data;
		File pom = getPom(data.name, "app/test/pom.xml");
		if (!pom.exists())
			return;
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
		try {
			Files.write(buff.toString().getBytes(), pom);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File getPom(String name, String pom) {
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

		return new File(parent.getAbsolutePath() + "/" + pom);
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

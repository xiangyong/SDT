package sdt.wizards.newproject;

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
		} else if (!ws.isEditable()) {

		}
		wsm.addToWorkingSets(project, new IWorkingSet[] { ws });

		// super performFinish
		boolean f = super.performFinish(monitor);

		JavaCore.create(project);

		return f;
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

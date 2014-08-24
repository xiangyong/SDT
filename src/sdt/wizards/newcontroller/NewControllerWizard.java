package sdt.wizards.newcontroller;

import org.eclipse.jface.wizard.IWizardPage;

import sdt.wizards.NewPreviewWizardPage;
import sdt.wizards.NewWizard;

public class NewControllerWizard extends NewWizard {

	private NewControllerWizardPage wizardPage;

	@Override
	public void addPages() {
		NewControllerState data = new NewControllerState();
		wizardPage = new NewControllerWizardPage(data);
		wizardPage.setTitle("New Controller");
		wizardPage.setDescription("New Controller");
		addPage(wizardPage);

		previewPage = new NewPreviewWizardPage(data);
		previewPage.setTitle("New Controller Preview");
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

package sdt.wizards.newclient;

import org.eclipse.jface.wizard.IWizardPage;

import sdt.wizards.NewPreviewWizardPage;
import sdt.wizards.NewWizard;

public class NewClientWizard extends NewWizard {

	private NewClientWizardPage wizardPage;

	@Override
	public void addPages() {
		NewClientState data = new NewClientState();
		wizardPage = new NewClientWizardPage(data);
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

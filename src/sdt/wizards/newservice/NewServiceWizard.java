package sdt.wizards.newservice;

import org.eclipse.jface.wizard.IWizardPage;

import sdt.wizards.NewPreviewWizardPage;
import sdt.wizards.NewWizard;

public class NewServiceWizard extends NewWizard {

	private NewServiceWizardPage wizardPage;

	@Override
	public void addPages() {
		NewServiceState data = new NewServiceState();
		wizardPage = new NewServiceWizardPage(data);
		wizardPage.setTitle("New Service");
		wizardPage.setDescription("create Interface, Implementation and Spring Xml");
		addPage(wizardPage);

		previewPage = new NewPreviewWizardPage(data);
		previewPage.setTitle("New Service Preview");
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

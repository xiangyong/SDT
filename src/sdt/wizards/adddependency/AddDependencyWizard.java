package sdt.wizards.adddependency;

import org.eclipse.jface.wizard.IWizardPage;

import sdt.wizards.NewPreviewWizardPage;
import sdt.wizards.NewWizard;

public class AddDependencyWizard extends NewWizard {

	private AddDependencyWizardPage wizardPage;

	@Override
	public void addPages() {
		AddDependencyState data = new AddDependencyState();
		wizardPage = new AddDependencyWizardPage(data);
		wizardPage.setTitle("Add Dependency");
		wizardPage.setDescription("Add Dependency");
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

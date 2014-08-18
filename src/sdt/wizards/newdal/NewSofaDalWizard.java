package sdt.wizards.newdal;

import org.eclipse.jface.wizard.IWizardPage;

import sdt.wizards.NewPreviewWizardPage;
import sdt.wizards.NewWizard;

public class NewSofaDalWizard extends NewWizard {

	private NewSofaDalWizardPage wizardPage;

	@Override
	public void addPages() {
		NewSofaDalState data = new NewSofaDalState();
		
		wizardPage = new NewSofaDalWizardPage(selection, data);
		wizardPage.setTitle("New Sofa Dal");
		wizardPage.setDescription("New a Sofa Dal");
		addPage(wizardPage);

		previewPage = new NewPreviewWizardPage(data);
		previewPage.setTitle("New Sofa Dal Preview");
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

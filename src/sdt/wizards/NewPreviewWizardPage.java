package sdt.wizards;

import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.internal.ui.refactoring.PreviewWizardPage;

@SuppressWarnings("restriction")
public class NewPreviewWizardPage extends PreviewWizardPage {

	public NewWizardState data;

	public NewPreviewWizardPage(NewWizardState data) {
		super(true);
		this.data = data;
	}

	public void setVisible(boolean visible) {
		if (visible) {
			CompositeChange root = new CompositeChange("Create template of dal", this.data.computeChanges());
			setChange(root);
		}
		super.setVisible(visible);
	}

	@Override
	public boolean isPageComplete() {
		if (!this.hasChanges()) {
			return false;
		}
		return super.isPageComplete();
	}
}

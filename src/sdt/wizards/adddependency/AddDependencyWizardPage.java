package sdt.wizards.adddependency;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;

import sdt.wizards.NewWizardPage;

@SuppressWarnings("restriction")
public class AddDependencyWizardPage extends NewWizardPage {

	private AddDependencyState data;

	private StringDialogField fGroupId;
	private StringDialogField fArtifactId;
	private StringDialogField fVersion;
	private StringButtonDialogField fProject;

	public AddDependencyWizardPage(AddDependencyState data) {
		super("New Service");
		this.data = data;

		int i = 1;
		fGroupId = createStringDialogField(this, "&" + i++ + " Group Id:", null);
		fArtifactId = createStringDialogField(this, "&" + i++ + " Artifact Id:", null);
		fVersion = createStringDialogField(this, "&" + i++ + " Version:", null);

		fProject = createStringButtonDialogField("&" + i++ + " Project:", "Browse &Q", PROJECT, null, null, false,
				null);
	}

	// TODO IDialogFieldListener dialogFieldChanged
	@Override
	public void dialogFieldChanged(DialogField field) {
		updateStatus();
	}

	protected IStatus getStatus() {
		IStatus f = getStatus(fGroupId, fArtifactId, fVersion, fProject);
		if (f != null)
			return f;

		return new StatusInfo();
	}

	protected void refreshData() {
		data.fGroupId = fGroupId.getText();
		data.fArtifactId = fArtifactId.getText();
		data.fVersion = fVersion.getText();
		data.fProject = fProject.getText();
	}
}

package sdt.wizards.newclient;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;

import sdt.SDTPlugin;
import sdt.wizards.NewWizardPage;

@SuppressWarnings("restriction")
public class NewClientWizardPage extends NewWizardPage {

	@SuppressWarnings("unused")
	private NewClientState data;

	private StringButtonDialogField fProjectField;
	private StringButtonDialogField fFacadeField;
	private StringButtonDialogField fPackageField;
	private StringDialogField fNameField;
	private StringButtonDialogField fXmlField;

	public NewClientWizardPage(NewClientState data) {
		super("New Client");
		this.data = data;

		int i = 1;
		fProjectField = createStringButtonDialogField("&" + i++ + " Project:", "Browse &Q", this.PROJECT,
				"service-integration$", null, false, null);
		fFacadeField = createStringButtonDialogField("&" + i++ + " Facade:", "Browse &W", this.CLASS, null, null,
				false, null);
		fPackageField = createStringButtonDialogField("&" + i++ + " Package:", "Browse &E", this.PACKAGE, null,
				null, true, fProjectField);
		fNameField = createStringDialogField(this, "&" + i++ + " Name:", null);
		fXmlField = createStringButtonDialogField("&" + i++ + " Xml:", "Browse &R", this.FILE, ".xml$",
				SDTPlugin.D_SPRING, true, fProjectField);

	}

	// TODO IDialogFieldListener dialogFieldChanged
	@Override
	public void dialogFieldChanged(DialogField field) {
		updateStatus();
	}

	protected IStatus getStatus() {
		return new StatusInfo();
	}

	public void refreshData() {
	}
}

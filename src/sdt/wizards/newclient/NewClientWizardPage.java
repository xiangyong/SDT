package sdt.wizards.newclient;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import sdt.wizards.NewWizardPage;

@SuppressWarnings("restriction")
public class NewClientWizardPage extends NewWizardPage implements IDialogFieldListener {

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
				"service-integration$", null, null);
		fFacadeField = createStringButtonDialogField("&" + i++ + " Facade:", "Browse &W", this.CLASS,
				"service-integration$", null, null);
		fPackageField = createStringButtonDialogField("&" + i++ + " Package:", "Browse &E", this.PACKAGE,
				"service-integration$", null, fProjectField);
		fNameField = createStringDialogField(this, "&" + i++ + " Name:");
		fXmlField = createStringButtonDialogField("&" + i++ + " Xml:", "Browse &R", this.FILE,
				"service-integration$", null, null);

	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NULL);
		setControl(composite);

		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		int nColumns = 4;
		layout.numColumns = nColumns;

		createStringButtonDialogField(composite, nColumns, this.fProjectField, false);
		createStringButtonDialogField(composite, nColumns, this.fFacadeField, true);
		createStringButtonDialogField(composite, nColumns, this.fPackageField, true);
		createStringDialogField(composite, nColumns, this.fNameField);
		createStringButtonDialogField(composite, nColumns, this.fXmlField, true);

		updateStatus();

	}

	// TODO IDialogFieldListener dialogFieldChanged
	@Override
	public void dialogFieldChanged(DialogField field) {
		updateStatus();
	}

	private void updateStatus() {
		IStatus status = getStatus(fProjectField, fFacadeField, fPackageField, fNameField, fXmlField);
		super.updateStatus(status);

	}

	private IStatus getStatus(StringDialogField... fields) {
		return new StatusInfo();
	}

	public void refreshData() {
	}
}

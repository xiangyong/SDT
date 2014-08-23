package sdt.wizards.newcontroller;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.swt.SWT;

import sdt.SDTPlugin;
import sdt.wizards.GroupTypeField;
import sdt.wizards.NewWizardPage;

@SuppressWarnings("restriction")
public class NewControllerWizardPage extends NewWizardPage implements IStringButtonAdapter, IDialogFieldListener {

	private NewControllerState data;

	private StringButtonDialogField fProjectField;
	private StringButtonDialogField fPackageField;
	private StringDialogField fNameField;
	private GroupTypeField fSurfixField;
	private static final String CONTROLLER = "Controller";

	public NewControllerWizardPage(NewControllerState data) {
		super("NewServiceWizard");
		this.data = data;

		int i = 1;
		// service
		fProjectField = createStringButtonDialogField("&" + i++ + " Project:", "Browse &Q", this.PROJECT, "-web-",
				null, false, null);
		fPackageField = createStringButtonDialogField("&" + i++ + " Package:", "Browse &W", this.PACKAGE, null,
				null, true, fProjectField);
		fNameField = createStringDialogField(this, "&" + i++ + " Name:", null);
		fSurfixField = createGroupTypeField("&" + i++ + " Name Surfix :", SWT.CHECK, CONTROLLER, CONTROLLER);

	}

	// TODO IDialogFieldListener dialogFieldChanged
	@Override
	public void dialogFieldChanged(DialogField field) {
		updateStatus();
	}

	private void updateStatus() {

		IStatus f = getStatus(this.fProjectField, this.fPackageField, this.fNameField);
		super.updateStatus(f);
		if (f.isOK()) {
			refreshData();
		}

	}

	private IStatus getStatus(StringDialogField... fields) {
		for (StringDialogField field : fields) {
			if (field.getText().isEmpty()) {
				String m = "\"" + field.getLabelControl(null).getText() + "\" is Emply";
				if (field instanceof StringButtonDialogField) {
					m = m + ", Using \"" + ((StringButtonDialogField) field).getChangeControl(null).getText()
							+ "\" to choose one";
				}
				return new StatusInfo(IStatus.ERROR, m);
			}
		}

		{
			IPackageFragment p = SDTPlugin
					.getPackageFragment(fProjectField.getText(), this.fPackageField.getText());
			if (p != null && p.exists()) {
				ICompilationUnit cu = p.getCompilationUnit(fNameField.getText() + ".java");
				if (cu != null && cu.exists()) {
					return new StatusInfo(IStatus.ERROR, "\"" + fNameField.getLabelControl(null).getText()
							+ "\" is already exist");
				}
			}
		}

		return new StatusInfo();
	}

	public void refreshData() {
		String className = this.fNameField.getText();
		if (this.fSurfixField.getSelection(CONTROLLER)) {
			className = className + CONTROLLER;
		}

		this.data.fFile = SDTPlugin.getPackageFragmentRoot(fProjectField.getText()).getPackageFragment(
				this.fPackageField.getText()).getPath().toString()
				+ "/" + className + ".java";
		this.data.fPackage = this.fPackageField.getText();
		this.data.fName = this.fNameField.getText();
		this.data.fClassName = className;

	}
}

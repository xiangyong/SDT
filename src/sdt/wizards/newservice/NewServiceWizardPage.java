package sdt.wizards.newservice;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import sdt.SDTPlugin;
import sdt.wizards.GroupTypeField;
import sdt.wizards.NewWizardPage;

@SuppressWarnings("restriction")
public class NewServiceWizardPage extends NewWizardPage {

	private NewServiceState data;

	private StringButtonDialogField fServiceProjField;
	private StringButtonDialogField fServicePackageField;
	private StringDialogField fServiceNameField;

	private StringButtonDialogField fImplProjField;
	private StringButtonDialogField fImplPackageField;
	private StringDialogField fImplNameField;

	private StringButtonDialogField fServiceXmlField;

	private GroupTypeField fServiceTypeField;

	public NewServiceWizardPage(NewServiceState data) {
		super("New Service");
		this.data = data;

		int i = 1;
		// service

		fServiceProjField = createStringButtonDialogField("&" + i++ + " Service Project:", "Browse &E",
				this.PROJECT, null, null, null);
		fServicePackageField = createStringButtonDialogField("&" + i++ + " Service Package:", "Browse &D",
				this.PACKAGE, null, null, fServiceProjField);
		fServiceNameField = createStringDialogField(this, "&" + i++ + " Service Name:");

		// service impl
		fImplProjField = createStringButtonDialogField("&" + i++ + " Impl Project:", "Browse &Q", this.PROJECT,
				null, null, null);
		fImplPackageField = createStringButtonDialogField("&" + i++ + " Impl Package:", "Browse &A", this.PACKAGE,
				null, null, fImplProjField);
		fImplNameField = createStringDialogField(this, "&" + i++ + " Impl Name:");

		// xml
		fServiceXmlField = createStringButtonDialogField("&" + i++ + " Service Xml:", "Browse &G", this.FILE,
				".xml$", SDTPlugin.D_SPRING, fImplProjField);

		// type
		fServiceTypeField = createGroupTypeField("&" + i++ + " Service Type:", SWT.CHECK, "W&S", "T&R");

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

		createStringButtonDialogField(composite, nColumns, fServiceProjField, false);
		createStringButtonDialogField(composite, nColumns, fServicePackageField, true);
		createStringDialogField(composite, nColumns, fServiceNameField);

		createSeparator(composite, nColumns);

		createStringButtonDialogField(composite, nColumns, fImplProjField, false);
		createStringButtonDialogField(composite, nColumns, fImplPackageField, true);
		createStringDialogField(composite, nColumns, fImplNameField);

		createSeparator(composite, nColumns);

		createStringButtonDialogField(composite, nColumns, fServiceXmlField, true);

		createGroupTypeDialogField(composite, nColumns, fServiceTypeField, null);

		updateStatus();

	}

	// TODO IDialogFieldListener dialogFieldChanged
	@Override
	public void dialogFieldChanged(DialogField field) {
		if (field == this.fServiceNameField) {
			if (fServiceNameField.getText().isEmpty()) {
				fImplNameField.setText("");
			} else {
				fImplNameField.setText(fServiceNameField.getText() + "Impl");
			}
		} else if (field == this.fServiceProjField) {
			if (this.fImplProjField.getText().isEmpty() && !fServiceProjField.getText().endsWith("-facade")) {
				fImplProjField.setText(fServiceProjField.getText());
			}
		} else if (field == this.fServicePackageField) {
			if (this.fImplPackageField.getText().isEmpty() && !fServiceProjField.getText().endsWith("-facade")) {
				fImplPackageField.setText(fServicePackageField.getText() + ".impl");
			}
		}

		updateStatus();
	}

	private void updateStatus() {

		IStatus f = getStatus(this.fServiceProjField, this.fServicePackageField, this.fServiceNameField,
				this.fImplProjField, this.fImplPackageField, this.fImplNameField, this.fServiceXmlField);
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
			IPackageFragment p = SDTPlugin.getPackageFragment(fServiceProjField.getText(), fServicePackageField
					.getText());
			if (p != null && p.exists()) {
				ICompilationUnit cu = p.getCompilationUnit(fServiceNameField.getText() + ".java");
				if (cu != null && cu.exists()) {
					return new StatusInfo(IStatus.ERROR, "\"" + fServiceNameField.getLabelControl(null).getText()
							+ "\" is already exist");
				}
			}
		}

		{
			IPackageFragment p = SDTPlugin
					.getPackageFragment(fImplProjField.getText(), fImplPackageField.getText());
			if (p != null && p.exists()) {
				ICompilationUnit cu = p.getCompilationUnit(fImplNameField.getText() + ".java");
				if (cu != null && cu.exists()) {
					return new StatusInfo(IStatus.ERROR, "\"" + fImplNameField.getLabelControl(null).getText()
							+ "\" is already exist");
				}
			}
		}

		if (!this.fServiceXmlField.getText().endsWith(".xml")) {
			return new StatusInfo(IStatus.ERROR, "\"" + this.fServiceXmlField.getLabelControl(null).getText()
					+ "\" is not end vith \".xml\"");
		}
		return new StatusInfo();
	}

	public void refreshData() {

		this.data.serviceFile = SDTPlugin.getPackageFragment(fServiceProjField.getText(), fServicePackageField
				.getText())
				+ "/" + this.fServiceNameField.getText() + ".java";
		this.data.servicePackage = this.fServicePackageField.getText();
		this.data.serviceName = this.fServiceNameField.getText();

		this.data.implFile = SDTPlugin.getPackageFragment(fImplProjField.getText(), fImplPackageField.getText())
				.getPath().toString()
				+ "/" + this.fImplNameField.getText() + ".java";
		this.data.implPackage = this.fImplPackageField.getText();
		this.data.implName = this.fImplNameField.getText();

		IFolder f = SDTPlugin.getProject(this.fImplProjField.getText()).getFolder(SDTPlugin.D_SPRING);
		IResource r = f.findMember(this.fServiceXmlField.getText());
		if (r == null || !r.exists()) {
			this.data.createXml = true;
			this.data.xmlFile = f.getFullPath().toString() + "/" + this.fServiceXmlField.getText();
		} else {
			this.data.xmlFile = r.getFullPath().toString();
		}

		this.data.serviceType = this.fServiceTypeField.getValue();

	}
}

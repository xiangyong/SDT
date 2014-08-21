package sdt.wizards.newproject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.JavaPackageCompletionProcessor;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import sdt.SDTPlugin;
import sdt.wizards.NewWizardPage;

@SuppressWarnings("restriction")
public class NewProjectWizardPage extends NewWizardPage implements IStringButtonAdapter, IDialogFieldListener {

	private NewProjectState data;

	private StringButtonDialogField typeField;
	private StringButtonDialogField systemField;
	private StringDialogField nameField;

	public NewProjectWizardPage(NewProjectState data) {
		super("NewProjectWizard");
		this.data = data;
		int i = 1;

		systemField = createStringButtonDialogField(this, this, "&" + i++ + " System Name:", "Browse &E");
		typeField = createStringButtonDialogField(this, this, "&" + i++ + " Project Type:", "Browse &D");
		nameField = createStringDialogField(this, "Project Name:");

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

		// TODO
		createStringButtonDialogField(composite, nColumns, this.systemField);
		this.systemField.getTextControl(composite).setEditable(false);

		createStringButtonDialogField(composite, nColumns, this.typeField);
		this.typeField.getTextControl(composite).setEditable(false);

		createStringDialogField(composite, nColumns, this.nameField);

	}

	@Override
	public JavaPackageCompletionProcessor getJavaPackageCompletionProcessor(StringButtonDialogField field) {
		return null;
	}

	@Override
	public StringButtonDialogField getProjFieldByPkgField(StringButtonDialogField field) {
		return null;
	}

	@Override
	public void changeControlPressed(DialogField field) {
		if (field == this.typeField) {
			chooseType();
		} else if (field == this.systemField) {
			chooseSystem();
		}
	}

	private void chooseSystem() {

		IProject[] projects = wsroot.getProjects();

		List<String> systems = new ArrayList<String>();
		for (IProject project : projects) {
			String projectName = project.getName();
			if (projectName.endsWith("-assembly-template")) {
				String system = projectName.substring(0, projectName.indexOf("-"));
				systems.add(system);
			}
		}
		if (systems.isEmpty()) {
			return;
		}

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
		dialog.setIgnoreCase(false);
		dialog.setTitle("Select System");
		dialog.setMessage("Select System");
		dialog.setEmptyListMessage("No System");
		dialog.setElements(systems.toArray());
		dialog.setHelpAvailable(false);

		if (dialog.open() == Window.OK) {
			this.systemField.setText(dialog.getFirstResult().toString());
		}
	}

	private void chooseType() {
		if (this.systemField.getText().isEmpty()) {
			return;
		}
		String system = this.systemField.getText();

		List<String> types = new ArrayList<String>();
		types.add("biz-service-impl");
		types.add("web-home");
		types.add("core-service");

		List<String> filteredTypes = new ArrayList<String>();
		IProject[] projects = wsroot.getProjects();
		for (IProject project : projects) {
			String projectName = project.getName();
			for (String type : types) {
				if (projectName.equals(system + "-" + type)) {
					filteredTypes.add(type);
				}
			}

		}
		if (filteredTypes.isEmpty()) {
			return;
		}

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
		dialog.setIgnoreCase(false);
		dialog.setTitle("Select Project");
		dialog.setMessage("Select Project");
		dialog.setEmptyListMessage("No Project");
		dialog.setElements(filteredTypes.toArray());
		dialog.setHelpAvailable(false);

		if (dialog.open() == Window.OK) {
			this.typeField.setText(dialog.getFirstResult().toString());
		}
	}

	@Override
	public void dialogFieldChanged(DialogField field) {
		if (field == this.systemField || field == this.typeField) {
			String name = this.nameField.getText();
			if ((name.isEmpty() || name.endsWith("-")) && !this.systemField.getText().isEmpty()
					&& !this.typeField.getText().isEmpty()) {
				String type = this.typeField.getText();
				type = type.substring(0, type.indexOf("-"));
				String nameAuto = this.systemField.getText() + "-" + type + "-";
				this.nameField.setText(nameAuto);
				this.nameField.setFocus();
				this.nameField.getTextControl(null).setSelection(nameAuto.length());
			}
		}

		updateStatus();
	}

	@Override
	public void refreshData() {
		this.data.system = this.systemField.getText();
		this.data.name = this.nameField.getText();
		this.data.type = this.typeField.getText();

		IProject p = SDTPlugin.getProject(this.systemField.getText() + "-" + this.typeField.getText());

		try {
			String name = this.nameField.getText();
			this.data.dir = new File(p.getDescription().getLocationURI()).getParentFile() + File.separator
					+ name.substring(name.lastIndexOf("-") + 1);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void updateStatus() {
		IStatus status = getStatus();
		super.updateStatus(status);
		if (status.isOK()) {
			refreshData();
		}
	}

	public IStatus getStatus() {
		if (this.systemField.getText().isEmpty()) {
			return new StatusInfo(IStatus.ERROR, getLabel(this.systemField) + " is Empty");
		}
		if (this.typeField.getText().isEmpty()) {
			return new StatusInfo(IStatus.ERROR, getLabel(this.typeField) + " is Empty");
		}
		if (this.nameField.getText().isEmpty()) {
			return new StatusInfo(IStatus.ERROR, getLabel(this.nameField) + " is Empty");
		}
		if (this.nameField.getText().endsWith("-")) {
			return new StatusInfo(IStatus.ERROR, getLabel(this.nameField) + " is Invalid");
		}
		{
			IProject p = SDTPlugin.getProject(this.nameField.getText());
			if (p.exists()) {
				return new StatusInfo(IStatus.ERROR, getLabel(this.nameField) + " is Already Exist");
			}
		}
		{
			IProject p = SDTPlugin.getProject(this.systemField.getText() + "-" + this.typeField.getText());
			try {
				String pl = new File(p.getDescription().getLocationURI()).getParentFile() + File.separator
						+ this.nameField.getText();
				if (new File(pl).exists()) {
					return new StatusInfo(IStatus.ERROR, pl + " is Already Exist");
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return new StatusInfo();
	}

	private String getLabel(StringDialogField field) {
		return "\"" + field.getLabelControl(null).getText() + "\"";
	}

}

package sdt.wizards.newproject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import sdt.SDTPlugin;
import sdt.wizards.NewWizardPage;

@SuppressWarnings("restriction")
public class NewProjectWizardPage extends NewWizardPage implements IStringButtonAdapter, IDialogFieldListener {

	private NewProjectState data;

	private StringButtonDialogField fTypeField;
	private StringButtonDialogField fSystemField;
	private StringDialogField fNameField;

	public NewProjectWizardPage(NewProjectState data) {
		super("New Project");
		this.data = data;
		int i = 1;

		fSystemField = createStringButtonDialogField("&" + i++ + " System:", "Browse &Q", this.OTHER, null, null,
				false, null);
		fTypeField = createStringButtonDialogField("&" + i++ + " Type:", "Browse &W", this.OTHER, null, null,
				false, null);
		fNameField = createStringDialogField(this, "Project Name:", null);

	}

	@Override
	public void chooseOther(StringButtonDialogField field) {
		if (field == this.fTypeField) {
			chooseType();
		} else if (field == this.fSystemField) {
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
			this.fSystemField.setText(dialog.getFirstResult().toString());
		}
	}

	private void chooseType() {
		if (this.fSystemField.getText().isEmpty()) {
			return;
		}
		String system = this.fSystemField.getText();

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
			this.fTypeField.setText(dialog.getFirstResult().toString());
		}
	}

	@Override
	public void dialogFieldChanged(DialogField field) {
		if (field == this.fSystemField || field == this.fTypeField) {
			String name = this.fNameField.getText();
			if ((name.isEmpty() || name.endsWith("-")) && !this.fSystemField.getText().isEmpty()
					&& !this.fTypeField.getText().isEmpty()) {
				String type = this.fTypeField.getText();
				type = type.substring(0, type.indexOf("-"));
				String nameAuto = this.fSystemField.getText() + "-" + type + "-";
				this.fNameField.setText(nameAuto);
				this.fNameField.setFocus();
				this.fNameField.getTextControl(null).setSelection(nameAuto.length());
			}
		}

		updateStatus();
	}

	@Override
	protected void refreshData() {
		this.data.system = this.fSystemField.getText();
		this.data.name = this.fNameField.getText();
		this.data.type = this.fTypeField.getText();

		IProject p = SDTPlugin.getProject(this.fSystemField.getText() + "-" + this.fTypeField.getText());

		try {
			String name = this.fNameField.getText();
			this.data.dir = new File(p.getDescription().getLocationURI()).getParentFile() + File.separator
					+ name.substring(name.lastIndexOf("-") + 1);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	protected IStatus getStatus() {
		if (this.fSystemField.getText().isEmpty()) {
			return new StatusInfo(IStatus.ERROR, getLabel(this.fSystemField) + " is Empty");
		}
		if (this.fTypeField.getText().isEmpty()) {
			return new StatusInfo(IStatus.ERROR, getLabel(this.fTypeField) + " is Empty");
		}
		if (this.fNameField.getText().isEmpty()) {
			return new StatusInfo(IStatus.ERROR, getLabel(this.fNameField) + " is Empty");
		}
		if (this.fNameField.getText().endsWith("-")) {
			return new StatusInfo(IStatus.ERROR, getLabel(this.fNameField) + " is Invalid");
		}
		{
			IProject p = SDTPlugin.getProject(this.fNameField.getText());
			if (p.exists()) {
				return new StatusInfo(IStatus.ERROR, getLabel(this.fNameField) + " is Already Exist");
			}
		}
		{
			IProject p = SDTPlugin.getProject(this.fSystemField.getText() + "-" + this.fTypeField.getText());
			try {
				String pl = new File(p.getDescription().getLocationURI()).getParentFile() + File.separator
						+ this.fNameField.getText();
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

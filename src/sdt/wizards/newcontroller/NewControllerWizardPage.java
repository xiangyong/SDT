package sdt.wizards.newcontroller;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.ControlContentAssistHelper;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.JavaPackageCompletionProcessor;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sdt.wizards.GroupTypeField;
import sdt.wizards.NewWizardPage;

@SuppressWarnings("restriction")
public class NewControllerWizardPage extends NewWizardPage implements IStringButtonAdapter, IDialogFieldListener {

	private NewControllerState data;

	private StringButtonDialogField projectField;
	private StringButtonDialogField packageField;
	private JavaPackageCompletionProcessor packageCompletionProcessor;
	private StringDialogField nameField;
	private GroupTypeField surfixField;
	private static final String CONTROLLER = "Controller";

	public NewControllerWizardPage(NewControllerState data) {
		super("NewServiceWizard");
		this.data = data;

		packageCompletionProcessor = new JavaPackageCompletionProcessor();

		int i = 1;
		// service
		projectField = createStringButtonDialogField(this, this, "&" + i++ + " Project:", "Browse &Q");
		projectField = createStringButtonDialogField(this, this, "&" + i++ + " Package:", "Browse &W");
		nameField = createStringDialogField(this, "&" + i++ + " Name:");

		surfixField = new GroupTypeField(SWT.CHECK);
		surfixField.setDialogFieldListener(this);
		surfixField.setLabels(CONTROLLER);
		surfixField.setLabelText("&" + i++ + " Name Surfix :");

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

		createStringButtonDialogField(composite, nColumns, this.projectField);
		projectField.getTextControl(composite).setEditable(false);

		createStringButtonDialogField(composite, nColumns, this.packageField);
		Text text = packageField.getTextControl(null);
		ControlContentAssistHelper.createTextContentAssistant(text, packageCompletionProcessor);
		TextFieldNavigationHandler.install(text);

		createStringDialogField(composite, nColumns, this.nameField);

		surfixField.doFillIntoGrid(composite, nColumns);
		surfixField.setValue(CONTROLLER);

		updateStatus();

	}

	// TODO IStringButtonAdapter changeControlPressed
	@Override
	public void changeControlPressed(DialogField field) {
		if (field == this.projectField) {
			chooseProject((StringButtonDialogField) field);
		} else if (field == this.packageField) {
			choosePackage((StringButtonDialogField) field);
		}
	}

	protected List<IProject> filterProjects(IProject[] projects) {
		List<IProject> f = new ArrayList<IProject>();
		for (IProject p : super.filterProjects(projects)) {
			if (p.getName().contains("-web-")) {
				f.add(p);
			}
		}
		return f;
	}

	public JavaPackageCompletionProcessor getJavaPackageCompletionProcessor(StringButtonDialogField field) {
		if (field == this.projectField) {
			return this.packageCompletionProcessor;
		}

		return null;
	}

	public StringButtonDialogField getProjFieldByPkgField(StringButtonDialogField field) {
		if (field == this.packageField) {
			return this.projectField;
		} else {
			return null;
		}
	}

	// TODO IDialogFieldListener dialogFieldChanged
	@Override
	public void dialogFieldChanged(DialogField field) {
		updateStatus();
	}

	private void updateStatus() {

		IStatus f = getStatus(this.projectField, this.packageField, this.nameField);
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
			IPackageFragment p = getPackageFragmentRoot(this.projectField).getPackageFragment(
					this.packageField.getText());
			if (p != null && p.exists()) {
				ICompilationUnit cu = p.getCompilationUnit(nameField.getText() + ".java");
				if (cu != null && cu.exists()) {
					return new StatusInfo(IStatus.ERROR, "\"" + nameField.getLabelControl(null).getText()
							+ "\" is already exist");
				}
			}
		}

		return new StatusInfo();
	}

	public void refreshData() {
		String className = this.nameField.getText();
		if (this.surfixField.getSelection(CONTROLLER)) {
			className = className + CONTROLLER;
		}

		this.data.fFile = getPackageFragmentRoot(this.projectField).getPackageFragment(this.packageField.getText())
				.getPath().toString()
				+ "/" + className + ".java";
		this.data.fPackage = this.packageField.getText();
		this.data.fName = this.nameField.getText();
		this.data.fClassName = className;

	}
}

package sdt.wizards.newservice;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import sdt.SDTPlugin;
import sdt.wizards.GroupTypeField;
import sdt.wizards.NewWizardPage;
import sdt.wizards.ResourceItemLabelProvider;

@SuppressWarnings("restriction")
public class NewServiceWizardPage extends NewWizardPage implements IStringButtonAdapter, IDialogFieldListener {

	private NewServiceState data;

	private StringButtonDialogField serviceProjField;
	private StringButtonDialogField servicePackageField;
	private JavaPackageCompletionProcessor servicePackageCompletionProcessor;
	private StringDialogField serviceNameField;

	private StringButtonDialogField implProjField;
	private StringButtonDialogField implPackageField;
	private JavaPackageCompletionProcessor implPackageCompletionProcessor;
	private StringDialogField implNameField;

	private StringButtonDialogField serviceXmlField;

	private GroupTypeField serviceTypeField;

	public NewServiceWizardPage(NewServiceState data) {
		super("NewServiceWizard");
		this.data = data;

		servicePackageCompletionProcessor = new JavaPackageCompletionProcessor();
		implPackageCompletionProcessor = new JavaPackageCompletionProcessor();

		int i = 1;
		// service
		serviceProjField = createStringButtonDialogField(this, this, "&" + i++ + " Service Project:", "Browse &E");
		servicePackageField = createStringButtonDialogField(this, this, "&" + i++ + " Service Package:",
				"Browse &D");
		serviceNameField = createStringDialogField(this, "&" + i++ + " Service Name:");

		// service impl
		implProjField = createStringButtonDialogField(this, this, "&" + i++ + " Impl Project:", "Browse &Q");
		implPackageField = createStringButtonDialogField(this, this, "&" + i++ + " Impl Package:", "Browse &A");
		implNameField = createStringDialogField(this, "&" + i++ + " Impl Name:");

		// xml
		serviceXmlField = createStringButtonDialogField(this, this, "&" + i++ + " Service Xml:", "Browse &G");

		// type
		serviceTypeField = new GroupTypeField(SWT.CHECK);
		serviceTypeField.setDialogFieldListener(this);
		serviceTypeField.setLabelText("&" + i++ + " Service Type:");
		serviceTypeField.setLabels("W&S", "T&R");
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

		createStringButtonDialogField(composite, nColumns, this.serviceProjField);
		serviceProjField.getTextControl(composite).setEditable(false);
		createStringButtonDialogField(composite, nColumns, this.servicePackageField);
		{
			Text text = servicePackageField.getTextControl(null);
			ControlContentAssistHelper.createTextContentAssistant(text, servicePackageCompletionProcessor);
			TextFieldNavigationHandler.install(text);
		}
		createStringDialogField(composite, nColumns, this.serviceNameField);
		
		createSeparator(composite, nColumns);

		createStringButtonDialogField(composite, nColumns, this.implProjField);
		implProjField.getTextControl(composite).setEditable(false);
		createStringButtonDialogField(composite, nColumns, this.implPackageField);
		{
			Text text = implPackageField.getTextControl(null);
			ControlContentAssistHelper.createTextContentAssistant(text, implPackageCompletionProcessor);
			TextFieldNavigationHandler.install(text);
		}
		createStringDialogField(composite, nColumns, this.implNameField);

		createSeparator(composite, nColumns);
		
		createStringButtonDialogField(composite, nColumns, this.serviceXmlField);
		serviceTypeField.doFillIntoGrid(composite, nColumns);

		updateStatus();

	}

	// TODO IStringButtonAdapter changeControlPressed
	@Override
	public void changeControlPressed(DialogField field) {
		if (field == this.serviceProjField || field == this.implProjField) {
			chooseProject((StringButtonDialogField) field);
		} else if (field == this.servicePackageField || field == this.implPackageField) {
			choosePackage((StringButtonDialogField) field);
		} else if (field == this.serviceXmlField) {
			chooseXml();
		}
	}

	public JavaPackageCompletionProcessor getJavaPackageCompletionProcessor(StringButtonDialogField field) {
		if (field == this.serviceProjField) {
			return this.servicePackageCompletionProcessor;
		} else if (field == this.implProjField) {
			return this.implPackageCompletionProcessor;
		}

		return null;
	}

	public StringButtonDialogField getProjFieldByPkgField(StringButtonDialogField field) {
		if (field == this.servicePackageField) {
			return this.serviceProjField;
		} else if (field == this.implPackageField) {
			return this.implProjField;
		} else {
			return null;
		}
	}

	private void chooseXml() {
		String name = this.implProjField.getText();
		IFolder folder = SDTPlugin.getProject(name).getFolder(SDTPlugin.D_SPRING);
		if (!folder.exists())
			return;

		List<IResource> f = new ArrayList<IResource>();
		getSpringXml(f, folder);
		if (f.isEmpty())
			return;

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(),
				new ResourceItemLabelProvider(f));
		dialog.setIgnoreCase(false);
		dialog.setTitle("Ñ¡Ôñ Spring Xml");
		dialog.setMessage("Ñ¡Ôñ Spring Xml");
		dialog.setEmptyListMessage("Ñ¡Ôñ Spring Xml");
		dialog.setElements(f.toArray());
		dialog.setHelpAvailable(false);

		if (dialog.open() == Window.OK) {
			IResource r = (IResource) dialog.getFirstResult();
			this.serviceXmlField.setText(r.getFullPath().makeRelativeTo(folder.getFullPath()).toString());
		}
	}

	private void getSpringXml(List<IResource> list, IFolder root) {
		IResource[] rs = null;
		try {
			rs = root.members();
			for (IResource r : rs) {
				if (r.getType() == IFile.FILE && r.getFileExtension().equals("xml")) {
					list.add(r);
				} else if (r.getType() == IFile.FOLDER) {
					getSpringXml(list, (IFolder) r);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	// TODO IDialogFieldListener dialogFieldChanged
	@Override
	public void dialogFieldChanged(DialogField field) {
		if (field == this.serviceNameField) {
			if (serviceNameField.getText().isEmpty()) {
				implNameField.setText("");
			} else {
				implNameField.setText(serviceNameField.getText() + "Impl");
			}
		} else if (field == this.serviceProjField) {
			if (this.implProjField.getText().isEmpty() && !serviceProjField.getText().endsWith("-facade")) {
				implProjField.setText(serviceProjField.getText());
			}
		} else if (field == this.servicePackageField) {
			if (this.implPackageField.getText().isEmpty() && !serviceProjField.getText().endsWith("-facade")) {
				implPackageField.setText(servicePackageField.getText()+".impl");
			}
		}

		updateStatus();
	}

	private void updateStatus() {

		IStatus f = getStatus(this.serviceProjField, this.servicePackageField, this.serviceNameField,
				this.implProjField, this.implPackageField, this.implNameField, this.serviceXmlField);
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
			IPackageFragment p = getPackageFragmentRoot(this.serviceProjField).getPackageFragment(
					this.servicePackageField.getText());
			if (p != null && p.exists()) {
				ICompilationUnit cu = p.getCompilationUnit(serviceNameField.getText() + ".java");
				if (cu != null && cu.exists()) {
					return new StatusInfo(IStatus.ERROR, "\"" + serviceNameField.getLabelControl(null).getText()
							+ "\" is already exist");
				}
			}
		}

		{
			IPackageFragment p = getPackageFragmentRoot(this.implProjField).getPackageFragment(
					this.implPackageField.getText());
			if (p != null && p.exists()) {
				ICompilationUnit cu = p.getCompilationUnit(implNameField.getText() + ".java");
				if (cu != null && cu.exists()) {
					return new StatusInfo(IStatus.ERROR, "\"" + implNameField.getLabelControl(null).getText()
							+ "\" is already exist");
				}
			}
		}

		if (!this.serviceXmlField.getText().endsWith(".xml")) {
			return new StatusInfo(IStatus.ERROR, "\"" + this.serviceXmlField.getLabelControl(null).getText()
					+ "\" is not end vith \".xml\"");
		}
		return new StatusInfo();
	}

	public void refreshData() {

		this.data.serviceFile = getPackageFragmentRoot(this.serviceProjField).getPackageFragment(
				this.servicePackageField.getText()).getPath().toString()
				+ "/" + this.serviceNameField.getText() + ".java";
		this.data.servicePackage = this.servicePackageField.getText();
		this.data.serviceName = this.serviceNameField.getText();

		this.data.implFile = getPackageFragmentRoot(this.implProjField).getPackageFragment(
				this.implPackageField.getText()).getPath().toString()
				+ "/" + this.implNameField.getText() + ".java";
		this.data.implPackage = this.implPackageField.getText();
		this.data.implName = this.implNameField.getText();

		IFolder f = SDTPlugin.getProject(this.implProjField.getText()).getFolder(SDTPlugin.D_SPRING);
		IResource r = f.findMember(this.serviceXmlField.getText());
		if (r == null || !r.exists()) {
			this.data.createXml = true;
			this.data.xmlFile = f.getFullPath().toString() + "/" + this.serviceXmlField.getText();
		} else {
			this.data.xmlFile = r.getFullPath().toString();
		}

		this.data.serviceType = this.serviceTypeField.getValue();

	}
}

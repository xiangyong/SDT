package sdt.wizards.newclient;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.search.JavaSearchScopeFactory;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import sdt.NameUtil;
import sdt.SDTPlugin;
import sdt.wizards.GroupTypeField;
import sdt.wizards.NewWizardPage;

@SuppressWarnings("restriction")
public class NewClientWizardPage extends NewWizardPage {

	private NewClientState data;

	private StringButtonDialogField fProjectField;
	private StringButtonDialogField fFacadeField;
	private StringButtonDialogField fPackageField;
	private StringDialogField fNameField;
	private StringButtonDialogField fXmlField;
	private GroupTypeField fTypeField;

	private IJavaElement fFacade;

	public NewClientWizardPage(NewClientState data) {
		super("New Client");
		this.data = data;

		int i = 1;
		fProjectField = createStringButtonDialogField("&" + i++ + " Project:", "Browse &Q", this.PROJECT,
				"service-integration$", null, false, null);
		fFacadeField = createStringButtonDialogField("&" + i++ + " Facade:", "Browse &W", this.OTHER, null, null,
				false, fProjectField);
		fPackageField = createStringButtonDialogField("&" + i++ + " Package:", "Browse &E", this.PACKAGE, null,
				null, true, fProjectField);
		fNameField = createStringDialogField(this, "&" + i++ + " Name:", null);
		fXmlField = createStringButtonDialogField("&" + i++ + " Xml:", "Browse &R", this.FILE, ".xml$",
				SDTPlugin.D_SPRING, true, fProjectField);
		fTypeField = createGroupTypeField("&" + i++ + " Service Type:", SWT.RADIO, "W&S", "W&S", "&TR");

	}

	protected void chooseOther(StringButtonDialogField field) {
		Shell parent = JavaPlugin.getActiveWorkbenchShell();
		parent = getShell();
		boolean multi = false;
		IRunnableContext context = new ProgressMonitorDialog(parent);
		IProject project = SDTPlugin.getProject(fProjectField.getText());
		IJavaSearchScope scope = JavaSearchScopeFactory.getInstance().createJavaSearchScope(
				new IResource[] { project }, JavaSearchScopeFactory.LIBS);
		int elementKinds = IJavaElementSearchConstants.CONSIDER_INTERFACES; // IJavaSearchConstants.INTERFACE
		String filter = "*facade";
		FilteredTypesSelectionDialog dialog = null;
		try {

			dialog = (FilteredTypesSelectionDialog) JavaUI.createTypeDialog(parent, context, scope, elementKinds,
					multi, filter);
			dialog.setTitle("Select Facade");
			dialog.setMessage("Select Facade");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (dialog == null)
			return;

		if (dialog.open() == Window.OK) {
			IJavaElement je = (IJavaElement) dialog.getFirstResult();
			fFacade = je;
			field.setText(je.getElementName());
		}
	}

	// TODO IDialogFieldListener dialogFieldChanged
	@Override
	public void dialogFieldChanged(DialogField field) {
		if (field == fProjectField || field == fFacadeField) {
			if (!fFacadeField.getText().isEmpty() && !fFacadeField.getText().isEmpty()) {
				String project = fProjectField.getText();
				String system = NameUtil.firstString(project, '-');
				String facadeSystem = NameUtil.firstString(fFacade.getParent().getParent().getParent()
						.getElementName(), '-');
				String pkg = "com.alipay." + system + ".common.service.integration." + facadeSystem;
				fPackageField.setText(pkg);
				fNameField.setText(fFacadeField.getText() + "Client");
			}
		}
		updateStatus();
	}

	protected IStatus getStatus() {
		IStatus f = getStatus(fPackageField, fNameField, fXmlField);
		if (f != null)
			return f;

		return new StatusInfo();
	}

	public void refreshData() {
		data.fProject = fProjectField.getText();
		data.fFacade = fFacade;
		data.fPackage = fPackageField.getText();
		data.fName = fNameField.getText();
		data.fXml = fXmlField.getText();
		data.fType = fTypeField.getValue();
	}
}

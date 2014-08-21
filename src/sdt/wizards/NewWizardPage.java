package sdt.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.JavaPackageCompletionProcessor;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.Separator;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import sdt.SDTPlugin;

@SuppressWarnings("restriction")
public abstract class NewWizardPage extends NewElementWizardPage {

	public IWorkspaceRoot wsroot;

	public NewWizardPage(String name) {
		super(name);
		this.wsroot = ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	public void createControl(Composite parent) {
	}

	protected StringButtonDialogField createStringButtonDialogField(IStringButtonAdapter adapter,
			IDialogFieldListener listener, String label, String buttonLabel) {
		StringButtonDialogField f = new StringButtonDialogField(adapter);
		f.setDialogFieldListener(listener);
		f.setLabelText(label);
		f.setButtonLabel(buttonLabel);
		return f;
	}

	protected StringDialogField createStringDialogField(IDialogFieldListener listener, String label) {
		StringDialogField f = new StringDialogField();
		f.setDialogFieldListener(listener);
		f.setLabelText(label);
		return f;
	}

	protected void createStringButtonDialogField(Composite composite, int nColumns, StringButtonDialogField field) {
		field.doFillIntoGrid(composite, nColumns);
		Text text = field.getTextControl(null);
		LayoutUtil.setWidthHint(text, getMaxFieldWidth());
		LayoutUtil.setHorizontalGrabbing(text);
	}

	protected void createStringDialogField(Composite composite, int nColumns, StringDialogField field) {
		field.doFillIntoGrid(composite, nColumns - 1);
		DialogField.createEmptySpace(composite);

		Text text = field.getTextControl(null);
		LayoutUtil.setWidthHint(text, getMaxFieldWidth());
		TextFieldNavigationHandler.install(text);

	}

	protected void createSeparator(Composite composite, int nColumns) {
		(new Separator(SWT.SEPARATOR | SWT.HORIZONTAL)).doFillIntoGrid(composite, nColumns,
				convertHeightInCharsToPixels(1));
	}

	protected int getMaxFieldWidth() {
		return convertWidthInCharsToPixels(40);
	}

	final protected void chooseProject(StringButtonDialogField field) {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		List<IProject> filteredPorjects = filterProjects(projects);

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(),
				new JavaElementLabelProvider());
		dialog.setIgnoreCase(false);
		dialog.setTitle("Select Project");
		dialog.setMessage("Select Project");
		dialog.setEmptyListMessage("No Project");
		dialog.setElements(filteredPorjects.toArray());
		dialog.setHelpAvailable(false);
		if (dialog.open() == Window.OK) {
			IProject f = (IProject) dialog.getFirstResult();
			field.setText(f.getName());

			JavaPackageCompletionProcessor processor = getJavaPackageCompletionProcessor(field);
			if (processor != null) {
				IPackageFragmentRoot root = getPackageFragmentRoot(field);
				processor.setPackageFragmentRoot(root);
			}
		}
	}

	protected List<IProject> filterProjects(IProject[] projects) {
		List<IProject> f = new ArrayList<IProject>();
		for (IProject project : projects) {
			IFolder folder = project.getFolder(SDTPlugin.D_RES);
			if (folder == null || !folder.exists())
				continue;

			folder = project.getFolder(SDTPlugin.D_JAVA);
			if (folder == null || !folder.exists())
				continue;

			f.add(project);
		}
		return f;
	}

	final public IPackageFragmentRoot getPackageFragmentRoot(StringButtonDialogField field) {
		if (field == null)
			return null;

		IPackageFragmentRoot f = null;
		String name = field.getText();
		IProject p = SDTPlugin.getProject(name);
		IJavaProject jp = JavaCore.create(p);
		try {
			f = jp.findPackageFragmentRoot(new Path("/" + name + "/" + SDTPlugin.D_JAVA));
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return f;
	}

	public abstract JavaPackageCompletionProcessor getJavaPackageCompletionProcessor(StringButtonDialogField field);

	final public void choosePackage(StringButtonDialogField field) {
		IPackageFragmentRoot root = getPackageFragmentRoot(getProjFieldByPkgField(field));
		IJavaElement[] packages = null;
		try {
			packages = root.getChildren();
		} catch (JavaModelException e) {
			JavaPlugin.log(e);
		}
		if (packages == null) {
			packages = new IJavaElement[0];
		}

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(),
				new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT));
		dialog.setIgnoreCase(false);
		dialog.setTitle("choose a package");
		dialog.setMessage("choose a package");
		dialog.setEmptyListMessage("choose a package");
		dialog.setElements(packages);
		dialog.setHelpAvailable(false);

		if (dialog.open() == Window.OK) {
			IPackageFragment p = (IPackageFragment) dialog.getFirstResult();
			field.setText(p.getElementName());
		}
	}

	public abstract StringButtonDialogField getProjFieldByPkgField(StringButtonDialogField field);

	public abstract void refreshData();
}

package sdt.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.TextFieldNavigationHandler;
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
public abstract class NewWizardPage extends NewElementWizardPage implements IStringButtonAdapter,
		IDialogFieldListener {

	protected final short PROJECT = 1; // MAX 1 << 14
	protected final short PACKAGE = 2;
	protected final short CLASS = 4;
	protected final short FILE = 8;
	protected final short OTHER = 16;

	protected final Map<StringButtonDialogField, Short> fFieldAndType = new HashMap<StringButtonDialogField, Short>();
	protected final Map<StringButtonDialogField, StringButtonDialogField> fFieldAndParendField = new HashMap<StringButtonDialogField, StringButtonDialogField>();
	protected final Map<StringButtonDialogField, String> fFieldAndFilter = new HashMap<StringButtonDialogField, String>();
	protected final Map<StringButtonDialogField, String> fFieldAndRoot = new HashMap<StringButtonDialogField, String>();

	public IWorkspaceRoot wsroot;

	public NewWizardPage(String name) {
		super(name);
		this.wsroot = ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	public void createControl(Composite parent) {
	}

	protected StringButtonDialogField createStringButtonDialogField( //
			String label, //
			String buttonLabel, //
			short type, //
			String filter, //
			String filterRoot, //
			StringButtonDialogField parentField //

	) {
		StringButtonDialogField f = new StringButtonDialogField(this);
		f.setDialogFieldListener(this);
		f.setLabelText(label);
		f.setButtonLabel(buttonLabel);

		fFieldAndType.put(f, type);
		if (filter != null)
			fFieldAndFilter.put(f, filter);

		if (parentField != null)
			fFieldAndParendField.put(f, parentField);

		if (filterRoot != null)
			fFieldAndRoot.put(f, filterRoot);

		return f;
	}

	protected GroupTypeField createGroupTypeField(String label, int type, String... labels) {
		GroupTypeField f = new GroupTypeField(type);
		f.setDialogFieldListener(this);
		f.setLabels(labels);
		f.setLabelText(label);
		return f;
	}

	protected StringDialogField createStringDialogField(IDialogFieldListener listener, String label) {
		StringDialogField f = new StringDialogField();
		f.setDialogFieldListener(listener);
		f.setLabelText(label);
		return f;
	}

	protected void createStringButtonDialogField(Composite composite, int nColumns, StringButtonDialogField field,
			boolean editable) {
		field.doFillIntoGrid(composite, nColumns);
		Text text = field.getTextControl(null);
		text.setEditable(editable);
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

	protected void createGroupTypeDialogField(Composite composite, int nColumns, GroupTypeField field, String label) {
		field.doFillIntoGrid(composite, nColumns - 1);
		if (label != null) {
			field.setValue(label);
		}
	}

	protected void createSeparator(Composite composite, int nColumns) {
		(new Separator(SWT.SEPARATOR | SWT.HORIZONTAL)).doFillIntoGrid(composite, nColumns,
				convertHeightInCharsToPixels(1));
	}

	protected int getMaxFieldWidth() {
		return convertWidthInCharsToPixels(40);
	}

	// TOOD
	public void changeControlPressed(DialogField field) {
		switch (fFieldAndType.get(field)) {
		case PROJECT:
			chooseProject((StringButtonDialogField) field);
			break;
		case PACKAGE:
			choosePackage((StringButtonDialogField) field);
			break;
		case FILE:
			chooseFile((StringButtonDialogField) field);
			break;
		case CLASS:
			// TODO
			break;
		case OTHER:
			chooseOther((StringButtonDialogField) field);
			break;
		}
	}

	final protected void chooseProject(StringButtonDialogField field) {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		List<IProject> filteredPorjects = filterProjects(projects, fFieldAndFilter.get(field));

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
		}
	}

	protected List<IProject> filterProjects(IProject[] projects, String filter) {
		List<IProject> f = new ArrayList<IProject>();
		for (IProject project : projects) {
			IFolder folder = project.getFolder(SDTPlugin.D_RES);
			if (folder == null || !folder.exists())
				continue;

			folder = project.getFolder(SDTPlugin.D_JAVA);
			if (folder == null || !folder.exists())
				continue;

			String name = project.getName();
			if (filter != null) {
				String key = filter;
				if (filter.charAt(0) == '^') {
					key = key.substring(1);
					if (name.startsWith(key))
						f.add(project);
				} else if (filter.charAt(filter.length() - 1) == '$') {
					key = key.substring(0, key.length() - 1);
					if (name.endsWith(key))
						f.add(project);
				} else {
					if (name.contains(key))
						f.add(project);
				}
			} else {
				f.add(project);
			}
		}
		return f;
	}

	final public void choosePackage(StringButtonDialogField packageField) {
		IPackageFragmentRoot root = SDTPlugin.getPackageFragmentRoot(fFieldAndParendField.get(packageField)
				.getText());
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
			packageField.setText(p.getElementName());
		}
	}

	private void chooseFile(StringButtonDialogField fileField) {
		String root = fFieldAndRoot.get(fileField);
		IProject project = SDTPlugin.getProject(fFieldAndParendField.get(fileField).getText());
		IFolder folder = project.getFolder(root);
		if (!folder.exists())
			return;

		List<IResource> f = new ArrayList<IResource>();
		String filter = fFieldAndFilter.get(fileField);
		SDTPlugin.findResource(f, folder, filter);
		if (f.isEmpty())
			return;

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(),
				new ResourceItemLabelProvider(f));
		dialog.setIgnoreCase(false);
		dialog.setTitle("Select File");
		dialog.setMessage("Select File");
		dialog.setEmptyListMessage("Select File");
		dialog.setElements(f.toArray());
		dialog.setHelpAvailable(false);

		if (dialog.open() == Window.OK) {
			IResource r = (IResource) dialog.getFirstResult();
			fileField.setText(r.getFullPath().makeRelativeTo(folder.getFullPath()).toString());
		}
	}

	protected void chooseOther(StringButtonDialogField field) {
	}

	public abstract void refreshData();
}

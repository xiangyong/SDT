package sdt.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
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
import org.eclipse.swt.layout.GridLayout;
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
	protected final Map<DialogField, Boolean> fFieldAndEditable = new HashMap<DialogField, Boolean>();
	protected final Map<DialogField, String> fFieldAndDefault = new HashMap<DialogField, String>();
	protected final Map<DialogField, Object> fFieldAndResult = new HashMap<DialogField, Object>();

	private final Collection<DialogField> fFields = new ArrayList<DialogField>();
	public IWorkspaceRoot wsroot;

	public NewWizardPage(String name) {
		super(name);
		this.wsroot = ResourcesPlugin.getWorkspace().getRoot();
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

		for (DialogField field : fFields) {
			if (field instanceof Separator) {
				createSeparator(composite, nColumns, (Separator) field);
			} else if (field instanceof GroupTypeField) {
				createGroupTypeDialogField(composite, nColumns, (GroupTypeField) field, fFieldAndDefault.get(field));
			} else if (field instanceof StringButtonDialogField) {
				createStringButtonDialogField(composite, nColumns, (StringButtonDialogField) field,
						fFieldAndEditable.get(field));
			} else if (field instanceof StringDialogField) {
				createStringDialogField(composite, nColumns, (StringDialogField) field);
			}
		}

		updateStatus();
	}

	protected StringButtonDialogField createStringButtonDialogField( //
			String label, //
			String buttonLabel, //
			short type, //
			String filter, //
			String filterRoot, //
			boolean editable, //
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

		fFieldAndEditable.put(f, editable);

		fFields.add(f);
		return f;
	}

	protected GroupTypeField createGroupTypeField(String label, int type, String defaultValue, String... labels) {
		GroupTypeField f = new GroupTypeField(type);
		f.setDialogFieldListener(this);
		f.setLabels(labels);
		f.setLabelText(label);
		if (defaultValue != null)
			fFieldAndDefault.put(f, defaultValue);

		fFields.add(f);
		return f;
	}

	protected StringDialogField createStringDialogField(IDialogFieldListener listener, String label,
			String defaultValue) {
		StringDialogField f = new StringDialogField();
		f.setDialogFieldListener(listener);
		f.setLabelText(label);
		if (defaultValue != null)
			fFieldAndDefault.put(f, defaultValue);

		fFields.add(f);
		return f;
	}

	protected void createSeparator() {
		Separator f = new Separator(SWT.SEPARATOR | SWT.HORIZONTAL);
		fFields.add(f);
	}

	// TODO
	private void createStringButtonDialogField(Composite composite, int nColumns, StringButtonDialogField field,
			boolean editable) {
		field.doFillIntoGrid(composite, nColumns);
		Text text = field.getTextControl(composite);
		text.setEditable(editable);
		LayoutUtil.setWidthHint(text, getMaxFieldWidth());
		LayoutUtil.setHorizontalGrabbing(text);
	}

	private void createStringDialogField(Composite composite, int nColumns, StringDialogField field) {
		field.doFillIntoGrid(composite, nColumns - 1);
		DialogField.createEmptySpace(composite);

		Text text = field.getTextControl(composite);
		LayoutUtil.setWidthHint(text, getMaxFieldWidth());
		TextFieldNavigationHandler.install(text);
		String value = fFieldAndDefault.get(field);
		if (value != null) {
			text.setText(value);
		}
	}

	private void createGroupTypeDialogField(Composite composite, int nColumns, GroupTypeField field, String label) {
		field.doFillIntoGrid(composite, nColumns);
		if (label != null) {
			field.setValue(label);
		}
	}

	private void createSeparator(Composite composite, int nColumns, Separator field) {
		field.doFillIntoGrid(composite, nColumns, convertHeightInCharsToPixels(1));
	}

	private int getMaxFieldWidth() {
		return convertWidthInCharsToPixels(63); // ÐÞ¸Ä¿í¶È
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

	private void chooseProject(StringButtonDialogField field) {
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
			fFieldAndResult.put(field, f);
			field.setText(f.getName());
		}
	}

	private List<IProject> filterProjects(IProject[] projects, String filter) {
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

	private void choosePackage(StringButtonDialogField packageField) {
		IPackageFragmentRoot root = SDTPlugin.getPackageFragmentRoot(fFieldAndParendField.get(packageField)
				.getText());
		IJavaElement[] packages = null;
		try {
			packages = root.getChildren();
		} catch (JavaModelException e) {
			JavaPlugin.log(e);
		}
		if (packages == null) {
			packages = new IPackageFragment[0];
		}

		Collection<IPackageFragment> c = new ArrayList<IPackageFragment>();
		for (IJavaElement je : packages) {
			IPackageFragment pf = (IPackageFragment) je;
			if (pf.isDefaultPackage())
				continue;
			c.add(pf);
		}

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(),
				new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT));
		dialog.setIgnoreCase(false);
		dialog.setTitle("choose a package");
		dialog.setMessage("choose a package");
		dialog.setEmptyListMessage("choose a package");
		dialog.setElements(c.toArray());
		dialog.setHelpAvailable(false);

		if (dialog.open() == Window.OK) {
			IPackageFragment f = (IPackageFragment) dialog.getFirstResult();
			fFieldAndResult.put(packageField, f);
			packageField.setText(f.getElementName());
		}
	}

	private void chooseFile(StringButtonDialogField fileField) {
		String root = fFieldAndRoot.get(fileField);
		IProject project = SDTPlugin.getProject(fFieldAndParendField.get(fileField).getText());
		IFolder folder = project.getFolder(root);
		if (!folder.exists())
			return;

		List<IResource> list = new ArrayList<IResource>();
		String filter = fFieldAndFilter.get(fileField);
		SDTPlugin.findResource(list, folder, filter);
		if (list.isEmpty())
			return;

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(),
				new ResourceItemLabelProvider(list));
		dialog.setIgnoreCase(false);
		dialog.setTitle("Select File");
		dialog.setMessage("Select File");
		dialog.setEmptyListMessage("Select File");
		dialog.setElements(list.toArray());
		dialog.setHelpAvailable(false);

		if (dialog.open() == Window.OK) {
			IResource f = (IResource) dialog.getFirstResult();
			fFieldAndResult.put(fileField, f);
			fileField.setText(f.getFullPath().makeRelativeTo(folder.getFullPath()).toString());
		}
	}

	protected void chooseOther(StringButtonDialogField field) {
	}

	protected void updateStatus() {
		IStatus f = getStatus();
		updateStatus(f);
	}

	protected IStatus getStatus(StringDialogField... fields) {
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
		return null;
	}

	abstract protected void refreshData();

	abstract protected IStatus getStatus();

}

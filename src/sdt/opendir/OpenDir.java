package sdt.opendir;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.WorkingSet;

@SuppressWarnings("restriction")
public class OpenDir implements IObjectActionDelegate {
	private Object selected = null;
	@SuppressWarnings("unchecked")
	private Class selectedClass = null;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		if (this.selected == null) {
			MessageDialog.openInformation(new Shell(), "Open DIr", "Unable to explore "
					+ this.selectedClass.getSimpleName());
			return;
		}
		File directory = null;
		if ((this.selected instanceof IResource)) {
			directory = new File(((IResource) this.selected).getLocation().toOSString());
		} else if ((this.selected instanceof File)) {
			directory = (File) this.selected;
		}
		if ((this.selected instanceof IFile)) {
			directory = directory.getParentFile();
		}
		if ((this.selected instanceof File)) {
			directory = directory.getParentFile();
		}

		try {
			Runtime.getRuntime().exec("explorer.exe " + directory.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		IAdaptable adaptable = null;
		this.selected = null;
		if ((selection instanceof IStructuredSelection)) {
			adaptable = (IAdaptable) ((IStructuredSelection) selection).getFirstElement();
			this.selectedClass = adaptable.getClass();
			if ((adaptable instanceof IResource)) {
				this.selected = ((IResource) adaptable);
			} else if (((adaptable instanceof PackageFragment))
					&& ((((PackageFragment) adaptable).getPackageFragmentRoot() instanceof JarPackageFragmentRoot))) {
				this.selected = getJarFile(((PackageFragment) adaptable).getPackageFragmentRoot());
			} else if ((adaptable instanceof JarPackageFragmentRoot)) {
				this.selected = getJarFile(adaptable);
			} else if ((adaptable instanceof WorkingSet)) {
				this.selected = getWorkingSetFile((WorkingSet) adaptable);
			} else {
				this.selected = ((IResource) adaptable.getAdapter(IResource.class));
			}
		}
	}

	protected File getJarFile(IAdaptable adaptable) {
		JarPackageFragmentRoot jpfr = (JarPackageFragmentRoot) adaptable;
		File selected = jpfr.getPath().makeAbsolute().toFile();
		if (!selected.exists()) {
			File projectFile = new File(jpfr.getJavaProject().getProject().getLocation().toOSString());
			selected = new File(projectFile.getParent() + selected.toString());
		}
		return selected;
	}

	private File getWorkingSetFile(WorkingSet workingSet) {
		IAdaptable[] as = workingSet.getElements();
		for (IAdaptable a : as) {
			Object o = a.getAdapter(IProject.class);
			if (o == null)
				continue;
			IProject p = (IProject) o;
			if (p.getName().contains("-assembly-")) {
				File projectFile = new File(p.getLocationURI());
				return projectFile.getParentFile();
			}
		}
		return null;
	}
}

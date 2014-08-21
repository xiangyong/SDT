package sdt.opendir;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.WorkingSet;

@SuppressWarnings("restriction")
public class OpenWorkingSet implements IObjectActionDelegate {
	private Object selected = null;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		if (this.selected == null) {
			MessageDialog.openInformation(new Shell(), "Open WorkingSet", "Unable to open");
			return;
		}

		try {
			Runtime.getRuntime().exec("explorer.exe " + this.selected.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		IAdaptable adaptable = null;
		this.selected = null;
		adaptable = (IAdaptable) ((IStructuredSelection) selection).getFirstElement();

		IAdaptable[] as = ((WorkingSet) adaptable).getElements();
		for (IAdaptable a : as) {
			Object o = a.getAdapter(IProject.class);
			if (o == null)
				continue;
			IProject p = (IProject) o;
			if (p.getName().contains("-assembly-")) {
				File projectFile = new File(p.getLocationURI());
				this.selected = projectFile.getParentFile();
				break;
			}
		}

	}

}

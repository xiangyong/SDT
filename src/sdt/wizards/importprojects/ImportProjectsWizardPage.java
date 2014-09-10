package sdt.wizards.importprojects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;

import sdt.natrue.SofaNature;

@SuppressWarnings("restriction")
public class ImportProjectsWizardPage extends WizardProjectsImportPage {

	public boolean createProjects() {
		boolean f = super.createProjects();

		// TODO
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();

		Object[] selected = super.getProjectsList().getCheckedElements();
		for (Object obj : selected) {
			ProjectRecord pr = (ProjectRecord) obj;
			IProject project = workspace.getRoot().getProject(pr.getProjectName());
			if (project.getName().contains("-test")||project.getName().contains("-assembly-"))
				continue;
			addSofaSignature(project);
		}

		return f;
	}

	private void addSofaSignature(IProject project) {
		IProjectDescription description = null;
		try {
			description = project.getDescription();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (description == null)
			return;

		String[] natures = description.getNatureIds();

		// if exits, return
		for (int i = 0; i < natures.length; ++i) {
			if (SofaNature.NATURE_ID.equals(natures[i])) {
				return;
			}
		}

		// if not exits, Add the nature
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = SofaNature.NATURE_ID;
		description.setNatureIds(newNatures);
		try {
			project.setDescription(description, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}
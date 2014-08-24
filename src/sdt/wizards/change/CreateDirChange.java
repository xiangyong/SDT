package sdt.wizards.change;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;

import sdt.SDTPlugin;

public class CreateDirChange extends Change {
	protected IFolder fFolder;

	public CreateDirChange(String name) {
		this.fFolder = SDTPlugin.getFolder(name);
	}

	@Override
	public Object getModifiedElement() {
		return fFolder;
	}

	@Override
	public String getName() {
		return fFolder.getName() + "/ - " + fFolder.getParent().getFullPath().makeRelative();
	}

	@Override
	public void initializeValidationData(IProgressMonitor pm) {
	}

	@Override
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		String p = fFolder.getFullPath().toString();
		String[] ss = p.split("/");
		String s = ss[1];
		for (int i = 2; i < ss.length; i++) {
			s += "/" + ss[i];
			IFolder f = SDTPlugin.getFolder('/' + s);
			if (!f.exists())
				f.create(true, true, pm);
		}

		return new DeleteResourceChange(fFolder.getFullPath(), true);
	}
}
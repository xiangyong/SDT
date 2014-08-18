package sdt.wizards.change;

import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;

import sdt.SDTPlugin;

public class CreateDirChange extends Change {
	protected IFolder folder;

	public CreateDirChange(String name) {
		this.folder = SDTPlugin.getFolder(name);
	}

	@Override
	public Object getModifiedElement() {
		return folder;
	}

	@Override
	public String getName() {
		return folder.getName() + "/ - " + folder.getParent().getFullPath().makeRelative();
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
		new File(folder.getLocationURI().getPath()).mkdirs();
		return new DeleteResourceChange(folder.getFullPath(), true);
	}
}
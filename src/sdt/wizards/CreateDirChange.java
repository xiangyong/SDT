package sdt.wizards;

import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;

public class CreateDirChange extends Change {
	private IFolder folder;

	public CreateDirChange(IFolder folder) {
		this.folder = folder;
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
		System.err.println(folder.getLocationURI().getRawPath());
		new File(folder.getLocationURI().getPath()).mkdirs();
		return new DeleteResourceChange(folder.getFullPath(), true);
	}
}
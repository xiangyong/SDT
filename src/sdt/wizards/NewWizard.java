package sdt.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewWizard extends Wizard implements INewWizard {

	protected NewPreviewWizardPage previewPage;

	protected IWorkbench workbench;
	protected IStructuredSelection selection;

	@Override
	public boolean performFinish() {
		final AtomicBoolean success = new AtomicBoolean();
		try {
			getContainer().run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					boolean ok = performFinish(monitor);
					success.set(ok);
				}
			});

			return success.get();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@SuppressWarnings("restriction")
	protected boolean performFinish(IProgressMonitor monitor) {

		if (previewPage.hasChanges()) {
			monitor.beginTask("Creating template...", 1);
			try {
				doBefore(monitor);
				previewPage.getChange().perform(monitor);
				doAfter(monitor);
			} catch (Exception e) {
				e.printStackTrace();
				doException(monitor);
				return false;
			} finally {
				monitor.done();
			}
		}

		return true;
	}

	protected void doBefore(IProgressMonitor monitor) {
	}

	protected void doAfter(IProgressMonitor monitor) {
	}

	protected void doException(IProgressMonitor monitor) {
	}

	protected void doFinally() {
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		this.workbench = workbench;
	}
}
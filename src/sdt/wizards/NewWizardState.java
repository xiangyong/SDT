package sdt.wizards;

import org.eclipse.ltk.core.refactoring.Change;

public interface NewWizardState {
	public Change[] computeChanges();
}

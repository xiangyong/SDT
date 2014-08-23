package sdt.wizards.newclient;

import org.eclipse.ltk.core.refactoring.Change;

import sdt.wizards.NewWizardState;

public class NewClientState implements NewWizardState {

	public String fProject;
	public String fPakcage;
	public String fFacade;

	@Override
	public Change[] computeChanges() {
		// TODO Auto-generated method stub
		return null;
	}

}

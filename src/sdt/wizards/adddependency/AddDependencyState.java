package sdt.wizards.adddependency;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ltk.core.refactoring.Change;

import sdt.wizards.NewWizardState;
import sdt.wizards.change.ChangeEngine;

public class AddDependencyState implements NewWizardState {

	public String fGroupId;
	public String fArtifactId;
	public String fVersion;
	public String fProject;

	@Override
	public Change[] computeChanges() {
		Map<String, Object> context = new HashMap<String, Object>();

		return ChangeEngine.run(context, "dependency");
	}

}

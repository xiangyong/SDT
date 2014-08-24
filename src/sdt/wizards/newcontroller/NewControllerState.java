package sdt.wizards.newcontroller;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ltk.core.refactoring.Change;

import sdt.NameUtil;
import sdt.wizards.NewWizardState;
import sdt.wizards.change.ChangeEngine;

public class NewControllerState implements NewWizardState {

	public String fFile;
	public String fPackage;
	public String fName;
	public String fClassName;

	@Override
	public Change[] computeChanges() {
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("packageName", fPackage);
		context.put("name", fName);
		context.put("fName", NameUtil.aaaBbbCcc(fName));
		context.put("className", fClassName);
		context.put("file", fFile);

		return ChangeEngine.run(context, "controller");
	}

}

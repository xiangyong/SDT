package sdt.wizards.newcontroller;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.app.Velocity;
import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;

import sdt.NameUtil;
import sdt.SDTPlugin;
import sdt.wizards.NewWizardState;

public class NewControllerState implements NewWizardState {

	public String fFile;
	public String fPackage;
	public String fName;
	public String fClassName;

	public NewControllerState() {
		Velocity.init();
	}

	@Override
	public Change[] computeChanges() {
		//		VelocityContext context = new VelocityContext();
		Map<String, String> context = new HashMap<String, String>();
		context.put("packageName", fPackage);
		context.put("name", fName);
		context.put("fName", NameUtil.aaaBbbCcc(fName));
		context.put("className", fClassName);

		int i = 0, l = 1;
		Change[] f = new Change[l];

		IFile file = SDTPlugin.getFile(this.fFile);
		String txt = SDTPlugin.getTpl(context, "tpl/controller/controller.vm");
		TextFileChange change = SDTPlugin.createNewFileChange(file, txt);
		f[i++] = change;

		return f;
	}

}

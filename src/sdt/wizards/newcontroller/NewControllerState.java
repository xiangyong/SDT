package sdt.wizards.newcontroller;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;

import sdt.SDTPlugin;
import sdt.wizards.NewWizardState;

public class NewControllerState implements NewWizardState {

	public String fFile;
	public String fPackage;
	public String fName;

	public NewControllerState() {
		Velocity.init();
	}

	@Override
	public Change[] computeChanges() {
		VelocityContext context = new VelocityContext();
		context.put("package", fPackage);
		context.put("name", fName);

		int i = 0, l = 1;
		Change[] f = new Change[l];

		IFile file = SDTPlugin.getTargetFile(this.fFile);
		String txt = SDTPlugin.getTpl(context, "tpl/controller/controller.vm");
		TextFileChange change = null;
		change = SDTPlugin.createNewFileChange(file, txt);
		f[i++] = change;

		return f;
	}

}

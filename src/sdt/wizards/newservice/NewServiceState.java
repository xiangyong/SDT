package sdt.wizards.newservice;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.app.Velocity;
import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;

import sdt.SDTPlugin;
import sdt.wizards.NewWizardState;

public class NewServiceState implements NewWizardState {

	public String serviceFile;
	public String servicePackage;
	public String serviceName;

	public String implFile;
	public String implPackage;
	public String implName;

	public String xmlFile;
	public boolean createXml;

	public int serviceType;

	public NewServiceState() {
		Velocity.init();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Change[] computeChanges() {
		Map context = new HashMap();
		context.put("servicePackage", servicePackage);
		context.put("serviceName", serviceName);
		context.put("implPackage", implPackage);
		context.put("implName", implName);
		context.put("createXml", createXml);
		context.put("serviceType", serviceType);
		StringBuffer buff = new StringBuffer(this.serviceName);
		buff.setCharAt(0, Character.toLowerCase(buff.charAt(0)));
		context.put("objectName", buff);

		int i = 0, l = 3;
		Change[] f = new Change[l];

		// xml
		{
			IFile file = SDTPlugin.getFile(this.xmlFile);
			String txt = SDTPlugin.getTpl(context, "tpl/service/xml.vm");
			TextFileChange change = null;
			if (this.createXml) {
				change = SDTPlugin.createNewFileChange(file, txt);
			} else {
				change = SDTPlugin.createReplaceEdit(file, txt);
			}
			f[i++] = change;
		}
		// service
		{
			IFile file = SDTPlugin.getFile(this.serviceFile);
			String txt = SDTPlugin.getTpl(context, "tpl/service/service.vm");
			TextFileChange change = SDTPlugin.createNewFileChange(file, txt);
			f[i++] = change;
		}
		// impl
		{
			IFile file = SDTPlugin.getFile(this.implFile);
			String txt = SDTPlugin.getTpl(context, "tpl/service/impl.vm");
			TextFileChange change = SDTPlugin.createNewFileChange(file, txt);
			f[i++] = change;
		}
		return f;
	}

}

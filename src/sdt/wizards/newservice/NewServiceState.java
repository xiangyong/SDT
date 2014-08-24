package sdt.wizards.newservice;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ltk.core.refactoring.Change;

import sdt.NameUtil;
import sdt.wizards.NewWizardState;
import sdt.wizards.change.ChangeEngine;

public class NewServiceState implements NewWizardState {

	public String fServiceFile;
	public String fServicePackage;
	public String fServiceName;

	public String fImplFile;
	public String fImplPackage;
	public String fImplName;

	public String fXmlFile;
	public boolean fCreateXml;

	public int fServiceType;

	@SuppressWarnings("unchecked")
	@Override
	public Change[] computeChanges() {
		Map context = new HashMap();
		context.put("serviceFile", fServiceFile);
		context.put("servicePackage", fServicePackage);
		context.put("serviceName", fServiceName);
		context.put("objectName", NameUtil.aaaBbbCcc(fServiceName));

		context.put("implFile", fImplFile);
		context.put("implPackage", fImplPackage);
		context.put("implName", fImplName);

		context.put("xmlFile", fXmlFile);
		context.put("createXml", fCreateXml);
		context.put("X", fCreateXml ? "F" : "M");

		context.put("serviceType", fServiceType);

		return ChangeEngine.run(context, "service");
	}

}

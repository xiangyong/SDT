package sdt.wizards.newclient;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.ltk.core.refactoring.Change;

import sdt.NameUtil;
import sdt.SDTPlugin;
import sdt.wizards.NewWizardState;
import sdt.wizards.change.ChangeEngine;

public class NewClientState implements NewWizardState {

	public String fProject;
	public IJavaElement fFacade;
	public String fPackage;
	public String fName;
	public String fXml;
	public int fType;

	@Override
	public Change[] computeChanges() {
		// NewTypeWizardPage: createInheritedMethods: 2482
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("system", NameUtil.firstString(fProject, '-'));
		context.put("project", fProject);
		context.put("packageDir", fPackage.replace('.', '/'));
		context.put("clientPackage", fPackage);
		context.put("client", fName);
		context.put("clientObjectName", NameUtil.aaaBbbCcc(fName));
		context.put("impl", fName + "Impl");
		context.put("implPackage", fPackage + ".impl");
		context.put("xml", fXml);

		boolean createXml = !SDTPlugin.getProject(fProject).getFile(SDTPlugin.D_SPRING + "/" + fXml).exists();
		context.put("createXml", createXml);
		context.put("X", createXml ? "F" : "M");
		context.put("type", fType);

		try {
			context.put("facade", getFacade());
		} catch (Exception e) {
			e.printStackTrace();
		}
		context.put("vip", getVip());

		return ChangeEngine.run(context, "client");
	}

	private Facade getFacade() {
		Facade f = new Facade();
		f.name = fFacade.getElementName();
		f.objectName = NameUtil.aaaBbbCcc(f.name);

		try {
			String type;
			IType facade = (IType) fFacade;

			// fullName
			f.fullName = facade.getFullyQualifiedName();

			// imports
			IImportDeclaration[] facadeImports = facade.getCompilationUnit().getImports();
			f.imports = new String[facadeImports.length];
			for (int i = 0; i < facadeImports.length; i++) {
				IImportDeclaration id = facadeImports[i];
				f.imports[i] = id.getElementName();
			}

			// methods
			IMethod[] imds = facade.getMethods();
			f.methods = new Method[imds.length];
			for (int j = 0; j < imds.length; j++) {
				IMethod im = imds[j];
				Method m = new Method();
				f.methods[j] = m;
				m.name = im.getElementName();

				// rt
				type = im.getReturnType();
				m.rt = Signature.getSignatureSimpleName(type);

				String[] pns = im.getParameterNames();
				String[] pts = im.getParameterTypes();
				for (int i = 0; i < im.getNumberOfParameters(); i++) {
					if (i != 0) {
						m.pts += ',';
						m.pns += ',';
					}
					m.pns += pns[i];

					type = pts[i];
					m.pts += Signature.getSignatureSimpleName(type) + " " + pns[i];
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		return f;
	}

	public static class Facade {
		public String name;
		public String objectName;
		public String fullName;

		public String[] imports;
		public Method[] methods;
	}

	public static class Method {
		public String name;
		public String rt;
		public String pts = "";
		public String pns = "";
	}

	@SuppressWarnings("unused")
	private void addImports(Set<String> imports, String s) {
		int t = Signature.getTypeSignatureKind(s);
		if (t != Signature.CLASS_TYPE_SIGNATURE)
			return;

		String f = Signature.getSignatureQualifier(s);
		if (f.equals("java.lang"))
			return;

		imports.add(Signature.toString(s));
	}

	private String getVip() {
		try {
			String f = NameUtil.firstString(fFacade.getParent().getParent().getParent().getElementName(), '-');
			switch (fType) {
			case 1:
				return "${" + f + "_service_url}";
			case 2:
				return "${" + f + "_tr_service_url}";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}

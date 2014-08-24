package sdt.wizards.newclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;

import sdt.NameUtil;
import sdt.SDTPlugin;
import sdt.wizards.NewWizardState;
import sdt.wizards.change.CreateDirChange;
import sdt.wizards.change.CreatePackageChange;

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
		context.put("impl", fName + "Ipml");
		context.put("implPackage", fPackage + ".ipml");
		context.put("xml", fXml);

		boolean createXml = !SDTPlugin.getProject(fProject).getFile(SDTPlugin.D_SPRING + "/" + fXml).exists();
		context.put("createXml", createXml);
		context.put("X", createXml ? "F" : "M");
		context.put("type", fType);

		context.put("facade", getFacade());
		context.put("vip", getVip());

		Properties ps = new Properties();
		try {
			String dalConf = SDTPlugin.getTpl(context, "tpl/client/confx.vm");
			ps.load(new ByteArrayInputStream(dalConf.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Collection<Change> f = new ArrayList<Change>();
		for (Map.Entry<?, ?> entry : ps.entrySet()) {
			String key = entry.getKey().toString();
			List<String> args = f(entry.getValue().toString());

			char changeAction = args.get(0).charAt(0);
			IFile file;
			String text;
			Change change;
			switch (changeAction) {
			case 'F':
				file = SDTPlugin.getFile(args.get(1));
				text = SDTPlugin.getTpl(context, "tpl/client/" + key + ".vm");
				change = SDTPlugin.createNewFileChange(file, text);
				f.add(change);
				break;
			case 'M':
				file = SDTPlugin.getFile(args.get(1));
				text = SDTPlugin.getTpl(context, "tpl/client/" + key + ".vm");
				change = SDTPlugin.createReplaceEdit(file, text, args.subList(2, args.size())
						.toArray(new String[0]));
				f.add(change);
				break;
			case 'D':
				change = new CreateDirChange(args.get(1));
				f.add(change);
				break;
			case 'P':
				change = new CreatePackageChange(args.get(1), args.get(2));
				f.add(change);
				break;
			}

		}

		return f.toArray(new Change[0]);
	}

	private List<String> f(String line) {
		List<String> c = new ArrayList<String>();
		for (String s : line.split(" ")) {
			String w = s.trim();
			if (w.isEmpty())
				continue;
			c.add(w);
		}
		return c;
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
			Set<String> imports = new TreeSet<String>();
			imports.add(f.fullName);

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
				addImport(imports, type);

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
					addImport(imports, type);
				}
			}
			f.imports = imports.toArray(new String[0]);
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

	private void addImport(Set<String> imports, String s) {
		int t = Signature.getTypeSignatureKind(s);
		if (t != Signature.CLASS_TYPE_SIGNATURE)
			return;

		String f = Signature.getSignatureQualifier(s);
		if (f.equals("java.lang"))
			return;

		imports.add(Signature.toString(s));
	}

	private String getVip() {
		String f = NameUtil.firstString(fFacade.getParent().getParent().getParent().getElementName(), '-');
		switch (fType) {
		case 1:
			return "${" + f + "_service_url}";
		case 2:
			return "${" + f + "_tr_service_url}";
		}
		return "";
	}

}

package sdt.wizards.newclient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.ltk.core.refactoring.Change;

import sdt.NameUtil;
import sdt.SDTPlugin;
import sdt.core._;
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

		IType facade = (IType) fFacade;

		// fullName
		f.fullName = facade.getFullyQualifiedName();
		ICompilationUnit icu = facade.getCompilationUnit();
		if (icu == null) {
			String srcJar = facade.getPath().toString().replaceAll(".jar", "-sources.jar");
			String src = facade.getPackageFragment().getElementName().replace('.', '/') + '/'
					+ facade.getElementName() + ".java";
			String content = _.readFromJar(srcJar + "!/" + src);

			ASTParser astParser = ASTParser.newParser(AST.JLS3);
			astParser.setSource(content.toCharArray());
			astParser.setKind(ASTParser.K_COMPILATION_UNIT);
			CompilationUnit cu = (CompilationUnit) (astParser.createAST(null));
			SrcVisitor d = new SrcVisitor();
			cu.accept(d);
			f.imports = d.fImports.toArray(new String[0]);
			f.methods = d.fMethods.toArray(new Method[0]);
		}

		return f;
	}

	static class SrcVisitor extends ASTVisitor {
		public Set<String> fImports = new HashSet<String>();
		public Set<Method> fMethods = new HashSet<Method>();

		@Override
		public boolean visit(ImportDeclaration node) {
			fImports.add(node.getName().getFullyQualifiedName());
			return true;
		}

		static class SimpleMethod {
			public String name;
			public String returnType;
			public String parameters;

			public String toString() {
				return returnType + " " + name + " (" + parameters + ")";
			}
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			Method method = new Method();
			method.name = node.getName().toString();
			method.rt = node.getReturnType2().toString();
			StringBuffer pts = new StringBuffer();
			StringBuffer pns = new StringBuffer();
			for (int i = 0; i < node.parameters().size(); i++) {
				SingleVariableDeclaration var = (SingleVariableDeclaration) node.parameters().get(i);
				if (i != 0) {
					pts.append(',');
					pns.append(',');
				}
				pts.append(var);
				pns.append(var.getName());
			}
			method.pts = pts.toString();
			method.pns = pns.toString();
			fMethods.add(method);

			return true;
		}
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

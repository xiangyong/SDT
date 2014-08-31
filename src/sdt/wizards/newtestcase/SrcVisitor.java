package sdt.wizards.newtestcase;

import java.util.Collection;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import sdt.wizards.newtestcase.Code.Method;
import sdt.wizards.newtestcase.Code.Parameter;

public class SrcVisitor extends ASTVisitor {

	public boolean visit(PackageDeclaration node) {
		code.pkg = node.getName().getFullyQualifiedName();
		return true;
	}

	// 取当前方法的所有入参
	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(MethodDeclaration node) {
		Method m = new Method();
		code.methods.add(m);

		// visibility
		m.visibility = Visibility.get(node.getModifiers());

		// method name
		m.name = node.getName().getFullyQualifiedName();

		// return type
		m.returnType = new Parameter();
		m.returnType.javaType = node.getReturnType2();
		m.returnType.type = node.getReturnType2().getNodeType();

		// parameters
		Collection<SingleVariableDeclaration> parameters = node.parameters();
		for (SingleVariableDeclaration parameter : parameters) {
			Parameter p = new Parameter();
			p.name = parameter.getName().getFullyQualifiedName();
			p.type = parameter.getNodeType();
			p.javaType = parameter.getType();
			p.isDynamicArgs = parameter.toString().contains("...");
			m.parameters.add(p);
		}

		//System.err.println(m);
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if (node.isPackageMemberTypeDeclaration()) {
			code.name = node.getName().getFullyQualifiedName();
		}
		return true;
	}

	public Code code = new Code();
}

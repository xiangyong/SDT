package sdt.wizards.newtestcase;

import java.util.Collection;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import sdt.wizards.newtestcase.Code.Method;
import sdt.wizards.newtestcase.Code.Parameter;

public class SrcVisitor extends ASTVisitor {
	// visit if statements
	public boolean visit(IfStatement node) {
		System.err.println("=======================================================");
		System.err.println("case:" + node.getExpression());
		getMockExpression(node.getExpression());
		return true;
	}

	// get the mock expression
	public void getMockExpression(ASTNode node) {
		// System.err.println("node:" + node);
		// System.err.println("node:" + node.getClass());
		String f = null;
		String e = null;
		switch (node.getNodeType()) {
		case ASTNode.SIMPLE_NAME:
			SimpleName sn = (SimpleName) node;
			System.err.println("\tmock:" + sn.getFullyQualifiedName());
			f = sn.getFullyQualifiedName();
			e = " == true";
			break;
		case ASTNode.QUALIFIED_NAME:
			QualifiedName qn = (QualifiedName) node;
			System.err.println("\tmock:" + qn.getFullyQualifiedName());
			f = qn.getFullyQualifiedName();
			e = " == true";
			break;
		case ASTNode.METHOD_INVOCATION:
			MethodInvocation mi = (MethodInvocation) node;
			System.err.println("\tmock:" + mi.toString());
			f = mi.toString();
			break;
		case ASTNode.FIELD_ACCESS:
			FieldAccess fa = (FieldAccess) node;
			System.err.println("\tmock:" + fa);
			f = fa.getName().getFullyQualifiedName();
			e = " == true";
			break;
		case ASTNode.PREFIX_EXPRESSION:
			PrefixExpression pre = (PrefixExpression) node;
			getMockExpression(pre.getOperand());
			break;
		case ASTNode.PARENTHESIZED_EXPRESSION:
			ParenthesizedExpression pe = (ParenthesizedExpression) node;
			getMockExpression(pe.getExpression());
			break;
		case ASTNode.INFIX_EXPRESSION:
			InfixExpression ie = (InfixExpression) node;
			if (Operator.CONDITIONAL_AND.equals(ie.getOperator())
					|| Operator.CONDITIONAL_OR.equals(ie.getOperator())) {
				getMockExpression(ie.getLeftOperand());
				getMockExpression(ie.getRightOperand());
			} else {
				System.err.println("\tmock:" + ie.getLeftOperand() + " " + ie.getOperator() + " "
						+ ie.getRightOperand());
				f = ie.getLeftOperand().toString();
				System.err.println(ie.getLeftOperand().getClass());
				e = " " + ie.getOperator() + " " + ie.getRightOperand();
			}
			break;
		}

		f(f, e);
	}

	public void f(String name, String expression) {
		if (name == null || !name.contains("."))
			return;

		String f = name.substring(0, name.indexOf("."));
		System.err.println("\t\tMOCKV:" + f);
	}

	// int i = 1;
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		System.err.println("=======================================================");
		System.err.println("VariableDeclarationStatement:" + node);
		System.err.println("VariableDeclarationStatement:" + node.getClass());
		System.err.println("VariableDeclarationStatement:" + node.fragments());
		return true;
	}

	// i = 1;
	@Override
	public boolean visit(Assignment node) {
		System.err.println("=======================================================");
		System.err.println("Assignment:" + node);
		return true;
	}

	// visit method declaration
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

	@Override
	public boolean visit(PackageDeclaration node) {
		code.pkg = node.getName().getFullyQualifiedName();
		return true;
	}

	public Code code = new Code();
}

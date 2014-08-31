package sdt.wizards.newtestcase;

import java.util.Collection;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
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
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import sdt.wizards.newtestcase.Code.Method;
import sdt.wizards.newtestcase.Code.Parameter;

public class SrcVisitor extends ASTVisitor {
	// visit if statements
	public boolean visit(IfStatement node) {
		System.err.println("=======================================================");
		System.err.println("case:" + node.getExpression());
		f(node.getExpression());
		return false;
	}

	// get the mock expression
	public void f(ASTNode node) {
		// System.err.println("node:" + node);
		// System.err.println("node:" + node.getClass());
		switch (node.getNodeType()) {
		case ASTNode.SIMPLE_NAME:
			SimpleName sn = (SimpleName) node;
			System.err.println("\tmock:" + sn.getFullyQualifiedName());
			break;
		case ASTNode.QUALIFIED_NAME:
			QualifiedName qn = (QualifiedName) node;
			System.err.println("\tmock:" + qn.getFullyQualifiedName());
			break;
		case ASTNode.METHOD_INVOCATION:
			MethodInvocation mi = (MethodInvocation) node;
			System.err.println("\tmock:" + mi.toString());
			break;
		case ASTNode.FIELD_ACCESS:
			FieldAccess fa = (FieldAccess) node;
			System.err.println("\tmock:" + fa);
			break;
		case ASTNode.PREFIX_EXPRESSION:
			PrefixExpression pre = (PrefixExpression) node;
			f(pre.getOperand());
			break;
		case ASTNode.PARENTHESIZED_EXPRESSION:
			ParenthesizedExpression pe = (ParenthesizedExpression) node;
			f(pe.getExpression());
			break;
		case ASTNode.INFIX_EXPRESSION:
			InfixExpression ie = (InfixExpression) node;
			if (Operator.CONDITIONAL_AND.equals(ie.getOperator())
					|| Operator.CONDITIONAL_OR.equals(ie.getOperator())) {
				f(ie.getLeftOperand());
				f(ie.getRightOperand());
			} else {
				System.err.println("\tmock:" + ie.getLeftOperand() + " " + ie.getOperator() + " "
						+ ie.getRightOperand());
			}
			break;
		}
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

	@Override
	public boolean visit(PackageDeclaration node) {
		code.pkg = node.getName().getFullyQualifiedName();
		return true;
	}

	public Code code = new Code();
}

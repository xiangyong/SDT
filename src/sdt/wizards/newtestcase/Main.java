package sdt.wizards.newtestcase;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import sdt.core._;

public class Main {

	public static void main(String[] args) {
		ASTParser astParser = ASTParser.newParser(AST.JLS3);
		astParser.setSource(_.readFromFile(new File("src/com/alipay/test/A.java")).toCharArray());
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		CompilationUnit cu = (CompilationUnit) (astParser.createAST(new NullProgressMonitor()));
		SrcVisitor sv = new SrcVisitor();
		cu.accept(sv);
		Generator g = new Generator();
		g.code = sv.code;
		g.run();
	}

}

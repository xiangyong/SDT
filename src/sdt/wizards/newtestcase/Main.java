package sdt.wizards.newtestcase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import sdt.core._;

public class Main {

	public static void main(String[] args) {
		ASTParser astParser = ASTParser.newParser(AST.JLS3);
		astParser.setSource(_.readFromFile(new File("D:/w/3_rcp/TestProject/src/com/alipay/test/A.java"))
				.toCharArray());
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		CompilationUnit cu = (CompilationUnit) (astParser.createAST(new NullProgressMonitor()));

		SrcVisitor sv = new SrcVisitor();
		cu.accept(sv);

		// generate test case
		String txt = _.readFromFile(new File("./tpl/testcase/testcase.vm"));
		Map<String, Object> ctx = new HashMap<String, Object>();
		ctx.put("code", sv.code);
		String s = _.f(txt, ctx);
		_.writeToFile(new File("D:/w/3_rcp/TestProject/src/com/alipay/test/A_Test.java"), s);
	}

}

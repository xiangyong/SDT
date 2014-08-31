package sdt.wizards.newtestcase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import sdt.core._;

public class Generator {
	public Code code;

	public void run() {
		if (code == null)
			return;
		String txt = _.readFromFile(new File("./tpl/testcase.vm"));
		Map<String, Object> ctx = new HashMap<String, Object>();
		ctx.put("code", code);
		String s = _.f(txt, ctx);
		System.err.println(s);
		_.writeToFile(new File("src/com/alipay/test/A_Test.java"), s);
	}
}

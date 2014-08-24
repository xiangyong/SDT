package sdt.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class _ {

	public static String f(String txt, Map<String, ?> ctx) {
		String[] ss = txt.split("\n");
		StringBuffer js = new StringBuffer("_='';");
		for (String s : ss) {
			s = s.replaceAll("\\'", "\\\\\\'");
			if (s.startsWith("#")) {
				js.append(s.substring(1) + "\n");
			} else {
				s += "\\n";
				js.append("_+=\'");
				js.append(var(s));
				js.append("';");
				js.append("\n");
			}
		}
		js.append(";_");

		ScriptEngineManager sem = new ScriptEngineManager();
		ScriptEngine se = sem.getEngineByName("js");
		try {
			SimpleBindings bindings = new SimpleBindings();
			bindings.putAll(ctx);
			se.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
			return se.eval(js.toString()).toString();
		} catch (ScriptException e) {
			System.err.println(js.toString());
			e.printStackTrace();
		}

		return "";
	}

	private static String var(String s) {
		if (!s.contains("{")) {
			return s;
		}
		StringBuffer f = new StringBuffer(s);
		int start = -1;
		int l = s.length();
		for (int i = 0; i < l; i++) {
			char c = f.charAt(i);

			if (c == '{') {
				int j = i + 1;
				int k = i - 1;
				if (j < l && Character.isJavaIdentifierStart(f.charAt(j)) && (k >= 0 && f.charAt(k) != '$')) {
					start = i;
				}
			}

			if (start > -1 && c == '}') {
				int j = start + 1;
				String k = f.substring(j, i);

				f.setCharAt(start, '\'');
				f.setCharAt(i, '\'');

				f.delete(j, i);
				String v = ";_+=" + k + ";_+=";
				f.insert(j, v);
				i += j - i + v.length();

				l = f.length();
				start = -1;
			}
		}
		return f.toString();
	}

	public static String readFromFile(File file) {
		StringBuffer f = new StringBuffer();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				f.append(line).append("\n");
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
					br = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return f.toString();
	}

	public static void writeToFile(File file, String content) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			fw.write(content);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fw != null) {
					fw.close();
					fw = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}

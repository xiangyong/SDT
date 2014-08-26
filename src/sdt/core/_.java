package sdt.core;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class _ {

	// TODO Template Engine
	public static String f(String txt, Map<String, ?> ctx) {
		String[] ss = txt.split("\n");
		StringBuffer js = new StringBuffer("_='';");
		for (String s : ss) {
			s = s.replaceAll("\\'", "\\\\\\'");
			if (s.startsWith("#")) {
				js.append(s.substring(1) + "\n");
			} else {
				if (s.endsWith("\\")) {
					s = s.substring(0, s.length() - 1);
				} else {
					s += "\\n";
				}
				js.append("_+=\'");
				js.append(var(s));
				js.append("';\n");
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
				if ((j < l && Character.isJavaIdentifierStart(f.charAt(j))) //
						&& (k >= 0 && f.charAt(k) != '$')//
						|| i == 0) {
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

	// TODO Template Engine ~~

	// TODO Read From File
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

	// TODO Write To File
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

	// TODO Read From Jar
	public static String readFromJar(String file) { // ***.jar!/***.java
		StringBuffer f = new StringBuffer();
		try {
			URL url = new URL("jar:file:" + file);
			JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
			InputStream in = jarConnection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				f.append(line).append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return f.toString();
	}

	// Translate English To Chinese
	public static String[] en2cn(String... words) {
		InputStream is = null;
		BufferedInputStream bis = null;

		int wordCount = words.length;
		StringBuffer query = new StringBuffer();
		for (int i = 0; i < wordCount; i++) {
			if (i != 0)
				query.append("%0A");
			query.append(words[i]);
		}
		try {
			URL fUrl = new URL("http://fanyi.baidu.com/v2transapi?from=en&to=zh&query=" + query);

			URLConnection connection = fUrl.openConnection();
			is = connection.getInputStream();
			int len = 2048;
			bis = new BufferedInputStream(is, len);
			byte[] bs = new byte[len];
			bis.read(bs);
			String s = new String(bs);

			String key = "\"dst\":\"";
			String[] f = new String[wordCount];
			int i = 0, start = 0, end = 0;
			while ((start = s.indexOf(key, end)) > 0) {
				int wordStart = start + key.length();
				end = s.indexOf('"', wordStart);
				f[i++] = decode(s.substring(wordStart, end));
			}

			return f;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
				if (bis != null) {
					bis.close();
					bis = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return new String[0];
	}

	public static String decode(String unicode) {
		if (unicode == null) {
			return null;
		}
		StringBuffer f = new StringBuffer();
		int l = unicode.length();
		for (int i = 0; i < l; i++) {
			if (unicode.charAt(i) == '\\') {
				if ((i < l - 5) && ((unicode.charAt(i + 1) == 'u') || (unicode.charAt(i + 1) == 'U')))
					try {
						f.append((char) Integer.parseInt(unicode.substring(i + 2, i + 6), 16));
						i += 5;
					} catch (NumberFormatException localNumberFormatException) {
						f.append(unicode.charAt(i));
					}
				else
					f.append(unicode.charAt(i));
			} else {
				f.append(unicode.charAt(i));
			}
		}
		return f.toString();
	}

}

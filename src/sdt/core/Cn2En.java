package sdt.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class Cn2En {
	public static String run(String word) {
		String f = null;
		InputStream in = null;
		BufferedInputStream bis = null;
		try {
			URL fUrl = new URL("http://fanyi.baidu.com/v2transapi?from=en&to=zh&query=" + word);
			URLConnection connection = fUrl.openConnection();
			in = connection.getInputStream();
			int len = 256;
			bis = new BufferedInputStream(in, len);
			byte[] bs = new byte[len];
			bis.read(bs);
			String s = new String(bs);
			int p = s.indexOf('"', 90);
			f = decode(s.substring(90, p));
			bis.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
					in = null;
				}
				if (bis != null) {
					bis.close();
					bis = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return f;
	}

	public static String decode(String unicodeStr) {
		if (unicodeStr == null) {
			return null;
		}
		StringBuffer retBuf = new StringBuffer();
		int maxLoop = unicodeStr.length();
		for (int i = 0; i < maxLoop; i++) {
			if (unicodeStr.charAt(i) == '\\') {
				if ((i < maxLoop - 5) && ((unicodeStr.charAt(i + 1) == 'u') || (unicodeStr.charAt(i + 1) == 'U')))
					try {
						retBuf.append((char) Integer.parseInt(unicodeStr.substring(i + 2, i + 6), 16));
						i += 5;
					} catch (NumberFormatException localNumberFormatException) {
						retBuf.append(unicodeStr.charAt(i));
					}
				else
					retBuf.append(unicodeStr.charAt(i));
			} else {
				retBuf.append(unicodeStr.charAt(i));
			}
		}
		return retBuf.toString();
	}

}

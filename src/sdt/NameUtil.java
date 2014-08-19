package sdt;

public final class NameUtil {

	public static String trim(String AaA_bBb) {
		if (AaA_bBb == null) {
			return null;
		}
		return AaA_bBb.trim();
	}

	public static String AAA_BBB(String AaA_bBb) {
		if (AaA_bBb == null) {
			return null;
		}
		return AaA_bBb.trim().toUpperCase();
	}

	public static String aaa_bbb(String AaA_bBb) {
		if (AaA_bBb == null) {
			return null;
		}
		return AaA_bBb.trim().toLowerCase();
	}

	public static String AaaBbb(String AaA_bBb) {
		StringBuffer buff = new StringBuffer(AaA_bBb.trim().toLowerCase());

		boolean isUnderscore = false;
		for (int i = 0; i < buff.length(); i++) {

			while (buff.charAt(i) == '_') {
				isUnderscore = true;
				buff.delete(i, i + 1);
			}

			if (i == 0 || isUnderscore) {
				buff.setCharAt(i, (char) (buff.charAt(i) - 32));
				isUnderscore = false;
			}
		}

		return buff.toString();
	}

	public static String aaaBbb(String AaA_bBb) {
		StringBuffer buff = new StringBuffer(AaA_bBb.trim().toLowerCase());

		boolean isUnderscore = false;
		for (int i = 0; i < buff.length(); i++) {

			while (buff.charAt(i) == '_') {
				isUnderscore = true;
				buff.delete(i, i + 1);
			}

			if (isUnderscore) {
				buff.setCharAt(i, (char) (buff.charAt(i) - 32));
				isUnderscore = false;
			}
		}

		return buff.toString();
	}

	public static String aaaBbbCcc(String s) {
		char c = s.charAt(0);
		char cl = Character.toLowerCase(c);
		return s.replaceFirst(String.valueOf(c), String.valueOf(cl));
	}

	/**
	 * "biz" -> "Biz"<br>
	 * "biz service impl" -> "Biz Service Impl"
	 */
	public static String cap(String s) {
		StringBuffer f = new StringBuffer(s.toLowerCase());
		for (int i = 0; i < f.length(); i++) {
			if (i == 0 || f.charAt(i - 1) == ' ') {
				f.setCharAt(i, (char) (f.charAt(i) - 32));
			}
		}
		return f.toString();
	}

	public static String lastString(String s, char c) {
		return s.substring(s.lastIndexOf(c) + 1);
	}

	public static String firstString(String s, char c) {
		return s.substring(0, s.indexOf(c));
	}

	public static String renameFile(String... param) {
		return param[0].replaceAll(param[1], param[2]).replaceAll(param[3], param[4]);
	}

	public static void main(String[] args) {
		System.out.println(lastString("java.sql.Date", '.'));
		System.out.println(AaaBbb("TABLE_NAME"));
		System.out.println(renameFile("${ClassName}DO.java", "UserInfoDetail"));
	}
}

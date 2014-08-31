package sdt.wizards.newtestcase;

import org.eclipse.jdt.core.dom.Modifier;

public class Visibility {
	public static String get(int modifiers) {
		if (Modifier.isPublic(modifiers))
			return "public";
		else if (Modifier.isProtected(modifiers))
			return "protected";
		else if (Modifier.isPrivate(modifiers))
			return "private";
		else
			return "";
	}
}

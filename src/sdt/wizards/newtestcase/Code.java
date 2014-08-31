package sdt.wizards.newtestcase;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Type;

public class Code {
	public String pkg;
	public String name;
	public List<Method> methods = new ArrayList<Method>(0);

	public static class Method {
		public String visibility;
		public String name;
		public Parameter returnType;
		public List<Parameter> parameters = new ArrayList<Parameter>(0);

		public String toString() {
			StringBuffer f = new StringBuffer();
			if (visibility != null && !visibility.isEmpty()) {
				f.append(visibility);
				f.append(" ");
			}
			f.append(returnType.javaType);
			f.append(" ");
			f.append(name);
			f.append(" (");
			for (int i = 0; i < parameters.size(); i++) {
				if (i != 0)
					f.append(", ");
				f.append(parameters.get(i).javaType);
				f.append(" ");
				f.append(parameters.get(i).name);
			}
			f.append(");");
			return f.toString();
		}
	}

	public static class Parameter {
		public String name;
		public Type javaType;
		public int type;
		public boolean isDynamicArgs;

		public String getJavaType() {
			if (javaType.isArrayType())
				return javaType.toString().replaceAll("\\[\\]", "");
			if (javaType.isParameterizedType())
				return javaType.toString().replaceAll("<.*>", "");
			return javaType.toString();
		}

		public String getDefaultCase() {
			StringBuffer f = new StringBuffer();
			f.append(javaType);
			if (isDynamicArgs)
				f.append("[]");
			f.append(" ");
			f.append(name);
			f.append(" = ");

			String typeName = getJavaType();
			if (isDynamicArgs) {
				f.append("new ");
				f.append(javaType);
				f.append("[]{};");
			} else if (javaType.isArrayType()) {
				f.append("new ");
				f.append(javaType);
				f.append("{};");
			} else if (javaType.isPrimitiveType()) {
				f.append(typeName.equals("boolean") ? true : 0);
				f.append(";");
			} else {
				if (typeName.equals("String")) {
					f.append("\"\";");
				} else {
					f.append("mock(");
					f.append(javaType);
					f.append(".class);");
				}

			}
			return f.toString();
		}
	}

	public String getObjectName() {
		return name.replaceFirst(name.charAt(0) + "", Character.toLowerCase(name.charAt(0)) + "");
	}

}

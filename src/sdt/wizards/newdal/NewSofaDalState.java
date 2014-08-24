package sdt.wizards.newdal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.ltk.core.refactoring.Change;

import sdt.NameUtil;
import sdt.wizards.NewWizardState;
import sdt.wizards.change.ChangeEngine;

public class NewSofaDalState implements NewWizardState {

	public Table fTable;
	public String fProject;
	public String fPackage;

	public Change[] computeChanges() {

		Map<String, Object> context = new HashMap<String, Object>();
		context.put("projectName", fProject);
		context.put("systemName", NameUtil.firstString(fProject, '-'));

		context.put("packageRoot", fPackage);
		context.put("packageRootDir", fPackage.replace('.', '/'));

		context.put("table", fTable);

		return ChangeEngine.run(context, "dal");
	}

	public static class Table {
		public String name;
		public String schema;
		public Column[] columns;

		public String toString() {
			return name + "@" + schema;
		}

		public String toStr() {
			return schema + "." + name;
		}

		public String getClassName() {
			return NameUtil.AaaBbb(this.name);
		}

		public String getObjectName() {
			return NameUtil.aaaBbb(this.name);
		}

		public String getTableName() {
			return NameUtil.aaa_bbb(this.name);
		}

		public String[] getImports() {
			Set<String> f = new TreeSet<String>();
			for (Column column : this.columns) {
				String javaType = column.getJavaType();
				if (javaType.equals("java.lang.String") || javaType.equals("boolean") || javaType.equals("int")
						|| javaType.equals("long") || javaType.equals("float") || javaType.equals("double"))
					continue;
				f.add(javaType);
			}
			return f.toArray(new String[0]);
		}

		public static class Column {
			public String name;
			public String type;
			public boolean pk;
			public boolean ai;

			public String getJavaName() {
				return NameUtil.aaaBbb(this.name);
			}

			public String getDbName() {
				return NameUtil.aaa_bbb(this.name);
			}

			public String getGetMethod() {
				return "get" + NameUtil.AaaBbb(this.name);
			}

			public String getSetMethod() {
				return "set" + NameUtil.AaaBbb(this.name);
			}

			public String getDbType() {
				return type.toUpperCase();
			}

			public String getJavaTypeShort() {
				String javaType = getJavaType();
				return javaType.substring(javaType.lastIndexOf(".") + 1);
			}

			public String getJavaType() {
				String type = getDbType();
				if (type.equals("VARCHAR") || type.equals("CHAR") || getDbType().equals("TEXT")) {
					return "java.lang.String";
				} else if (type.equals("BLOB")) {
					return "java.lang.byte[]";
				} else if (type.equals("INT")) {
					// return "java.lang.Long";
					return "long";
				} else if (type.equals("TINYINT") || type.equals("SMALLINT") || type.equals("MEDIUMINT")
						|| type.equals("BOOLEAN")) {
					// return "java.lang.Integer";
					return "int";
				} else if (type.equals("BIT")) {
					// return "java.lang.Boolean";
					return "boolean";
				} else if (type.equals("BIGINT")) {
					return "java.math.BigInteger";
				} else if (type.equals("FLOAT")) {
					// return "java.lang.Float";
					return "float";
				} else if (type.equals("DOUBLE")) {
					// return "java.lang.Double";
					return "double";
				} else if (type.equals("DECIMAL")) {
					return "java.math.BigDecimal";
				} else if (type.equals("DATE")) {
					// return "java.sql.Date";
					return "java.util.Date";
				} else if (type.equals("TIME")) {
					// return "java.sql.Time";
					return "java.util.Date";
				} else if (type.equals("DATETIME") || type.equals("TIMESTAMP")) {
					// return "java.sql.Timestamp";
					return "java.util.Date";
				} else if (type.equals("YEAR")) {
					// return "java.sql.Date";
					return "java.util.Date";
				}

				return "java.lang.String";
			}

			public boolean isPk() {
				return this.pk;
			}

			public boolean isAi() {
				return this.ai;
			}
		}
	}

}

package net.twentyonesolutions.lucee.core;

import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.exp.PageException;
import lucee.runtime.util.Cast;
import net.twentyonesolutions.lucee.app.LuceeApps;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Dumper {

	final static Cast cast = LuceeApps.getCastUtil();

	public static DumpTable toDumpData(Object o) {

		DumpTable table = new DumpTable(o.getClass().getName(), "#6289a3", "#dee3e9", "#000000");
		Class clazz;
		if (o instanceof Class)
			clazz = (Class) o;
		else
			clazz = o.getClass();

		String fullClassName = clazz.getName();
		int pos = fullClassName.lastIndexOf('.');
		String className = (pos == -1) ? fullClassName : fullClassName.substring(pos + 1);

		table.setTitle(className);
		table.appendRow(1, new DumpString("class"), new DumpString(fullClassName));

		Field[] fields = clazz.getFields();
		DumpTable fieldDump = new DumpTable("#6289a3", "#dee3e9", "#000000");
		fieldDump.appendRow(-1, new DumpString("name"), new DumpString("pattern"), new DumpString("value"));

		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			DumpData value;
			try {

				value = new DumpString(cast.toString(field.get(o), ""));
			}
			catch (Exception e) {

				value = new DumpString("");
			}
			fieldDump.appendRow(0, new DumpString(field.getName()), new DumpString(field.toString()), value);
		}
		if (fields.length > 0)
			table.appendRow(1, new DumpString("fields"), fieldDump);

		StringBuilder objMethods = new StringBuilder();
		Method[] methods = clazz.getMethods();
		DumpTable methodDump = new DumpTable("#6289a3", "#dee3e9", "#000000");
		methodDump.appendRow(-1, new DumpString("return"), new DumpString("interface"), new DumpString("exceptions"));
		for (int i = 0; i < methods.length; i++) {

			Method method = methods[i];
			if (Object.class == method.getDeclaringClass()) {
				if (objMethods.length() > 0)
					objMethods.append(", ");
				objMethods.append(method.getName());
				continue;
			}

			StringBuilder sbExceptions = new StringBuilder();
			Class[] exceptions = method.getExceptionTypes();
			for (int p = 0; p < exceptions.length; p++) {
				if (p > 0)
					sbExceptions.append("\n");
				sbExceptions.append(DumpString.toClassName(exceptions[p]));
			}

			StringBuilder sbParameters = new StringBuilder(method.getName());
			sbParameters.append('(');
			Class[] parameters = method.getParameterTypes();
			for (int p = 0; p < parameters.length; p++) {
				if (p > 0)
					sbParameters.append(", ");
				sbParameters.append(DumpString.toClassName(parameters[p]));
			}
			sbParameters.append(')');

			methodDump.appendRow(0, new DumpString(DumpString.toClassName(method.getReturnType())),
					new DumpString(sbParameters.toString()), new DumpString(sbExceptions.toString()));
		}
		if (methods.length > 0)
			table.appendRow(1, new DumpString("methods"), methodDump);

		DumpTable inherited = new DumpTable("#6289a3", "#dee3e9", "#000000");
		inherited.appendRow(7, new DumpString("Inherited Methods"));
		inherited.appendRow(0, new DumpString(objMethods.toString()));
		table.appendRow(1, new DumpString(""), inherited);

		return table;
	}

	public static class DumpString implements DumpData {

		private String data;

		public DumpString(String data) {

			this.data = data;
		}

		public DumpString(Object data) {

			try {
				this.data = cast.toString(data);
			}
			catch (PageException e) {
				this.data = data.toString();
			}
		}

		public DumpString(boolean data) {

			this.data = cast.toString(data);
		}

		public DumpString(double data) {

			this.data = cast.toString(data);
		}

		@Override
		public String toString() {
			return data;
		}

		public static String toClassName(Object o) {

			if (o == null)
				return "null";

			return toClassName(o.getClass());
		}

		public static String toClassName(Class clazz) {

			if (clazz.isArray())
				return toClassName(clazz.getComponentType()) + "[]";

			return clazz.getName();
		}

	}

}
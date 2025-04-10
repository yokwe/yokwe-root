package yokwe.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.function.Function;

public class ToString {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static class ClassInfo {
		private static Map<String, ClassInfo> map = new HashMap<>();

		private static class FieldInfo {
			final Field  field;
			final String name;
			
			FieldInfo(Field field) {
				this.field = field;
				this.name  = field.getName();
			}
			
			Object get(Object o) {
				try {
					return field.get(o);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					String exceptionName = e.getClass().getSimpleName();
					logger.error("{} {}", exceptionName, e);
					throw new UnexpectedException(exceptionName, e);
				}
			}
		}
		
		private static ClassInfo getInstance(Class<?> clazz) {
			var key = clazz.getTypeName();
			var ret = map.get(key);
			if (ret != null) return ret;
			
			var value = new ClassInfo(clazz);
			map.put(key, value);
			return value;
		}
		
		private final FieldInfo[]  fieldInfos;
		
		private ClassInfo(Class<?> clazz) {			
			var list = new ArrayList<FieldInfo>();
			for(var field: clazz.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) continue;
				field.setAccessible(true); // allow access private field
				list.add(new FieldInfo(field));
			}
			
			this.fieldInfos = list.toArray(FieldInfo[]::new);
		}
	}
	
	private static final Map<String, Function<Object, String>> functionMap = new TreeMap<>();
	static {
		functionMap.put(Boolean.class.getTypeName(), o -> (boolean)o ? "true" : "false");
		functionMap.put(Double.class.getTypeName(),  o -> String.valueOf((double)o));
		functionMap.put(Float.class.getTypeName(),   o -> String.valueOf((float)o));
		functionMap.put(Integer.class.getTypeName(), o -> String.valueOf((int)o));
		functionMap.put(Long.class.getTypeName(),    o -> String.valueOf((long)o));
		functionMap.put(Short.class.getTypeName(),   o -> String.valueOf((short)o));
		functionMap.put(Byte.class.getTypeName(),    o -> String.valueOf((byte)o));
		
		functionMap.put(Character.class.getTypeName(),  o -> "'" + String.valueOf((char)o).replace("\\",	"\\\\").replace("'", "\\\'") + "'");
		functionMap.put(String.class.getTypeName(),     o -> "\"" + (String)o.toString().replace("\\", "\\\\").replace("\"", "\\\"") + "\"");
		functionMap.put(BigDecimal.class.getTypeName(), o -> ((BigDecimal)o).toPlainString());
	}
	
	
	public static class Options {
		public static class Builder {
			private boolean      withFieldName;
			private List<String> excludePackageList = new ArrayList<>();
			
			private Builder() {
				withFieldName = true;
				excludePackageList.add("java.");
				excludePackageList.add("javax.");
				excludePackageList.add("jdk.");
				excludePackageList.add("sun.");
				excludePackageList.add("com.sun.");
			}
			
			public Builder withFieldName(boolean value) {
				withFieldName = value;
				return this;
			}
			public Builder excludePackage(String string) {
				excludePackageList.add(string);
				return this;
			}
			
			public Options build() {
				return new Options(this);
			}
		}
		
		public static Builder builder() {
			return new Builder();
		}
		
		public final boolean      withFieldName;
		public final List<String> excludePackageList;
		
		private Options(Builder builder) {
			this.withFieldName      = builder.withFieldName;
			this.excludePackageList = builder.excludePackageList;
		}
	}
	public static final Options WITH_FIELD_NAME    = Options.builder().withFieldName(true).build();
	public static final Options WITHOUT_FIELD_NAME = Options.builder().withFieldName(false).build();
	
	public static String withFieldName(Object o) {
		return toString(o, WITH_FIELD_NAME);
	}
	public static String withoutFieldName(Object o) {
		return toString(o, WITHOUT_FIELD_NAME);
	}
	public static String toString(Object o, Options options) {
		if (o == null) return "null";
		
		var clazz     = o.getClass();
		var typeName  = clazz.getTypeName();
		var function  = functionMap.get(typeName);
		
		if (function != null)            return function.apply(o);
		if (clazz.isArray())             return toStringArray(o, options);
		
		for(var pakcagePrifx: options.excludePackageList) {
			if (typeName.startsWith(pakcagePrifx)) return o.toString();
		}
		
		return toStringObject(o, options);
	}
	
	private static String toStringArray(Object o, Options options) {
		var stringJoiner = new StringJoiner(", ", "[", "]");
		
		var length = Array.getLength(o);
		for(int i = 0; i < length; i++) {
			var element = Array.get(o, i);
			stringJoiner.add(toString(element, options));
		}
		
		return stringJoiner.toString();
	}
	
	private static String toStringObject(Object o, Options options) {
		var classInfo = ClassInfo.getInstance(o.getClass());
		
		var stringJoiner = new StringJoiner(", ", "{", "}");
		
		for(var fieldInfo: classInfo.fieldInfos) {
			if (options.withFieldName) {
				stringJoiner.add(fieldInfo.name + ": " + toString(fieldInfo.get(o), options));
			} else {
				stringJoiner.add(toString(fieldInfo.get(o), options));
			}
		}
		
		return stringJoiner.toString();
	}
}

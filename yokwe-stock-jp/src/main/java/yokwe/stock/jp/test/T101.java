package yokwe.security.japan.test;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.util.AutoIndentPrintWriter;
import yokwe.util.FileUtil;

public class T101 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T101.class);
	
	enum FieldType {
		OBJECT,
		ARRAY,
		//
		BOOLEAN,
		NUMBER,
		STRING,
		DATE_TIME,
	}
	
	private static Map<String, Field> map = new TreeMap<>();
	private static void addField(Field newValue) {
		if (map.containsKey(newValue.name)) {
			Field oldField = map.get(newValue.name);
			if (!newValue.equals(oldField)) {
				logger.error("old don't mache new");
				logger.error("  old {}", oldField);
				logger.error("  new {}", newValue);
				throw new UnexpectedException("old don't mache new");
			}
		} else {
			map.put(newValue.name, newValue);
		}

	}
	public static abstract class Field {
		public final FieldType type;
		public final String    name;
		
		public Field(FieldType type, String name) {
			this.type = type;
			this.name = name;
		}
		
		@Override
		public String toString() {
			return String.format("{%s %s}", type, name);
		}
		@Override
		public boolean equals(Object object) {
			if (object instanceof Field) {
				Field that = (Field)object;
				if (this.type == that.type && this.name.equals(that.name)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}
	
	public static class FieldBoolean extends Field {
		public FieldBoolean(String name) {
			super(FieldType.BOOLEAN, name);
		}
	}
	public static class FieldNumber extends Field {
		public FieldNumber(String name) {
			super(FieldType.NUMBER, name);
		}
	}
	public static class FieldString extends Field {
		public FieldString(String name) {
			super(FieldType.STRING, name);
		}
	}
	public static class FieldDateTime extends Field {
		public FieldDateTime(String name) {
			super(FieldType.DATE_TIME, name);
		}
	}
	
	private static final Pattern PAT_DATETIME = Pattern.compile("(19|20)[0-9][0-9]-[01][0-9]-[012][0-9]T[012][0-9]:[0-5][0-9]:[0-5][0-9]");
	
	private static boolean isDateTime(String string) {
		if (PAT_DATETIME.matcher(string).matches()) return true;
		return false;
	}
	
	public static class FieldArray extends Field {
		public final int         size;
		public final List<Field> list = new ArrayList<>();
		
		public FieldArray(String name, JsonArray jsonArray) {
			super(FieldType.ARRAY, name);
			
			this.size = jsonArray.size();
			
			if (size == 0) {
				logger.error("array size is zero");
				logger.error("  name {}", name);
        		throw new UnexpectedException("array size is zero");
			} else {
				for(JsonValue jsonValue: jsonArray) {
					list.add(getField(null, null, jsonValue));
				}
			}
		}
		
		@Override
		public String toString() {
			return String.format("{%s %s %s %s}", type, name, size, list.size());
		}
		@Override
		public boolean equals(Object object) {
			if (object instanceof FieldArray) {
				FieldArray that = (FieldArray)object;
				if (this.type == that.type && this.name.equals(that.name)) {
					// FIXME compare list of this and that
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}
	
	public static class FieldObject extends Field {
		public final List<Field> list = new ArrayList<>();
		
		public FieldObject(String name, JsonObject jsonObject) {
			super(FieldType.OBJECT, name);
			
			for(String fieldName: jsonObject.keySet()) {
				Field field = getField(name, fieldName, jsonObject.get(fieldName));
				switch(field.type) {
				case ARRAY:
				case OBJECT:
					addField(field);
					break;
				case BOOLEAN:
				case NUMBER:
				case STRING:
				case DATE_TIME:
					addField(field);
					break;
				default:
		    		logger.error("Unexpected field.type");
		    		logger.error("  field.type {}", field.type);    		
		    		throw new UnexpectedException("Unexpected field.type");
				}
			}
		}
		
		@Override
		public String toString() {
			return String.format("{%s %s %s}", type, name, list);
		}
		@Override
		public boolean equals(Object object) {
			if (object instanceof FieldObject) {
				FieldObject that = (FieldObject)object;
				if (this.type == that.type && this.name.equals(that.name)) {
					// FIXME check list of this and that
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}
	
//	private static Field getField(String fieldName, JsonValue jsonValue) {
//		ValueType valueType = jsonValue.getValueType();
//		
//		switch(valueType) {
//		case OBJECT:
//			return new FieldObject(fieldName, (JsonObject)jsonValue);
//		case ARRAY:
//			return new FieldArray(fieldName, (JsonArray)jsonValue);
//		case TRUE:
//		case FALSE:
//			return new FieldBoolean(fieldName);
//		case NUMBER:
//			return new FieldNumber(fieldName);
//		case STRING:
//		{
//			String string = ((JsonString)jsonValue).getString();
//			if (isDateTime(string)) {
//				return new FieldDateTime(fieldName);
//			} else {
//				return new FieldString(fieldName);
//			}
//		}
//		case NULL:
//		default:
//    		logger.error("Unexpected valueType");
//    		logger.error("  valueType {}", valueType);    		
//    		throw new UnexpectedException("Unexpected valueType");
//		}
//	}
	
	private static Field getField(String objectName, String fieldName, JsonValue jsonValue) {
		ValueType valueType = jsonValue.getValueType();
		
		switch(valueType) {
		case OBJECT:
			return new FieldObject(fieldName, (JsonObject)jsonValue);
		case ARRAY:
			return new FieldArray(fieldName, (JsonArray)jsonValue);
		case TRUE:
		case FALSE:
			return new FieldBoolean(objectName + "#" + fieldName);
		case NUMBER:
			return new FieldNumber(objectName + "#" + fieldName);
		case STRING:
		{
			String string = ((JsonString)jsonValue).getString();
			if (isDateTime(string)) {
				return new FieldDateTime(objectName + "#" + fieldName);
			} else {
				return new FieldString(objectName + "#" + fieldName);
			}
		}
		case NULL:
		default:
    		logger.error("Unexpected valueType");
    		logger.error("  valueType {}", valueType);    		
    		throw new UnexpectedException("Unexpected valueType");
		}
	}

	private static void genSourceFile(String packageName, String className, String jsonPath) {
		logger.info("packageName {}", packageName);
		logger.info("className   {}", className);
		logger.info("jsonPath    {}", jsonPath);
		
    	String sourcePath = String.format("src/%s/%s.java", packageName.replace(".", "/"), className);
		logger.info("sourcePath  {}", sourcePath);

    	final JsonReader jsonReader;
    	{
        	String string = FileUtil.read().file(jsonPath);
        	if (string == null) {
        		logger.error("cannot read file");
        		logger.error("  path {}", jsonPath);    		
        		throw new UnexpectedException("cannot read file");
        	}
        	jsonReader = Json.createReader(new StringReader(string));
    	}
    	
    	final Field field;
    	{
        	JsonStructure jsonStructure = jsonReader.read();
        	logger.info("jsonStructure {}", jsonStructure.getValueType());
        	
        	switch(jsonStructure.getValueType()) {
        	case OBJECT:
        	case ARRAY:
        		field = getField(null, className, jsonStructure);
        		break;
        	default:
        		logger.error("Unexpecteed valueType");
        		logger.error("  valueType {}", jsonStructure.getValueType());
        		throw new UnexpectedException("Unexpecteed valueType");
        	}
    	}
    	
    	logger.info("map {}", map.size());
    	for(var e: map.values()) {
    		logger.info("field {}", e);
    	}
    	
		try (AutoIndentPrintWriter out = new AutoIndentPrintWriter(new PrintWriter(sourcePath))) {
			out.println("package %s;", packageName);
			out.println();
			out.println("import yokwe.util.StringUtil;");
			out.println();

			out.println("public final class %s {", className);
			
			out.println("@Override");
			out.println("public String toString() {");
			out.println("return StringUtil.toString(this);");
			out.println("}");

			out.println("}");
		} catch (FileNotFoundException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
    	

	}
	public static void main(String[] args) {
    	logger.info("START");

    	genSourceFile("yokwe.security.japan.smbctb", "Security", "tmp/F000005MIQ.json");
        
    	logger.info("STOP");
    }

}
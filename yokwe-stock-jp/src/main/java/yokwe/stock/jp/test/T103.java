package yokwe.security.japan.test;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

public class T103 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T103.class);
	
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
		public final String    simpleName;
		
		public Field(FieldType type, String name) {
			this.type = type;
			this.name = name;
			
			String[] names = name.split("#");
			this.simpleName = names[names.length - 1];
		}
		
		@Override
		public String toString() {
//			return String.format("{%s %s}", type, name);
			return String.format("{%s %s}", type, simpleName);
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
	
	public static class FieldCount {
		public final Field field;
		public       int   count;
		
		public FieldCount(Field field) {
			this.field = field;
			this.count = 1;
		}
		public void increment() {
			count++;
		}
		
		@Override
		public String toString() {
			return String.format("%3d %s", count, field);
		}
	}
	public static class FieldArray extends Field {
		public final int                size;
		public final Map<String, FieldCount> map = new LinkedHashMap<>();
		
		public FieldArray(String name, JsonArray jsonArray) {
			super(FieldType.ARRAY, name);
			
			this.size = jsonArray.size();
			
			if (size == 0) {
				logger.error("array size is zero");
				logger.error("  name {}", name);
        		throw new UnexpectedException("array size is zero");
			} else {
				for(JsonValue jsonValue: jsonArray) {
					ValueType valueType = jsonValue.getValueType();
					
					if (valueType == ValueType.OBJECT) {
						JsonObject jsonObject = (JsonObject)jsonValue;
						
						for(String fieldName: jsonObject.keySet()) {
							JsonValue fieldValue = jsonObject.get(fieldName);
							ValueType fieldType = fieldValue.getValueType();
							
							final Field field;
							
							switch(fieldType) {
							case OBJECT:
								field = new FieldObject(name + "#" + fieldName, (JsonObject)fieldValue);
								addField(field);
								break;
							case ARRAY:
								field = new FieldArray(name + "#" + fieldName, (JsonArray)fieldValue);
								addField(field);
								break;
							case TRUE:
							case FALSE:
								field = new FieldBoolean(fieldName);
								break;
							case NUMBER:
								field = new FieldNumber(fieldName);
								break;
							case STRING:
							{
								String string = ((JsonString)fieldValue).getString();
								if (isDateTime(string)) {
									field = new FieldDateTime(fieldName);
								} else {
									field = new FieldString(fieldName);
								}
							}
								break;
							case NULL:
							default:
					    		logger.error("Unexpected valueType");
					    		logger.error("  valueType {}", valueType);    		
					    		throw new UnexpectedException("Unexpected valueType");
							}
							
							if (map.containsKey(fieldName)) {
								map.get(fieldName).increment();
							} else {
								map.put(fieldName, new FieldCount(field));
							}
						}
					} else {
			    		logger.error("Unexpected valueType");
			    		logger.error("  valueType {}", valueType);    		
			    		throw new UnexpectedException("Unexpected valueType");
					}
				}
			}
		}
		
		@Override
		public String toString() {
//			return String.format("{%s %s %s %s}", type, name, size, map);
			List<String> list = map.values().stream().map(o -> String.format("{%3d %s %s}", o.count, o.field.type, o.field.name)).collect(Collectors.toList());
			return String.format("{%-6s %-47s %2d %s}", type, name, size, list);
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
				JsonValue jsonValue = jsonObject.get(fieldName);
				ValueType valueType = jsonValue.getValueType();
				
				final Field field;
				
				switch(valueType) {
				case OBJECT:
					field = new FieldObject(fieldName, (JsonObject)jsonValue);
					addField(field);
					break;
				case ARRAY:
					field = new FieldArray(fieldName, (JsonArray)jsonValue);
					addField(field);
					break;
				case TRUE:
				case FALSE:
					field = new FieldBoolean(fieldName);
					break;
				case NUMBER:
					field = new FieldNumber(fieldName);
					break;
				case STRING:
				{
					String string = ((JsonString)jsonValue).getString();
					if (isDateTime(string)) {
						field = new FieldDateTime(fieldName);
					} else {
						field = new FieldString(fieldName);
					}
				}
					break;
				case NULL:
				default:
		    		logger.error("Unexpected valueType");
		    		logger.error("  valueType {}", valueType);    		
		    		throw new UnexpectedException("Unexpected valueType");
				}

				list.add(field);
			}
		}
		
		@Override
		public String toString() {
			return String.format("{%-6s %-50s %s}", type, name, list);
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
        		field = new FieldObject(className, (JsonObject)jsonStructure);
        		break;
        	case ARRAY:
        		field = new FieldArray(className, (JsonArray)jsonStructure);
        		break;
        	default:
        		logger.error("Unexpecteed valueType");
        		logger.error("  valueType {}", jsonStructure.getValueType());
        		throw new UnexpectedException("Unexpecteed valueType");
        	}
    	}
    	
    	logger.info("map {}", map.size());
    	for(var e: map.entrySet()) {
    		logger.info("field {}", e.getValue());
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
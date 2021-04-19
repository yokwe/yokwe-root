/*******************************************************************************
 * Copyright (c) 2020, Yasuhiro Hasegawa
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. The name of the author may not be used to endorse or promote products derived
 *      from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *******************************************************************************/
package yokwe.stock.jp.smbctb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.util.AutoIndentPrintWriter;
import yokwe.util.AutoIndentPrintWriter.Layout;
import yokwe.util.FileUtil;

public class GenearateJSONStub {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(GenearateJSONStub.class);
	
	public static String toJavaClassName(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}
	
	private static Set<String> javaKeywordSet = new TreeSet<>();
	static {
		javaKeywordSet.add("return");
	}
	
	private static Pattern PAT_JAVA_VARIABLE = Pattern.compile("([A-Z]+?)([A-Z][a-z0-9_$][A-Za-z0-9_$]+)");
	public static String toJavaVariableName(String name) {
		Matcher m = PAT_JAVA_VARIABLE.matcher(name);
		String ret;
		if (m.matches()) {
			String g1 = m.group(1);
			String g2 = m.group(2);
			
			ret = g1.toLowerCase() + g2;
		} else {
			ret = name.substring(0, 1).toLowerCase() + name.substring(1);
		}
		if (javaKeywordSet.contains(ret)) {
			return ret + "_";
		}
		ret = ret.replace("-", "_");
		
		return ret;
	}
	public static class FieldMap {
		public Map<String, Field> map = new TreeMap<>();
		
		public void add(Field newValue) {
			if (map.containsKey(newValue.name)) {
				Field oldValue = map.get(newValue.name);
				if (newValue.type == Field.Type.OBJECT && oldValue.type == Field.Type.OBJECT) {
					FieldObject fieldObject= (FieldObject)newValue;
					if (fieldObject.list.isEmpty()) return;
				}
				if (oldValue.equals(newValue)) return;
				
				logger.error("duplicate field name");
				logger.error("  name {}", newValue.name);
				logger.error("  old  {}", oldValue);
				logger.error("  new  {}", newValue);
				throw new UnexpectedException("duplicate field name");
			} else {
				if (newValue.type == Field.Type.OBJECT) {
					FieldObject fieldObject= (FieldObject)newValue;
					if (fieldObject.list.isEmpty()) return;
				}
				map.put(newValue.name, newValue);
			}
		}
		public int size() {
			return map.size();
		}
		public Collection<Field> values() {
			return map.values();
		}
		public Field get(String name) {
			if (map.containsKey(name)) {
				return map.get(name);
			}
			logger.error("Unexpected name");
			logger.error("  name {}", name);
			throw new UnexpectedException("Unexpected name");
		}
	}
	
	public static abstract class Field {
		public enum Type {
			OBJECT,
			ARRAY,
			//
			BOOLEAN,
			NUMBER,
			STRING,
		}
		
		public enum NumberFormat {
			INT,
			REAL
		}
		
		public enum StringFormat {
			INT,
			REAL,
			DATE,
			DATE_TIME,
			STRING,
		}

		
		public final Type   type;
		public final String name;
		public final String simpleName;
		
		public Field(Type type, String name) {
			this.type = type;
			this.name = name;
			
			String[] names = name.split("#");
			this.simpleName = names[names.length - 1];
		}
		
		@Override
		public String toString() {
			if (this instanceof FieldString) {
				return String.format("{%s %s %s}", type, simpleName, ((FieldString)this).format);
			} else if (this instanceof FieldNumber) {
				return String.format("{%s %s %s}", type, simpleName, ((FieldNumber)this).format);
			} else {
				return String.format("{%s %s}", type, simpleName);
			}
		}
		@Override
		public boolean equals(Object object) {
			if (object instanceof Field) {
				Field that = (Field)object;
				return this.type == that.type && this.name.equals(that.name);
			} else {
				return false;
			}
		}
	}
	
	public static class FieldBoolean extends Field {
		public FieldBoolean(String name) {
			super(Type.BOOLEAN, name);
		}
	}
	
	private static final Pattern PAT_INT  = Pattern.compile("(\\+|-)?[0-9]+");
	private static final Pattern PAT_REAL = Pattern.compile("(\\+|-)?[0-9]+\\.[0-9]+");
	public static class FieldNumber extends Field {
		public final NumberFormat format;
		
		public FieldNumber(String name, JsonNumber jsonNumber) {
			super(Type.NUMBER, name);
			
			String string = jsonNumber.toString();
			if (PAT_INT.matcher(string).matches()) {
				format = NumberFormat.INT;
			} else if (PAT_REAL.matcher(string).matches()) {
				format = NumberFormat.REAL;
			} else {
        		logger.error("Unexpecteed format");
        		logger.error("  string {}", string);
        		throw new UnexpectedException("Unexpecteed format");
			}
		}
		
		@Override
		public boolean equals(Object object) {
			if (object instanceof FieldNumber) {
				FieldNumber that = (FieldNumber)object;
				return this.type == that.type && this.name.equals(that.name) && this.format == that.format;
			} else {
				return false;
			}
		}
	}
	
	private static final Pattern PAT_DATE        = Pattern.compile("(19|20)[0-9][0-9]-[01][0-9]-[012][0-9]");
	private static final Pattern PAT_DATE_000000 = Pattern.compile("(19|20)[0-9][0-9]-[01][0-9]-[012][0-9]T00:00:00");
	private static final Pattern PAT_DATE_TIME   = Pattern.compile("(19|20)[0-9][0-9]-[01][0-9]-[012][0-9]T[012][0-9]:[0-5][0-9]:[0-5][0-9]");
	public static class FieldString extends Field {
		public final StringFormat format;
		
		public FieldString(String name, JsonString jsonString) {
			super(Type.STRING, name);
			
			String string = jsonString.getString();
			if (PAT_INT.matcher(string).matches()) {
				format = StringFormat.INT;
			} else if (PAT_REAL.matcher(string).matches()) {
				format = StringFormat.REAL;
			} else if (PAT_DATE.matcher(string).matches()) {
				format = StringFormat.DATE;
			} else if (PAT_DATE_000000.matcher(string).matches()) {
				format = StringFormat.DATE;
			} else if (PAT_DATE_TIME.matcher(string).matches()) {
				format = StringFormat.DATE_TIME;
			} else {
				format = StringFormat.STRING;
			}
		}
		
		@Override
		public boolean equals(Object object) {
			if (object instanceof FieldNumber) {
				FieldString that = (FieldString)object;
				return this.type == that.type && this.name.equals(that.name) && this.format == that.format;
			} else {
				return false;
			}
		}
	}
	
	public static class FieldArray extends Field {
		public final int         size;
		
		public final Map<String, FieldCount> map = new LinkedHashMap<>();
		
		public FieldArray(FieldMap fieldMap, String name, JsonArray jsonArray) {
			super(Type.ARRAY, name);
			
			this.size = jsonArray.size();
			
			if (size == 0) {
				logger.warn("array size is zero");
				logger.warn("  name {}", name);
//        		throw new UnexpectedException("array size is zero");
			} else {
				for(JsonValue jsonValue: jsonArray) {
					ValueType valueType = jsonValue.getValueType();
					
					switch(valueType) {
					case OBJECT:
					{
						JsonObject jsonObject = (JsonObject)jsonValue;
						
						for(String fieldName: jsonObject.keySet()) {
							JsonValue fieldValue = jsonObject.get(fieldName);
							ValueType fieldType = fieldValue.getValueType();
							
							final Field field;
							
							switch(fieldType) {
							case OBJECT:
								field = new FieldObject(fieldMap, name + "#" + fieldName, (JsonObject)fieldValue);
								fieldMap.add(field);
								break;
							case ARRAY:
								field = new FieldArray(fieldMap, name + "#" + fieldName, (JsonArray)fieldValue);
								fieldMap.add(field);
								break;
							case TRUE:
							case FALSE:
								field = new FieldBoolean(fieldName);
								break;
							case NUMBER:
								field = new FieldNumber(fieldName, (JsonNumber)fieldValue);
								break;
							case STRING:
								field = new FieldString(fieldName, (JsonString)fieldValue);
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
					}
						break;
					case STRING:
					{
						JsonString jsonString = (JsonString)jsonValue;
						if (map.containsKey("STRING")) {
							map.get("STRING").increment();
						} else {
							map.put("STRING", new FieldCount(new FieldString("STRING", jsonString)));
						}
					}
						break;
					case NUMBER:
					{
						JsonNumber jsonNumber = (JsonNumber)jsonValue;
						if (map.containsKey("NUMBER")) {
							map.get("NUMBER").increment();
						} else {
							map.put("NUMBER", new FieldCount(new FieldNumber("NUMBER", jsonNumber)));
						}
					}
						break;
					default:
			    		logger.error("Unexpected valueType");
			    		logger.error("  name      {}", name);    		
			    		logger.error("  jsonValue {}", jsonValue);    		
			    		throw new UnexpectedException("Unexpected valueType");
					}
				}
			}
		}
		
		@Override
		public String toString() {
			List<String> list = new ArrayList<>();
			
			for(FieldCount fieldCount: map.values()) {
				switch(fieldCount.field.type) {
				case NUMBER:
					list.add(String.format("{%d %s %s %s}", fieldCount.count, fieldCount.field.type, fieldCount.field.name, ((FieldNumber)fieldCount.field).format));
					break;
				case STRING:
					list.add(String.format("{%d %s %s %s}", fieldCount.count, fieldCount.field.type, fieldCount.field.name, ((FieldString)fieldCount.field).format));
					break;
				case OBJECT:
				case ARRAY:
				case BOOLEAN:
					list.add(String.format("{%d %s %s}", fieldCount.count, fieldCount.field.type, fieldCount.field.name));
					break;
				default:
		    		logger.error("Unexpected type");
		    		logger.error("  fieldCount.field {}", fieldCount.field);    		
		    		throw new UnexpectedException("Unexpected type");
				}
			}
			return String.format("{%-6s %s %d %s}", type, name, size, list);
		}
		public String toStringSimple() {
			return String.format("{%-6s %s %d}", type, name, size);
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
		
		public FieldObject(FieldMap fieldMap, String name, JsonObject jsonObject) {
			super(Type.OBJECT, name);
			
			for(String fieldName: jsonObject.keySet()) {
				JsonValue jsonValue = jsonObject.get(fieldName);
				ValueType valueType = jsonValue.getValueType();
				
				final Field field;
				
				switch(valueType) {
				case OBJECT:
					field = new FieldObject(fieldMap, name + "#" + fieldName, (JsonObject)jsonValue);
					fieldMap.add(field);
					break;
				case ARRAY:
					field = new FieldArray(fieldMap, name + "#" + fieldName, (JsonArray)jsonValue);
					fieldMap.add(field);
					break;
				case TRUE:
				case FALSE:
					field = new FieldBoolean(fieldName);
					break;
				case NUMBER:
					field = new FieldNumber(fieldName, (JsonNumber)jsonValue);
					break;
				case STRING:
					field = new FieldString(fieldName, (JsonString)jsonValue);
					break;
				case NULL:
					continue;
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
			List<String> stringList = new ArrayList<>();
			for(var e: list) {
				if (e.type == Type.ARRAY) {
					stringList.add(((FieldArray)e).toStringSimple());
				} else {
					stringList.add(e.toString());
				}
			}
			return String.format("{%-6s %s %s}", type, name, stringList);
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
			return String.format("%d %s", count, field);
		}
	}

	private static void genClass(AutoIndentPrintWriter out, FieldMap fieldMap, List<Field> fieldList, String className, boolean classIsStataic) {
		out.println("public %sfinal class %s {", classIsStataic ? "static ": "", className);

		for(Field childField: fieldList) {
			String childClassName = toJavaClassName(childField.simpleName);

			switch(childField.type) {
			case OBJECT:
				genClass(out, fieldMap, (FieldObject)childField, childClassName, true);
				break;
			case ARRAY:
				genClass(out, fieldMap, (FieldArray)childField, childClassName, true);
				break;
			case BOOLEAN:
			case NUMBER:
			case STRING:
				break;
			default:
        		logger.error("Unexpecteed field type");
        		logger.error("  childField {}", childField);
        		throw new UnexpectedException("Unexpecteed field type");	
			}
		}

		out.prepareLayout();
		for(Field childField: fieldList) {
			String simpleName     = childField.simpleName;
			String childClassName = toJavaClassName(simpleName);
			String childFieldName = toJavaVariableName(simpleName);
			
			switch(childField.type) {
			case BOOLEAN:
				out.println("public @Name(\"%s\") boolean %s; // BOOLEAN", simpleName, childFieldName);
				break;
			case NUMBER:
				out.println("public @Name(\"%s\") BigDecimal %s; // NUMBER %s", simpleName, childFieldName, ((FieldNumber)childField).format);
//				switch(((FieldNumber)childField).format) {
//				case INT:
//					out.println("public @Name(\"%s\") int %s;", simpleName, childFieldName);
//					break;
//				case REAL:
//					out.println("public @Name(\"%s\") double %s;", simpleName, childFieldName);
//					break;
//				default:
//	        		logger.error("Unexpecteed format");
//	        		logger.error("  childField {}", childField);
//	        		throw new UnexpectedException("Unexpecteed format");	
//				}
				break;
			case STRING:
				out.println("public @Name(\"%s\") String %s; // STRING %s", simpleName, childFieldName, ((FieldString)childField).format);
//				switch(((FieldString)childField).format) {
//				case INT:
//					out.println("public @Name(\"%s\") int %s;", simpleName, childFieldName);
//					break;
//				case REAL:
//					out.println("public @Name(\"%s\") double %s;", simpleName, childFieldName);
//					break;
//				case DATE:
//					out.println("public @Name(\"%s\") LocalDate %s;", simpleName, childFieldName);
//					break;
//				case DATE_TIME:
//					out.println("public @Name(\"%s\") LocalDateTime %s;", simpleName, childFieldName);
//					break;
//				case STRING:
//					out.println("public @Name(\"%s\") String %s;", simpleName, childFieldName);
//					break;
//				default:
//	        		logger.error("Unexpecteed format");
//	        		logger.error("  childField {}", childField);
//	        		throw new UnexpectedException("Unexpecteed format");	
//				}
				break;
			case OBJECT:
				out.println("public @Name(\"%s\") %s %s; // OBJECT", simpleName, childClassName, childFieldName);
				break;
			case ARRAY:
				out.println("public @Name(\"%s\") %s[] %s; // ARRAY %d", simpleName, childClassName, childFieldName, ((FieldArray)childField).size);
//
//			{
//				FieldArray childFieldArray = (FieldArray)childField;
//				if (childFieldArray.size == 1) {
//					out.println("public @Name(\"%s\") %s %s;", simpleName, childClassName, childFieldName);
//				} else {
//					out.println("public @Name(\"%s\") %s[] %s;", simpleName, childClassName, childFieldName);
//				}
//			}
				break;
			default:
        		logger.error("Unexpecteed field type");
        		logger.error("  childField {}", childField);
        		throw new UnexpectedException("Unexpecteed field type");	
			}
		}
		out.layout(Layout.LEFT, Layout.LEFT, Layout.LEFT, Layout.LEFT, Layout.LEFT);
		
		out.println();
		out.println("@Override");
		out.println("public String toString() {");
		out.println("return StringUtil.toString(this);");
		out.println("}");
		
		out.println("}");
		out.println();
	}
	private static void genClass(AutoIndentPrintWriter out, FieldMap fieldMap, FieldArray fieldArray, String className, boolean classIsStatic) {
		List<Field> fieldList = fieldArray.map.values().stream().map(o -> o.field).collect(Collectors.toList());
		genClass(out, fieldMap, fieldList, className, classIsStatic);
	}
	private static void genClass(AutoIndentPrintWriter out, FieldMap fieldMap, FieldObject fieldObject, String className, boolean classIsStatic) {
		List<Field> fieldList = fieldObject.list;
		genClass(out, fieldMap, fieldList, className, classIsStatic);
	}
	
	public static void generate(String packageName, String className, String jsonString) {
		logger.info("====");
		logger.info("packageName {}", packageName);
		logger.info("className   {}", className);
		logger.info("jsonString  {}", jsonString.length());
		
    	String sourcePath = String.format("src/%s/%s.java", packageName.replace(".", "/"), className);
		logger.info("sourcePath  {}", sourcePath);

    	final JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
    	
    	FieldMap fieldMap = new FieldMap();
    	
    	final Field rootField;
    	{
        	JsonStructure jsonStructure = jsonReader.read();
        	logger.info("jsonStructure {}", jsonStructure.getValueType());
        	
        	switch(jsonStructure.getValueType()) {
        	case OBJECT:
        		rootField = new FieldObject(fieldMap, className, (JsonObject)jsonStructure);
        		fieldMap.add(rootField);
        		break;
        	case ARRAY:
        		rootField = new FieldArray(fieldMap, className, (JsonArray)jsonStructure);
        		fieldMap.add(rootField);
				break;
        	default:
        		logger.error("Unexpecteed valueType");
        		logger.error("  valueType {}", jsonStructure.getValueType());
        		throw new UnexpectedException("Unexpecteed valueType");
        	}
    	}
    	
    	logger.info("fieldMap {}", fieldMap.size());
    	for(var e: fieldMap.values()) {
    		logger.info("field {}", e);
    	}
    	logger.info("rootField {} {}", rootField.type, rootField.name);
    	
		try (AutoIndentPrintWriter out = new AutoIndentPrintWriter(new PrintWriter(sourcePath))) {
			out.println("package %s;", packageName);
			out.println();
			out.println("import java.math.BigDecimal;");
//			out.println("import java.time.LocalDate;");
//			out.println("import java.time.LocalDateTime;");
			out.println();
			out.println("import yokwe.util.StringUtil;");
			out.println("import yokwe.util.json.JSON.Name;");
			out.println();

			switch(rootField.type) {
			case OBJECT:
				genClass(out, fieldMap, (FieldObject)rootField, className, false);
				break;
			case ARRAY:
				genClass(out, fieldMap, (FieldArray)rootField, className, false);
				break;
			default:
        		logger.error("Unexpecteed field type");
        		logger.error("  rootField {}", rootField);
        		throw new UnexpectedException("Unexpecteed field type");	
			}
		} catch (FileNotFoundException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	public static void generate(String packageName, String className, File jsonFile) {
    	String jsonString = FileUtil.read().file(jsonFile);
    	if (jsonString == null) {
    		logger.error("cannot read file");
    		logger.error("  path {}", jsonFile);    		
    		throw new UnexpectedException("cannot read file");
    	}
    	generate(packageName, className, jsonString);
	}
}
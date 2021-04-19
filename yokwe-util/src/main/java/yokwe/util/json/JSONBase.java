package yokwe.util.json;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yokwe.util.GenericInfo;
import yokwe.util.http.HttpUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class JSONBase {
	static final Logger logger = LoggerFactory.getLogger(JSONBase.class);
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface JSONName {
		String value();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface IgnoreField {
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface DateTimeFormat {
		String value();
	}
	

	public static final LocalDate     NULL_LOCAL_DATE      = LocalDate.of(0, 1, 1);
	public static final LocalTime     NULL_LOCAL_TIME      = LocalTime.of(0, 0, 0);
	public static final LocalDateTime NULL_LOCAL_DATE_TIME = LocalDateTime.of(NULL_LOCAL_DATE, NULL_LOCAL_TIME);

	public static LocalDateTime getLocalDateTimeFromMilli(long value) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC);
	}

	@Override
	public String toString() {
		return StringUtil.toString(this);
	}

	protected JSONBase() {
		//
	}
	
	protected JSONBase(JsonObject jsonObject) {
		try {
			ClassInfo iexInfo = ClassInfo.get(this);
			// Sanity check
			for(ClassInfo.FieldInfo fieldInfo: iexInfo.fieldInfos) {
				if (fieldInfo.ignoreField)                      continue;
				if (jsonObject.containsKey(fieldInfo.jsonName)) continue;
				// jsonObject doesn't contains field named fieldInfo.jsonName
				logger.warn("Missing json field  {}  {}  {}", iexInfo.clazzName, fieldInfo.jsonName, jsonObject.keySet());
			}
			for(String jsonKey: jsonObject.keySet()) {
				if (iexInfo.fieldNameSet.contains(jsonKey)) continue;
				// this class doesn't contains field named jsonKey
				logger.warn("Unknown json field  {}  {}  {}", iexInfo.clazzName, jsonKey, iexInfo.fieldNameSet);
			}
			
			for(ClassInfo.FieldInfo fieldInfo: iexInfo.fieldInfos) {
				// Skip field if name is not exist in jsonObject
				if (!jsonObject.containsKey(fieldInfo.jsonName))continue;
				
				ValueType valueType = jsonObject.get(fieldInfo.jsonName).getValueType();
				
//				logger.debug("parse {} {} {}", fieldInfo.name, valueType.toString(), fieldInfo.type);
				
				switch(valueType) {
				case NUMBER:
					setValue(fieldInfo, jsonObject.getJsonNumber(fieldInfo.jsonName));
					break;
				case STRING:
					setValue(fieldInfo, jsonObject.getJsonString(fieldInfo.jsonName));
					break;
				case TRUE:
					setValue(fieldInfo, true);
					break;
				case FALSE:
					setValue(fieldInfo, false);
					break;
				case NULL:
					setValue(fieldInfo);
					break;
				case OBJECT:
					setValue(fieldInfo, jsonObject.getJsonObject(fieldInfo.jsonName));
					break;
				case ARRAY:
					setValue(fieldInfo, jsonObject.getJsonArray(fieldInfo.jsonName));
					break;
				default:
					logger.error("Unknown valueType {} {}", valueType.toString(), fieldInfo.toString());
					throw new UnexpectedException("Unknown valueType");
				}
			}
			
			// Assign default value for LocalDate and LocalDateTime, if field value is null and not appeared in jsonObject
			for(ClassInfo.FieldInfo fieldInfo: iexInfo.fieldInfos) {
				// Skip if name is exist in jsonObject
				if (jsonObject.containsKey(fieldInfo.jsonName)) continue;

				Object o = fieldInfo.field.get(this);
				// If field is null, assign default value
				if (o == null) {
					if (!fieldInfo.ignoreField) {
						logger.warn("Assign default value  {} {} {}", iexInfo.clazzName, fieldInfo.name, fieldInfo.type);
					}
					setValue(fieldInfo);
				}
			}
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	private void setValue(ClassInfo.FieldInfo fieldInfo, JsonNumber jsonNumber) throws IllegalArgumentException, IllegalAccessException {
		switch(fieldInfo.type) {
		case "double":
			fieldInfo.field.set(this, jsonNumber.doubleValue());
			break;
		case "long":
			fieldInfo.field.set(this, jsonNumber.longValue());
			break;
		case "int":
			fieldInfo.field.set(this, jsonNumber.intValue());
			break;
		case "java.math.BigDecimal":
			fieldInfo.field.set(this, jsonNumber.bigDecimalValue());
			break;
		case "java.lang.String":
			// To handle irregular case in Symbols, add this code. Value of iexId in Symbols can be number or String.
			fieldInfo.field.set(this, jsonNumber.toString());
			break;
		case "java.time.LocalDateTime":
			fieldInfo.field.set(this, getLocalDateTimeFromMilli(jsonNumber.longValue()));
			break;
		default:
			logger.error("Unexptected type {}", toString());
			throw new UnexpectedException("Unexptected type");
		}
	}
	private void setValue(ClassInfo.FieldInfo fieldInfo, JsonString jsonString) throws IllegalArgumentException, IllegalAccessException {
		switch(fieldInfo.type) {
		case "java.lang.String":
			fieldInfo.field.set(this, jsonString.getString());
			break;
		case "double":
			fieldInfo.field.set(this, Double.valueOf((jsonString.getString().length() == 0) ? "0" : jsonString.getString()));
			break;
		case "long":
			fieldInfo.field.set(this, Long.valueOf((jsonString.getString().length() == 0) ? "0" : jsonString.getString()));
			break;
		case "int":
			fieldInfo.field.set(this, Integer.valueOf((jsonString.getString().length() == 0) ? "0" : jsonString.getString()));
			break;
		case "java.time.LocalDate":
			if (fieldInfo.dateTimeFormatter != null) {
				fieldInfo.field.set(this, LocalDate.parse(jsonString.getString(), fieldInfo.dateTimeFormatter));
			} else {
				fieldInfo.field.set(this, LocalDate.parse(jsonString.getString()));
			}
			break;
		case "java.time.LocalDateTime":
			if (fieldInfo.dateTimeFormatter != null) {
				fieldInfo.field.set(this, LocalDateTime.parse(jsonString.getString(), fieldInfo.dateTimeFormatter));
			} else {
				fieldInfo.field.set(this, LocalDateTime.parse(jsonString.getString()));
			}
			break;
		default:
			if (fieldInfo.enumMap != null) {
				String value = jsonString.getString();
				if (fieldInfo.enumMap.containsKey(value)) {
					fieldInfo.field.set(this, fieldInfo.enumMap.get(value));
				} else {
					logger.error("Unknow enum value  {}  {}", fieldInfo.clazz.getName(), value);
					throw new UnexpectedException("Unknow enum value");
				}
			} else {
				logger.error("Unexptected type {}", toString());
				throw new UnexpectedException("Unexptected type");
			}
		}
	}
	private void setValue(ClassInfo.FieldInfo fieldInfo, boolean value) throws IllegalArgumentException, IllegalAccessException {
		switch(fieldInfo.type) {
		case "boolean":
			fieldInfo.field.set(this, value);
			break;
		default:
			logger.error("Unexptected type {}", toString());
			throw new UnexpectedException("Unexptected type");
		}
	}
	private void setValue(ClassInfo.FieldInfo fieldInfo) throws IllegalArgumentException, IllegalAccessException {
		switch(fieldInfo.type) {
		case "double":
			fieldInfo.field.set(this, 0);
			break;
		case "long":
			fieldInfo.field.set(this, 0);
			break;
		case "int":
			fieldInfo.field.set(this, 0);
			break;
		case "java.time.LocalDateTime":
			fieldInfo.field.set(this, NULL_LOCAL_DATE_TIME);
			break;
		case "java.time.LocalDate":
			fieldInfo.field.set(this, NULL_LOCAL_DATE);
			break;
		case "java.lang.String":
			fieldInfo.field.set(this, "");
			break;
		default:
			if (fieldInfo.field.getType().isPrimitive()) {
				logger.error("Unexpected fiedl type");
				logger.error("  field {}", fieldInfo.name);
				logger.error("  type  {}", fieldInfo.type);
				throw new UnexpectedException("Unexpected fiedl type");
			} else {
				fieldInfo.field.set(this, null);
			}
			break;
		}
	}
	private void setValue(ClassInfo.FieldInfo fieldInfo, JsonArray jsonArray) throws IllegalArgumentException, IllegalAccessException {
		if (!fieldInfo.isArray) {
			logger.error("Field is not array  {}", toString());
			throw new UnexpectedException("Field is not array");
		}
		
		Class<?> componentType = fieldInfo.field.getType().getComponentType();
		String componentTypeName = componentType.getName();
		switch(componentTypeName) {
		case "java.lang.String":
		{
			int jsonArraySize = jsonArray.size();
			String[] value = new String[jsonArray.size()];
			
			for(int i = 0; i < jsonArraySize; i++) {
				JsonValue jsonValue = jsonArray.get(i);
				switch(jsonValue.getValueType()) {
				case STRING:
					value[i] = jsonArray.getString(i);
					break;
				default:
					logger.error("Unexpected json array element type {} {}", jsonValue.getValueType().toString(), toString());
					throw new UnexpectedException("Unexpected json array element type");
				}
			}
			fieldInfo.field.set(this, value);
		}
			break;
		default:
			if (JSONBase.class.isAssignableFrom(componentType)) {
				ClassInfo classInfo = ClassInfo.get(componentType);
				int jsonArraySize = jsonArray.size();
				JSONBase[] value = (JSONBase[])Array.newInstance(componentType, jsonArraySize);
				
				try {
					for(int i = 0; i < jsonArraySize; i++) {
						JsonValue jsonValue = jsonArray.get(i);
						
						switch(jsonValue.getValueType()) {
						case OBJECT:
							value[i] = classInfo.construcor.newInstance(jsonValue.asJsonObject());
							break;
						default:
							logger.error("Unexpected json array element type {} {}", jsonValue.getValueType().toString(), toString());
							throw new UnexpectedException("Unexpected json array element type");
						}
					}
					fieldInfo.field.set(this, value);
				} catch (InstantiationException | InvocationTargetException e) {
					String exceptionName = e.getClass().getSimpleName();
					logger.error("{} {}", exceptionName, e);
					throw new UnexpectedException(exceptionName, e);
				}
			} else {
				logger.error("Unexpected array component type {} {}", componentTypeName, toString());
				throw new UnexpectedException("Unexpected array component type");
			}
		}
	}

	private static Map<String, Long> buildLongMap(JsonObject jsonObject) {
		Map<String, Long> ret = new TreeMap<>();
		
		for(String childKey: jsonObject.keySet()) {
			JsonValue childValue = jsonObject.get(childKey);
			ValueType childValueType = childValue.getValueType();
			
			switch(childValueType) {
			case NUMBER:
			{
				JsonNumber jsonNumber = jsonObject.getJsonNumber(childKey);
				long value = jsonNumber.longValue();
				ret.put(childKey, value);
			}
				break;
			case STRING:
			{
				JsonString jsonString = jsonObject.getJsonString(childKey);
				long value = Long.valueOf((jsonString.getString().length() == 0) ? "0" : jsonString.getString());
				ret.put(childKey, value);
			}
				break;
			default:
				logger.error("Unexptected childValueType {}", childValueType);
				throw new UnexpectedException("Unexptected childValueType");
			}
		}
		return ret;
	}
	private static Map<String, Integer> buildIntegerMap(JsonObject jsonObject) {
		Map<String, Integer> ret = new TreeMap<>();
		
		for(String childKey: jsonObject.keySet()) {
			JsonValue childValue = jsonObject.get(childKey);
			ValueType childValueType = childValue.getValueType();
			
			switch(childValueType) {
			case NUMBER:
			{
				JsonNumber jsonNumber = jsonObject.getJsonNumber(childKey);
				int value = jsonNumber.intValue();
				ret.put(childKey, value);
			}
				break;
			case STRING:
			{
				JsonString jsonString = jsonObject.getJsonString(childKey);
				int value = Integer.valueOf((jsonString.getString().length() == 0) ? "0" : jsonString.getString());
				ret.put(childKey, value);
			}
				break;
			default:
				logger.error("Unexptected childValueType {}", childValueType);
				throw new UnexpectedException("Unexptected childValueType");
			}
		}
		return ret;
	}
	private static Map<String, String> buildStringMap(JsonObject jsonObject) {
		Map<String, String> ret = new TreeMap<>();
		
		for(String childKey: jsonObject.keySet()) {
			JsonValue childValue = jsonObject.get(childKey);
			ValueType childValueType = childValue.getValueType();
			
			switch(childValueType) {
			case NUMBER:
			{
				JsonNumber jsonNumber = jsonObject.getJsonNumber(childKey);
				int value = jsonNumber.intValue();
				ret.put(childKey, Integer.toString(value));
			}
				break;
			case STRING:
			{
				String value = jsonObject.getString(childKey);
				ret.put(childKey, value);
			}
				break;
			default:
				logger.error("Unexptected childValueType {}", childValueType);
				throw new UnexpectedException("Unexptected childValueType");
			}
		}
		return ret;
	}
	private static Map<String, JSONBase> buildBaseMap(JsonObject jsonObject, Class<?> mapValueClass) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		ClassInfo classInfo = ClassInfo.get(mapValueClass);
		
		Map<String, JSONBase> ret = new TreeMap<>();
		
		for(String childKey: jsonObject.keySet()) {
			JsonValue childValue = jsonObject.get(childKey);
			ValueType childValueType = childValue.getValueType();
			
			switch(childValueType) {
			case OBJECT:
			{
				JsonObject jsonObjectValue = jsonObject.getJsonObject(childKey);
				JSONBase value = classInfo.construcor.newInstance(jsonObjectValue);

				ret.put(childKey, value);
			}
				break;
			default:
				logger.error("Unexptected childValueType {}", childValueType);
				throw new UnexpectedException("Unexptected childValueType");
			}
		}
		return ret;
	}

	private void setValue(ClassInfo.FieldInfo fieldInfo, JsonObject jsonObject) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<?> fieldType = fieldInfo.field.getType();
		
		if (JSONBase.class.isAssignableFrom(fieldType)) {
			ClassInfo classInfo = ClassInfo.get(fieldType);
			JSONBase child = classInfo.construcor.newInstance(jsonObject);
//			logger.info("child {}", child.toString());
			
			fieldInfo.field.set(this, child);
		} else {
			String fieldTypeName = fieldType.getName();
			switch(fieldTypeName) {
			case "java.util.Map":
			{
				GenericInfo genericInfo = new GenericInfo(fieldInfo.field);
				if (genericInfo.classArguments.length != 2) {
					logger.error("Unexptected genericInfo.classArguments.length {}", genericInfo.classArguments.length);
					throw new UnexpectedException("Unexptected genericInfo.classArguments.length");
				}
				Class<?> mapKeyClass   = genericInfo.classArguments[0];
				Class<?> mapValueClass = genericInfo.classArguments[1];
											
				String mapKeyClassName   = mapKeyClass.getTypeName();
				String mapValueClassName = mapValueClass.getTypeName();
				
//				logger.info("mapKeyClassName   {}", mapKeyClassName);
//				logger.info("mapValueClassName {}", mapValueClassName);
				
				if (!mapKeyClassName.equals("java.lang.String")) {
					logger.error("Unexptected keyTypeName {}", mapKeyClassName);
					throw new UnexpectedException("Unexptected keyTypeName");
				}
				
				switch(mapValueClassName) {
				case "java.lang.Long":
					fieldInfo.field.set(this, buildLongMap(jsonObject));
					break;
				case "java.lang.Integer":
					fieldInfo.field.set(this, buildIntegerMap(jsonObject));
					break;
				case "java.lang.String":
					fieldInfo.field.set(this, buildStringMap(jsonObject));
					break;
				default:
					// If value extends from Base
					if (JSONBase.class.isAssignableFrom(mapValueClass)) {
						fieldInfo.field.set(this, buildBaseMap(jsonObject, mapValueClass));
					} else {
						logger.error("Unexptected mapValueClassName");
						logger.error("  name              {}", fieldInfo.name);
						logger.error("  type              {}", fieldInfo.type);
						logger.error("  mapKeyClassName   {}", mapKeyClassName);
						logger.error("  mapValueClassName {}", mapValueClassName);
						throw new UnexpectedException("Unexptected mapValueClassName");
					}
				}
			}
				break;
			default:
				logger.error("Unexptected type {}", toString());
				logger.error("fieldTypeName {}", fieldTypeName);
				throw new UnexpectedException("Unexptected type");
			}
		}
	}

	public static <E extends JSONBase> E getObject(String url, Class<E> clazz) {
		ClassInfo classInfo = ClassInfo.get(clazz);

		HttpUtil.Result result = HttpUtil.getInstance().download(url);
		if (result.result == null) {
			logger.error("result.result == null");
			throw new UnexpectedException("result.result == null");
		}
		
		String jsonString = result.result;
//		logger.info("jsonString = {}", jsonString);

		try (JsonReader reader = Json.createReader(new StringReader(jsonString))) {
			// Assume result is only one object
			JsonObject arg = reader.readObject();
			@SuppressWarnings("unchecked")
			E ret = (E)classInfo.construcor.newInstance(arg);
			return ret;
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

	public static <E extends JSONBase> List<E> getArray(String url, Class<E> clazz) {
		ClassInfo classInfo = ClassInfo.get(clazz);

		HttpUtil.Result result = HttpUtil.getInstance().download(url);
		if (result.result == null) {
			logger.error("result.result == null");
			throw new UnexpectedException("result.result == null");
		}
		
		String jsonString = result.result;
//		logger.info("jsonString = {}", jsonString);
		
		try (JsonReader reader = Json.createReader(new StringReader(jsonString))) {
			// Assume result is array
			JsonArray jsonArray = reader.readArray();
			
			int jsonArraySize = jsonArray.size();
			List<E> ret = new ArrayList<>(jsonArraySize);
			
			for(int i = 0; i < jsonArraySize; i++) {
				JsonValue jsonValue = jsonArray.get(i);
				ValueType valueType = jsonValue.getValueType();
				switch (valueType) {
				case OBJECT:
				{
					JsonObject arg = jsonValue.asJsonObject();
					@SuppressWarnings("unchecked")
					E e = (E)classInfo.construcor.newInstance(arg);
					ret.add(e);
				}
					break;
				case NULL:
					// Skip NULL
					break;
				default:
					logger.info("Unexpected valueType  {}  {}  {}", i, valueType, jsonValue);
					throw new UnexpectedException("Unexpected valueType");
				}
			}
			
			return ret;
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

	public static <E extends JSONBase> List<List<E>> getArrayArray(String url, Class<E> clazz) {
		ClassInfo classInfo = ClassInfo.get(clazz);

		HttpUtil.Result result = HttpUtil.getInstance().download(url);
		if (result.result == null) {
			logger.error("result.result == null");
			throw new UnexpectedException("result.result == null");
		}
		
		String jsonString = result.result;
//		logger.info("jsonString = {}", jsonString);
		
		List<List<E>> ret = new ArrayList<>();
		
		try (JsonReader reader = Json.createReader(new StringReader(jsonString))) {
			// Assume result is array in array
			JsonArray jsonArray     = reader.readArray();
			int       jsonArraySize = jsonArray.size();
			
			for(int i = 0; i < jsonArraySize; i++) {
				JsonArray jsonArray2     = jsonArray.getJsonArray(i);
				int       jsonArraySize2 = jsonArray2.size();

				List<E> list = new ArrayList<>();
				
				for(int j = 0; j < jsonArraySize2; j++) {
					JsonObject arg = jsonArray2.getJsonObject(j);
					@SuppressWarnings("unchecked")
					E e = (E)classInfo.construcor.newInstance(arg);
					list.add(e);
				}
				
				ret.add(list);
			}
			
			return ret;
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

	
	//
	// toJSONString
	//
	private static void toJSONStringArray(JsonGenerator gen, Object[] array, String name) throws IllegalArgumentException, IllegalAccessException {
		if (name == null) {
			gen.writeStartArray();
		} else {
			gen.writeStartArray(name);
		}
		
		if (array.length == 0) {
			// Do nothing
		} else {
			for(Object o: array) {
				toJSONStringVariable(gen, o, null);
			}
		}
		
		gen.writeEnd();
	}
	private static void toJSONStringObject(JsonGenerator gen, Object o, String name) throws IllegalArgumentException, IllegalAccessException {
		if (name == null) {
			gen.writeStartObject();
		} else {
			gen.writeStartObject(name);
		}

		Class<?>  clazz = o.getClass();
		ClassInfo classInfo = ClassInfo.get(clazz);

		for(ClassInfo.FieldInfo fieldInfo: classInfo.fieldInfos) {
			if (fieldInfo.ignoreField) continue;
			
			String fieldName  = fieldInfo.jsonName != null ? fieldInfo.jsonName : fieldInfo.name;
			Object fieldValue = fieldInfo.field.get(o);
			
			if (fieldInfo.clazz.equals(Map.class)) {
				GenericInfo genericInfo = new GenericInfo(fieldInfo.field);
				if (genericInfo.classArguments.length != 2) {
					logger.error("Unexptected genericInfo.classArguments.length {}", genericInfo.classArguments.length);
					throw new UnexpectedException("Unexptected genericInfo.classArguments.length");
				}
				Class<?> mapKeyClass   = genericInfo.classArguments[0];
											
				String mapKeyClassName   = mapKeyClass.getTypeName();
				
//				logger.info("mapKeyClassName   {}", mapKeyClassName);
//				logger.info("mapValueClassName {}", mapValueClassName);
				
				if (!mapKeyClassName.equals("java.lang.String")) {
					logger.error("Unexptected keyTypeName {}", mapKeyClassName);
					throw new UnexpectedException("Unexptected keyTypeName");
				}
				
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>)fieldValue;
				
				// output map
				gen.writeStartObject(fieldName);
				for(Map.Entry<String, Object> e: map.entrySet()) {
					String key   = e.getKey();
					Object value = e.getValue();
					
					toJSONStringVariable(gen, value, key);
				}
				gen.writeEnd();

			} else {
				toJSONStringVariable(gen, fieldValue, fieldName);
			}
		}
		
		gen.writeEnd();
	}
	
	/*

	private static void toJSONStringList(JsonGenerator gen, Object o, String name) throws IllegalArgumentException, IllegalAccessException {
		if (o == null) {
			if (name == null) {
				gen.writeNull();
			} else {
				gen.writeNull(name);
			}
		} else {
			List<?> list = (List<?>)o;
			
			if (name == null) {
				gen.writeStartArray();
			} else {
				gen.writeStartArray(name);
			}

			if (list.size() == 0) {
				gen.writeEnd();
			} else {
				for(Object e: list) {
					toJSONStringVariable(gen, e);
				}
			}
			gen.writeEnd();
		}
	}

	 */
	private static void toJSONStringVariable(JsonGenerator gen, Object o, String name) throws IllegalArgumentException, IllegalAccessException {
		if (o == null) {
			if (name == null) {
				gen.writeNull();
			} else {
				gen.writeNull(name);
			}
		} else {
			Class<?> clazz = o.getClass();
			
			if (clazz.isArray()) {
				toJSONStringArray(gen, (Object[])o, name);
			} else if (simpleTypeSet.contains(clazz.getName())) {
				toJSONStringSimpleType(gen, o, name);
			} else if (clazz.equals(String.class)) {
				if (name == null) {
					gen.write((String)o);
				} else {
					gen.write(name, (String)o);
				}
			} else if (clazz.isEnum()) {
				if (name == null) {
					gen.write(o.toString());
				} else {
					gen.write(name, o.toString());
				}
//			} else if (List.class.isAssignableFrom(clazz)) {
//				toJSONStringList(gen, o, name);
			} else {
				toJSONStringObject(gen, o, name);
			}
		}
	}
	
	private static Set<String> simpleTypeSet = new TreeSet<>();
	static {
		simpleTypeSet.add(Boolean.TYPE.getName());
		simpleTypeSet.add(Boolean.class.getName());
		simpleTypeSet.add(Integer.TYPE.getName());
		simpleTypeSet.add(Integer.class.getName());
		simpleTypeSet.add(Long.TYPE.getName());
		simpleTypeSet.add(Long.class.getName());
		simpleTypeSet.add(Double.TYPE.getName());
		simpleTypeSet.add(Double.class.getName());
		simpleTypeSet.add(Float.TYPE.getName());
		simpleTypeSet.add(Float.class.getName());
	}
	private static void toJSONStringSimpleType(JsonGenerator gen, Object o, String name) throws IllegalArgumentException, IllegalAccessException {
		String clazzName = o.getClass().getName();

		switch(clazzName) {
		case "java.lang.Boolean":
		case "boolean":
			if (name == null) {
				gen.write((boolean)o);
			} else {
				gen.write(name, (boolean)o);
			}
			break;
		case "java.lang.Integer":
		case "int":
			if (name == null) {
				gen.write((int)o);
			} else {
				gen.write(name, (int)o);
			}
			break;
		case "java.lang.Long":
		case "long":
			if (name == null) {
				gen.write((long)o);
			} else {
				gen.write(name, (long)o);
			}
			break;
		case "java.lang.Double":
		case "double":
			if (name == null) {
				gen.write((double)o);
			} else {
				gen.write(name, (double)o);
			}
			break;
		case "java.lang.Float":
		case "float":
			if (name == null) {
				gen.write((float)o);
			} else {
				gen.write(name, (float)o);
			}
			break;
		default:
			logger.error("Unexpected type");
			logger.error("  clazzName {}", clazzName);
			logger.error("  name      {}", name);
			throw new UnexpectedException("Unexpected type");
		}
	}

	private static void toJSONStringVariable(JsonGenerator gen, Object o) throws IllegalArgumentException, IllegalAccessException {
		toJSONStringVariable(gen, o, null);
	}
	
	public String toJSONString() {
		StringWriter writer = new StringWriter();
		
		try (JsonGenerator gen = Json.createGenerator(writer)) {
			toJSONStringVariable(gen, this);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	
		return writer.toString();
	}
	
	public static <E extends JSONBase> E getInstance(Class<E> clazz, String jsonString) {
		ClassInfo classInfo = ClassInfo.get(clazz);
		try (JsonReader reader = Json.createReader(new StringReader(jsonString))) {
			// Assume result is only one object
			JsonObject arg = reader.readObject();
			@SuppressWarnings("unchecked")
			E ret = (E)classInfo.construcor.newInstance(arg);
			return ret;
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	public static <E extends JSONBase> List<E> getList(Class<E> clazz, String jsonString) {
		ClassInfo classInfo = ClassInfo.get(clazz);
		try (JsonReader reader = Json.createReader(new StringReader(jsonString))) {
			// Assume result is array
			JsonArray jsonArray = reader.readArray();
			
			int jsonArraySize = jsonArray.size();
			List<E> ret = new ArrayList<>(jsonArraySize);
			
			for(int i = 0; i < jsonArraySize; i++) {
				JsonValue jsonValue = jsonArray.get(i);
				ValueType valueType = jsonValue.getValueType();
				switch (valueType) {
				case OBJECT:
				{
					JsonObject arg = jsonValue.asJsonObject();
					@SuppressWarnings("unchecked")
					E e = (E)classInfo.construcor.newInstance(arg);
					ret.add(e);
				}
					break;
				case NULL:
					// Skip NULL
					break;
				default:
					logger.info("Unexpected valueType  {}  {}  {}", i, valueType, jsonValue);
					throw new UnexpectedException("Unexpected valueType");
				}
			}
			
			return ret;
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

}

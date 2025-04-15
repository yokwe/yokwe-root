package yokwe.util.json;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.stream.JsonGenerator;
import yokwe.util.GenericInfo;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;

public class JSON {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Name {
		String value();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Ignore {
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Optional {
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface DateTimeFormat {
		String value();
	}
	
	
	//
	// unmarshal
	//
	public static <E> E unmarshal(Class<E> clazz, String jsonString) {
		return unmarshal(clazz, new StringReader(jsonString));
	}
	public static <E> E unmarshal(Class<E> clazz, Reader reader) {
		return unmarshal(clazz, getJsonValue(reader));
	}
	private static <E> E unmarshal(Class<E> clazz, JsonValue jsonValue) {
		var typeName = clazz.getTypeName();
		var valueType = jsonValue.getValueType();

		// process null value
		if (valueType == ValueType.NULL) {
			if (clazz.isPrimitive()) {
				logger.error("Unexpected clazz is primitive but jsonValue is NULL");
				logger.error("  clazz      {}", typeName);
				logger.error("  valueType  {}", valueType);
				logger.error("  jsonValue  {}", jsonValue);
				throw new UnexpectedException("Unexpected clazz is primitive but jsonValue is NULL");
			}
			return null;
		}
		
		// enum
		if (clazz.isEnum()) {
			if (valueType == ValueType.STRING) {
				return unmarshalEnum(clazz, jsonValue);
			} else {
				logger.error("Unexpected valueType");
				logger.error("  expect STRING");
				logger.error("  clazz      {}", typeName);
				logger.error("  valueType  {}", valueType);
				logger.error("  jsonValue  {}", jsonValue);
				throw new UnexpectedException("Unexpected valueType");
			}
		}
		
		// array
		if (clazz.isArray()) {
			if (valueType == ValueType.ARRAY) {
				var ret = unmarshalArray(clazz, jsonValue.asJsonArray());
				return (E)ret;
			} else {
				logger.error("Unexpected valueType");
				logger.error("  expect ARRAY");
				logger.error("  clazz      {}", typeName);
				logger.error("  valueType  {}", valueType);
				logger.error("  jsonValue  {}", jsonValue);
				throw new UnexpectedException("Unexpected valueType");
			}
		}
		
		// process common java class
		{
			var function = marshalFunctionMap.get(clazz.getTypeName());
			if (function != null) {
				var o = function.apply(jsonValue);
				@SuppressWarnings("unchecked")
				E ret = (E)o;
				return ret;
			}
		}

		// process object
		if (valueType == ValueType.OBJECT) {
			return unmarshalObject(clazz, jsonValue.asJsonObject());
		} else {
			logger.error("Unexpected valueType");
			logger.error("  expect OBJECT");
			logger.error("  clazz      {}", typeName);
			logger.error("  valueType  {}", valueType);
			logger.error("  jsonValue  {}", jsonValue);
			throw new UnexpectedException("Unexpected valueType");
		}
	}
	
	
	//
	// getList
	//
	public static <E> List<E> getList(Class<E> clazz, String jsonString) {
		return getList(clazz, new StringReader(jsonString));
	}
	public static <E> List<E> getList(Class<E> clazz, Reader reader) {
		var jsonValue = getJsonValue(reader);
		var valueType = jsonValue.getValueType();
		if (valueType == ValueType.ARRAY) {
			return getList(clazz, jsonValue.asJsonArray());
		} else {
			logger.error("Unexpected valueType");
			logger.error("  expect ARRAY");
			logger.error("  valueType {}", valueType);
			logger.error("  jsonValue {}", jsonValue);
			throw new UnexpectedException("Unexpected valueType");
		}
	}
	private static <E> List<E> getList(Class<E> clazz, JsonArray jsonArray) {
		var jsonArraySize = jsonArray.size();
		
		List<E> ret = new ArrayList<>(jsonArraySize);
		for(var i = 0; i < jsonArraySize; i++) {
			ret.add(unmarshal(clazz, jsonArray.get(i)));
		}
		return ret;
	}

	
	//
	// unmarshalObject
	//
	private static <E> E unmarshalObject(Class<E> clazz, JsonObject jsonObject) {
		// sanity check
		if (clazz.isArray()) {
			logger.error("Unexpected clazz is array");
			logger.error("  clazz      {}", clazz.getTypeName());
			throw new UnexpectedException("Unexpected clazz is array");
		}
		if (clazz.isEnum()) {
			logger.error("Unexpected clazz is enum");
			logger.error("  clazz      {}", clazz.getTypeName());
			throw new UnexpectedException("Unexpected clazz is enum");
		}
		
		// order of object field is not significant
		// invoke default constructor of E and set field value from jsonObject
		
		try {
			var fieldInfoArray   = FieldInfo.getFieldInfoArray(clazz);
			
			// sanity check
			{
				var foundError = false;
				
				var jsonObjectKeySet = jsonObject.keySet();				
				for(var fieldInfo: fieldInfoArray) {
					if (fieldInfo.ignore) continue;
					
					if (!jsonObjectKeySet.contains(fieldInfo.jsonName)) {
						if (fieldInfo.optional) {
							// OK
						} else {
							foundError = true;
							logger.error("field not found in jsonObject");
							logger.error("  fieldName  {}", fieldInfo.fieldName);
							logger.error("  jsonName   {}", fieldInfo.jsonName);
							logger.error("  type       {}", fieldInfo.typeName);
							logger.error("  jsonObject {}", jsonObject.toString());
						}
					}
				}
				var fieldNameSet = Arrays.stream(fieldInfoArray).map(o -> o.jsonName).collect(Collectors.toSet());
				for(var jsonName: jsonObjectKeySet) {
					if (!fieldNameSet.contains(jsonName)) {
						foundError = true;
						logger.error("jsonObject jsonName not found in field");
						logger.error("  clazz      {}", clazz.getTypeName());
						logger.error("  jsonName   {}", jsonName);
						logger.error("  jsonObject {}", jsonObject.toString());
					}
				}
				if (foundError) {
					throw new UnexpectedException("found error");
				}
			}
			
			// invoke default constructor
			E ret;
			{
				var constructor = clazz.getDeclaredConstructor();
				if (constructor == null) {
					logger.error("no default constructor");
					logger.error("  {}", clazz.getTypeName());
					throw new UnexpectedException("no default constructor");
				}
				constructor.setAccessible(true); // enable invoke private constructor
				ret = constructor.newInstance();
			}
			
			// set object field using fieldInfoArray
			for(var fieldInfo: fieldInfoArray) {
				if (fieldInfo.ignore) continue;
				
				var key = fieldInfo.jsonName;
				if (jsonObject.containsKey(key)) {
					var jsonValue = jsonObject.get(key);
					Object fieldValue;
					
					// special for LocalDateTime, LocalDate, LocalTime and Map
					if (fieldInfo.type.equals(LocalDateTime.class)) {
						fieldValue = unmarshalLocalDateTime(jsonValue, fieldInfo.dateTimeFormatter);
					} else if (fieldInfo.type.equals(LocalDate.class)) {
						fieldValue = unmarshalLocalDate(jsonValue, fieldInfo.dateTimeFormatter);
					} else if (fieldInfo.type.equals(LocalTime.class)) {
						fieldValue = unmarshalLocalTime(jsonValue, fieldInfo.dateTimeFormatter);
					} else if (fieldInfo.type.equals(java.util.Map.class)) {
						fieldValue = unmarshalMap(fieldInfo.field, jsonValue);
					} else {
						fieldValue = unmarshal(fieldInfo.type, jsonValue);
					}
					
					fieldInfo.field.set(ret, fieldValue);
				}
			}
			
			return ret;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
				InvocationTargetException | NoSuchMethodException | SecurityException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e.toString());
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	private static LocalDateTime unmarshalLocalDateTime(JsonValue jsonValue, DateTimeFormatter dateTimeFormatter) {
		var valueType = jsonValue.getValueType();
		if (valueType == ValueType.STRING) {
			var string    = jsonValueToString(jsonValue);
			var formatter = dateTimeFormatter == null ? DateTimeFormatter.ISO_LOCAL_DATE_TIME : dateTimeFormatter;
			return LocalDateTime.parse(string, formatter);
		} else {
			logger.error("Unexpected valueType");
			logger.error("  expect STRING");
			logger.error("  valueType {}", valueType);
			logger.error("  jsonValue {}", jsonValue);
			throw new UnexpectedException("Unexpected valueType");
		}
	}
	private static LocalDate unmarshalLocalDate(JsonValue jsonValue, DateTimeFormatter dateTimeFormatter) {
		var valueType = jsonValue.getValueType();
		if (valueType == ValueType.STRING) {
			var string    = jsonValueToString(jsonValue);
			var formatter = dateTimeFormatter == null ? DateTimeFormatter.ISO_LOCAL_DATE : dateTimeFormatter;
			return LocalDate.parse(string, formatter);
		} else {
			logger.error("Unexpected valueType");
			logger.error("  expect STRING");
			logger.error("  valueType {}", valueType);
			logger.error("  jsonValue {}", jsonValue);
			throw new UnexpectedException("Unexpected valueType");
		}
	}
	private static LocalTime unmarshalLocalTime(JsonValue jsonValue, DateTimeFormatter dateTimeFormatter) {
		var valueType = jsonValue.getValueType();
		if (valueType == ValueType.STRING) {
			var string    = jsonValueToString(jsonValue);
			var formatter = dateTimeFormatter == null ? DateTimeFormatter.ISO_LOCAL_TIME : dateTimeFormatter;
			return LocalTime.parse(string, formatter);
		} else {
			logger.error("Unexpected valueType");
			logger.error("  expect STRING");
			logger.error("  valueType {}", valueType);
			logger.error("  jsonValue {}", jsonValue);
			throw new UnexpectedException("Unexpected valueType");
		}
	}
	public static Map<String, ?> unmarshalMap(Field field, JsonValue jsonValue) {
		GenericInfo genericInfo = new GenericInfo(field);
		if (genericInfo.classArguments.length != 2) {
			logger.error("Unexptected genericInfo.classArguments.length {}", genericInfo.classArguments.length);
			throw new UnexpectedException("Unexptected genericInfo.classArguments.length");
		}
		Class<?> mapKeyClass   = genericInfo.classArguments[0];
		Class<?> mapValueClass = genericInfo.classArguments[1];
		
		if (!mapKeyClass.equals(String.class)) {
			logger.error("Unexpected map key class");
			logger.error("unexpeced fieldName");
			logger.error("  field  {}", field);
			logger.error("  key    {}", mapKeyClass.getTypeName());
			throw new UnexpectedException("Unexpected map key class");
		}
		
		return unmarshalMap(mapValueClass, jsonValue);
	}
	public static <V> Map<String, V> unmarshalMap(Class<V> mapValueClass, JsonValue jsonValue) {
		if (jsonValue.getValueType() == ValueType.NULL) return null;
		
		var jsonObject = jsonValue.asJsonObject();
		
		var ret = new TreeMap<String, V>();
		
		for(var entry: jsonObject.entrySet()) {
			var key   = entry.getKey();
			var value = entry.getValue();
			
			ret.put(key, unmarshal(mapValueClass, value));
		}

		return ret;
	}
	
	
	private static Map<String, FieldInfo[]> fieldInfoMap = new TreeMap<>();
	private static class FieldInfo {
		static FieldInfo[] getFieldInfoArray(Class<?> clazz) {
			var key = clazz.getTypeName();
			var ret = fieldInfoMap.get(key);
			if (ret != null) return ret;
			
			var list = new ArrayList<FieldInfo>();
			for(var field: clazz.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) continue;
				field.setAccessible(true); // allow access private field
				list.add(new FieldInfo(field));
			}
			var array = list.toArray(FieldInfo[]::new);
			fieldInfoMap.put(key, array);
			return array;
		}
		
		Field             field;
		String            fieldName;
		Class<?>          type; // type of field
		String            typeName;
		String            jsonName;
		boolean           ignore;
		boolean           optional;
		DateTimeFormatter dateTimeFormatter;
		
		FieldInfo(Field field) {
			var jsonName       = field.getDeclaredAnnotation(Name.class);
			var ignore         = field.getDeclaredAnnotation(Ignore.class);
			var optional       = field.getDeclaredAnnotation(Optional.class);
			var dateTimeFormat = field.getDeclaredAnnotation(DateTimeFormat.class);

			this.field             = field;
			this.fieldName         = field.getName();
			this.type              = field.getType();
			this.typeName          = this.type.getTypeName();
			this.jsonName          = (jsonName == null) ? fieldName : jsonName.value();
			this.ignore            = ignore != null;
			this.optional          = optional != null;
			this.dateTimeFormatter = (dateTimeFormat == null) ? null : DateTimeFormatter.ofPattern(dateTimeFormat.value());
		}
		
		public Object get(Object object) {
			try {
				return field.get(object);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e.toString());
				throw new UnexpectedException(exceptionName, e);
			}
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	
	//
	// unmarshalArray
	//
	private static <E> E unmarshalArray(Class<E> clazz, JsonArray jsonArray) {
		// sanity check
		if (!clazz.isArray()) {
			logger.error("Unexpected clazz is not array");
			logger.error("  clazz      {}", clazz.getTypeName());
			throw new UnexpectedException("Unexpected clazz is not array");
		}
		
		var componentType = clazz.getComponentType();
		var size          = jsonArray.size();
		@SuppressWarnings("unchecked")
		E ret = (E)Array.newInstance(componentType, size);
		
		for(int i = 0; i < size; i++) {
			Array.set(ret, i, unmarshal(componentType, jsonArray.get(i)));
		}
		return ret;
	}
	
	
		
	//
	// Functions convert JsonValue to Object
	//
	private static Map<String, Function<JsonValue, Object>> marshalFunctionMap = new TreeMap<>();
	static {
		marshalFunctionMap.put(Boolean.class.getTypeName(),   new MarshalFunctions.BooleanClass());
		marshalFunctionMap.put(Double.class.getTypeName(),    new MarshalFunctions.DoubleClass());
		marshalFunctionMap.put(Float.class.getTypeName(),     new MarshalFunctions.FloatClass());
		marshalFunctionMap.put(Long.class.getTypeName(),      new MarshalFunctions.LongClass());
		marshalFunctionMap.put(Integer.class.getTypeName(),   new MarshalFunctions.IntegerClass());
		marshalFunctionMap.put(Short.class.getTypeName(),     new MarshalFunctions.ShortClass());
		marshalFunctionMap.put(Byte.class.getTypeName(),      new MarshalFunctions.ByteClass());
		marshalFunctionMap.put(Character.class.getTypeName(), new MarshalFunctions.CharacterClass());
		//
		marshalFunctionMap.put(Boolean.TYPE.getTypeName(),   new MarshalFunctions.BooleanClass());
		marshalFunctionMap.put(Double.TYPE.getTypeName(),    new MarshalFunctions.DoubleClass());
		marshalFunctionMap.put(Float.TYPE.getTypeName(),     new MarshalFunctions.FloatClass());
		marshalFunctionMap.put(Long.TYPE.getTypeName(),      new MarshalFunctions.LongClass());
		marshalFunctionMap.put(Integer.TYPE.getTypeName(),   new MarshalFunctions.IntegerClass());
		marshalFunctionMap.put(Short.TYPE.getTypeName(),     new MarshalFunctions.ShortClass());
		marshalFunctionMap.put(Byte.TYPE.getTypeName(),      new MarshalFunctions.ByteClass());
		marshalFunctionMap.put(Character.TYPE.getTypeName(), new MarshalFunctions.CharacterClass());
		//
		marshalFunctionMap.put(String.class.getTypeName(),        new MarshalFunctions.StringClass());
		//
		marshalFunctionMap.put(LocalDateTime.class.getTypeName(), new MarshalFunctions.LocalDateTimeClass());
		marshalFunctionMap.put(LocalDate.class.getTypeName(),     new MarshalFunctions.LocalDateClass());
		marshalFunctionMap.put(LocalTime.class.getTypeName(),     new MarshalFunctions.LocalTimeClass());
		//
		marshalFunctionMap.put(BigDecimal.class.getTypeName(),    new MarshalFunctions.BigDecimalClass());
	}
	private static class MarshalFunctions {
		private static class BooleanClass implements Function<JsonValue, Object> {
			@Override
			public Object apply(JsonValue jsonValue) {
				var valueType = jsonValue.getValueType();
				switch(valueType) {
				case TRUE:  return Boolean.TRUE;
				case FALSE: return Boolean.FALSE;
				default:
					break;
				}
				logger.error("Unexpected valueType");
				logger.error("  expect TRUE or FALSE");
				logger.error("  valueType {}", valueType);
				logger.error("  jsonValue {}", jsonValue);
				throw new UnexpectedException("Unexpected valueType");
			}
		}
		private static class DoubleClass implements Function<JsonValue, Object> {
			@Override
			public Object apply(JsonValue jsonValue) {
				var valueType = jsonValue.getValueType();
				if (valueType == ValueType.NUMBER) return Double.valueOf(jsonValue.toString());
				
				logger.error("Unexpected valueType");
				logger.error("  expect NUMBER");
				logger.error("  valueType {}", valueType);
				logger.error("  jsonValue {}", jsonValue);
				throw new UnexpectedException("Unexpected valueType");
			}
		}
		private static class FloatClass implements Function<JsonValue, Object> {
			@Override
			public Object apply(JsonValue jsonValue) {
				var valueType = jsonValue.getValueType();
				if (valueType == ValueType.NUMBER) return Float.valueOf(jsonValue.toString());
				
				logger.error("Unexpected valueType");
				logger.error("  expect NUMBER");
				logger.error("  valueType {}", valueType);
				logger.error("  jsonValue {}", jsonValue);
				throw new UnexpectedException("Unexpected valueType");
			}
		}
		private static class LongClass implements Function<JsonValue, Object> {
			@Override
			public Object apply(JsonValue jsonValue) {
				var valueType = jsonValue.getValueType();
				if (valueType == ValueType.NUMBER) return Long.valueOf(jsonValue.toString());
				
				logger.error("Unexpected valueType");
				logger.error("  expect NUMBER");
				logger.error("  valueType {}", valueType);
				logger.error("  jsonValue {}", jsonValue);
				throw new UnexpectedException("Unexpected valueType");
			}
		}
		private static class IntegerClass implements Function<JsonValue, Object> {
			@Override
			public Object apply(JsonValue jsonValue) {
				var valueType = jsonValue.getValueType();
				if (valueType == ValueType.NUMBER) return Integer.valueOf(jsonValue.toString());
				
				logger.error("Unexpected valueType");
				logger.error("  expect NUMBER");
				logger.error("  valueType {}", valueType);
				logger.error("  jsonValue {}", jsonValue);
				throw new UnexpectedException("Unexpected valueType");
			}
		}
		private static class ShortClass implements Function<JsonValue, Object> {
			@Override
			public Object apply(JsonValue jsonValue) {
				var valueType = jsonValue.getValueType();
				if (valueType == ValueType.NUMBER) return Short.valueOf(jsonValue.toString());
				
				logger.error("Unexpected valueType");
				logger.error("  expect NUMBER");
				logger.error("  valueType {}", valueType);
				logger.error("  jsonValue {}", jsonValue);
				throw new UnexpectedException("Unexpected valueType");
			}
		}
		private static class ByteClass implements Function<JsonValue, Object> {
			@Override
			public Object apply(JsonValue jsonValue) {
				var valueType = jsonValue.getValueType();
				if (valueType == ValueType.NUMBER) return Byte.valueOf(jsonValue.toString());
				
				logger.error("Unexpected valueType");
				logger.error("  expect NUMBER");
				logger.error("  valueType {}", valueType);
				logger.error("  jsonValue {}", jsonValue);
				throw new UnexpectedException("Unexpected valueType");
			}
		}
		private static class CharacterClass implements Function<JsonValue, Object> {
			@Override
			public Object apply(JsonValue jsonValue) {
				var valueType = jsonValue.getValueType();
				if (valueType == ValueType.STRING)  {
					var string = jsonValueToString(jsonValue);
					if (string.length() != 1) {
						logger.error("Unexpected string");
						logger.error("  expect length of string is 1");
						logger.error("  {}!", string);
						throw new UnexpectedException("Unexpected string");
					}
					char c = string.charAt(0);
					return Character.valueOf(c);
				}
				
				logger.error("Unexpected valueType");
				logger.error("  expect STRING");
				logger.error("  valueType {}", valueType);
				logger.error("  jsonValue {}", jsonValue);
				throw new UnexpectedException("Unexpected valueType");
			}
		}
		//
		private static class StringClass implements Function<JsonValue, Object> {
			@Override
			public Object apply(JsonValue jsonValue) {
				var valueType = jsonValue.getValueType();
				if (valueType == ValueType.STRING) return jsonValueToString(jsonValue);
				
				logger.error("Unexpected valueType");
				logger.error("  expect STRING");
				logger.error("  valueType {}", valueType);
				logger.error("  jsonValue {}", jsonValue);
				throw new UnexpectedException("Unexpected valueType");
			}
		}
		private static class LocalDateTimeClass implements Function<JsonValue, Object> {
			@Override
			public Object apply(JsonValue jsonValue) {
				var valueType = jsonValue.getValueType();
				if (valueType == ValueType.STRING) return LocalDateTime.parse(jsonValueToString(jsonValue));
				
				logger.error("Unexpected valueType");
				logger.error("  expect STRING");
				logger.error("  valueType {}", valueType);
				logger.error("  jsonValue {}", jsonValue);
				throw new UnexpectedException("Unexpected valueType");
			}
		}
		private static class LocalDateClass implements Function<JsonValue, Object> {
			@Override
			public Object apply(JsonValue jsonValue) {
				var valueType = jsonValue.getValueType();
				if (valueType == ValueType.STRING) return LocalDate.parse(jsonValueToString(jsonValue));
				
				logger.error("Unexpected valueType");
				logger.error("  expect STRING");
				logger.error("  valueType {}", valueType);
				logger.error("  jsonValue {}", jsonValue);
				throw new UnexpectedException("Unexpected valueType");
			}
		}
		private static class LocalTimeClass implements Function<JsonValue, Object> {
			@Override
			public Object apply(JsonValue jsonValue) {
				var valueType = jsonValue.getValueType();
				if (valueType == ValueType.STRING) return LocalTime.parse(jsonValueToString(jsonValue));
				
				logger.error("Unexpected valueType");
				logger.error("  expect STRING");
				logger.error("  valueType {}", valueType);
				logger.error("  jsonValue {}", jsonValue);
				throw new UnexpectedException("Unexpected valueType");
			}
		}
		//
		private static class BigDecimalClass implements Function<JsonValue, Object> {
			@Override
			public Object apply(JsonValue jsonValue) {
				var valueType = jsonValue.getValueType();
				if (valueType == ValueType.NUMBER) return new BigDecimal(jsonValue.toString());
				
				logger.error("Unexpected valueType");
				logger.error("  expect NUMBER");
				logger.error("  valueType {}", valueType);
				logger.error("  jsonValue {}", jsonValue);
				throw new UnexpectedException("Unexpected valueType");
			}
		}
	}
	
	
	
	//
	// utility methods
	//
	private static JsonValue getJsonValue(Reader reader) {
		try (JsonReader jsonReader = Json.createReader(reader)) {
			return jsonReader.readValue();
		} catch(JsonException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	private static String jsonValueToString(JsonValue jsonValue) {
		var string = jsonValue.toString();
		if (jsonValue.getValueType() == ValueType.STRING) {			
			// sanity check
			var first = string.charAt(0);
			var last  = string.charAt(string.length() - 1);
			if (first != '"' || last != '"') {
				// unexpected
				logger.error("Unexpected first or last character");
				logger.error("  string {}!", string);
				logger.error("  first  {}", first);
				logger.error("  last   {}", last);
				throw new UnexpectedException("Unexpected first or last character");
			}
			
			// remove first and last character of string
			return string.substring(1, string.length() - 1);
		} else {
			return string;
		}
	}
	
	private static Map<String, Map<String, Object>> enumValueMap = new TreeMap<>();
	private static <E> E unmarshalEnum(Class<?> clazz, JsonValue jsonValue) {
		// sanity check
		if (!clazz.isEnum()) {
			logger.error("Unexpected clazz is not enum");
			logger.error("  clazz      {}", clazz.getTypeName());
			throw new UnexpectedException("Unexpected clazz is not enum");
		}
		
		var typeName = clazz.getTypeName();
		var map = enumValueMap.get(typeName);
		if (map == null) {
			map = new TreeMap<String, Object>();
			for(var e: clazz.getEnumConstants()) {
				map.put(e.toString(), e);
			}
			enumValueMap.put(typeName, map);
		}
		
		var valueType = jsonValue.getValueType();
		if (valueType == ValueType.STRING) {
			var string = jsonValueToString(jsonValue);
			var o = map.get(string);
			if (o != null) {
				@SuppressWarnings("unchecked")
				E ret = (E)o;
				return ret;
			}
			logger.error("Unexpected enum string");
			logger.error("  clazz     {}", typeName);
			logger.error("  string    {}!", string);
			logger.error("  map       {}!", map.keySet());
			throw new UnexpectedException("Unexpected enum string");
		} else {
			logger.error("Unexpected valueType");
			logger.error("  expect STRING");
			logger.error("  valueType {}", valueType);
			logger.error("  jsonValue {}", jsonValue);
			throw new UnexpectedException("Unexpected valueType");
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
		var fieldInfoArray = FieldInfo.getFieldInfoArray(clazz);

		for(FieldInfo fieldInfo: fieldInfoArray) {
			if (fieldInfo.ignore) continue;
			
			String fieldName  = fieldInfo.jsonName != null ? fieldInfo.jsonName : fieldInfo.fieldName;
			Object fieldValue = fieldInfo.field.get(o);
			
			if (fieldInfo.type.equals(Map.class)) {
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
			} else if (clazz.equals(BigDecimal.class)) {
				BigDecimal value = (BigDecimal)o;
				if (name == null) {
					gen.write(value.toPlainString());
				} else {
					gen.write(name, value.toPlainString());
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
		simpleTypeSet.add(Double.TYPE.getName());
		simpleTypeSet.add(Double.class.getName());
		simpleTypeSet.add(Float.TYPE.getName());
		simpleTypeSet.add(Float.class.getName());
		simpleTypeSet.add(Long.TYPE.getName());
		simpleTypeSet.add(Long.class.getName());
		simpleTypeSet.add(Integer.TYPE.getName());
		simpleTypeSet.add(Integer.class.getName());
		simpleTypeSet.add(Short.TYPE.getName());
		simpleTypeSet.add(Short.class.getName());
		simpleTypeSet.add(Byte.TYPE.getName());
		simpleTypeSet.add(Byte.class.getName());
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
		case "java.lang.Long":
		case "long":
			if (name == null) {
				gen.write((long)o);
			} else {
				gen.write(name, (long)o);
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
		case "java.lang.Short":
		case "short":
			if (name == null) {
				gen.write((short)o);
			} else {
				gen.write(name, (short)o);
			}
			break;
		case "java.lang.Byte":
		case "byte":
			if (name == null) {
				gen.write((byte)o);
			} else {
				gen.write(name, (byte)o);
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
	
	public static String toJSONString(Object object) {
		StringWriter writer = new StringWriter();
		
		try (JsonGenerator gen = Json.createGenerator(writer)) {
			toJSONStringVariable(gen, object);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	
		return writer.toString();
	}
	
	
	//
	// unmarshal
	//
	public static final class Options {
		private List<String> includePackageList;
		private List<String> excludePackageList;
		
		private Options() {
			this.includePackageList = new ArrayList<>();
			this.excludePackageList = new ArrayList<>();
		}
		
		private Options(Options that) {
			this.includePackageList = that.includePackageList;
			this.excludePackageList = that.excludePackageList;
		}
		public Options includePackage(String... stringArray) {
			for(var string: stringArray) {
				includePackageList.add(string);
			}
			return this;
		}
		public Options excludePackage(String... stringArray) {
			for(var string: stringArray) {
				excludePackageList.add(string);
			}
			return this;
		}
		
		public boolean matchIncludePackage(String typeName) {
			for(var packagePrefix: includePackageList) {
				if (typeName.startsWith(packagePrefix)) return true;
			}
			return false;
		}
		public boolean matchExcludePackage(String typeName) {
			for(var packagePrefix: excludePackageList) {
				if (typeName.startsWith(packagePrefix)) return true;
			}
			return false;
		}
	}
	
	public static final Options DEFAULT_OPTIONS;
	static {
		DEFAULT_OPTIONS = new Options();
		DEFAULT_OPTIONS.excludePackage("java.", "javax.", "jdk.", "sun.", "com.sun.");
	}

	public static String unmarshal(Object object) {
		if (object == null) return "null";
		
		var clazz = object.getClass();
		if (clazz.isEnum()) return object.toString();
		if (clazz.isArray()) return unmarshalArray(object);
		
		// process common java class
		var typeName  = clazz.getTypeName();
		var function  = unmarshalFunctionMap.get(typeName);
		if (function != null) return function.apply(object);
		
		if (DEFAULT_OPTIONS.matchIncludePackage(typeName)) return unmarshalObject(object);
		if (DEFAULT_OPTIONS.matchExcludePackage(typeName)) return "\"" + escapeString(object.toString()) + "\""; 
		
		return unmarshalObject(object);
	}
	
	private static String unmarshalArray(Object object) {
		var stringJoiner = new StringJoiner(", ", "[", "]");
		
		int arrayLength = Array.getLength(object);
		for(var i = 0; i < arrayLength; i++) {
			var element = Array.get(object, i);
			stringJoiner.add(unmarshal(element));
		}
		
		return stringJoiner.toString();
	}
	private static String unmarshalObject(Object object) {
		var stringJoiner = new StringJoiner(", ", "{", "}");
		
		var clazz = object.getClass();
		var fieldInfoArray   = FieldInfo.getFieldInfoArray(clazz);

		for(var fieldInfo: fieldInfoArray) {
			var name  = fieldInfo.jsonName;
			var value = unmarshal(fieldInfo.get(object));
			stringJoiner.add(name + ": " + value);
		}
		return stringJoiner.toString();
	}
	
	
	private static Map<Character, String>escapeMap = new TreeMap<>();
	static {
		escapeMap.put('"', "\"");
		escapeMap.put('\\', "\\");
		escapeMap.put('/', "\\/");
		escapeMap.put('\b', "\\b");
		escapeMap.put('\f', "\\f");
		escapeMap.put('\n', "\\n");
		escapeMap.put('\r', "\\r");
		escapeMap.put('\t', "\\t");
	}
	private static String escapeString(String string) {
		var sb = new StringBuilder();
		for(int i = 0; i < string.length(); i++) {
			var c = string.charAt(i);
			var s = escapeMap.get(c);
			if (s != null) {
				sb.append(s);
			} else {
				if (c < 0x020) {
					sb.append(String.format("\\u%04x", c));
				} else {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	
	private static Map<String, Function<Object, String>> unmarshalFunctionMap = new TreeMap<>();
	static {
		unmarshalFunctionMap.put(Boolean.class.getTypeName(),   new UnmarshalFunctions.BooleanClass());
		unmarshalFunctionMap.put(Double.class.getTypeName(),    new UnmarshalFunctions.DoubleClass());
		unmarshalFunctionMap.put(Float.class.getTypeName(),     new UnmarshalFunctions.FloatClass());
		unmarshalFunctionMap.put(Long.class.getTypeName(),      new UnmarshalFunctions.LongClass());
		unmarshalFunctionMap.put(Integer.class.getTypeName(),   new UnmarshalFunctions.IntegerClass());
		unmarshalFunctionMap.put(Short.class.getTypeName(),     new UnmarshalFunctions.ShortClass());
		unmarshalFunctionMap.put(Byte.class.getTypeName(),      new UnmarshalFunctions.ByteClass());
		unmarshalFunctionMap.put(Character.class.getTypeName(), new UnmarshalFunctions.CharacterClass());
		//
		unmarshalFunctionMap.put(Boolean.TYPE.getTypeName(),   new UnmarshalFunctions.BooleanClass());
		unmarshalFunctionMap.put(Double.TYPE.getTypeName(),    new UnmarshalFunctions.DoubleClass());
		unmarshalFunctionMap.put(Float.TYPE.getTypeName(),     new UnmarshalFunctions.FloatClass());
		unmarshalFunctionMap.put(Long.TYPE.getTypeName(),      new UnmarshalFunctions.LongClass());
		unmarshalFunctionMap.put(Integer.TYPE.getTypeName(),   new UnmarshalFunctions.IntegerClass());
		unmarshalFunctionMap.put(Short.TYPE.getTypeName(),     new UnmarshalFunctions.ShortClass());
		unmarshalFunctionMap.put(Byte.TYPE.getTypeName(),      new UnmarshalFunctions.ByteClass());
		unmarshalFunctionMap.put(Character.TYPE.getTypeName(), new UnmarshalFunctions.CharacterClass());
		//
		unmarshalFunctionMap.put(String.class.getTypeName(),        new UnmarshalFunctions.StringClass());
		//
		unmarshalFunctionMap.put(BigDecimal.class.getTypeName(),    new UnmarshalFunctions.BigDecimalClass());
	}
	private static class UnmarshalFunctions {
		private static class BooleanClass implements Function<Object, String> {
			@Override
			public String apply(Object object) {
				var clazz = object.getClass();
				if (clazz.equals(Boolean.class)) {
					var value = (Boolean)object;
					return Boolean.toString(value);
				}
				logger.error("Unexpected object class");
				logger.error("  expect Boolean");
				logger.error("  clazz  {}", clazz.getTypeName());
				logger.error("  object {}", object.toString());
				throw new UnexpectedException("Unexpected object class");
			}
		}
		private static class DoubleClass implements Function<Object, String> {
			@Override
			public String apply(Object object) {
				var clazz = object.getClass();
				if (clazz.equals(Double.class)) {
					var value = (Double)object;
					return Double.toString(value);
				}
				logger.error("Unexpected object class");
				logger.error("  expect Double");
				logger.error("  clazz  {}", clazz.getTypeName());
				logger.error("  object {}", object.toString());
				throw new UnexpectedException("Unexpected object class");
			}
		}
		private static class FloatClass implements Function<Object, String> {
			@Override
			public String apply(Object object) {
				var clazz = object.getClass();
				if (clazz.equals(Float.class)) {
					var value = (Float)object;
					return Float.toString(value);
				}
				logger.error("Unexpected object class");
				logger.error("  expect Float");
				logger.error("  clazz  {}", clazz.getTypeName());
				logger.error("  object {}", object.toString());
				throw new UnexpectedException("Unexpected object class");
			}
		}
		private static class LongClass implements Function<Object, String> {
			@Override
			public String apply(Object object) {
				var clazz = object.getClass();
				if (clazz.equals(Long.class)) {
					var value = (Long)object;
					return Long.toString(value);
				}
				logger.error("Unexpected object class");
				logger.error("  expect Long");
				logger.error("  clazz  {}", clazz.getTypeName());
				logger.error("  object {}", object.toString());
				throw new UnexpectedException("Unexpected object class");
			}
		}
		private static class IntegerClass implements Function<Object, String> {
			@Override
			public String apply(Object object) {
				var clazz = object.getClass();
				if (clazz.equals(Integer.class)) {
					var value = (Integer)object;
					return Integer.toString(value);
				}
				logger.error("Unexpected object class");
				logger.error("  expect Integer");
				logger.error("  clazz  {}", clazz.getTypeName());
				logger.error("  object {}", object.toString());
				throw new UnexpectedException("Unexpected object class");
			}
		}
		private static class ShortClass implements Function<Object, String> {
			@Override
			public String apply(Object object) {
				var clazz = object.getClass();
				if (clazz.equals(Short.class)) {
					var value = (Short)object;
					return Short.toString(value);
				}
				logger.error("Unexpected object class");
				logger.error("  expect Short");
				logger.error("  clazz  {}", clazz.getTypeName());
				logger.error("  object {}", object.toString());
				throw new UnexpectedException("Unexpected object class");
			}
		}
		private static class ByteClass implements Function<Object, String> {
			@Override
			public String apply(Object object) {
				var clazz = object.getClass();
				if (clazz.equals(Byte.class)) {
					var value = (Byte)object;
					return Byte.toString(value);
				}
				logger.error("Unexpected object class");
				logger.error("  expect Byte");
				logger.error("  clazz  {}", clazz.getTypeName());
				logger.error("  object {}", object.toString());
				throw new UnexpectedException("Unexpected object class");
			}
		}
		private static class CharacterClass implements Function<Object, String> {
			@Override
			public String apply(Object object) {
				var clazz = object.getClass();
				if (clazz.equals(Character.class)) {
					var value = (Character)object;
					var string = String.valueOf(value);
					return "\"" + escapeString(string) + "\""; 
				}
				logger.error("Unexpected object class");
				logger.error("  expect Character");
				logger.error("  clazz  {}", clazz.getTypeName());
				logger.error("  object {}", object.toString());
				throw new UnexpectedException("Unexpected object class");
			}
		}
		private static class StringClass implements Function<Object, String> {
			@Override
			public String apply(Object object) {
				var clazz = object.getClass();
				if (clazz.equals(String.class)) {
					var value = (String)object;
					return "\"" + escapeString(value) + "\"";
				}
				logger.error("Unexpected object class");
				logger.error("  expect String");
				logger.error("  clazz  {}", clazz.getTypeName());
				logger.error("  object {}", object.toString());
				throw new UnexpectedException("Unexpected object class");
			}
		}
		private static class BigDecimalClass implements Function<Object, String> {
			@Override
			public String apply(Object object) {
				var clazz = object.getClass();
				if (clazz.equals(BigDecimal.class)) {
					var value = (BigDecimal)object;
					return value.toPlainString();
				}
				logger.error("Unexpected object class");
				logger.error("  expect BigDecimal");
				logger.error("  clazz  {}", clazz.getTypeName());
				logger.error("  object {}", object.toString());
				throw new UnexpectedException("Unexpected object class");
			}
		}
	}
}

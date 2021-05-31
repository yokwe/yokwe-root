package yokwe.stock.jp.xbrl.tdnet.report;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import yokwe.stock.jp.xbrl.tdnet.inline.BooleanValue;
import yokwe.stock.jp.xbrl.tdnet.inline.Context;
import yokwe.stock.jp.xbrl.tdnet.inline.DateValue;
import yokwe.stock.jp.xbrl.tdnet.inline.Document;
import yokwe.stock.jp.xbrl.tdnet.inline.InlineXBRL;
import yokwe.stock.jp.xbrl.tdnet.inline.NumberValue;
import yokwe.stock.jp.xbrl.tdnet.inline.StringValue;
import yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_ED_T_LABEL;
import yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_RE_T_LABEL;
import yokwe.util.UnexpectedException;
import yokwe.util.xml.QValue;

public abstract class AbstractReport {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractReport.class);

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface TSE_ED {
		TSE_ED_T_LABEL label();
		Context[]      contextIncludeAll()   default {};
		Context[]      contextExcludeAny()   default {};
		boolean        acceptNullOrEmpty()   default false;
	}
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface TSE_RE {
		TSE_RE_T_LABEL label();
		Context[]      contextIncludeAll()   default {};
		Context[]      contextExcludeAny()   default {};
		boolean        acceptNullOrEmpty()   default false;
	}
	// type of field can be InlineXBRL, String, Boolean, BigDecimal, boolean, int, long, float, double
	
	
	private static class ClassInfo {
		static Map<String, ClassInfo> cache = new TreeMap<>();
		
		final String          className;
		final List<FieldInfo> fieldInfoList;
		// FIXME Is filename really necessary?
		final Field           filename;  
		
		static ClassInfo get(Class<? extends AbstractReport> clazz) {
			String className = clazz.getName();
			if (cache.containsKey(className)) {
				return cache.get(className);
			} else {
				ClassInfo classInfo = new ClassInfo(clazz);
				cache.put(className, classInfo);
				return classInfo;
			}
		}
		
		ClassInfo(Class<? extends AbstractReport> clazz) {
			this.className     = clazz.getName();
			this.fieldInfoList = new ArrayList<>();
			
			for(Field field: clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(TSE_ED.class)) {
					TSE_ED annotation = field.getDeclaredAnnotation(TSE_ED.class);
					FieldInfo fieldInfo = new FieldInfo(field, annotation);
					
					// Sanity check
					{
						String   fieldName = fieldInfo.fieldName;
						Class<?> fieldType = fieldInfo.fieldType;
						int      modifiers = field.getModifiers();
						
						if (Modifier.isStatic(modifiers)) {
							logger.error("Unexpected modifiers static  {} {}", className, fieldName);
							throw new UnexpectedException("Unexpected modifiers static");
						}
						if (Modifier.isFinal(modifiers)) {
							logger.error("Unexpected modifiers final  {} {}", className, fieldName);
							throw new UnexpectedException("Unexpected modifiers final");
						}
						if (!Modifier.isPublic(modifiers)) {
							logger.error("Unexpected modifiers not public  {} {}", className, fieldName);
							throw new UnexpectedException("Unexpected modifiers not public");
						}
						
						if (fieldType.isArray()) {
							logger.error("Unexpected field is array  {} {}", className, fieldName);
							throw new UnexpectedException("Unexpected field is array");
						}
					}

					fieldInfoList.add(fieldInfo);
				}
				
				if (field.isAnnotationPresent(TSE_RE.class)) {
					TSE_RE annotation = field.getDeclaredAnnotation(TSE_RE.class);
					FieldInfo fieldInfo = new FieldInfo(field, annotation);
					
					// Sanity check
					{
						String   fieldName = fieldInfo.fieldName;
						Class<?> fieldType = fieldInfo.fieldType;
						int      modifiers = field.getModifiers();
						
						if (Modifier.isStatic(modifiers)) {
							logger.error("Unexpected modifiers static  {} {}", className, fieldName);
							throw new UnexpectedException("Unexpected modifiers static");
						}
						if (Modifier.isFinal(modifiers)) {
							logger.error("Unexpected modifiers final  {} {}", className, fieldName);
							throw new UnexpectedException("Unexpected modifiers final");
						}
						if (!Modifier.isPublic(modifiers)) {
							logger.error("Unexpected modifiers not public  {} {}", className, fieldName);
							throw new UnexpectedException("Unexpected modifiers not public");
						}
						
						if (fieldType.isArray()) {
							logger.error("Unexpected field is array  {} {}", className, fieldName);
							throw new UnexpectedException("Unexpected field is array");
						}
					}

					fieldInfoList.add(fieldInfo);
				}

			}
			
			// FIXME Is filename really necessary?
			{
				Field field = null;
				try {
					field = clazz.getDeclaredField("filename");
					int modifiers = field.getModifiers();
					if ((!Modifier.isStatic(modifiers)) && Modifier.isPublic(modifiers)) {
						// accept this field
					} else {
						// reject this field
						field = null;
					}
				} catch (NoSuchFieldException e) {
					//
				} catch (SecurityException e) {
					String exceptionName = e.getClass().getSimpleName();
					logger.error("{} {}", exceptionName, e);
					throw new UnexpectedException(exceptionName, e);
				}
				this.filename = field;
			}

		}
	}
	
	private static class FieldInfo {
		final Field    field;
		final String   fieldName;
		final Class<?> fieldType;
		final String   fieldTypeName;
		
		final QValue    qName;
		final Context[] contextIncludeAll;
		final Context[] contextExcludeAny;
		final boolean   acceptNullOrEmpty;

		FieldInfo(Field field, TSE_ED annotation) {
			this.field            = field;
			this.fieldName        = field.getName();
			this.fieldType        = field.getType();
			this.fieldTypeName    = fieldType.getName();
			
			this.qName             = annotation.label().qName;
			this.contextIncludeAll = annotation.contextIncludeAll();
			this.contextExcludeAny = annotation.contextExcludeAny();
			this.acceptNullOrEmpty = annotation.acceptNullOrEmpty();
		}
		FieldInfo(Field field, TSE_RE annotation) {
			this.field            = field;
			this.fieldName        = field.getName();
			this.fieldType        = field.getType();
			this.fieldTypeName    = fieldType.getName();
			
			this.qName             = annotation.label().qName;
			this.contextIncludeAll = annotation.contextIncludeAll();
			this.contextExcludeAny = annotation.contextExcludeAny();
			this.acceptNullOrEmpty = annotation.acceptNullOrEmpty();
		}
	}
	
	private void assignField(FieldInfo fieldInfo, StringValue value) throws IllegalArgumentException, IllegalAccessException {
		final Field  field         = fieldInfo.field;
		final String fieldName     = fieldInfo.fieldName;
		final String fieldTypeName = fieldInfo.fieldTypeName;
		
		switch(fieldTypeName) {
		case "yokwe.security.japan.xbrl.InlineXBRL":
			field.set(this, (InlineXBRL)value);
			break;
		case "java.lang.String":
			field.set(this, value.stringValue);
			break;
		default:
			logger.error("Unexpected field type");
			logger.error("   fieldName     {}", fieldName);
			logger.error("   fieldTypeName {}", fieldTypeName);
			throw new UnexpectedException("Unexpected field type");
		}
	}
	private void assignField(FieldInfo fieldInfo, BooleanValue value) throws IllegalArgumentException, IllegalAccessException {
		final Field  field         = fieldInfo.field;
		final String fieldName     = fieldInfo.fieldName;
		final String fieldTypeName = fieldInfo.fieldTypeName;
		
		switch(fieldTypeName) {
		case "yokwe.security.japan.xbrl.InlineXBRL":
			field.set(this, (InlineXBRL)value);
			break;
		case "boolean":
		case "java.lang.Boolean":
			field.set(this, value.booleanValue);
			break;
		default:
			logger.error("Unexpected field type");
			logger.error("   fieldName     {}", fieldName);
			logger.error("   fieldTypeName {}", fieldTypeName);
			throw new UnexpectedException("Unexpected field type");
		}
	}
	private void assignField(FieldInfo fieldInfo, NumberValue value) throws IllegalArgumentException, IllegalAccessException {
		final Field  field         = fieldInfo.field;
		final String fieldName     = fieldInfo.fieldName;
		final String fieldTypeName = fieldInfo.fieldTypeName;

		switch(fieldTypeName) {
		case "yokwe.security.japan.xbrl.InlineXBRL":
			field.set(this, (InlineXBRL)value);
			break;
		case "java.math.BigDecimal":
			field.set(this, value.numberValue);
			break;
		// FLOAT
		case "float":
		case "java.lang.Float":
		{
			float floatValue = value.numberValue.floatValue();
			if (floatValue == Float.POSITIVE_INFINITY || floatValue == Float.POSITIVE_INFINITY) {
				logger.error("Float value is infinity");
				logger.error("   fieldName     {}", fieldName);
				logger.error("   value         {}", value.numberValue);
				throw new UnexpectedException("Unexpected field type");
			}
			if (floatValue == Float.NaN) {
				logger.error("Float value is not a number");
				logger.error("   fieldName     {}", fieldName);
				logger.error("   value         {}", value.numberValue);
				throw new UnexpectedException("Unexpected field type");
			}
			
			field.set(this, floatValue);
		}
			break;
		// DOUBLE
		case "double":
		case "java.lang.Double":
		{
			double doubleValue = value.numberValue.doubleValue();
			if (doubleValue == Double.POSITIVE_INFINITY || doubleValue == Double.POSITIVE_INFINITY) {
				logger.error("Float value overflow");
				logger.error("   fieldName     {}", fieldName);
				logger.error("   value         {}", value.numberValue);
				throw new UnexpectedException("Unexpected field type");
			}
			if (doubleValue == Double.NaN) {
				logger.error("Float value is not a number");
				logger.error("   fieldName     {}", fieldName);
				logger.error("   value         {}", value.numberValue);
				throw new UnexpectedException("Unexpected field type");
			}
			field.set(this, doubleValue);
		}
			break;
		// INT INTEGER
		case "int":
		case "java.lang.Integer":
		{
			int intValue = value.numberValue.intValueExact();
			field.set(this, intValue);
		}
			break;

		// LONG
		case "long":
		case "java.lang.Long":
		{
			long longValue = value.numberValue.longValueExact();
			field.set(this, longValue);
		}
			break;

		default:
			logger.error("Unexpected field type");
			logger.error("   fieldName     {}", fieldName);
			logger.error("   fieldTypeName {}", fieldTypeName);
			throw new UnexpectedException("Unexpected field type");
		}
	}
	private void assignField(FieldInfo fieldInfo, DateValue value) throws IllegalArgumentException, IllegalAccessException {
		final Field  field         = fieldInfo.field;
		final String fieldName     = fieldInfo.fieldName;
		final String fieldTypeName = fieldInfo.fieldTypeName;

		switch(fieldTypeName) {
		case "yokwe.security.japan.xbrl.InlineXBRL":
			field.set(this, (InlineXBRL)value);
			break;
		case "java.lang.String":
			field.set(this, value.dateValue.toString());
			break;
		case "java.time.LocalDate":
			field.set(this, value.dateValue);
			break;
		default:
			logger.error("Unexpected field type");
			logger.error("   fieldName     {}", fieldName);
			logger.error("   fieldTypeName {}", fieldTypeName);
			throw new UnexpectedException("Unexpected field type");
		}
	}
	private void assignFieldZeroOrEmptyString(FieldInfo fieldInfo) throws IllegalArgumentException, IllegalAccessException {
		final Field  field         = fieldInfo.field;
		final String fieldName     = fieldInfo.fieldName;
		final String fieldTypeName = fieldInfo.fieldTypeName;
		
		switch(fieldTypeName) {
		case "java.lang.String":
			field.set(this, "");
			break;
		
		// BigDecimal
		case "java.math.BigDecimal":
			field.set(this, BigDecimal.ZERO);
			break;
			
		// FLOAT
		case "float":
		case "java.lang.Float":
			field.set(this, (float)0);
			break;
		// DOUBLE
		case "double":
		case "java.lang.Double":
			field.set(this, (double)0);
			break;
		// INT INTEGER
		case "int":
		case "java.lang.Integer":
			field.set(this, (int)0);
			break;

		// LONG
		case "long":
		case "java.lang.Long":
			field.set(this, (long)0);
			break;

//		case "java.time.LocalDate":
//			field.set(this, null);
//			break;
//		case "yokwe.security.japan.xbrl.InlineXBRL":
//			field.set(this, null);
//			break;
			
		default:
			logger.error("Unexpected field type");
			logger.error("   fieldName     {}", fieldName);
			logger.error("   fieldTypeName {}", fieldTypeName);
			throw new UnexpectedException("Unexpected field type");
		}
	}

	
	protected void init(Document ixDoc) {
		// use reflection to initialize annotated variable in class
		ClassInfo classInfo = ClassInfo.get(this.getClass());
		
		// FIXME Is filename really necessary?
		if (classInfo.filename != null) {
			try {
				classInfo.filename.set(this, ixDoc.filename);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}
		}
		
		for(FieldInfo fieldInfo: classInfo.fieldInfoList) {
			final QValue    qName             = fieldInfo.qName;
			final Context[] contextIncludeAll = fieldInfo.contextIncludeAll;
			final Context[] contextExcludeAny = fieldInfo.contextExcludeAny;
			final boolean   acceptNullOrEmpty = fieldInfo.acceptNullOrEmpty;
			
			List<InlineXBRL> list = ixDoc.getStream(qName).filter(InlineXBRL.contextIncludeAll(contextIncludeAll)).filter(InlineXBRL.contextExcludeAny(contextExcludeAny)).collect(Collectors.toList());
			int size = list.size();
			
			try {
				if (size == 0) {
					if (acceptNullOrEmpty) {
						assignFieldZeroOrEmptyString(fieldInfo);
					} else {
						// doesn't exist
						logger.error("No matching entry");
						logger.error("   filename          {}", ixDoc.filename);
						logger.error("   fieldName         {}", fieldInfo.fieldName);
						logger.error("   namespace         {}", qName.namespace);
						logger.error("   name              {}", qName.value);
						logger.error("   contextIncludeAll {}", Arrays.asList(contextIncludeAll));
						logger.error("   contextExcludeAny {}", Arrays.asList(contextExcludeAny));
						for(InlineXBRL e: ixDoc.getList(qName)) {
							logger.error("   ixDoc {}", e);
						}
						throw new UnexpectedException("No matching entry");
					}
				} else if (list.size() == 1) {
					InlineXBRL ix = list.get(0);
					if (ix.isNull) {
						if (acceptNullOrEmpty) {
							assignFieldZeroOrEmptyString(fieldInfo);
						} else {
							logger.error("Entry is null");
							logger.error("   filename          {}", ixDoc.filename);
							logger.error("   fieldName         {}", fieldInfo.fieldName);
							logger.error("   namespace         {}", qName.namespace);
							logger.error("   name              {}", qName.value);
							logger.error("   contextIncludeAll {}", Arrays.asList(contextIncludeAll));
							logger.error("   contextExcludeAny {}", Arrays.asList(contextExcludeAny));
							for(InlineXBRL e: ixDoc.getList(qName)) {
								logger.error("   ixDoc {}", e);
							}
							throw new UnexpectedException("Entry is null");
						}
					} else {
						// not null
						switch(ix.kind) {
						case STRING:
							assignField(fieldInfo, (StringValue)ix);
							break;
						case BOOLEAN:
							assignField(fieldInfo, (BooleanValue)ix);
							break;
						case NUMBER:
							assignField(fieldInfo, (NumberValue)ix);
							break;
						case DATE:
							assignField(fieldInfo, (DateValue)ix);
							break;
						default:
							logger.error("Unexpected kind");
							logger.error("   ix {}", ix);
							throw new UnexpectedException("Unexpected kind");
						}
					}
				} else {
					// multiple hit
					{
						int countNotNull = 0;
						InlineXBRL ix = null;
						for(InlineXBRL e: list) {
							if (!e.isNull) {
								ix = e;
								countNotNull++;
							}
						}
						switch(countNotNull) {
						case 0:
							if (acceptNullOrEmpty) {
								assignFieldZeroOrEmptyString(fieldInfo);
							} else {
								logger.error("Entry is null");
								logger.error("   filename          {}", ixDoc.filename);
								logger.error("   fieldName         {}", fieldInfo.fieldName);
								logger.error("   namespace         {}", qName.namespace);
								logger.error("   name              {}", qName.value);
								logger.error("   contextIncludeAll {}", Arrays.asList(contextIncludeAll));
								logger.error("   contextExcludeAny {}", Arrays.asList(contextExcludeAny));
								for(InlineXBRL e: list) {
									logger.error("   list  {}", e);
								}
								for(InlineXBRL e: ixDoc.getList(qName)) {
									logger.error("   ixDoc {}", e);
								}
								throw new UnexpectedException("Entry is null");
							}
							break;
						case 1:
							switch(ix.kind) {
							case STRING:
								assignField(fieldInfo, (StringValue)ix);
								break;
							case BOOLEAN:
								assignField(fieldInfo, (BooleanValue)ix);
								break;
							case NUMBER:
								assignField(fieldInfo, (NumberValue)ix);
								break;
							case DATE:
								assignField(fieldInfo, (DateValue)ix);
								break;
							default:
								logger.error("Unexpected kind");
								logger.error("   ix {}", ix);
								throw new UnexpectedException("Unexpected kind");
							}
							break;
						default:
							logger.error("More than one matching entry");
							logger.error("   filename          {}", ixDoc.filename);
							logger.error("   fieldName         {}", fieldInfo.fieldName);
							logger.error("   namespace         {}", qName.namespace);
							logger.error("   name              {}", qName.value);
							logger.error("   contextIncludeAll {}", Arrays.asList(contextIncludeAll));
							logger.error("   contextExcludeAny {}", Arrays.asList(contextExcludeAny));
							for(InlineXBRL e: list) {
								logger.error("   list  {}", e);
							}
							for(InlineXBRL e: ixDoc.getList(qName)) {
								logger.error("   ixDoc {}", e);
							}
							throw new UnexpectedException("More than one matching entry");
						}
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}
		}
	}

	public static <E extends AbstractReport> E getInstance(Class<E> clazz, Document ixDoc) {
		try {
			E ret = clazz.getDeclaredConstructor().newInstance();
			ret.init(ixDoc);
			return ret;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
}

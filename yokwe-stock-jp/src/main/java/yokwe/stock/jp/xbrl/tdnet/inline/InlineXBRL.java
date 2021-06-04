package yokwe.stock.jp.xbrl.tdnet.inline;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import yokwe.stock.jp.xbrl.XBRL;
import yokwe.stock.jp.xbrl.XML;
import yokwe.util.UnexpectedException;
import yokwe.util.xml.Element;
import yokwe.util.xml.QValue;

public abstract class InlineXBRL {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InlineXBRL.class);

	public enum Kind {
		STRING, BOOLEAN, DATE, NUMBER
	}
	
	private interface Builder {
		public InlineXBRL getInstance(Element element);
	}
	
	private static Map<QValue, Builder> nonNumericBuilderMap = new TreeMap<>();
	static {
		nonNumericBuilderMap.put(XBRL.IXT_BOOLEAN_TRUE,               o -> new BooleanValue(o));
		nonNumericBuilderMap.put(XBRL.IXT_BOOLEAN_FALSE,              o -> new BooleanValue(o));
		nonNumericBuilderMap.put(XBRL.IXT_DATE_YEAR_MONTH_DAY_CJK,    o -> new DateValue(o));
		nonNumericBuilderMap.put(XBRL.IXT_DATE_ERA_YEAR_MONTH_DAY_JP, o -> new DateValue(o));
	}
	private static class NonNumericBuilder implements Builder {
		public InlineXBRL getInstance(Element element) {
			QValue qFormat = getQFormat(element);
			if (qFormat == null) {
				return new StringValue(element);
			} else {
				if (nonNumericBuilderMap.containsKey(qFormat)) {
					Builder builder = nonNumericBuilderMap.get(qFormat);
					return builder.getInstance(element);
				} else {
					logger.error("Unexpected element {}", element);
					logger.error("  qFormat {}", qFormat);
					throw new UnexpectedException("Unexpected element");	
				}
			}
		}
	}
	
	private static Map<QValue, Builder> nonFractionalBuilderMap = new TreeMap<>();
	static {
		nonFractionalBuilderMap.put(XBRL.IXT_NUM_DOT_DECIMAL,  o -> new NumberValue(o));
		nonFractionalBuilderMap.put(XBRL.IXT_NUM_UNIT_DECIMAL, o -> new NumberValue(o));
	}
	private static class NonFractionBuilder implements Builder {
		public InlineXBRL getInstance(Element element) {
			if (isNull(element)) {
				return new NumberValue(element);
			} else {
				QValue qFormat = getQFormat(element);
				if (qFormat == null) {
					return new NumberValue(element);
				} else {
					if (nonFractionalBuilderMap.containsKey(qFormat)) {
						Builder builder = nonFractionalBuilderMap.get(qFormat);
						return builder.getInstance(element);
					} else {
						logger.error("Unexpected format");
						logger.error("  qFormat {}", qFormat);
						throw new UnexpectedException("Unexpected format");
					}
				}
			}
		}
	}
	
	private static Map<QValue, Builder> elementBuilderMap = new TreeMap<>();
	static {
		elementBuilderMap.put(XBRL.IX_NON_NUMERIC,  new NonNumericBuilder());
		elementBuilderMap.put(XBRL.IX_NON_FRACTION, new NonFractionBuilder());
	}
	private static Builder getBuilder(Element element) {
		QValue key = new QValue(element);
		if (elementBuilderMap.containsKey(key)) {
			return elementBuilderMap.get(key);
		} else {
			logger.error("Unexpected key {}", key);
			throw new UnexpectedException("Unexpected key");
		}
	}
	
	public static boolean canGetInstance(Element element) {
		QValue key = new QValue(element);
		return elementBuilderMap.containsKey(key);
	}
	public static InlineXBRL getInstance(Element element) {
		if (canGetInstance(element)) {
			Builder builder = getBuilder(element);
			return builder.getInstance(element);
		} else {
			logger.error("Unexpected name {}", element.name);
			throw new UnexpectedException("Unexpected name");
		}
	}
	public static QValue getQName(Element element) {
		String name  = element.getAttribute("name");
		QValue qName = element.expandNamespacePrefix(name);
		return qName;
	}
	public static QValue getQFormat(Element element) {
		String format = element.getAttributeOrNull("format");
		return (format == null) ? null : element.expandNamespacePrefix(format);
	}
	public static boolean isNull(Element element) {
		String nilValue = element.getAttributeOrNull(XML.XSI_NIL);
		if (nilValue == null) {
			return false;
		} else {
			switch(nilValue) {
			case "true":
				return true;
//			case "false":
//				this.isNull = false;
//				break;
			default:
				logger.error("Unexpected nilValue {}!", nilValue);
				throw new UnexpectedException("Unexpected nilValue");
			}
		}
	}
	
	public static String normalizeNumberCharacter(String value) {
		// １２３４５６７８９０
		value = value.replace("１", "1");
		value = value.replace("２", "2");
		value = value.replace("３", "3");
		value = value.replace("４", "4");
		value = value.replace("５", "5");
		value = value.replace("６", "6");
		value = value.replace("７", "7");
		value = value.replace("８", "8");
		value = value.replace("９", "9");
		value = value.replace("０", "0");

		// Remove space in value
		value = value.replace(" ", "");
		
		// 0.0<br/>
		value = value.replace("<br/>", "");
		// 0.0<br />
		value = value.replace("<br />", "");
		
		return value;
	}

	public final Kind        kind;
	public final Element     element;
	
	public final Set<String> contextSet;
	
	public final String      name;
	public final String      format;
	public final String      value;
	
	public final QValue      qName;
	public final QValue      qFormat;
	public final boolean     isNull;

	protected InlineXBRL(Kind kind, Element element) {
		this.kind       = kind;
		this.element    = element;
		
		this.contextSet = Arrays.asList(element.getAttribute("contextRef").split("_")).stream().collect(Collectors.toUnmodifiableSet());
		
		this.name       = element.getAttribute("name");
		this.format     = element.getAttributeOrNull("format");
		this.value      = element.content;
		
		this.qName      = getQName(element);
		this.qFormat    = getQFormat(element);
		this.isNull     = isNull(element);
	}
	
	
	private static class ContextIncludeAllFilter implements Predicate<InlineXBRL>  {
		private final String[] contextArray;
		private ContextIncludeAllFilter(Context... contexts) {
			contextArray = Arrays.stream(contexts).map(o -> o.toString()).toArray(String[]::new);
		}
		
		@Override
		public boolean test(InlineXBRL ix) {
			for(var context: contextArray) {
				if (!ix.contextSet.contains(context)) return false;
			}
			return true;
		}
	}
	public static Predicate<InlineXBRL> contextIncludeAll(Context... contexts) {
		return new ContextIncludeAllFilter(contexts);
	}
	
	private static class ContextExcludeAnyFilter implements Predicate<InlineXBRL>  {
		private final String[] contextArray;
		private ContextExcludeAnyFilter(Context... contexts) {
			contextArray = Arrays.stream(contexts).map(o -> o.toString()).toArray(String[]::new);
		}
		
		@Override
		public boolean test(InlineXBRL ix) {
			for(var context: contextArray) {
				if (ix.contextSet.contains(context)) return false;
			}
			return true;
		}
	}
	public static Predicate<InlineXBRL> contextExcludeAny(Context... contexts) {
		return new ContextExcludeAnyFilter(contexts);
	}
	
	private static class NullFilter implements Predicate<InlineXBRL>  {
		private final boolean acceptNull;
		private NullFilter(boolean acceptNull) {
			this.acceptNull = acceptNull;
		}
		
		@Override
		public boolean test(InlineXBRL ix) {
			if (ix.isNull) {
				return acceptNull;
			} else {
				return true;
			}
		}
	}
	public static Predicate<InlineXBRL> nullFilter(boolean acceptNull) {
		return new NullFilter(acceptNull);
	}
}

package yokwe.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class StringUtil {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public interface MatcherFunction<T> {
		public T apply(Matcher matcher);
	}
	
	public static String replace(String string, Pattern pattern, MatcherFunction<String> operator) {
		StringBuilder ret = new StringBuilder();
		
		Matcher m = pattern.matcher(string);
		int lastEnd = 0;
		while(m.find()) {
			int start = m.start();
			int end   = m.end();

			// preamble
			if (lastEnd != start) {
				ret.append(string.substring(lastEnd, start));
			}
			
			// replace
			ret.append(operator.apply(m));
			
			lastEnd = end;
		}
		// postamble
		ret.append(string.substring(lastEnd));
		
		return ret.toString();
	}

	private static final Pattern PAT_UNESCAPE_HTML_CHAR = Pattern.compile("\\&\\#(?<code>[0-9]+)\\;");
	private static final MatcherFunction<String> OP_UNESCAPE_HTML_CHAR = (m) -> Character.toString((char)Integer.parseInt(m.group("code")));
	public static String unescapceHTMLChar(String string) {
		String ret = replace(string, PAT_UNESCAPE_HTML_CHAR, OP_UNESCAPE_HTML_CHAR);
		
		// unescape common char entity
		ret = ret.replace("&amp;",   "&");
		ret = ret.replace("&gt;",    ">");
		ret = ret.replace("&rsquo;", "'");
		ret = ret.replace("&nbsp;",  " ");
		ret = ret.replace("&#39;",   "'");

		return ret;
	}

	public static <T> Stream<T> find(String string, Pattern pattern, MatcherFunction<T> operator) {
		Stream.Builder<T> builder = Stream.builder();
		
		Matcher m = pattern.matcher(string);
		while(m.find()) {
			T value = operator.apply(m);
			builder.add(value);
		}
		
		return builder.build();
	}
	
	//
	// return matched group as array of string
	//
	public static String[] getGroup(Pattern pat, String string) {
		Matcher m = pat.matcher(string);
		if (m.find()) {
			int count = m.groupCount();
			String[] ret = new String[count];
			for(int i = 0; i < count; i++) {
				ret[i] = m.group(i + 1);
			}
			return ret;
		} else {
			return null;
		}
	}
	
	//
	// expect just one group and return it
	//
	public static String getGroupOne(Pattern pat, String string) {
		String[] result = getGroup(pat, string);
		if (result == null) {
			return null;
		} else {
			if (result.length == 1) {
				return result[0];
			} else {
				logger.error("result.length != 1");
				logger.error("  result {}", Arrays.asList(result));
				throw new UnexpectedException("result.length != 1");
			}
		}
	}

	
	//
	// removeBOM
	//
	public static String removeBOM(String string) {
		String ret = new String(string);
		
		// Remove BOM
		if (ret.startsWith("\uFEFF")) ret = ret.substring(1);
		if (ret.startsWith("\uFFFE")) ret = ret.substring(1);
		
		return ret;
	}
	
	
	//
	// urlEncode
	//
	public static String urlEncode(String symbol) {
		try {
			return URLEncoder.encode(symbol, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

	
	//
	// toJavaConstName
	//
	private enum CharKind {
		LOWER,
		UPPER,
		DIGIT,
		UNKNOWN
	}
	public static String toJavaConstName(String name) {
		StringBuilder ret = new StringBuilder();
		CharKind lastCharKind = CharKind.UNKNOWN;
		
		for(int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);

			if (Character.isLowerCase(c)) {
				// If this is first lower character after upper character, need special handling.
				// abcREITProfit => ABC_REIT_PROFIT
				if (lastCharKind == CharKind.UPPER) {
					int length = ret.length();
					if (2 <= length) {
						String c1 = ret.substring(0, length - 2);
						String c2 = ret.substring(length - 2, length - 1);
						String c3 = ret.substring(length - 1, length);
						if (c2.equals("_")) {
							//
						} else {
							ret.setLength(0);
							ret.append(c1);
							ret.append(c2);
							ret.append("_");
							ret.append(c3);
						}
					}
				}
				ret.append(Character.toUpperCase(c));
				lastCharKind = CharKind.LOWER;
			} else if (Character.isDigit(c)) {
				if (lastCharKind == CharKind.DIGIT) {
					ret.append(c);
				} else {
					if (ret.length() == 0) {
						ret.append(c);
					} else {
						ret.append('_').append(c);
					}
				}
				lastCharKind = CharKind.DIGIT;
			} else if (Character.isUpperCase(c)) {
				if (lastCharKind == CharKind.UPPER) {
					ret.append(c);
				} else {
					if (ret.length() == 0) {
						ret.append(c);
					} else {
						ret.append('_').append(c);
					}
				}
				lastCharKind = CharKind.UPPER;
			} else if (c == '-') {
				ret.append('_');
			} else {
				logger.error("{}", String.format("Unknown character type = %c - %04X", c, (int)c));
				logger.error("  name {}", name);
				throw new UnexpectedException("Unknown character");
			}
		}
		return ret.toString();
	}

	//
	// toHexString
	//
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	public static String toHexString(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	//
	// toString(Object)
	//
	public enum TimeZone {
		UTC,
		LOCAL,
		NEW_YORK,
	}
	
	public static final ZoneId UTC      = ZoneOffset.UTC;
	public static final ZoneId LOCAL    = ZoneId.systemDefault();
	public static final ZoneId NEW_YORK = ZoneId.of("America/New_York");
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface UseTimeZone {
		TimeZone value();
	}

	private static class ClassInfo {
		private static class FieldInfo {
			final Field field;
			final String name;
			final String type;
			final boolean isArray;
			final boolean isEnum;
			final TimeZone useTimeZone;

			FieldInfo(Field field) {
				Class<?> type = field.getType();
				
				this.field   = field;
				this.name    = field.getName();
				this.type    = type.getName();
				this.isArray = type.isArray();
				this.isEnum  = type.isEnum();
				
				UseTimeZone useTimeZone = field.getDeclaredAnnotation(UseTimeZone.class);
				if (useTimeZone != null) {
					this.useTimeZone = useTimeZone.value();
				} else {
					this.useTimeZone = null;
				}
			}
		}

		private static Map<String, ClassInfo> map = new TreeMap<>();
		
		final FieldInfo[] fieldInfos;
		
		static ClassInfo get(Object o) {
			Class<?> clazz = o.getClass();
			String clazzName = clazz.getName();
			if (map.containsKey(clazzName)) {
				return map.get(clazzName);
			} else {
				ClassInfo classInfo = new ClassInfo(clazz);
				map.put(clazzName, classInfo);
				return classInfo;
			}
		}
		
		ClassInfo(Class<?> clazz) {
			{
				List<FieldInfo> list = new ArrayList<>();
				
				for(Field field: clazz.getDeclaredFields()) {
					int modifiers = field.getModifiers();
					// Ignore static
					if (Modifier.isStatic(modifiers)) continue;
					field.setAccessible(true); // to access protected and private file, call setAccessble(true) of the field
					list.add(new FieldInfo(field));
				}
				fieldInfos = list.toArray(new FieldInfo[0]);
			}
		}
	}
	
	public static String toString(Object o) {
		// handle special case
		if (o == null) {
			return "null";
		}
		if (o.getClass().equals(String.class)) {
			return o.toString();
		}
		
		try {
			ClassInfo classInfo = ClassInfo.get(o);

			List<String>  result = new ArrayList<>();
			StringBuilder line   = new StringBuilder();
			
			for(ClassInfo.FieldInfo fieldInfo: classInfo.fieldInfos) {
				line.setLength(0);
				line.append(fieldInfo.name).append(": ");
				
				switch(fieldInfo.type) {
				case "double":
					line.append(Double.toString(fieldInfo.field.getDouble(o)));
					break;
				case "float":
					line.append(fieldInfo.field.getFloat(o));
					break;
				case "long":
					line.append(fieldInfo.field.getLong(o));
					break;
				case "int":
					line.append(fieldInfo.field.getInt(o));
					break;
				case "short":
					line.append(fieldInfo.field.getShort(o));
					break;
				case "byte":
					line.append(fieldInfo.field.getByte(o));
					break;
				case "char":
					line.append(String.format("'%c'", fieldInfo.field.getChar(o)));
					break;
				case "boolean":
					line.append(fieldInfo.field.getBoolean(o) ? "true" : "false");
					break;
				default:
				{
					Object value = fieldInfo.field.get(o);
					if (value == null) {
						line.append("null");
					} else if (value instanceof String) {
						// Quote special character in string \ => \\  " => \"
						String stringValue = value.toString().replace("\\", "\\\\").replace("\"", "\\\"");
						line.append("\"").append(stringValue).append("\"");
					} else if (value instanceof BigDecimal) {
						BigDecimal bigDecimal = (BigDecimal)value;
						line.append(bigDecimal.toPlainString());
					} else if (value instanceof LocalDateTime) {
						LocalDateTime  localDateTime  = (LocalDateTime)value;
						OffsetDateTime offsetDateTime = localDateTime.atOffset(ZoneOffset.UTC);
						
						String stringValue;
						if (fieldInfo.useTimeZone != null) {
							switch(fieldInfo.useTimeZone) {
							case UTC:
								stringValue = offsetDateTime.atZoneSameInstant(UTC).toLocalDateTime().toString();
								break;
							case LOCAL:
								stringValue = offsetDateTime.atZoneSameInstant(LOCAL).toLocalDateTime().toString();
								break;
							case NEW_YORK:
								stringValue = offsetDateTime.atZoneSameInstant(NEW_YORK).toLocalDateTime().toString();
								break;
							default:
								logger.error("Unexptected useTimeZone value {}", fieldInfo.useTimeZone);
								throw new UnexpectedException("Unexptected useTimeZone value");
							}
						} else {
							// Treat as LOCAL
							stringValue = offsetDateTime.atZoneSameInstant(LOCAL).toLocalDateTime().toString();
						}
						line.append(stringValue);
					} else if (fieldInfo.isArray) {
						List<String> arrayElement = new ArrayList<>();
						int length = Array.getLength(value);
						for(int i = 0; i < length; i++) {
							Object element = Array.get(value, i);
							if (element instanceof String) {
								// Quote special character in string \ => \\  " => \"
								String stringValue = toString(element).replace("\\", "\\\\").replace("\"", "\\\"");
								arrayElement.add(String.format("\"%s\"", stringValue));
							} else {
								arrayElement.add(String.format("%s", toString(element)));
							}
						}						
						line.append("[").append(String.join(", ", arrayElement)).append("]");
					} else if (fieldInfo.isEnum) {
						line.append(value.toString());
					} else {
						if (fieldInfo.type.startsWith("java")) {
							// Dont' dig into system class
							line.append(value.toString());
						} else {
							Class<?> clazz = value.getClass();
							
							Method method = null;
							for(var e: clazz.getDeclaredMethods()) {
								if (e.getName().equals("toString") && e.getParameterCount() == 0) {
									method = e;
									break;
								}
							}
							if (method != null) {
								String string = (String)method.invoke(value);
								line.append(string);
							} else {
								line.append(toString(value));
							}
						}
					}
				}
					break;
				}
				result.add(line.toString());
			}
			
			return String.format("{%s}", String.join(", ", result));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

	public static String toURLString(File file) {
		try {
			return file.toURI().toURL().toString();
		} catch (MalformedURLException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	public static String toURLString(String filePath) {
		return toURLString(new File(filePath));
	}

	//
	// Add padding by character width
	//
	public static int width(String string) {
		int ret = 0;
				
		for(int i = 0; i < string.length(); i = string.offsetByCodePoints(i, 1)) {
			char[] c = Character.toChars(string.codePointAt(i));
			Character.UnicodeBlock  ub = Character.UnicodeBlock.of(c[0]);
			switch(ub.toString()) {
			case "KATAKANA":
			case "KATAKANA_PHONETIC_EXTENSIONS":
			case "HIRAGANA":
			//
			case "CJK_SYMBOLS_AND_PUNCTUATION":
			case "ENCLOSED_CJK_LETTERS_AND_MONTHS":
			case "CJK_COMPATIBILITY":
			case "CJK_UNIFIED_IDEOGRAPHS":
			case "CJK_COMPATIBILITY_IDEOGRAPHS":
			case "CJK_COMPATIBILITY_FORMS":
			case "CJK_RADICALS_SUPPLEMENT":
			case "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A":
			case "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B":
			case "CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT":
			case "CJK_STROKES":
			case "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C":
			case "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D":
			case "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E":
			case "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F":
			case "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_G":
				ret += 2;
				break;
			default:
				ret++;
				break;
			}
		}
		return ret;
	}
	public static String padLeft(String string, int width, String pad) {
		int w = width(string);
		if (width <= w) return string;
		return pad.repeat(width - w) + string;
	}
	public static String padRight(String string, int width, String pad) {
		int w = width(string);
		if (width <= w) return string;
		return string + pad.repeat(width - w);
	}
	public static String padLeftSpace(String string, int width) {
		return padLeft(string, width, " ");
	}
	public static String padRightSpace(String string, int width) {
		return padRight(string, width, " ");
	}
	
	//
	// toHTTPDateTime  -- date time to RFC-1123 format string that is used in HTTP header
	//
	public static String toHTTPDateTime(LocalDate localDate) {
		return toHTTPDateTime(localDate.atStartOfDay());
	}
	public static String toHTTPDateTime(LocalDateTime localDateTime) {
		return toHTTPDateTime(localDateTime.atZone(DateTimeFormatter.RFC_1123_DATE_TIME.getZone()));
	}
	public static String toHTTPDateTime(ZonedDateTime zonedDateTime) {
		// Sun, 06 Nov 1994 08:49:37 GMT
		// Sat, 29 Jul 2023 20:21:21 GMT
		return DateTimeFormatter.RFC_1123_DATE_TIME.format(zonedDateTime);
	}
	
	//
	// replaceCharacter
	//
	public static String replaceCharacter(String fromString, String toString, String string) {
		// sanity check
		{
			if (fromString.length() != toString.length()) {
				logger.error("length not equal");
				logger.error("  from {}  !{}!", fromString.length(), fromString);
				logger.error("  to   {}  !{}!", toString.length(), toString);
				throw new UnexpectedException("length not equal");
			}				
		}
		
		StringBuilder result = new StringBuilder(string.length());
		
		for(var c: string.toCharArray()) {
			var index = fromString.indexOf(c);
			if (index == -1) {
				result.append(c);
			} else {
				result.append(toString.charAt(index));
			}
		}
		
		return result.toString();
	}
	private static final String HALFWIDTH_STRING = "" +
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
			"abcdefghijklmnopqrstuvwxyz" +
			"`1234567890-=" +
			"~!@#$%^&*()_+" +
			"[]\\{}|" +
			";':\"" +
			",./<>?" +
			"ｱｲｳｴｵ" +
			"ｶｷｸｹｵ" +
			"ｻｼｽｾﾄ" + 
			"ﾀﾁﾂﾃﾄ" +
			"ﾅﾆﾇﾈﾉ" +
			"ﾊﾋﾌﾍﾎ" +
			"ﾏﾐﾑﾒﾓ" +
			"ﾔﾕﾖ" +
			"ﾗﾘﾙﾚﾛ" +
			"ﾜｦﾝ" +
			" ";
	private static final String FULLWIDTH_STRING = "" +
			"ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ" +
			"ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ" +
			"｀１２３４５６７８９０ー＝" +
			"〜！＠＃＄％＾＆＊（）＿＋" +
			"「」￥『』｜" +
			"；’：”" +
			"、。・＜＞？" +
			"アイウエオ" +
			"カキクケコ" +
			"サシスセソ" +
			"タチツテト" +
			"ナニヌネノ" +
			"ハヒフヘホ" +
			"マミムメモ" +
			"ヤユヨ" +
			"ラリルレロ" +
			"ワヲン" +
			"　";
	
	//
	// toFullWidth
	//
	public static String toFullWidth(String string) {
		return replaceCharacter(HALFWIDTH_STRING, FULLWIDTH_STRING, string);
	}
	//
	// toHalfWidth
	//
	public static String toHalfWidth(String string) {
		return replaceCharacter(HALFWIDTH_STRING, FULLWIDTH_STRING, string);
	}
}

package yokwe.util;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JapaneseDate implements Comparable<JapaneseDate> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final JapaneseDate UNDEFIEND = new JapaneseDate("*UNDEFINED*", 0, 0, 0);

	public static String findDateString(String string) {
		Matcher m = pat_DATE.matcher(string);
		if (m.find()) {
			return m.group(0);
		} else {
			return null;
		}
	}
	
	public static JapaneseDate getInstance(String string) {
		if (string == null) return UNDEFIEND;
		String dateString = findDateString(string);
		return dateString == null ? null : new JapaneseDate(dateString);
	}
	
	private static Map<String, Integer> offsetMap = new TreeMap<>();
	static {
		offsetMap.put("享和", 1801);
		offsetMap.put("文化", 1804);
		offsetMap.put("文政", 1818);
		offsetMap.put("天保", 1831);
		offsetMap.put("弘化", 1845);
		offsetMap.put("嘉永", 1848);
		offsetMap.put("安政", 1855);
		offsetMap.put("万延", 1860);
		offsetMap.put("文久", 1861);
		offsetMap.put("元治", 1864);
		offsetMap.put("慶応", 1865);
		
		offsetMap.put("明治", 1868);
		offsetMap.put("大正", 1912);
		offsetMap.put("昭和", 1926);
		offsetMap.put("平成", 1989);
		offsetMap.put("令和", 2019);
	}
		
	public final String string;
	public final int    year;
	public final int    month;
	public final int    day;
	
	private static Pattern pat_DATE  = Pattern.compile("(..)(元|[0-9]{1,2})年([1,2]?[0-9])月([1-3]?[0-9])日");

	private JapaneseDate(String string, int year, int month, int day) {
		this.string = string;
		this.year   = year;
		this.month  = month;
		this.day    = day;
	}
	private JapaneseDate(String newValue) {
		string = newValue;
		Matcher m = pat_DATE.matcher(string);
		if (m.matches()) {
			String era         = m.group(1);
			String yearString  = m.group(2);
			String monthString = m.group(3);
			String dayString   = m.group(4);
			
			if (offsetMap.containsKey(era)) {
				year  = offsetMap.get(era) + (yearString.equals("元") ? 1 : Integer.valueOf(yearString)) - 1;
				month = Integer.valueOf(monthString);
				day   = Integer.valueOf(dayString);
			} else {
				logger.error("Unpexpeced  era");
				logger.error("string {}!", string);
				logger.error("era {}!", era);
				throw new UnexpectedException("Unpexpeced  era");
			}
		} else {
			logger.error("Unpexpeced string");
			logger.error("string {}!", string);
			throw new UnexpectedException("Unpexpeced string");
		}
	}
	
	boolean isDefined() {
		return !equals(UNDEFIEND);
	}

	@Override
	public String toString() {
		return string;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof JapaneseDate) {
			return compareTo((JapaneseDate)o) == 0;
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(JapaneseDate that) {
		int ret = this.year - that.year;
		if (ret == 0) ret = this.month - that.month;
		if (ret == 0) ret = this.day - that.day;
		return ret;
	}

}

package yokwe.util;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JapaneseDate implements Comparable<JapaneseDate> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final JapaneseDate UNDEFIEND = new JapaneseDate("", 0, 0, 0);

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
	
	static class EraData {
		String era;
		int    year;
	}
	private static final String DATA_PATH = "/yokwe/util/japanese-date.csv";
	private static Map<String, Integer> eraMap = new TreeMap<>();
	//                 era     star year
	static {
		List<EraData> list = CSVUtil.read(EraData.class).file(EraData.class, DATA_PATH, StandardCharsets.UTF_8);
		for(var e: list) {
			eraMap.put(e.era, e.year);
		}
		logger.info("JapaneseDate map {}", eraMap.size());
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
			
			if (eraMap.containsKey(era)) {
				year  = eraMap.get(era) + (yearString.equals("元") ? 1 : Integer.valueOf(yearString)) - 1;
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
	
	public boolean isDefined() {
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

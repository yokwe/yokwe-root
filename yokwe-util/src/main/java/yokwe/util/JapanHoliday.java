package yokwe.util;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JapanHoliday {
	private static final Logger logger = LoggerFactory.getLogger(JapanHoliday.class);

	public static final String PATH_JAPAN_HOLIDAY_CSV = "/yokwe/util/japanHoliday.csv";
	
	public static final int YEAR_START = 2015;
	public static final int YEAR_END   = 2025;
	
	private static final Matcher MAT_YYYY_MM_DD = Pattern.compile("^(20[0-9]{2})-([01]?[0-9])-([0-3]?[0-9])$").matcher("");
	private static final Matcher MAT_MM_DD      = Pattern.compile("^([01]?[0-9])-([0-3]?[0-9])$").matcher("");
	private static final Matcher MAT_MM_DDM     = Pattern.compile("^([01]?[0-9])-([0-3]?[0-9])M$").matcher("");
	private static final Matcher MAT_YYYY       = Pattern.compile("^(20[0-9]{2})$").matcher("");

	private static final DateTimeFormatter FORMAT_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-M-d");


	public static class Data {
		public String event;
		public String date;
		public String start;
		public String end;
		
		public Data() {
			event = "";
			date  = "";
			start = "";
			end   = "";
		}
		
		public Data(Data that) {
			this.event = that.event;
			this.date  = that.date;
			this.start = that.start;
			this.end   = that.end;
		}
		
		@Override
		public String toString() {
			return String.format("%s %s %s %s",
				event, date, (start.isEmpty() ? "-" : start), (end.isEmpty() ? "-" : end));
		}
	}
	
	private static Map<LocalDate, Data> publicHolidayMap = new TreeMap<>();
	static {
		List<Data> dataList = CSVUtil.read(Data.class).file(JapanHoliday.class, PATH_JAPAN_HOLIDAY_CSV, StandardCharsets.UTF_8);
		
		for(Data data: dataList) {
			if (data.event.length() == 0) continue;
			
			final int yearStart;
			final int yearEnd;
			
			if (data.start.length() != 0) {
				MAT_YYYY.reset(data.start);
				if (MAT_YYYY.matches()) {
					yearStart = Integer.parseInt(data.start);
				} else {
					logger.error("Unexpected start {}", data);
					throw new UnexpectedException("Unexpected");
				}
			} else {
				yearStart = YEAR_START;
			}
			if (data.end.length() != 0) {
				MAT_YYYY.reset(data.end);
				if (MAT_YYYY.matches()) {
					yearEnd = Integer.parseInt(data.end);
				} else {
					logger.error("Unexpected start {}", data);
					throw new UnexpectedException("Unexpected");
				}
			} else {
				yearEnd = YEAR_END;
			}
			
			{
				MAT_YYYY_MM_DD.reset(data.date);
				MAT_MM_DD.reset(data.date);
				MAT_MM_DDM.reset(data.date);
				
				if (MAT_YYYY_MM_DD.matches()) {
					LocalDate date = LocalDate.parse(data.date, FORMAT_YYYY_MM_DD);
					publicHolidayMap.put(date, data);
				} else if (MAT_MM_DD.matches()) {
					if (MAT_MM_DD.groupCount() != 2) {
						logger.error("Unexpected date format {}", data);
						throw new UnexpectedException("Unexpected");
					}
					
					int mm = Integer.parseInt(MAT_MM_DD.group(1));
					int dd = Integer.parseInt(MAT_MM_DD.group(2));
					
					for(int yyyy = yearStart; yyyy <= yearEnd; yyyy++) {
						LocalDate date = LocalDate.of(yyyy, mm, dd);
						publicHolidayMap.put(date, data);
					}
				} else if (MAT_MM_DDM.matches()) {
					if (MAT_MM_DDM.groupCount() != 2) {
						logger.error("Unexpected date format {}", data);
						throw new UnexpectedException("Unexpected");
					}
					
					int mm = Integer.parseInt(MAT_MM_DDM.group(1));
					int dd = Integer.parseInt(MAT_MM_DDM.group(2));
					
					if (mm < 1 || 12 < mm) {
						logger.error("Unexpected date format {}", data);
						throw new UnexpectedException("Unexpected");
					}
					if (dd < 1 || 4 < dd) {
						logger.error("Unexpected date format {}", data);
						throw new UnexpectedException("Unexpected");
					}
					
					for(int yyyy = yearStart; yyyy <= yearEnd; yyyy++) {
						LocalDate firstDateOfMonth = LocalDate.of(yyyy, mm, 1);
						LocalDate firstMonday = firstDateOfMonth.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
						LocalDate date = firstMonday.plusDays((dd - 1) * 7);
						publicHolidayMap.put(date, data);
					}
				} else {
					logger.error("Unexpected date format {}", data);
					throw new UnexpectedException("Unexpected");
				}
			}
		}
		Map<LocalDate, Data> observedList = new TreeMap<>();
		for(LocalDate date: publicHolidayMap.keySet()) {
			if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
				Data data = publicHolidayMap.get(date);
				LocalDate nextDate = date.plusDays(0);
				for(;;) {
					nextDate = nextDate.plusDays(1);
					if (publicHolidayMap.containsKey(nextDate)) continue;
					break;
				}
				Data nextData = new Data(data);
				nextData.event = String.format("Observed %s", data.event);
				observedList.put(nextDate, nextData);
//				logger.info("Observed  {}  {}  {}", date, nextDate, nextData);
			}
		}
		publicHolidayMap.putAll(observedList);
		logger.info("publicHolidayMap {} {} {}", YEAR_START, YEAR_END, publicHolidayMap.size());
	}

	public static final boolean isPublicHoliday(LocalDate date) {
		return publicHolidayMap.containsKey(date);
	}
	public static final boolean isPublicHoliday(String date) {
		return isPublicHoliday(LocalDate.parse(date));
	}
	
	private static Set<String> marketHolidaySet = new TreeSet<>();
	static {
		marketHolidaySet.add("0101");
		marketHolidaySet.add("0102");
		marketHolidaySet.add("0103");
		marketHolidaySet.add("1231");
	}
	
	public static final boolean isClosed(LocalDateTime dateTime) {
		return isClosed(dateTime.toLocalDate());
	}
	public static final boolean isClosed(LocalDate date) {
		// Check day of week
		DayOfWeek dayOfWeek = date.getDayOfWeek();
		if (dayOfWeek == DayOfWeek.SATURDAY) return true;
		if (dayOfWeek == DayOfWeek.SUNDAY)   return true;
		
		// Check public holiday
		if (isPublicHoliday(date)) return true;
		
		// Check market holiday
		int mm = date.getMonthValue();
		int dd = date.getDayOfMonth();
		String key = String.format("%02d%02d", mm, dd);
		return marketHolidaySet.contains(key);
	}
	public static final boolean isClosed(String date) {
		return isClosed(LocalDate.parse(date));
	}
	
	public static final ZoneId ZONE_ID           = ZoneId.of("Asia/Tokyo");
	public static final int    HOUR_CLOSE_MARKET = 15; // market close at 1500

	private static LocalDate lastTradingDate = null;
	public static LocalDate getLastTradingDate() {
		if (lastTradingDate == null) {
			LocalDateTime now = LocalDateTime.now(ZONE_ID);
			LocalDate date = now.toLocalDate();

//			if (now.getHour() < HOUR_CLOSE_MARKET) date = date.minusDays(1); // Move to yesterday if it is before market close

			if (isClosed(date)) {
				date = getPreviousTradingDate(date);
			}
			
			lastTradingDate = date;
			logger.info("Last Trading Date {}", lastTradingDate);
		}
		return lastTradingDate;
	}
	public static LocalDate getNextTradingDate(LocalDate date) {
		date = date.plusDays(1);
		while(isClosed(date)) {
			date = date.plusDays(1);
		}
		return date;
	}
	public static LocalDate getPreviousTradingDate(LocalDate date) {
		date = date.minusDays(1);
		while(isClosed(date)) {
			date = date.minusDays(1);
		}
		return date;
	}

	public static void main(String[] args) {
		logger.info("START");
		for(Map.Entry<LocalDate, Data> entry: publicHolidayMap.entrySet()) {
			logger.info("{}  {}", entry.getKey(), entry.getValue());
		}
		logger.info("END");
	}
	
}

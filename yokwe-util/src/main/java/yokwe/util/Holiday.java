package yokwe.util;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Holiday {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String PATH_MARKET_HOLIDAY_JP = "/yokwe/util/market-holiday-jp.csv";
	private static final String PATH_MARKET_HOLIDAY_US = "/yokwe/util/market-holiday-us.csv";

	public static final int YEAR_START_DEFAULT = 2015;
	public static final int YEAR_END_DEFAULT   = Year.now().getValue() + 1;
	
	private static final Pattern PAT_YYYY_MM_DD  = Pattern.compile("^(20[0-9]{2})-([01]?[0-9])-([0-3]?[0-9])$");
	private static final Pattern PAT_MM_DD       = Pattern.compile("^([01]?[0-9])-([0-3]?[0-9])$");
	private static final Pattern PAT_MM_DDM      = Pattern.compile("^([01]?[0-9])-([0-4])M$");
	private static final Pattern PAT_MM_DDT      = Pattern.compile("^([01]?[0-9])-([0-4])T$");
	private static final Pattern PAT_MM_LM       = Pattern.compile("^([01]?[0-9])-LM$");
	private static final Pattern PAT_YYYY        = Pattern.compile("^(20[0-9]{2})$");
	private static final Pattern PAT_GOOD_FRIDAY = Pattern.compile("^GOOD_FRIDAY$");

	private static final DateTimeFormatter FORMAT_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-M-d");
	private static final DateTimeFormatter FORMAT_MM_DD      = DateTimeFormatter.ofPattern("M-d");

	public static LocalDate goodFriday(int year) {
	    int g = year % 19;
	    int c = year / 100;
	    int h = (c - (int)(c / 4) - (int)((8 * c + 13) / 25) + 19 * g + 15) % 30;
	    int i = h - (int)(h / 28) * (1 - (int)(h / 28) * (int)(29 / (h + 1)) * (int)((21 - g) / 11));

	    int day   = i - ((year + (int)(year / 4) + i + 2 - c + (int)(c / 4)) % 7) + 28;
	    int month = 3;

	    if (31 < day) {
	        month++;
	        day -= 31;
	    }

	    return LocalDate.of(year, month, day).minusDays(2);
	}
	
	public static class Data {
		public String event;
		public String date;
		public String start;
		public String end;
		
		public Data(String event, String date, String start, String end) {
			this.event   = event;
			this.date    = date;
			this.start   = start;
			this.end     = end;
		}
		public Data() {
			event   = "";
			date    = "";
			start   = "";
			end     = "";
		}
		
		public Data(Data that) {
			this.event   = that.event;
			this.date    = that.date;
			this.start   = that.start;
			this.end     = that.end;
		}
		public Data(Data that, String event) {
			this.event   = event;
			this.date    = that.date;
			this.start   = that.start;
			this.end     = that.end;
		}
		
		@Override
		public String toString() {
			return String.format("%s %s %s %s",
				event, date, (start.isEmpty() ? "-" : start), (end.isEmpty() ? "-" : end));
		}
	}
	
	protected final int yearStartDefault;
	protected final int yearEndDefault;
	
	protected final Map<LocalDate, Data> holidayMap;
	
	protected abstract void processObserved();
	
	private Holiday(int yearStartDefault, int yearEndDefault, String path) {
		this.yearStartDefault = yearStartDefault;
		this.yearEndDefault   = yearEndDefault;
		this.holidayMap       = new TreeMap<>();
		
		List<Data> dataList = CSVUtil.read(Data.class).file(JapanHoliday.class, path, StandardCharsets.UTF_8);
		
		for(Data data: dataList) {
			if (data.event.length() == 0) continue;
			
			final int yearStart;
			final int yearEnd;
			
			if (data.start.length() != 0) {
				var m = PAT_YYYY.matcher(data.start);
				if (m.matches()) {
					yearStart = Integer.parseInt(data.start);
				} else {
					logger.error("Unexpected start {}", data);
					throw new UnexpectedException("Unexpected");
				}
			} else {
				yearStart = yearStartDefault;
			}
			if (data.end.length() != 0) {
				var m = PAT_YYYY.matcher(data.end);
				if (m.matches()) {
					yearEnd = Integer.parseInt(data.end);
				} else {
					logger.error("Unexpected start {}", data);
					throw new UnexpectedException("Unexpected");
				}
			} else {
				yearEnd = yearEndDefault;
			}
			
			Matcher m;
			
			m = PAT_YYYY_MM_DD.matcher(data.date);
			if (m.matches()) {
				LocalDate date = LocalDate.parse(data.date, FORMAT_YYYY_MM_DD);
				holidayMap.put(date, data);
				continue;
			}

			m = PAT_MM_DD.matcher(data.date);
			if (m.matches()) {
				if (m.groupCount() != 2) {
					logger.error("Unexpected date format {}", data);
					throw new UnexpectedException("Unexpected");
				}
				
				int mm = Integer.parseInt(m.group(1));
				int dd = Integer.parseInt(m.group(2));
				
				for(int yyyy = yearStart; yyyy <= yearEnd; yyyy++) {
					LocalDate date = LocalDate.of(yyyy, mm, dd);
					holidayMap.put(date, data);
				}
				continue;
			}

			m = PAT_MM_DDM.matcher(data.date);
			if (m.matches()) {
				if (m.groupCount() != 2) {
					logger.error("Unexpected date format {}", data);
					throw new UnexpectedException("Unexpected");
				}
				
				int mm = Integer.parseInt(m.group(1));
				int dd = Integer.parseInt(m.group(2));
				
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
					holidayMap.put(date, data);
				}
				continue;
			}
			
			m = PAT_MM_DDT.matcher(data.date);
			if (m.matches()) {
				if (m.groupCount() != 2) {
					logger.error("Unexpected date format {}", data);
					throw new UnexpectedException("Unexpected");
				}
				
				int mm = Integer.parseInt(m.group(1));
				int dd = Integer.parseInt(m.group(2));
				
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
					LocalDate firstThrusday = firstDateOfMonth.with(TemporalAdjusters.firstInMonth(DayOfWeek.THURSDAY));
					LocalDate date = firstThrusday.plusDays((dd - 1) * 7);
					holidayMap.put(date, data);
				}
				continue;
			}
			
			m = PAT_MM_LM.matcher(data.date);
			if (m.matches()) {
				if (m.groupCount() != 1) {
					logger.error("Unexpected date format {}", data);
					throw new UnexpectedException("Unexpected");
				}
				
				int mm = Integer.parseInt(m.group(1));
				
				if (mm < 1 || 12 < mm) {
					logger.error("Unexpected date format {}", data);
					throw new UnexpectedException("Unexpected");
				}
				// last Monday
				for(int yyyy = yearStart; yyyy <= yearEnd; yyyy++) {
					LocalDate firstDateOfMonth = LocalDate.of(yyyy, mm, 1);
					LocalDate lastMonday = firstDateOfMonth.with(TemporalAdjusters.lastInMonth(DayOfWeek.MONDAY));
					
					holidayMap.put(lastMonday, data);
				}
				continue;
			}

			m = PAT_GOOD_FRIDAY.matcher(data.date);
			if (m.matches()) {
				for(int yyyy = yearStart; yyyy <= yearEnd; yyyy++) {
					LocalDate goodFriday = goodFriday(yyyy);
					
					holidayMap.put(goodFriday, data);
				}
				continue;
			}
			
			logger.error("Unexpected date format {}", data);
			throw new UnexpectedException("Unexpected");
		}
		
		// Observed
		processObserved();
		
		// remove out of range entry
		for(var i = holidayMap.entrySet().iterator(); i.hasNext();) {
			var e = i.next();
			var key = e.getKey();
			int year = key.getYear();
			if (year < yearStartDefault || yearEndDefault < year) i.remove();
		}
		
		logger.info("holidayMap {} {} {}", yearStartDefault, yearEndDefault, holidayMap.size());
	}
	
	public void addHoliday(LocalDate date, String event) {
		int year = date.getYear();
		if (yearStartDefault <= year && year <= yearEndDefault) {
			Data data = new Data(event, date.format(FORMAT_MM_DD), Integer.toString(year), Integer.toString(year));
			holidayMap.put(date, data);
		} else {
			logger.error("Unexpected date");
			logger.error("  date       {}", date.toString());
			logger.error("  YEAR_START {}", yearStartDefault);
			logger.error("  YEAR_END   {}", yearEndDefault);
			throw new UnexpectedException("Unexpected date");
		}
	}
	
	public boolean isHoliday(LocalDate date) {
		int year = date.getYear();
		if (yearStartDefault <= year && year <= yearEndDefault) {
			return holidayMap.containsKey(date);
		} else {
			logger.error("Unexpected date");
			logger.error("  date       {}", date.toString());
			logger.error("  YEAR_START {}", yearStartDefault);
			logger.error("  YEAR_END   {}", yearEndDefault);
			throw new UnexpectedException("Unexpected date");
		}
	}	
	public boolean isWeekend(LocalDate date) {
		DayOfWeek dayOfWeek = date.getDayOfWeek();
		switch(dayOfWeek) {
		case SATURDAY:
		case SUNDAY:
			return true;
		default:
			return false;
		}
	}
	public boolean isClosed(LocalDate date) {
		if (isWeekend(date)) return true;
		if (isHoliday(date)) return true;
		return false;
	}


	public static class JP extends Holiday {
		public JP() {
			super(YEAR_START_DEFAULT, YEAR_END_DEFAULT, PATH_MARKET_HOLIDAY_JP);
			
			// There is no observed holiday for 1/2 1/3 12/31
			for(int year = yearStartDefault; year <= yearEndDefault; year++) {
				LocalDate date0102 = LocalDate.parse(year + "-01-02");
				LocalDate date0103 = LocalDate.parse(year + "-01-03");
				LocalDate date1231 = LocalDate.parse(year + "-12-31");
				
				if (!isHoliday(date0102)) addHoliday(date0102, "1月2日");
				if (!isHoliday(date0103)) addHoliday(date0103, "1月3日");
				if (!isHoliday(date1231)) addHoliday(date1231, "大晦日");
			}
		}
		
		protected void processObserved() {
			Map<LocalDate, Data> observedMap = new TreeMap<>();

			for(var i = holidayMap.entrySet().iterator(); i.hasNext();) {
				var entry = i.next();
				var date  = entry.getKey();
				var data  = entry.getValue();
				
				if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
					var observedDate = date.plusDays(0);
					for(;;) {
						observedDate = observedDate.plusDays(1);
						if (holidayMap.containsKey(observedDate)) continue;
						break;
					}
					var observedData = new Data(data, String.format("Observed %s", data.event));
					
//					logger.info("Observed  {}  {}  {}", date, observedDate, observedData);
					observedMap.put(observedDate, observedData);
					i.remove();
				}					
			}
			holidayMap.putAll(observedMap);
		}
	}
	public static class US extends Holiday {
		public US() {
			super(YEAR_START_DEFAULT, YEAR_END_DEFAULT, PATH_MARKET_HOLIDAY_US);
		}
		
		protected void processObserved() {
			Map<LocalDate, Data> observedMap = new TreeMap<>();

			for(var i = holidayMap.entrySet().iterator(); i.hasNext();) {
				var entry = i.next();
				var date  = entry.getKey();
				var data  = entry.getValue();
				
				int adjust = 0;
				switch(date.getDayOfWeek()) {
				case SATURDAY:
					adjust = -1;
					break;
				case SUNDAY:
					adjust = 1;
					break;
				default:
					break;
				}
				if (adjust != 0) {
					var observedDate = date.plusDays(adjust);
					var observedData = new Data(data, String.format("Observed %s", data.event));
					if (observedDate.getMonthValue() == 12 && observedDate.getDayOfMonth() == 31) {
						// See Rule 7.2 Holidays
						// https://nyseguide.srorules.com/rules/document?treeNodeId=csh-da-filter!WKUS-TAL-DOCS-PHC-%7B4A07B716-0F73-46CC-BAC2-43EB20902159%7D--WKUS_TAL_19401%23teid-15
						//
						//   The Exchange will not be open for business on New Year's Day, Martin Luther King Jr. Day,
						//   Presidents' Day, Good Friday, Memorial Day, Independence Day, Labor Day, Thanksgiving Day and Christmas Day.
						//
						//   When a holiday observed by the Exchange falls on a Saturday, the Exchange will not be open for business
						//   on the preceding Friday and when any holiday observed by the Exchange falls on a Sunday,
						//   the Exchange will not be open for business on the succeeding Monday, unless unusual business conditions exist,
						//   such as the ending of a monthly or yearly accounting period.
						i.remove();
					} else {
//						logger.info("Observed  {}  {}  {}", date, observedDate, observedData);
						observedMap.put(observedDate, observedData);
						i.remove();
					}
					
				}
			}
			holidayMap.putAll(observedMap);
		}
	}

	public static void main(String[] args) {
		logger.info("START");
		
		{
			logger.info("MARKET HOLIDAY JP");
			Holiday holiday = new JP();
			for(var entry: holiday.holidayMap.entrySet()) {
				logger.info("{}  {}", entry.getKey(), entry.getValue().event);
			}
		}
		{
			logger.info("MARKET HOLIDAY US");
			Holiday holiday = new US();
			for(var entry: holiday.holidayMap.entrySet()) {
				logger.info("{}  {}", entry.getKey(), entry.getValue().event);
			}
		}
		
		logger.info("END");
	}

}

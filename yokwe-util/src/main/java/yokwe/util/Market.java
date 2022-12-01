package yokwe.util;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Market {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final String PATH_MARKET_HOLIDAY_CSV = "/yokwe/util/marketHoliday.csv";
	public static final int    HOUR_CLOSE_MARKET       = 16; // market close at 1600
	public static final ZoneId ZONE_ID                 = ZoneId.of("America/New_York");
	
	public static class MarketHoliday {
		public String date;
		public String event;
		public String status; // Closed or other
	}
	public static class Holiday {
		public final LocalDate date;
		public final boolean   closed;
		public Holiday(LocalDate date, boolean closed) {
			this.date   = date;
			this.closed = closed;
		}
	}
	private static final Map<LocalDate, Holiday> holidayMap = new TreeMap<>();
	static {
		List<MarketHoliday> marketHolidayList = CSVUtil.read(MarketHoliday.class).file(Market.class, PATH_MARKET_HOLIDAY_CSV, StandardCharsets.UTF_8);
		for(MarketHoliday marketHoliday: marketHolidayList) {
			if (marketHoliday.date.startsWith("#")) continue;
			
			LocalDate date   = LocalDate.parse(marketHoliday.date);
			boolean   closed = marketHoliday.status.toLowerCase().startsWith("close"); // To avoid confusion comes from misspelled word
			holidayMap.put(date, new Holiday(date, closed));
		}
	}
	
	private static LocalDate lastTradingDate = null;
	
	public static LocalDate getLastTradingDate() {
		if (lastTradingDate == null) {
			LocalDateTime today = LocalDateTime.now(ZONE_ID);
			if (today.getHour() < HOUR_CLOSE_MARKET) today = today.minusDays(1); // Move to yesterday if it is before market close
			
			for(;;) {
				if (isClosed(today)) {
					today = today.minusDays(1);
					continue;
				}

				break;
			}
			
			lastTradingDate  = today.toLocalDate();
			logger.info("Last Trading Date {}", lastTradingDate);
		}
		return lastTradingDate;
	}
	
	public static final boolean isClosed(LocalDateTime dateTime) {
		return isClosed(dateTime.toLocalDate());
	}
	public static final boolean isClosed(String date) {
		return isClosed(LocalDate.parse(date));
	}
	public static final boolean isClosed(LocalDate date) {
		DayOfWeek dayOfWeek = date.getDayOfWeek();
		if (dayOfWeek == DayOfWeek.SUNDAY)   return true;
		if (dayOfWeek == DayOfWeek.SATURDAY) return true;
		
		Holiday holiday = holidayMap.get(date);
		return holiday != null && holiday.closed;
	}
	public static final boolean isSaturdayOrSunday(String date) {
		return isSaturdayOrSunday(LocalDate.parse(date));
	}
	public static final boolean isSaturdayOrSunday(LocalDate date) {
		DayOfWeek dayOfWeek = date.getDayOfWeek();
		if (dayOfWeek == DayOfWeek.SUNDAY)   return true;
		if (dayOfWeek == DayOfWeek.SATURDAY) return true;
		return false;
	}
	
	public static LocalDate getNextTradeDate(LocalDate date) {
		LocalDate nextDate = date;
		for(;;) {
			nextDate = nextDate.plusDays(1);
			if (isClosed(nextDate)) continue;
			return nextDate;
		}
	}
	public static LocalDate getPreviousTradeDate(LocalDate date) {
		LocalDate prevDate = date;
		for(;;) {
			prevDate = prevDate.minusDays(1);
			if (isClosed(prevDate)) continue;
			return prevDate;
		}
	}
	
	
	// See settlement calendar below for settlement date
	//    https://stlcl.com/?year=2016&month=11
	public static final String PATH_IRREGULAR_SETTLEMENT = "/yokwe/util/irregularSettlement.csv";
	public static class IrregularSettlement {
		public String tradeDate;
		public String settlementDate;
	}
	private static Map<LocalDate, LocalDate> irregularSttlementDateMap = new TreeMap<>();
	//                 tradeDate  settlementDate
	static {
		List<IrregularSettlement> list = CSVUtil.read(IrregularSettlement.class).file(Market.class, PATH_IRREGULAR_SETTLEMENT, StandardCharsets.UTF_8);

		for(IrregularSettlement irregularSettlement: list) {
			LocalDate tradeDate      = LocalDate.parse(irregularSettlement.tradeDate);
			LocalDate settlementDate = LocalDate.parse(irregularSettlement.settlementDate);
			irregularSttlementDateMap.put(tradeDate, settlementDate);
		}
	}
	
	private static LocalDate T2_SETTLEMENT_START_DATE = LocalDate.of(2017, 9, 5);
	public static LocalDate toSettlementDate(LocalDate tradeDate) {
		if (irregularSttlementDateMap.containsKey(tradeDate)) {
			return irregularSttlementDateMap.get(tradeDate);
		}
			
		LocalDate t0 = tradeDate;
		LocalDate t1 = getNextTradeDate(t0);
		LocalDate t2 = getNextTradeDate(t1);
		LocalDate t3 = getNextTradeDate(t2);
		
		if (tradeDate.isBefore(T2_SETTLEMENT_START_DATE)) {
			return t3;
		} else {
			return t2;
		}
	}
	
}

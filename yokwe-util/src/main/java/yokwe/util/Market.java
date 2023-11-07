package yokwe.util;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//Use MarketHoliday.US
@Deprecated
public class Market {
	public static LocalDate getLastTradingDate() {
		return MarketHoliday.US.getLastTradingDate();
	}
	
	public static final boolean isClosed(LocalDateTime dateTime) {
		return MarketHoliday.US.isClosed(dateTime.toLocalDate());
	}
	public static final boolean isClosed(String date) {
		return MarketHoliday.US.isClosed(LocalDate.parse(date));
	}
	public static final boolean isClosed(LocalDate date) {
		return MarketHoliday.US.isClosed(date);
	}
	public static final boolean isSaturdayOrSunday(String date) {
		return MarketHoliday.US.isWeekend(LocalDate.parse(date));
	}
	public static final boolean isSaturdayOrSunday(LocalDate date) {
		return MarketHoliday.US.isWeekend(date);
	}
	
	public static LocalDate getNextTradeDate(LocalDate date) {
		return MarketHoliday.US.getNextTradingDate(date);
	}
	public static LocalDate getPreviousTradeDate(LocalDate date) {
		return MarketHoliday.US.getPreviousTradingDate(date);
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
		List<IrregularSettlement> list = CSVUtil.read(IrregularSettlement.class).withCharset(StandardCharsets.UTF_8).file(Market.class, PATH_IRREGULAR_SETTLEMENT);

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

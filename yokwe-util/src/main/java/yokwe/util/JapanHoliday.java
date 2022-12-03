package yokwe.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Use MarketHoliday.JP
@Deprecated
public class JapanHoliday {
	public static final boolean isPublicHoliday(LocalDate date) {
		return MarketHoliday.JP.isMarketHoliday(date);
	}
	public static final boolean isPublicHoliday(String date) {
		return MarketHoliday.JP.isMarketHoliday(LocalDate.parse(date));
	}
	
	public static final boolean isClosed(LocalDateTime dateTime) {
		return MarketHoliday.JP.isClosed(dateTime.toLocalDate());
	}
	public static final boolean isClosed(LocalDate date) {
		return MarketHoliday.JP.isClosed(date);
	}
	public static final boolean isClosed(String date) {
		return MarketHoliday.JP.isClosed(LocalDate.parse(date));
	}
	
	public static LocalDate getLastTradingDate() {
		return MarketHoliday.JP.getLastTradingDate();
	}
	public static LocalDate getNextTradingDate(LocalDate date) {
		return MarketHoliday.JP.getNextTradingDate(date);
	}
	public static LocalDate getPreviousTradingDate(LocalDate date) {
		return MarketHoliday.JP.getPreviousTradingDate(date);
	}
}

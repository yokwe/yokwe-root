package yokwe.finance.provider.google;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

import yokwe.finance.type.Currency;
import yokwe.util.ScrapeUtil;
import yokwe.util.http.HttpUtil;

public class CurrencyPairRate {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_QUOTE = "https://www.google.com/finance/quote/%s-%s";
	
	private static final ZoneId ZONE_LOCAL = ZoneId.systemDefault();
	
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM d, h:m:s a z");
	
	public static class FXRateInfo {
		public static final Pattern PAT = Pattern.compile(
			"<div class=\"YMlKec fxKbKc\">(?<value>.+?)</div>" +
			".+?" +
			"<div class=\"ygUjEc\" jsname=\"Vebqub\">(?<dateTime>.+? UTC) " +
			"",
			Pattern.DOTALL
		);
				
		public static FXRateInfo getInstance(String page) {
			return ScrapeUtil.get(FXRateInfo.class, PAT, page.replace("\u202f", " ")); // replace NARROW NO-BREAK SPACE (202f) with ordinary space
		}
		
		public final BigDecimal value;
		public final String     dateTime;
		
		public FXRateInfo(BigDecimal value, String dateTime) {
			this.value    = value;
			this.dateTime = dateTime;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s}", value.toPlainString(), dateTime);
		}
	}
	
	public static CurrencyPairRate getInstance(Currency base, Currency quote) {
		var now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		
		var url = String.format(URL_QUOTE, base.name(), quote.name());
//		logger.info("url  {}", url);
		
		var result = HttpUtil.getInstance().download(url);		
		var string = result.result;
//		logger.info("string  {}", string.length());
		
		var fxRateInfo = FXRateInfo.getInstance(string);
//		logger.info("fxRateInfo  {}", fxRateInfo);
				
		LocalDateTime dateTimeLocal;
		{
			var temporal   = DATE_TIME_FORMATTER.parse(fxRateInfo.dateTime);
			var zoneId     = ZoneId.from(temporal).normalized();
			var zoneOffset = ZoneOffset.of(zoneId.getId());
			
			var date     = MonthDay.from(temporal).atYear(now.atOffset(zoneOffset).getYear());
			var dateTime = LocalDateTime.of(date, LocalTime.from(temporal)).atZone(zoneId);
						
			dateTimeLocal = dateTime.withZoneSameInstant(ZONE_LOCAL).toLocalDateTime();
		}
		
		return new CurrencyPairRate(dateTimeLocal, base, quote, fxRateInfo.value);
	}
	
	public static CurrencyPairRate getInstance(Currency base) {
		return getInstance(base, Currency.JPY);
	}

	public final LocalDateTime dateTime;
	public final Currency      base;
	public final Currency      quote;
	public final BigDecimal    rate;
	
	public CurrencyPairRate(LocalDateTime dateTime, Currency base, Currency quote, BigDecimal rate) {
		this.dateTime = dateTime;
		this.base     = base;
		this.quote    = quote;
		this.rate     = rate;
	}
	
	@Override
	public String toString() {
		return String.format("{%s  %s/%s  %s}", dateTime, base, quote, rate);
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		var usdjpy = getInstance(Currency.USD);
		
		logger.info("usdjpy  {}", usdjpy);
		
		logger.info("STOP");
	}
}
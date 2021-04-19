package yokwe.util.test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yokwe.util.FileUtil;
import yokwe.util.StringUtil;
import yokwe.util.json.JSON;
import yokwe.util.json.JSON.Name;

public class T132 {
	private static final Logger logger = LoggerFactory.getLogger(T132.class);
	
	public static class DividendFile {
		public enum CurrencyID {
			JPY("JPY");
			
			public final String value;
			
			private CurrencyID(String value) {
				this.value = value;
			}
			
			@Override
			public String toString() {
				return value;
			}
		}
		public static class Security {
			@Name("Id")             public String           id;
			@Name("DividendSeries") public DividendSeries[] dividendSeries;
		}
		public static class DividendSeries {
			@Name("HistoryDetail") public HistoryDetail[] historyDetails;
		}
		public static class HistoryDetail {
			@Name("EndDate") public LocalDate  endDate;
			@Name("Value")   public Value[]    values;
		}
		public static class Value {
			@Name("CurrencyId") public CurrencyID currencyId;
			@Name("value")      public BigDecimal value;
		}

		@Name("Security") public Security[] security;
	}
	
	public static class PriceFile {
		public static class TimeSeries {
			@Name("Security") public Security[] securities;
		}
		public static class Security {
			@Name("Id")            public String          id;
			@Name("HistoryDetail") public HistoryDetail[] historyDetails;
		}
		public static class HistoryDetail {
			@Name("EndDate") public LocalDate  endDate;
			@Name("Value")   public BigDecimal value;
		}

		@Name("TimeSeries") public TimeSeries timeSeries;
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		logger.info("START");
		
		{
			String content = FileUtil.read().file("tmp/F000000MU9-div");
			DividendFile dividendFile = JSON.unmarshal(DividendFile.class, content);
			logger.info("dividendFile {}", StringUtil.toString(dividendFile));
		}
		{
			String content = FileUtil.read().file("tmp/F000000MU9-price");
			PriceFile priceFile = JSON.unmarshal(PriceFile.class, content);
			logger.info("priceFile {}", StringUtil.toString(priceFile));
		}
		
		logger.info("STOP");
	}
}

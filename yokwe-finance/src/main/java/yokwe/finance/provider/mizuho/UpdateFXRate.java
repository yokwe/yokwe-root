package yokwe.finance.provider.mizuho;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yokwe.finance.fx.StorageFX;
import yokwe.finance.type.FXRate;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateFXRate {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final String  URL_CSV      = "https://www.mizuhobank.co.jp/market/quote.csv";
	private static final String  ENCODING_CSV = "SHIFT_JIS";
	private static final Pattern PAT_YYYYMMDD = Pattern.compile("^(?<yyyy>20[0-9]{2})/(?<mm>[01]?[0-9])/(?<dd>[0-3]?[0-9])$");
	
	private static final File    FILE_CSV     = StorageMizuho.storage.getFile("quote.csv");
	
	private static final LocalDate EPOCH_DATE = LocalDate.of(2024, 1, 1);
	
	public static class Quote {
		public String DATE;
		public String USD;
		public String GBP;
		public String EUR;
		public String CAD;
		public String CHF;
		public String SEK;
		public String DKK;
		public String NOK;
		public String AUD;
		public String NZD;
		public String ZAR;
		public String BHD;
		public String IDR;
		public String CNY;
		public String HKD;
		public String INR;
		public String MYR;
		public String PHP;
		public String SGD;
		public String KRW;
		public String THB;
		public String KWD;
		public String SAR;
		public String AED;
		public String MXN;
		public String PGK;
		public String HUF;
		public String CZK;
		public String PLN;
		public String TRY;
		public String XXX;
		public String IDR2;
		public String CNY2;
		public String MYR2;
		public String KRW2;
		public String TWD;
		public String RUB;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		logger.info("EPOCH DATE  {}", EPOCH_DATE);
		
		String string = HttpUtil.getInstance().withCharset(ENCODING_CSV).download(URL_CSV).result;
		logger.info("save  {}  {}", string.length(), FILE_CSV.getPath());
		FileUtil.write().file(FILE_CSV, string);
		
		Reader reader = new StringReader(string);
		List<Quote> list = CSVUtil.read(Quote.class).withHeader(false).file(reader);		
				
		logger.info("list {}", list.size());
		// sanity check
		{
			// line 0
			{
				Quote quote = list.get(0);
				if (!quote.DATE.isEmpty()) {
					logger.error("Unexpected");
					throw new UnexpectedException("Unexpected");
				}
				// 参考相場
				if (!quote.IDR2.equals("参考相場")) {
					logger.error("Unexpected");
					throw new UnexpectedException("Unexpected");
				}
			}
			// line 1
			{
				Quote quote = list.get(1);
				if (!quote.DATE.isEmpty()) {
					logger.error("Unexpected");
					throw new UnexpectedException("Unexpected");
				}
				// 米ドル
				if (!quote.USD.equals("米ドル")) {
					logger.error("Unexpected");
					throw new UnexpectedException("Unexpected");
				}
			}
			// line 2
			{
				Quote quote = list.get(2);
				if (!quote.DATE.isEmpty()) {
					logger.error("Unexpected");
					throw new UnexpectedException("Unexpected");
				}
				if (!quote.USD.equals("USD")) {
					logger.error("Unexpected");
					throw new UnexpectedException("Unexpected");
				}
			}
			// line 3
			{
				Quote quote = list.get(3);
				if (!quote.DATE.equals("2002/4/1")) {
					logger.error("Unexpected");
					throw new UnexpectedException("Unexpected");
				}
			}
		}
		
		List<FXRate> result = new ArrayList<>();
		for(int i = 3; i < list.size(); i++) {
			Quote value = list.get(i);
			
			LocalDate date;
			{
				Matcher m = PAT_YYYYMMDD.matcher(value.DATE);
				if (m.matches() && m.groupCount() == 3) {
					int yyyy = Integer.parseInt(m.group("yyyy"));
					int mm   = Integer.parseInt(m.group("mm"));
					int dd   = Integer.parseInt(m.group("dd"));
					
					date = LocalDate.of(yyyy, mm, dd);
				} else {
					logger.error("Unexpected");
					logger.error("  date {}", value.DATE);
					throw new UnexpectedException("Unexpected");
				}
			}
						
			var usd = new BigDecimal(value.USD).setScale(2);
			var eur = new BigDecimal(value.EUR).setScale(2);
			var gbp = new BigDecimal(value.GBP).setScale(2);
			var aud = new BigDecimal(value.AUD).setScale(2);
			var nzd = new BigDecimal(value.NZD).setScale(2);
			
			result.add(new FXRate(date, usd, eur, gbp, aud, nzd));
		}
		
		// remove entry before EPOC_DATE
		result.removeIf(o -> o.date.isBefore(EPOCH_DATE));
		
		logger.info("date  {} - {}", result.get(0).date, result.get(result.size() - 1).date);
		logger.info("save  {}  {}", result.size(), StorageFX.FXRate.getPath());
		StorageFX.FXRate.save(result);
		
		logger.info("STOP");
	}

}

package yokwe.stock.trade.data;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateFX {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateFX.class);

	private static final String URL_CSV       = "https://www.mizuhobank.co.jp/market/quote.csv";
	public static final String ENCODING_CSV   = "SHIFT_JIS";
	
	public static class MarketQuote {
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
		public String RUB;
		public String TRY;
		public String XXX;
		public String IDR2;
		public String CNY2;
		public String MYR2;
		public String KRW2;
		public String TWD;
		
		public MarketQuote() {
			DATE = "";
			USD = "";
			GBP = "";
			EUR = "";
			CAD = "";
			CHF = "";
			SEK = "";
			DKK = "";
			NOK = "";
			AUD = "";
			NZD = "";
			ZAR = "";
			BHD = "";
			IDR = "";
			CNY = "";
			HKD = "";
			INR = "";
			MYR = "";
			PHP = "";
			SGD = "";
			KRW = "";
			THB = "";
			KWD = "";
			SAR = "";
			AED = "";
			MXN = "";
			PGK = "";
			HUF = "";
			CZK = "";
			PLN = "";
			RUB = "";
			TRY = "";
			XXX = "";
			IDR2 = "";
			CNY2 = "";
			MYR2 = "";
			KRW2 = "";
			TWD = "";
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		String contents = HttpUtil.getInstance().withCharset(ENCODING_CSV).download(URL_CSV).result;
		Reader reader = new StringReader(contents);
		List<MarketQuote> list = CSVUtil.read(MarketQuote.class).withHeader(false).file(reader);
		
		logger.info("list {}", list.size());
		// sanity check
		{
			MarketQuote value = list.get(2);
			if (!value.DATE.isEmpty()) {
				logger.error("Unexpected");
				throw new UnexpectedException("Unexpected");
			}
			if (!value.USD.equals("USD")) {
				logger.error("Unexpected");
				throw new UnexpectedException("Unexpected");
			}
		}
		
		List<FX> result = new ArrayList<>();
		for(int i = 3; i < list.size(); i++) {
			MarketQuote value = list.get(i);
			
			String yyyymmdd[] = value.DATE.split("\\/");
			if (yyyymmdd.length != 3) {
				logger.error("Unexpected");
				logger.error("yyyymmdd {}", yyyymmdd.length);
				throw new UnexpectedException("Unexpected");
			}
			if (yyyymmdd[1].length() == 1) yyyymmdd[1] = "0" + yyyymmdd[1];
			if (yyyymmdd[2].length() == 1) yyyymmdd[2] = "0" + yyyymmdd[2];
			String date = String.format("%s-%s-%s", yyyymmdd[0], yyyymmdd[1], yyyymmdd[2]);
			
			String usd;
			String nnmm[] = value.USD.split("\\.");
			if (nnmm.length == 1) {
				usd = nnmm[0] + ".00";
			} else if (nnmm.length == 2) {
				if (nnmm[1].length() == 1) {
					usd = nnmm[0] + "." + nnmm[1] + "0";
				} else {
					usd = nnmm[0] + "." + nnmm[1];
				}
			} else {
				logger.error("Unexpected");
				logger.error("nnmm {}", nnmm.length);
				throw new UnexpectedException("Unexpected");
			}
			
			FX fx = new FX(date, usd);
			result.add(fx);
		}
		
		FX.save(result);
		logger.info("save {} {}", result.size(), FX.PATH_FILE);
		
		logger.info("STOP");
	}
}

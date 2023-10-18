package yokwe.finance.provider.jita;

import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

import org.apache.hc.core5.http2.HttpVersionPolicy;

import yokwe.finance.type.DailyValue;
import yokwe.finance.type.FundPriceJP;
import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;

public class UpdateFundDivPriceJITA {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String  URL       = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/csv-file-download?isinCd=%s&associFundCd=%s";
	private static final Charset CHARSET   = Charset.forName("SHIFT_JIS");
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit";	
	

	private static void initialize(Download download) {
		int threadCount       = 10;
		int maxPerRoute       = 50;
		int maxTotal          = 100;
		int soTimeout         = 30;
		int connectionTimeout = 30;
		int progressInterval  = 100;
		logger.info("threadCount       {}", threadCount);
		logger.info("maxPerRoute       {}", maxPerRoute);
		logger.info("maxTotal          {}", maxTotal);
		logger.info("soTimeout         {}", soTimeout);
		logger.info("connectionTimeout {}", connectionTimeout);
		logger.info("progressInterval  {}", progressInterval);
		
		RequesterBuilder requesterBuilder = RequesterBuilder.custom()
				.setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
				.setSoTimeout(soTimeout)
				.setMaxTotal(maxTotal)
				.setDefaultMaxPerRoute(maxPerRoute);

		download.setRequesterBuilder(requesterBuilder);
		
		// Configure custom header
		download.setUserAgent(USER_AGENT);
		
		// Configure thread count
		download.setThreadCount(threadCount);
		
		// connection timeout in second
		download.setConnectionTimeout(connectionTimeout);
		
		// progress interval
		download.setProgressInterval(progressInterval);
	}
	
	
	private static class CSVData implements Comparable<CSVData> {
		// 年月日	        基準価額(円)	純資産総額（百万円）	分配金	決算期
		// 2022年04月25日	19239	        1178200	                0       4
		// 2022年04月26日	19167	        1174580   
		@CSVUtil.ColumnName("年月日")
		public String date;
		@CSVUtil.ColumnName("基準価額(円)")
		public String price;
		@CSVUtil.ColumnName("純資産総額（百万円）")
		public String nav;
		@CSVUtil.ColumnName("分配金")
		public String div;
		@CSVUtil.ColumnName("決算期")
		public String period;
		
		@Override
		public int compareTo(CSVData that) {
			return this.date.compareTo(that.date);
		}
	}
	private static class DivPriceConsumer implements Consumer<String> {
		public final String isinCode;
		
		public DivPriceConsumer(String isinCode) {
			this.isinCode = isinCode;
		}
		
		@Override
		public void accept(String string) {
			var divPriceList = CSVUtil.read(CSVData.class).file(new StringReader(string));
			if (divPriceList == null) {
				logger.error("Unexpected null");
				logger.error("string {}", string);
				throw new UnexpectedException("Unexpected null");
			}
			
			// build divList and priceList
			var divList   = new ArrayList<DailyValue>();
			var priceList = new ArrayList<FundPriceJP>();
			
			for(var divPrice: divPriceList) {
				LocalDate  date;
				{
					String dateString = divPrice.date;
					// 2000年01月01日
					// 01234 567 890
					if (dateString.length() == 11 && dateString.charAt(4) == '年' && dateString.charAt(7) == '月' && dateString.charAt(10) == '日') {
						int yyyy = Integer.parseInt(dateString.substring(0, 4));
						int mm   = Integer.parseInt(dateString.substring(5, 7));
						int dd   = Integer.parseInt(dateString.substring(8, 10));
						date = LocalDate.of(yyyy, mm, dd);
					} else {
						logger.error("Unexpected date");
						logger.error("  isinCode   {}", isinCode);
						logger.error("  dateString {}  !{}!", dateString.length(), dateString);
						throw new UnexpectedException("Unexpected date");
					}
				}
				BigDecimal price  = new BigDecimal(divPrice.price);
				BigDecimal nav    = new BigDecimal(divPrice.nav).scaleByPowerOfTen(6); // 純資産総額（百万円）
				String     div    = divPrice.div.trim();
				
				if (!div.isEmpty()) divList.add(new DailyValue(date, new BigDecimal(div)));
				
				priceList.add(new FundPriceJP(date, nav, price));
			}
			
			// save divList and priceList
			if (!divList.isEmpty())   StorageJITA.FundDivJITA.save(isinCode, divList);
			if (!priceList.isEmpty()) StorageJITA.FundPriceJITA.save(isinCode, priceList);
		}
	}

	
	private static void update() {
		Download download = new DownloadSync();
		initialize(download);
		
		var fundList = StorageJITA.FundInfoJITA.getList();
		Collections.shuffle(fundList); // shuffle fundList
		for(var fund: fundList) {
			String isinCode = fund.isinCode;
			String fundCode = fund.fundCode;
			String url = String.format(URL, isinCode, fundCode);

			var consumer = new DivPriceConsumer(isinCode);
			var task = StringTask.get(url, consumer, CHARSET);
			download.addTask(task);
		}
		
		int progressInterval = 500;
		logger.info("progressInterval  {}", progressInterval);
		download.setProgressInterval(progressInterval);
		
		logger.info("BEFORE RUN");
		download.startAndWait();
		logger.info("AFTER  RUN");
	}

	public static void main(String[] args) {
		logger.info("START");
				
		update();
				
		logger.info("STOP");
	}

}

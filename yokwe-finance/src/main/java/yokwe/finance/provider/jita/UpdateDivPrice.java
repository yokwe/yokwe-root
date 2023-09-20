package yokwe.finance.provider.jita;

import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hc.core5.http2.HttpVersionPolicy;

import yokwe.finance.fund.JITAFundInfoJP;
import yokwe.util.CSVUtil;
import yokwe.util.ListUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;

public class UpdateDivPrice {
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
		private static final Pattern PAT_DATE = Pattern.compile("(?<yyyy>[12][09][0-9][0-9])年(?<mm>[01]?[0-9])月(?<dd>[0123]?[0-9])日");

		public final String isinCode;
		
		public DivPriceConsumer(String isinCode) {
			this.isinCode = isinCode;
		}
		
		@Override
		public void accept(String string) {
			var divPriceList = ListUtil.load(CSVData.class, new StringReader(string));
			if (divPriceList == null) {
				logger.error("Unexpected null");
				logger.error("string {}", string);
				throw new UnexpectedException("Unexpected null");
			}
			
			// build divList and priceList
			var list  = new ArrayList<DivPrice>();
			
			for(var divPrice: divPriceList) {
				LocalDate  date;
				{
					Matcher m = PAT_DATE.matcher(divPrice.date);
					if (m.find()) {
						int yyyy = Integer.parseInt(m.group("yyyy"));
						int mm   = Integer.parseInt(m.group("mm"));
						int dd   = Integer.parseInt(m.group("dd"));
						date = LocalDate.of(yyyy, mm, dd);
					} else {
						logger.error("Unexpected date");
						logger.error("  isinCode {}", isinCode);
						logger.error("  divPrice {}", divPrice);
						throw new UnexpectedException("Unexpected date");
					}
				}
				BigDecimal price  = new BigDecimal(divPrice.price);
				BigDecimal nav    = new BigDecimal(divPrice.nav);
				String     div    = divPrice.div.trim();
				String     period = divPrice.period.trim();
				
				// add to priceList
				list.add(new DivPrice(date, price, nav, div, period));
			}
			
			if (!list.isEmpty()) {
				DivPrice.save(isinCode, list);
			}
		}
	}

	
	private static void update() {
		Download download = new DownloadSync();
		initialize(download);
		
		var fundList = JITAFundInfoJP.getList();
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

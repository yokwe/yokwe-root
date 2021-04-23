package yokwe.stock.jp.toushin;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;
import yokwe.util.http.Task;

public class UpdatePrice {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdatePrice.class);

	// https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/csv-file-download?isinCd=JP90C0009VE0&associFundCd=2931113C
	private static final String  CSV_URL     = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/csv-file-download?isinCd=%s&associFundCd=%s";
	private static final String  CSV_HEADER  = "年月日,基準価額(円),純資産総額（百万円）,分配金,決算期";
	private static final Charset CSV_CHARSET = Charset.forName("Shift_JIS");
	
	
	private static class Context {
		private Integer buildCount;
		
		public Context() {
			buildCount = 0;
		}
		
		public void incrementBuildCount() {
			synchronized(buildCount) {
				buildCount = buildCount + 1;
			}
		}
		public int getBuildCount() {
			int ret;
			synchronized(buildCount) {
				ret = buildCount;
			}
			return ret;
		}
	}
	
	private static class MyConsumer implements Consumer<String> {
		final Context context;
		final String isinCode;
		
		MyConsumer(Context context, String isinCode) {
			this.context = context;
			this.isinCode = isinCode;
		}
		
		@Override
		public void accept(String string) {
			context.incrementBuildCount();
			
			String[] lines = string.split("[\\r\\n]+");
			if (lines[0].equals(CSV_HEADER)) {
				List<Price> priceList = new ArrayList<>();
				
				for(int i = 1; i < lines.length; i++) {
					String line = lines[i];
					String[] fields = line.split(",", -1);
					if (fields.length != 5) {
						logger.warn("Unexpected field");
						logger.warn("  {} - {}!", fields.length, line);
						System.exit(0); // FIXME
					} else {
						String date          = fields[0]; // 年月日
						String basePrice     = fields[1]; // 基準価額(円) = 純資産総額 / (総口数 * 10,000)
						String netAssetValue = fields[2]; // 純資産総額（百万円）
						String dividend      = fields[3]; // 分配金
						String period        = fields[4]; // 決算期
						
						date = date.replace("年", "-").replace("月", "-").replace("日", "");

						Price fundPrice = new Price(date, basePrice, netAssetValue, dividend, period);
						priceList.add(fundPrice);
					}
				}
				Price.save(isinCode, priceList);
			} else {
				logger.warn("Unpexpected header");
				logger.warn("  {}!", lines[0]);
				System.exit(0); // FIXME
			}
		}
	}
	
	private static void buildContext(Context context) {
		int threadCount = 5; // 50 is too high for this site
		int maxPerRoute = 50;
		int maxTotal    = 100;
		int soTimeout   = 30;
		logger.info("threadCount {}", threadCount);
		logger.info("maxPerRoute {}", maxPerRoute);
		logger.info("maxTotal    {}", maxTotal);
		logger.info("soTimeout   {}", soTimeout);
		
		RequesterBuilder requesterBuilder = RequesterBuilder.custom()
				.setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
				.setSoTimeout(soTimeout)
				.setMaxTotal(maxTotal)
				.setDefaultMaxPerRoute(maxPerRoute);

//		Download download = new DownloadAsync();
		Download download = new DownloadSync();
		
		download.setRequesterBuilder(requesterBuilder);
		
		// Configure custom header
		download.setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit");
		
		// Configure thread count
		download.setThreadCount(threadCount);
		
		//
		List<Fund> list = Fund.load();
		Collections.shuffle(list);
		
		final int listSize = list.size();

		for(var e: list) {
			String isinCode  = e.isinCode;
			String uriString = String.format(CSV_URL, e.isinCode, e.fundCode);
			
			Task task = StringTask.text(uriString, new MyConsumer(context, isinCode), CSV_CHARSET);
			download.addTask(task);
		}
		
		logger.info("BEFORE RUN");
		download.startAndWait();
		logger.info("AFTER  RUN");
//		download.showRunCount();
		
		try {
			for(int i = 0; i < 10; i++) {
				int buildCount = context.getBuildCount();
				if (buildCount == listSize) break;
				logger.info("buildCount {} / {}", buildCount, listSize);
				Thread.sleep(1000);
			}
			{
				int buildCount = context.getBuildCount();
				if (buildCount != listSize) {
					logger.error("Unexpected");
					logger.error("  buildCount {}", buildCount);
					logger.error("  listSize   {}", listSize);
					throw new UnexpectedException("Unexpected");
				}
			}
			logger.info("AFTER  WAIT");
		} catch (InterruptedException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		Context context = new Context();
		buildContext(context);
		
		logger.info("STOP");
	}
}

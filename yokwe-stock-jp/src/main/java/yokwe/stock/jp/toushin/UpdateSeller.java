package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.slf4j.LoggerFactory;

import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadAsync;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;
import yokwe.util.http.Task;
import yokwe.util.json.JSON;

public class UpdateSeller {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateSeller.class);

	// https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/company-search
	
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
	
	//{"fdsInstCd":"21168","salesFee":0.0,"salesInstDiv":"1","kanaName":"イマムラ","instName":"今村証券","associFundCd":null},
	public static class SellerInfo {
		public String     fdsInstCd;
		public BigDecimal salesFee;
		public String     salesInstDiv;
		public String     kanaName;
		public String     instName;
		public String     associFundCd;
		
		public SellerInfo() {
			fdsInstCd    = null;
			salesFee     = null;
			salesInstDiv = null;
			kanaName     = null;
			instName     = null;
			associFundCd = null;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
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
			
			if (string.startsWith("[")) {
				List<Seller> list = new ArrayList<>();
				for(var e: JSON.getList(SellerInfo.class, string)) {
					if (e.salesFee == null) {
						e.salesFee = BigDecimal.ZERO;
					}
					
					list.add(new Seller(isinCode, e.fdsInstCd, e.salesFee, e.instName));
				}
				
				Seller.save(isinCode, list);
			} else {
				logger.warn("string is not JSON");
				logger.warn("string - {}", string);
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
		download.addHeader("Accept",           "*/*");
		download.addHeader("X-Requested-With", "XMLHttpRequest");
		download.addHeader("Origin",           "toushin-lib.fwg.ne.jp");
		download.addHeader("Accept-Encoding",  "gzip, deflate");
		download.addHeader("Accept-Language",  "ja");

		
		// Configure thread count
		download.setThreadCount(threadCount);
		
		//
		List<Fund> list = Fund.load();
		Collections.shuffle(list);
		
		final int listSize = list.size();

		for(var e: list) {
			String isinCode  = e.isinCode;
			String uriString = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/company-search";
			String content   = String.format("isinCd=%s", isinCode);
			Task   task      = StringTask.post(uriString, new MyConsumer(context, isinCode), content);
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

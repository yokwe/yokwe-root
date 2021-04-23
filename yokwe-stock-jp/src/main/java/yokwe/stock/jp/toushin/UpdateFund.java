package yokwe.stock.jp.toushin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.slf4j.LoggerFactory;

import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;
import yokwe.util.http.Task;

public class UpdateFund {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateFund.class);
	
	// https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000?isinCd=JP90C0009VE0
	// https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/csv-file-download?isinCd=JP90C0009VE0&associFundCd=2931113C
	
	private static final String URL_FUND = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000?isinCd=%s";
	
	public static class Toushin {
		private static final Pattern PAT = Pattern.compile(
			"<h3 .+?>(?<name>[^<+]+)</h3>\\s+<div [^>]+>\\s+愛称：.+?</div>" +
			".+?" +
			"<div .+?>\\s+運用会社名：(?<issuer>.+?)</div>" +
			".+?" +
			"<th>設定日<.+?<td .+?>(?<issueDate>.+?)</td>" +
			".+?" +
			"<th>償還日<.+?<td .+?>(?<redemptionDate>.+?)</td>" +
			".+?" +
			"<th>決算頻度<.+?<td .+?>(?<settlementFrequency>.+?)</td>" +
			".+?" +
			"<th>決算日<.+?<td .+?>(?<settlementDate>.+?)</td>" +
			".+?" +
			"<th>解約手数料<.+?<td .+?>(?<cancelationFee>.+?)</td>" +
			".+?" +
			"運用管理費用<br>（信託報酬）.+?<td .+?>(?<initialFeeLimit>.+?)</td>\\s+<td>(?<redemptionFee>.+?)</td>\\s+<td>(?<trustFee>.+?)</td>" +
			".+?" +
			"<tr>\\s+<th .+?>運用会社</th>\\s+<td>(?<trustFeeOperation>.+?)</td>\\s+</tr>" +
			".+?" +
			"<tr>\\s+<th .+?>販売会社</th>\\s+<td>(?<trustFeeSeller>.+?)</td>\\s+</tr>" +
			".+?" +
			"<tr>\\s+<th .+?>信託銀行</th>\\s+<td>(?<trustFeeBank>.+?)</td>\\s+</tr>" +
			".+?" +
			"<input type=\"hidden\"\\s+id=\"isinCd\"\\s+value=\"(?<isinCode>[0-9A-Z]+)\"" + 
			".+?" +
			"<input type=\"hidden\"\\s+id=\"associFundCd\"\\s+value=\"(?<fundCode>[0-9A-Z]+)\"" + 
			"",
			Pattern.DOTALL
			);
		
		public static Toushin getInstance(String page) {
			return ScrapeUtil.get(Toushin.class, PAT, page);
		}
		
		public String name;
		public String issuer;               // 運用会社名
		public String issueDate;            // 設定日
		public String redemptionDate;       // 償還日
		public String settlementFrequency;  // 決算頻度
		public String settlementDate;       // 決算日
		public String cancelationFee;       // 解約手数料
		public String initialFeeLimit;      // 購入時手数料 上限
		public String redemptionFee;        // 信託財産留保額
		public String trustFee;             // 信託報酬 
		public String trustFeeOperation ;   // 信託報酬 運用会社
		public String trustFeeSeller;       // 信託報酬 販売会社
		public String trustFeeBank;         // 信託報酬 信託銀行
		public String isinCode;
		public String fundCode;
		
		public Toushin(String name, String issuer, String issueDate, String redemptionDate,
				String settlementFrequency, String settlementDate, String cancelationFee, String initialFeeLimit, String redemptionFee,
				String trustFee, String trustFeeOperation, String trustFeeSeller, String trustFeeBank,
				String isinCode, String fundCode) {
			this.name                = name.trim();
			this.issuer              = issuer.trim();
			this.issueDate           = issueDate.trim().replace("/", "-");
			this.redemptionDate      = redemptionDate.trim().replace("/", "-");
			if (this.redemptionDate.equals("無期限")) {
				this.redemptionDate = Fund.NO_LIMIT;
			}
			
			this.settlementFrequency = settlementFrequency.trim();
			this.settlementDate      = settlementDate.trim();
			this.cancelationFee      = cancelationFee.trim();
			this.initialFeeLimit     = initialFeeLimit.trim();
			this.redemptionFee       = redemptionFee.trim();
			this.trustFee            = trustFee.trim();
			this.trustFeeOperation   = trustFeeOperation.trim();
			this.trustFeeSeller      = trustFeeSeller.trim();
			this.trustFeeBank        = trustFeeBank.trim();
			this.isinCode            = isinCode.trim();
			this.fundCode            = fundCode.trim();
		}

		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	private static class Context {
		private List<Fund> funds;
		private Integer buildCount;
		
		public Context() {
			funds = new ArrayList<>();
			buildCount = 0;
		}
		
		public void addFund(Fund fund) {
			synchronized(funds) {
				funds.add(fund);
			}
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
			
			if (string.contains("該当ファンドは存在しない")) {
				logger.warn("no fund data  {}", isinCode);
				return;
			}
			
			Toushin toushin = Toushin.getInstance(string);
			
			Fund fund = new Fund(
				toushin.isinCode, toushin.fundCode, 
				toushin.name, toushin.issuer, toushin.issueDate, toushin.redemptionDate,
				toushin.settlementFrequency, toushin.settlementDate, 
				toushin.cancelationFee, toushin.initialFeeLimit, toushin.redemptionFee,
				toushin.trustFee, toushin.trustFeeOperation, toushin.trustFeeSeller, toushin.trustFeeBank);
			
			context.addFund(fund);
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
		List<yokwe.stock.jp.jasdec.Fund> list = yokwe.stock.jp.jasdec.Fund.load();
		Collections.shuffle(list);
		
		final int listSize = list.size();

		for(var e: list) {
			String isinCode  = e.isinCode;
			String uriString = String.format(URL_FUND, isinCode);
			
			Task task = StringTask.text(uriString, new MyConsumer(context, isinCode));
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
		
		logger.info("save {} {}", context.funds.size(), Fund.getPath());
		Fund.save(context.funds);

		logger.info("STOP");
	}
}

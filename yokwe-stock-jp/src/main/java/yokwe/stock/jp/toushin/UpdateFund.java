package yokwe.stock.jp.toushin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

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
	
	private static void update(List<Fund> funds, HttpUtil httpUtil, String isinCode) {
//		logger.info("update {}", isinCode);
		String url = String.format(URL_FUND, isinCode);
		String page = httpUtil.download(url).result;
//		logger.info("page {}", page.length());
		
		if (page.contains("該当ファンドは存在しない")) {
			logger.warn("no fund data  {}", isinCode);
			return;
		}
		
		FileUtil.write().file("tmp/toushin.html", page);
		Toushin toushin = Toushin.getInstance(page);
		
		Fund fund = new Fund(
			toushin.isinCode, toushin.fundCode, 
			toushin.name, toushin.issuer, toushin.issueDate, toushin.redemptionDate,
			toushin.settlementFrequency, toushin.settlementDate, 
			toushin.cancelationFee, toushin.initialFeeLimit, toushin.redemptionFee,
			toushin.trustFee, toushin.trustFeeOperation, toushin.trustFeeSeller, toushin.trustFeeBank);
		
		funds.add(fund);
	}

	public static void main(String[] args) {
		logger.info("START");
		
		HttpUtil httpUtil = HttpUtil.getInstance();
		
		List<yokwe.stock.jp.jasdec.Fund> fundList = yokwe.stock.jp.jasdec.Fund.load();
		
		Collections.shuffle(fundList);
		
		List<Fund> funds = new ArrayList<>();
		
		int count = 0;
		for(var fund: fundList) {
			if ((count % 10) == 0) {
				logger.info("{} {}", String.format("%5d / %5d", count, fundList.size()), fund.isinCode);
			}
			count++;

			update(funds, httpUtil, fund.isinCode);
						
			// Make pause for not stress server
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error("Unexpected InterruptedException");
				
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException("Unexpected InterruptedException");
			}
		}
		
		logger.info("save {} {}", funds.size(), Fund.getPath());
		Fund.save(funds);
		
		logger.info("STOP");
	}
}

package yokwe.stock.jp.toushin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;

public class UpdatePage {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdatePage.class);

	public static class Page {
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
		
		public static Page getInstance(String page) {
			return ScrapeUtil.get(Page.class, PAT, page);
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
		
		public Page(String name, String issuer, String issueDate, String redemptionDate,
				String settlementFrequency, String settlementDate, String cancelationFee, String initialFeeLimit, String redemptionFee,
				String trustFee, String trustFeeOperation, String trustFeeSeller, String trustFeeBank,
				String isinCode, String fundCode) {
			this.name                = name.trim();
			this.issuer              = issuer.trim();
			this.issueDate           = issueDate.trim().replace("/", "-");
			this.redemptionDate      = redemptionDate.trim().replace("/", "-");
			if (this.redemptionDate.equals("無期限")) {
				this.redemptionDate = MutualFund.INDEFINITE;
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
	
	private static final String DOESNOT_EXIST = "該当ファンドは存在しない";
	public static void update() {
		logger.info("update");
		File dir = new File(DownloadFile.getPathPage(""));
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		List<MutualFund> list = new ArrayList<>();
		
		for(var file: dir.listFiles()) {
			if (file.isDirectory()) continue;
		
			String string = FileUtil.read().file(file);
			if (string.contains(DOESNOT_EXIST)) continue;
			
			Page page = Page.getInstance(string);
			MutualFund mutualFund = new MutualFund(
					page.isinCode, page.fundCode,
					0, 0,
					page.name, page.issuer, page.issueDate, page.redemptionDate,
					page.settlementFrequency, page.settlementDate, 
					page.cancelationFee, page.initialFeeLimit, page.redemptionFee,
					page.trustFee, page.trustFeeOperation, page.trustFeeSeller, page.trustFeeBank);

			list.add(mutualFund);
		}
		logger.info("save {} {}", list.size(), MutualFund.getPath());
		MutualFund.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
	

}

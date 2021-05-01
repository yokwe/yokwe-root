package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import yokwe.util.CSVUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;

public class MutualFund implements Comparable<MutualFund> {
	public static final String INDEFINITE = "9999-12-31";
	
	public static final String getPath() {
		return Toushin.getPath("mutual-fund.csv");
	}
	
	public static void save(List<MutualFund> list) {
		Collections.sort(list);
		CSVUtil.write(MutualFund.class).file(getPath(), list);
	}
	public static List<MutualFund> load() {
		return CSVUtil.read(MutualFund.class).file(getPath());
	}

	public String isinCode;             // isinCd
	public String fundCode;             // associFundCd
	
	@ScrapeUtil.Ignore
	public int    countPrice;
	@ScrapeUtil.Ignore
	public int    countSeller;
	
	@ScrapeUtil.Ignore
	public BigDecimal initialFeeMin;    // 購入時手数料 最小
	@ScrapeUtil.Ignore
	public BigDecimal initialFeeMax;    // 購入時手数料 最大
	
	public String issueDate;            // 設定日
	public String redemptionDate;       // 償還日
	public String cancelationFee;       // 解約手数料
	public String initialFeeLimit;      // 購入時手数料 上限
	public String redemptionFee;        // 信託財産留保額
	public String trustFee;             // 信託報酬 
	public String trustFeeOperation ;   // 信託報酬 運用会社
	public String trustFeeSeller;       // 信託報酬 販売会社
	public String trustFeeBank;         // 信託報酬 信託銀行

	public String settlementFrequency;  // 決算頻度
	public String settlementDate;       // 決算日

	public String issuer;               // 運用会社名
	public String name;
	
	
	public MutualFund(
			String isinCode, String fundCode,
			int countPrice, int countSeller,
			String name, String issuer, String issueDate, String redemptionDate,
			String settlementFrequency, String settlementDate, 
			String cancelationFee, String initialFeeLimit, String redemptionFee,
			String trustFee, String trustFeeOperation, String trustFeeSeller, String trustFeeBank) {
		this.isinCode            = isinCode;
		this.fundCode            = fundCode;
		this.countPrice          = countPrice;
		this.countSeller         = countSeller;
		this.name                = name;
		this.issuer              = issuer;
		this.issueDate           = issueDate;
		this.redemptionDate      = redemptionDate;
		this.settlementFrequency = settlementFrequency;
		this.settlementDate      = settlementDate;
		this.cancelationFee      = cancelationFee;
		this.initialFeeLimit     = initialFeeLimit;
		this.redemptionFee       = redemptionFee;
		this.trustFee            = trustFee;
		this.trustFeeOperation   = trustFeeOperation ;
		this.trustFeeSeller      = trustFeeSeller;
		this.trustFeeBank        = trustFeeBank;
	}
	public MutualFund() {
		this(null, null, 0, 0, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}

	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	@Override
	public int compareTo(MutualFund that) {
		return this.isinCode.compareTo(that.isinCode);
	}
}

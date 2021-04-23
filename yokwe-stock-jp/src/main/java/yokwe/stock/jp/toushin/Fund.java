package yokwe.stock.jp.toushin;

import java.util.Collections;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.CSVUtil;

public class Fund implements Comparable<Fund> {
	public static final String NO_LIMIT = "9999-12-31";
	public static final String PREFIX = "toushin";
	
	public static final String getPath() {
		return Storage.getPath(PREFIX, "fund.csv");
	}
	
	public static void save(List<Fund> funds) {
		Collections.sort(funds);
		CSVUtil.write(Fund.class).file(getPath(), funds);
	}
	public static List<Fund> load() {
		return CSVUtil.read(Fund.class).file(getPath());
	}

	public String fundCode;             // associFundCd
	public String isinCode;             // isinCd
	
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

	public Fund(
			String isinCode, String fundCode, 
			String name, String issuer, String issueDate, String redemptionDate,
			String settlementFrequency, String settlementDate, 
			String cancelationFee, String initialFeeLimit, String redemptionFee,
			String trustFee, String trustFeeOperation, String trustFeeSeller, String trustFeeBank) {
		this.isinCode            = isinCode;
		this.fundCode            = fundCode;
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
	public Fund() {
		this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}

	@Override
	public int compareTo(Fund that) {
		return this.fundCode.compareTo(that.fundCode);
	}
}

package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import yokwe.util.CSVUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;

public class MutualFund implements Comparable<MutualFund> {
	public static final LocalDate INDEFINITE = LocalDate.parse("9999-12-31");
	
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
	public int    countDividend;
	@ScrapeUtil.Ignore
	public int    countSeller;
	
	@ScrapeUtil.Ignore
	public BigDecimal initialFeeMin;    // 購入時手数料 最小
	@ScrapeUtil.Ignore
	public BigDecimal initialFeeMax;    // 購入時手数料 最大
	
	public String cat1; // 追加型 単位型
	public String cat2; // 国内 海外 内外
	public String cat3; // 債券 株式 資産複合
	public String cat4; // インデックス型 該当なし 特殊型
	
	public LocalDate issueDate;         // 設定日
	public LocalDate redemptionDate;    // 償還日
	public String cancelationFee;       // 解約手数料
	public String redemptionFee;        // 信託財産留保額
	
	public BigDecimal trustFee;             // 信託報酬 
	public BigDecimal trustFeeOperation ;   // 信託報酬 運用会社
	public BigDecimal trustFeeSeller;       // 信託報酬 販売会社
	public BigDecimal trustFeeBank;         // 信託報酬 信託銀行

	public int    settlementFrequency;  // 決算頻度
	public String settlementDate;       // 決算日

	public String issuer;               // 運用会社名
	public String name;
	
	
	public MutualFund(
			String isinCode, String fundCode,
			int countPrice, int countDividend, int countSeller,
			BigDecimal initialFeeMin, BigDecimal initialFeeMax,
			String cat1, String cat2, String cat3, String cat4,
			String name, String issuer, LocalDate issueDate, LocalDate redemptionDate,
			int settlementFrequency, String settlementDate, 
			String cancelationFee, String redemptionFee,
			BigDecimal trustFee, BigDecimal trustFeeOperation, BigDecimal trustFeeSeller, BigDecimal trustFeeBank) {
		this.isinCode            = isinCode;
		this.fundCode            = fundCode;
		this.countPrice          = countPrice;
		this.countDividend       = countDividend;
		this.countSeller         = countSeller;
		this.initialFeeMin       = initialFeeMin;
		this.initialFeeMax       = initialFeeMax;
		this.cat1				 = cat1;
		this.cat2				 = cat2;
		this.cat3				 = cat3;
		this.cat4				 = cat4;
		this.name                = name;
		this.issuer              = issuer;
		this.issueDate           = issueDate;
		this.redemptionDate      = redemptionDate;
		this.settlementFrequency = settlementFrequency;
		this.settlementDate      = settlementDate;
		this.cancelationFee      = cancelationFee;
		this.redemptionFee       = redemptionFee;
		this.trustFee            = trustFee;
		this.trustFeeOperation   = trustFeeOperation ;
		this.trustFeeSeller      = trustFeeSeller;
		this.trustFeeBank        = trustFeeBank;
	}
	public MutualFund() {
		this(null, null, 0, 0, 0, null, null, null, null, null, null, null, null, null, null, 0, null, null, null, null, null, null, null);
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

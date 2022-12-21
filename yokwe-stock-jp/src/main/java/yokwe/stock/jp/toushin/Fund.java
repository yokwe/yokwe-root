package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public final class Fund implements Comparable<Fund> {
	private static final String PATH = Storage.Toushin.getPath("fund.csv");
	public static String getPath() {
		return PATH;
	}

	public static void save(List<Fund> list) {
		ListUtil.checkDuplicate(list, o -> o.isinCode);
		ListUtil.save(Fund.class, getPath(), list);
	}
	
	public static List<Fund> load() {
		return ListUtil.load(Fund.class, getPath());
	}
	public static List<Fund> getList() {
		return ListUtil.getList(Fund.class, getPath());
	}
	public static Map<String, Fund> getMap() {
		//            stockCode
		var list = ListUtil.getList(Fund.class, getPath());
		return ListUtil.checkDuplicate(list, o -> o.isinCode);
	}
	
	public static final LocalDate NO_REDEMPTION_DATE = LocalDate.of(2999, 1, 1);

	// "isinCd" : "JP90C000DJ15",
    public String            isinCode;
    // "associFundCd" : "AE313167",
    public String            fundCode;
    // "establishedDate" : "2016-07-29 00:00:00",
    public LocalDate         listingDate;
	//  "redemptionDate" : "99999999",
    public LocalDate         redemptionDate;
	//  "setlFqcy" : "002",
    public int               divFreq;
    
    // "cancelLationFeeCd" : "1",
    public String            cancelLationFeeCode;
	//  "retentionMoneyCd" : "1",
    public String            retentionMoneyCode;


    //    運用管理費用 (信託報酬)
	//    "trustReward" : 0.925,
    public BigDecimal        expenseRatio;
    // 運用管理費用（信託報酬）運用会社
    // "entrustTrustReward" : 0.45,
    public BigDecimal        expenseRatioManagement;
    // 運用管理費用（信託報酬）販売会社	
    // "bondTrustReward" : 0.45,
    public BigDecimal        expenseRatioSales;
    // 運用管理費用（信託報酬）信託銀行
    // "custodyTrustReward" : 0.025,
    public BigDecimal        expenseRatioTrustBank;

    // 購入時手数料（上限）
    // "buyFee" : 0,
    public BigDecimal        buyFeeMax;
    
    // 単位型 追加型
    public String fundType;
    // 投資対象資産
    public String investingAsset;
    // 投資対象地域
    public String investingArea;
    // インデックスファンド区分
    public String indexFundType;
    
    // 決算日
    public String settlementDate;
    
    // LAST
    // "fundNm" : "あおぞら・グローバル・バランス・ファンド（部分為替ヘッジあり）",
    public String            name;
	
	
	public Fund(
		String isinCode, String fundCode, LocalDate listingDate, LocalDate redemptionDate, int divFreq, String name,
		BigDecimal expenseRatio, BigDecimal expenseRatioManagement, BigDecimal expenseRatioSales, BigDecimal expenseRatioTrustBank,
		BigDecimal buyFeeMax, String cancelationFeeCode, String retentionMoneyCode,
		String fundType, String investingArea, String investingAsset, String indexFundType, String settlementDate
		) {
		this.isinCode       = isinCode;
		this.fundCode       = fundCode;
		this.listingDate    = listingDate;
		this.redemptionDate = redemptionDate;
		this.divFreq        = divFreq;
		this.name           = name;
		
		this.expenseRatio           = expenseRatio;
		this.expenseRatioManagement = expenseRatioManagement;
		this.expenseRatioSales      = expenseRatioSales;
		this.expenseRatioTrustBank  = expenseRatioTrustBank;
		
		this.buyFeeMax = buyFeeMax;
		this.cancelLationFeeCode = cancelationFeeCode;
		this.retentionMoneyCode = retentionMoneyCode;
		
		this.fundType       = fundType;
		this.investingArea  = investingArea;
		this.investingAsset = investingAsset;
		this.indexFundType  = indexFundType;
		this.settlementDate = settlementDate;

		// sanity check
		
	}
	public Fund() {
		this(
			"", null, null, null, 0, null,
			null, null, null, null,
			null, null, null,
			null, null, null, null, null
			);
	}
	
    @Override
    public String toString() {
        return StringUtil.toString(this);
    }
    
    @Override
    public int compareTo(Fund that) {
    	return this.isinCode.compareTo(that.isinCode);
    }
    @Override
    public boolean equals(Object o) {
    	if (o == null) {
    		return false;
    	} else {
    		if (o instanceof Fund) {
    			Fund that = (Fund)o;
    			return this.compareTo(that) == 0;
    		} else {
    			return false;
    		}
    	}
    }
    @Override
    public int hashCode() {
    	return this.isinCode.hashCode();
    }
}

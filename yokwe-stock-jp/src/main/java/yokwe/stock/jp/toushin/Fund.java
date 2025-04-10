package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.ToString;

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
		//            isinCode
		var list = ListUtil.getList(Fund.class, getPath());
		return ListUtil.checkDuplicate(list, o -> o.isinCode);
	}
	
	public static final LocalDate NO_REDEMPTION_DATE        = LocalDate.of(2999, 1, 1);
	public static final String    NO_REDEMPTION_DATE_STRING = "99999999";
	
	// "isinCd" : "JP90C000DJ15",
    public String            isinCode;
    // "associFundCd" : "AE313167",
    public String            fundCode;
    
    // for ETF  stock code or NO_STOCK_CODE
    public String            stockCode;
    
    // "establishedDate" : "2016-07-29 00:00:00",
    public LocalDate         inceptionDate;
	//  "redemptionDate" : "99999999",
    public LocalDate         redemptionDate;
	//  "setlFqcy" : "002",
    public int               divFreq;
    
    //    運用管理費用 (信託報酬)
	//    "trustReward" : 0.925,
    public BigDecimal        expenseRatio;
    // 購入時手数料（上限）
    // "buyFee" : 0,
    public BigDecimal        buyFeeMax;
    
    // 単位型 追加型
    public String            fundType;
    // 投資対象資産
    public String            investingAsset;
    // 投資対象地域
    public String            investingArea;
    // インデックスファンド区分
    public String            indexFundType;
    
    // 決算日
    public String            settlementDate;
    
    // LAST
    // "fundNm" : "あおぞら・グローバル・バランス・ファンド（部分為替ヘッジあり）",
    public String            name;
	
	
	public Fund(
		String isinCode, String fundCode, String stockCode, LocalDate inceptionDate, LocalDate redemptionDate, int divFreq, String name,
		BigDecimal expenseRatio, BigDecimal buyFeeMax,
		String fundType, String investingArea, String investingAsset, String indexFundType, String settlementDate
		) {
		this.isinCode       = isinCode;
		this.fundCode       = fundCode;
		this.stockCode      = stockCode;
		this.inceptionDate  = inceptionDate;
		this.redemptionDate = redemptionDate;
		this.divFreq        = divFreq;
		this.name           = name;
		
		this.expenseRatio   = expenseRatio;
		this.buyFeeMax      = buyFeeMax;
		
		this.fundType       = fundType;
		this.investingArea  = investingArea;
		this.investingAsset = investingAsset;
		this.indexFundType  = indexFundType;
		this.settlementDate = settlementDate;

		// sanity check
		
	}
	public Fund() {
		this(
			"", null, null, null, null, 0, null,
			null, null,
			null, null, null, null, null
			);
	}
	
    @Override
    public String toString() {
        return ToString.withFieldName(this);
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

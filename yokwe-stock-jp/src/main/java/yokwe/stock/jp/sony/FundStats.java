package yokwe.stock.jp.sony;

import java.math.BigDecimal;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.sony.Fund.Region;
import yokwe.stock.jp.sony.Fund.Target;
import yokwe.util.ListUtil;

public class FundStats implements Comparable<FundStats> {
	private static final String PATH_FILE = Storage.Sony.getPath("fund-stats.csv");
	public static final String getPath() {
		return PATH_FILE;
	}

	public static void save(List<FundStats> list) {
		ListUtil.save(FundStats.class, getPath(), list);
	}
	
	public static List<FundStats> load() {
		return ListUtil.load(FundStats.class, getPath());
	}

	//
	// From Fund
	//
	public String isinCode; // IE0030804631
	public String category; // 国際REIT型
	public String fundName; // ワールド・リート・オープン（毎月決算型）
    public Company company;  // 081
    public String divFreq;  // 12
    public Region region;   // 01
    public Target target;   // 20
    public Currency currency; // JPY

    //
	// From FundInfo
	//
	// 設定日
	public String inceptionDate;
	// 償還日
	public String redemptionDate;
	// 決算日
	public String closingDate;
	// 信託報酬（税込）
	public BigDecimal trustFee;
	// 実質信託報酬（税込）
	public BigDecimal realTrustFee;
	// 信託財産留保額
	public BigDecimal cancelFee;
	
	//
	// From Price
	//
	public String     priceDate;
	public BigDecimal price1YCount;
	public BigDecimal price;
	public BigDecimal priceMin;
	public BigDecimal priceMax;
	public BigDecimal priceMinPCT;
	public BigDecimal priceMaxPCT;
	public BigDecimal netAsset;
	public BigDecimal netAssetMin;
	public BigDecimal netAssetMax;
	public BigDecimal netAssetMinPCT;
	public BigDecimal netAssetMaxPCT;
	
	
	//
	// From Dividend
	//
	public String     divDate;
    public BigDecimal div;
    public BigDecimal div1YCount;
    public BigDecimal div1Y;
    public BigDecimal yieldLast;
    public BigDecimal yield1Y;
    
	@Override
	public int compareTo(FundStats that) {
		return this.isinCode.compareTo(that.isinCode);
	}
}

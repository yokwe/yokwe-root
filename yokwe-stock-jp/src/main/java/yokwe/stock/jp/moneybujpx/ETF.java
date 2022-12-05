package yokwe.stock.jp.moneybujpx;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class ETF implements Comparable<ETF> {
	private static final String PATH = Storage.MoneyBuJPX.getPath("etf.csv");
	public static String getPath() {
		return PATH;
	}

	public static void save(List<ETF> list) {
		ListUtil.checkDuplicate(list, o -> o.stockCode);
		ListUtil.save(ETF.class, getPath(), list);
	}
	
	public static List<ETF> load() {
		return ListUtil.load(ETF.class, getPath());
	}
	public static List<ETF> getList() {
		return ListUtil.getList(ETF.class, getPath());
	}
	public static Map<String, ETF> getMap() {
		//            stockCode
		var list = ListUtil.getList(ETF.class, getPath());
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}

	
	public String     date;      // update date? YYYY-MM-DD
	public String     stockCode; // NNNNN
//	public int        unit;      // 売買単位
//	public int        exType;    // 0 for ETF
	
	public String     listingDate;   // YYYY/MM/DD
//	public BigDecimal managementFee; // 信託報酬 in percent
	public BigDecimal expenseRatio;

	// dividend
//	public BigDecimal dividend;      // 分配金…直近1年間の分配金額
//	public BigDecimal divAnnual;     // 分配金…直近1年間の分配金額
//	public String     dividendDate;  // （年4回）
	public int        divFreq;
//	public BigDecimal dividendYield; // 分配金利回り

	// price -- can be take from stock-price.csv
//	public BigDecimal  price;     // 終値・直近取引値
//	public String      priceDate; // YYYY/MM/DD
//	public long        volume;    // 売買高

	// description

	// category
	public String     categoryName;      // "不動産ETF"
//	public String     productType;       // "国内不動産"
//	public String     targetName;        // "東証REIT指数"
//	public String     targetDescription; // "東証REIT指数は、東京証券取引所に上場している不動産投信（J-REIT）全銘柄を対象とした「浮動株時価総額加重型」の指数です。JPX総研が算出・公表しています。算出方法は2003年3月31日の時価総額を1,000ポイントとして、その後の時価総額を指数化したものです。"

	public String     stockName; // "ＮＥＸＴ ＦＵＮＤＳ 東証ＲＥＩＴ指数連動型上場投信"
//	public String     feature;   // "わが国の不動産投信（J-REIT）市場全体の値動きを表す代表的な株価指数である「東証REIT指数｣との連動を目指すＥＴＦです。"

	
	// public DividendHist[]    dividendHist;
	
	// net asset
//	public boolean    hasNav;
//	public String     nav;        // 基準価額 "2,042.94"

//	public BigDecimal deviation; // 乖離率…前日市場価格÷前日基準価額を1口あたりに換算した値の割合を表示 -- 0.32

	// spread
//	public BigDecimal spread;     // スプレッド…最良の売気配値段と買気配値段の価格差（%）
//	public String     spreadDate; // YYYY/MM/DD
	

//	public long       rightUnit;  // 受益権口数
//	public String     sharesDate; // YYYY/MM/DD -- can be old data
	
//	public long       netAssets;     // 純資産総額
//	public String     netAssetsDate; // YYYY/MM/DD -- can be old data
	
	// trading value
//	public long       tradingValue; // 売買代金
//	public String     tvDate;       // YYYY/MM/DD
	
	// average trading values
//	public long        quarterTradingValue;  // 平均売買代金（直近90日）
//	public long        quarterTradingVolume; // 平均売買高（直近90日）
	
	
	public ETF(
			String     date,
			String     stockCode,
			String     listingDate,
			BigDecimal expenseRatio,
			
			int        divFreq,
			
			String     categoryName,
			String     stockName
			) {
			this.date          = date;
			this.stockCode     = stockCode;
			this.listingDate   = listingDate;
			this.expenseRatio  = expenseRatio;
			
			this.divFreq       = divFreq;
			
			this.categoryName  = categoryName;
			this.stockName     = stockName;
	}
	public ETF() {
		this(
			null, null, null, null,
			0,
			null, null);
	}
	
	@Override
	public String toString() {
	    return StringUtil.toString(this);
	}

	@Override
	public int compareTo(ETF that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	@Override
	public int hashCode() {
		return stockCode.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null) {
			if (o instanceof ETF) {
				ETF that = (ETF)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}

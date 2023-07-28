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

	
	public String     date;          // update date? YYYY-MM-DD
	public String     stockCode;     // NNNNN
	
	public String     listingDate;   // YYYY/MM/DD
	public BigDecimal expenseRatio;  // 信託報酬 in percent

	public int        divFreq;

	public String     categoryName;  // "不動産ETF"
	public String     targetIndex;   // "東証REIT指数"

	public String     stockName;     // "ＮＥＸＴ ＦＵＮＤＳ 東証ＲＥＩＴ指数連動型上場投信"
	
	
	public ETF(
			String     date,
			String     stockCode,
			String     listingDate,
			BigDecimal expenseRatio,
			
			int        divFreq,
			
			String     categoryName,
			String     targetIndex,
			String     stockName
			) {
			this.date          = date;
			this.stockCode     = stockCode;
			this.listingDate   = listingDate;
			this.expenseRatio  = expenseRatio;
			
			this.divFreq       = divFreq;
			
			this.categoryName  = categoryName;
			this.targetIndex   = targetIndex;
			this.stockName     = stockName;
	}
	public ETF() {
		this(
			null, null, null, null,
			0,
			null, null, null);
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

package yokwe.finance.provider.manebu;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class ETFInfo implements Comparable<ETFInfo> {
	private static final String PATH = Storage.provider_manebu.getPath("etf-info.csv");
	public static String getPath() {
		return PATH;
	}

	public static void save(List<ETFInfo> list) {
		ListUtil.checkDuplicate(list, o -> o.stockCode);
		ListUtil.save(ETFInfo.class, getPath(), list);
	}
	
	public static List<ETFInfo> load() {
		return ListUtil.load(ETFInfo.class, getPath());
	}
	public static List<ETFInfo> getList() {
		return ListUtil.getList(ETFInfo.class, getPath());
	}
	public static Map<String, ETFInfo> getMap() {
		//            stockCode
		var list = ListUtil.getList(ETFInfo.class, getPath());
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	
	
	public String     stockCode;
	public LocalDate  listingDate;
	
	public String     category;
	public String     productType;
	
	public BigDecimal expenseRatio;
		
	public int        divFreq;
	
	public BigDecimal shintakuRyuhogaku;
	
	public BigDecimal fundUnit; // can be 1, 10, 100, 1000

	public String     name;


	public ETFInfo(
		String stockCode, String category, BigDecimal expenseRatio, String name,
		int  divFreq, LocalDate listintDate,
		String productType, BigDecimal shintakuRyuhogaku, BigDecimal fundUnit) {
		this.stockCode    = stockCode;
		this.category     = category;
		this.expenseRatio = expenseRatio;
		this.name         = name;
		
		this.divFreq      = divFreq;
		this.listingDate  = listintDate;
		
		this.productType        = productType;
		this.shintakuRyuhogaku  = shintakuRyuhogaku;
		this.fundUnit           = fundUnit;
	}
	public ETFInfo() {}
	
	@Override
	public String toString() {
	    return StringUtil.toString(this);
	}

	@Override
	public int compareTo(ETFInfo that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	@Override
	public int hashCode() {
		return stockCode.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null) {
			if (o instanceof ETFInfo) {
				ETFInfo that = (ETFInfo)o;
				return
					this.stockCode.equals(that.stockCode) &&
					this.category.equals(that.category) &&
					this.expenseRatio.equals(that.expenseRatio) &&
					this.name.equals(that.name);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}

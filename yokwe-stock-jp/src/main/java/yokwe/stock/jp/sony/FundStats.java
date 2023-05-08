package yokwe.stock.jp.sony;

import java.math.BigDecimal;
import java.util.List;

import yokwe.stock.jp.Storage;
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
	public String isinCode;
	public String name;
	public String category;
	public String rating;

	//
	// From Price
	//
	public String     date;
	public int        pricec;
	public BigDecimal price;
	public BigDecimal priceMin;
	public BigDecimal priceMax;
	public double     priceRSI;
	public BigDecimal uam;
	public BigDecimal uamMin;
	public BigDecimal uamMax;
	public double     uamRSI;
	public BigDecimal unit;
	public BigDecimal unitMin;
	public BigDecimal unitMax;
	public double     unitRSI;
	
	//
	// From Dividend
	//
    public int        divc;
    public BigDecimal divLast;
    public BigDecimal div1Y;
    public BigDecimal yieldLast;
    public BigDecimal yield1Y;
    
	@Override
	public int compareTo(FundStats that) {
		return this.isinCode.compareTo(that.isinCode);
	}
}

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
	public String isinCode;
	public String name;
	public String category;

	//
	// From Price
	//
	public String     date;
	public int        pricec;
	public BigDecimal price;
	public BigDecimal priceMinPCT;
	public BigDecimal priceMaxPCT;
	public BigDecimal uam;
	public BigDecimal uamMinPCT;
	public BigDecimal uamMaxPCT;
	public BigDecimal unit;
	public BigDecimal unitMinPCT;
	public BigDecimal unitMaxPCT;
	
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

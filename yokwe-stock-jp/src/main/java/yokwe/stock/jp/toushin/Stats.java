package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class Stats implements Comparable<Stats> {
	private static final String PATH_FILE = Storage.Toushin.getPath("stats.csv");
	public static final String getPath() {
		return PATH_FILE;
	}
	public static void save(List<Stats> list) {
		ListUtil.save(Stats.class, getPath(), list);
	}
	public static List<Stats> load() {
		return ListUtil.load(Stats.class, getPath());
	}
	
	public String isinCode;             // isinCode
	public String fundCode;         // fundCode
	
	public LocalDate inception;
	public LocalDate redemption;
	
	public BigDecimal age; // yy.mm

	public String qCat1 = null;
	public String qCat2 = null;
	public String forex = null; // forex risk
	
	public String type; // index or active
	
	public LocalDate  date;
	public BigDecimal price;
	public BigDecimal nav;

	public int divc;

	public BigDecimal sd1Y;
	public BigDecimal sd3Y;
	public BigDecimal sd5Y;

	// reinvested return ratio -- annual
	public BigDecimal return1Y;
	public BigDecimal return3Y;
	public BigDecimal return5Y;

//	// sharpe ratio -- annual
//	// FIXME how to calculate annual sharpe ratio
//	public String sharpe1Y;
//	public String sharpe3Y;
//	public String sharpe5Y;

	public BigDecimal div1Y;
	public BigDecimal div3Y;
	public BigDecimal div5Y;
//
//	// yield -- annual
	public BigDecimal yield1Y;
	public BigDecimal yield3Y;
	public BigDecimal yield5Y;
	
	public BigDecimal divQ1Y;
	public BigDecimal divQ3Y;
	public BigDecimal divQ5Y;
	
	//
	public String name;
	public String seller;
//	public String description;
	
	
	
	
    @Override
    public String toString() {
        return StringUtil.toString(this);
    }
    @Override
    public int compareTo(Stats that) {
    	return this.isinCode.compareTo(that.isinCode);
    }
    @Override
    public boolean equals(Object o) {
    	if (o == null) {
    		return false;
    	} else {
    		if (o instanceof Fund) {
    			Stats that = (Stats)o;
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

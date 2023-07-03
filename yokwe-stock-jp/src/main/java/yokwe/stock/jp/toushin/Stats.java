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
	
	public String isinCode;         // isinCode
	public String fundCode;         // fundCode
	public String stockCode;        // stockCode
	
	public LocalDate inception;
	public LocalDate redemption;
	
	public BigDecimal age; // yy.mm
	
	public String investingAsset;
	public String investingArea;
	public String indexFundType;
	
	public BigDecimal expenseRatio;
    public BigDecimal buyFeeMax;
	public BigDecimal nav;
	public int        divc;
	
	public String sd1Y;
	public String sd3Y;
	public String sd5Y;
	public String sd10Y;

	public String return1Y;
	public String return3Y;
	public String return5Y;
	public String return10Y;
	
	public String div1Y;
	public String div3Y;
	public String div5Y;
	public String div10Y;
	
	public String yield1Y;
	public String yield3Y;
	public String yield5Y;
	public String yield10Y;
	
	public String divQ1Y;
	public String divQ3Y;
	public String divQ5Y;
	public String divQ10Y;
	
	//
	public String name;
	
	public String gmo;
	public String nikko;
	public String nomura;
	public String rakuten;
	public String sony;
	
	
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

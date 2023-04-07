package yokwe.stock.jp.jpx;

import java.math.BigDecimal;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;

public class StockStats implements Comparable<StockStats> {
	private static final String PATH_FILE = Storage.JPX.getPath("stock-stats.csv");
	public static final String getPath() {
		return PATH_FILE;
	}

	public static void save(List<StockStats> list) {
		ListUtil.save(StockStats.class, getPath(), list);
	}
	
	public static List<StockStats> load() {
		return ListUtil.load(StockStats.class, getPath());
	}

	public String     stockCode;
	public String     name;
	public String     category;
	public int        unit;

	public BigDecimal issued;

	// price
	public String     priceDate;
	public int        pricec;
	public BigDecimal price;
	public BigDecimal marketCap;
	public BigDecimal priceLastPCT;
	public BigDecimal priceMinPCT;
	public BigDecimal priceMaxPCT;
	public BigDecimal sd;
	public BigDecimal hv;
	public BigDecimal rsi;

	// dividend
    public int        divc;
    public BigDecimal divLast;
    public BigDecimal div1Y;
    public BigDecimal yieldLast;
    public BigDecimal yield1Y;
    
    // trade volume
    public BigDecimal vol;
    public BigDecimal vol5;
    public BigDecimal vol21;


	@Override
	public int compareTo(StockStats that) {
		return this.stockCode.compareTo(that.stockCode);
	}

}

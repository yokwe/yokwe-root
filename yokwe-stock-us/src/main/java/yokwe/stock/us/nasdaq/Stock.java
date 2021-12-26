package yokwe.stock.us.nasdaq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.us.Storage;
import yokwe.stock.us.nasdaq.api.AssetClass;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class Stock implements Comparable<Stock> {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Stock.class);

	private static final String PATH_FILE = Storage.NASDAQ.getPath("stock.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static void save(Collection<Stock> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<Stock> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(Stock.class).file(getPath(), list);
	}
	
	public static List<Stock> getList() {
		List<Stock> ret = CSVUtil.read(Stock.class).file(getPath());
		return ret == null ? new ArrayList<>() : ret;
	}
	public static Map<String, Stock> getMap() {
		Map<String, Stock> map = new TreeMap<>();
		for(var e: getList()) {
			final String key = e.symbol;
			if (map.containsKey(key)) {
				logger.error("Unexpected duplicate");
				logger.error("  old {}", map.get(key));
				logger.error("  new {}", e);
				throw new UnexpectedException("Unexpected duplicate");
			} else {
				map.put(key, e);
			}
		}
		return map;
	}
	
	public String     symbol;           // normalized symbol like TRNT-A and RDS.A not like TRTN^A and RDS/A
	public AssetClass assetClass;       // STOCKS or ETF
	public String     complianceStatus; // empty or "OUT OF COMPLIANCE"
	
	public String country;          // only for STOCKS
	public String industry;         // only for STOCKS
	public String sector;           // only for STOCKS
	
	public String name;             // copy from Quote.Info

	
	public Stock(
		String symbol, AssetClass assetClass, String complianceStatus, 
		String country, String industry, String sector,
		String name
		) {
		this.symbol           = symbol.trim();
		this.assetClass       = assetClass;
		this.complianceStatus = complianceStatus.trim();
				
		this.country  = country.trim();
		this.industry = industry.trim();
		this.sector   = sector.trim();
		
		this.name   = name.trim();
	}
	public Stock() {
		this(
			"", AssetClass.STOCK, "",
			"", "", "",
			""
			);
	}
	
	@Override
	public int compareTo(Stock that) {
		return this.symbol.compareTo(that.symbol);
	}

	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}

package yokwe.stock.us.nasdaq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.us.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.DoubleUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;

public class Dividend implements Comparable<Dividend> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final String PATH_DIR = Storage.NASDAQ.getPath("div");
	public static String getPath(String symbol) {
		return String.format("%s/%s.csv", PATH_DIR, symbol);
	}
	
	public static void save(Collection<Dividend> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<Dividend> list) {
		if (list.isEmpty()) return;
		String symbol = list.get(0).symbol;
		
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(Dividend.class).file(getPath(symbol), list);
	}
	
	public static List<Dividend> getList(String symbol) {
		List<Dividend> ret = CSVUtil.read(Dividend.class).file(getPath(symbol));
		return ret == null ? new ArrayList<>() : ret;
	}
	public static Map<String, Dividend> getMap(String symbol) {
		//            exDate
		Map<String, Dividend> ret = new TreeMap<>();
		
		for(var e: getList(symbol)) {
			String exDate = e.exDate;
			if (ret.containsKey(exDate)) {
				logger.error("Duplicate exDate");
				logger.error("  old {}", ret.get(exDate));
				logger.error("  new {}", e);
				throw new UnexpectedException("Duplicate exDate");
			} else {
				ret.put(exDate, e);
			}
		}
		
		return ret;
	}
	
	// "exOrEffDate":"10/27/2021","type":"CASH","amount":"$0.12","declarationDate":"01/18/2021","recordDate":"10/28/2021","paymentDate":"10/29/2021"
	public String symbol; // normalized symbol like TRNT-A and RDS.A not like TRTN^A and RDS/A
	public String type;
	@CSVUtil.DecimalPlaces(6)
	public double amount;
	public String declDate;   // YYYY-MM-DD
	public String exDate;     // YYYY-MM-DD
	public String recordDate; // YYYY-MM-DD
	public String payDate;    // YYYY-MM-DD
	
	public Dividend(String symbol, String type, double amount, String declDate, String exDate, String recordDate, String payDate) {
		this.symbol     = symbol == null ? null : symbol.trim();
		this.type       = type == null ? null : type.trim();
		this.amount     = DoubleUtil.roundQuantity(amount);
		this.declDate   = declDate;
		this.exDate     = exDate;
		this.recordDate = recordDate;
		this.payDate    = payDate;
	}
	public Dividend() {
		this(null, null, 0, null, null, null, null);
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}

	@Override
	public int compareTo(Dividend that) {
		int ret = this.symbol.compareTo(that.symbol);
		if (ret == 0) ret= this.exDate.compareTo(that.exDate);
		return ret;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Dividend) {
			Dividend that = (Dividend) o;
			return
				this.symbol.equals(that.symbol) &&
				this.type.equals(that.type) &&
				DoubleUtil.isAlmostEqual(this.amount,  that.amount) &&
				this.declDate.equals(that.declDate) &&
				this.exDate.equals(that.exDate) &&
				this.recordDate.equals(that.recordDate) &&
				this.payDate.equals(that.payDate);
		} else {
			return false;
		}
	}
}

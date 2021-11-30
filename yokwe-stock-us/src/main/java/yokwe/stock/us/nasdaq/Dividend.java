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
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class Dividend implements Comparable<Dividend> {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Dividend.class);

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
		//            recordDate
		Map<String, Dividend> ret = new TreeMap<>();
		
		for(var e: getList(symbol)) {
			String recordDate = e.recordDate;
			if (ret.containsKey(recordDate)) {
				logger.error("Duplicate payDate");
				logger.error("  recordDate {}", recordDate);
				logger.error("  old {}", ret.get(symbol));
				logger.error("  new {}", e);
				throw new UnexpectedException("Duplicate payDate");
			} else {
				ret.put(recordDate, e);
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
	public String recordDate; // YYYY-MM-DD
	public String payDate;    // YYYY-MM-DD
	
	public Dividend(String symbol, String type, double amount, String declDate, String recordDate, String payDate) {
		this.symbol     = symbol == null ? null : symbol.trim();
		this.type       = type == null ? null : type.trim();
		this.amount     = DoubleUtil.roundQuantity(amount);
		this.declDate   = declDate;
		this.recordDate = recordDate;
		this.payDate    = payDate;
	}
	public Dividend() {
		this(null, null, 0, null, null, null);
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}

	@Override
	public int compareTo(Dividend that) {
		int ret = this.symbol.compareTo(that.symbol);
		if (ret == 0) ret= this.recordDate.compareTo(that.recordDate);
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
				this.recordDate.equals(that.recordDate) &&
				this.payDate.equals(that.payDate);
		} else {
			return false;
		}
	}
}

package yokwe.stock.jp.sony;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.LoggerFactory;

import yokwe.util.CSVUtil;

public class Dividend implements Comparable<Dividend> {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Dividend.class);

	public static final String PATH_DIR_DATA = "tmp/data/sony/dividend"; // FIXME
	public static String getPath(String stockCode) {
		return String.format("%s/%s.csv", PATH_DIR_DATA, stockCode);
	}

	public static void save(Collection<Dividend> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<Dividend> list) {
		if (list.isEmpty()) return;
		Dividend price = list.get(0);
		String isinCode = price.isinCode;
		String path = getPath(isinCode);
		
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(Dividend.class).file(path, list);
	}

	public static List<Dividend> getList(String isinCode) {
		String path = getPath(isinCode);
		List<Dividend> ret = CSVUtil.read(Dividend.class).file(path);
		return ret == null ? new ArrayList<>() : ret;
	}

	
	public LocalDate  date;
	public String     isinCode;
	public Currency   currency;
	public BigDecimal dividend;
	
	public Dividend(LocalDate date, String isinCode, Currency currency, BigDecimal dividend) {
		this.date     = date;
		this.isinCode = isinCode;
		this.currency = currency;
		this.dividend = dividend;
	}
	public Dividend() {
		this(null, null, null, null);
	}

	@Override
	public int compareTo(Dividend that) {
		int ret = this.date.compareTo(that.date);
		if (ret == 0) ret = this.isinCode.compareTo(that.isinCode);
		return ret;
	}

	
	public static void main(String[] args) {
		logger.info("START");
		
		
		logger.info("STOP");
	}
}

package yokwe.security.japan.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import yokwe.security.japan.jpx.Stock;
import yokwe.security.japan.xbrl.report.REITReport;
import yokwe.security.japan.xbrl.report.StockReport;
import yokwe.util.CSVUtil;

public class T024 implements Comparable<T024> {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T024.class);

	public String  stockCode;
	public String  yearEnd;
	
	public T024(String stockCode, String yearEnd) {
		this.stockCode        = stockCode;
		this.yearEnd          = yearEnd;
	}
	public T024() {
		this("", "");
	}
	@Override
	public String toString() {
		return String.format("{%s %s}", stockCode, yearEnd);
	}
	@Override
	public int compareTo(T024 that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		Map<String, String> yearEndMap = new TreeMap<>();
		
		{
			List<StockReport> list = StockReport.getList();
			Collections.sort(list, (a, b) -> a.filename.compareTo(b.filename));
			for(StockReport e: list) {
				yearEndMap.put(e.stockCode, e.yearEnd);
			}
		}
		{
			List<REITReport> list = REITReport.getList();
			Collections.sort(list, (a, b) -> a.filename.compareTo(b.filename));
			for(REITReport e: list) {
				yearEndMap.put(e.stockCode, e.yearEnd);
			}
		}
		{
			Map<String, Stock> stockMap = Stock.getMap();

			List<T024> list = new ArrayList<>();
			for(Map.Entry<String, String> entry: yearEndMap.entrySet()) {
				String stockCode = entry.getKey();
				String yearEnd   = entry.getValue().substring(5);
				if (stockMap.containsKey(stockCode)) {
					list.add(new T024(stockCode, yearEnd));
				}
			}
			Collections.sort(list);
			CSVUtil.write(T024.class).file("tmp/data/year-end.csv", list);
		}
		
		logger.info("yearEndMap {}", yearEndMap.size());
		
		logger.info("STOP");
	}
}

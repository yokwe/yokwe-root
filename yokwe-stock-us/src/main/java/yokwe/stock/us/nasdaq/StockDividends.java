package yokwe.stock.us.nasdaq;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import yokwe.stock.us.Storage;
import yokwe.stock.us.nasdaq.api.API;
import yokwe.stock.us.nasdaq.api.Dividends;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;

public class StockDividends implements CSVUtil.Detail {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StockDividends.class);

	private static final String PATH_FILE = Storage.NASDAQ.getPath("stock-dividends.csv");
	
	private static final int DIVIDENDS_COUNT = 12 + 4;
	
	public static void save(List<StockDividends> list) {
		CSVUtil.save(StockDividends.class, list);
	}
	public static List<StockDividends> getList() {
		return CSVUtil.getList(StockDividends.class);
	}
	public static Map<String, StockDividends> getMap() {
		return CSVUtil.getMap(StockDividends.class);
	}
	
	public static void download(List<StockList> stockList) {
		List<StockList> list = new ArrayList<>();
		for(var e: stockList) {
			File file = new File(Dividends.getPath(e.symbol));
			if (file.exists()) continue;
			list.add(e);
		}
		logger.info("StockDividends download {}", list.size());
		
		int countTotal = list.size();
		int count = 0;
		for(var e: list) {
			if ((count % 100) == 0) logger.info("{}", String.format("%5d / %5d  %s", count, countTotal, e.symbol));
			count++;
			
			Dividends.getInstance(e.symbol, e.assetClass, DIVIDENDS_COUNT);
		}
	}
	public static void update(List<StockList> stockList) {
		logger.info("StockDividends update {}", stockList.size());
		List<StockDividends> list = new ArrayList<>(stockList.size());
		
		int countTotal = stockList.size();
		int count = 0;
		for(var e: stockList) {
			if ((count % 1000) == 0) logger.info("{}", String.format("%5d / %5d  %s", count, countTotal, e.symbol));
			count++;
			
			Dividends dividends = Dividends.getInstance(e.symbol, e.assetClass, DIVIDENDS_COUNT);
			if (dividends.data == null) {
				logger.warn("data is null {}", e.symbol);
				continue;
			}
			if (dividends.data.dividends == null) {
				logger.warn("dividends is null {}", e.symbol);
				continue;
			}
			if (dividends.data.dividends.rows == null) {
//				logger.warn("data is rows {}", e.symbol);
				continue;
			}
			
			for(var ee: dividends.data.dividends.rows) {
				String symbol = e.symbol;
				//
				String exOrEffDate     = API.convertDate(ee.exOrEffDate);
				String type            = ee.type.replace("N/A", "");
				String amount          = ee.amount.replace("$", "").replace("N/A", "");
				String declarationDate = API.convertDate(ee.declarationDate);
				String recordDate      = API.convertDate(ee.recordDate);
				String paymentDate     = API.convertDate(ee.paymentDate);

				list.add(new StockDividends(symbol, amount, declarationDate, exOrEffDate, paymentDate, recordDate, type));
			}			
		}
		StockDividends.save(list);

		logger.info("save {} {}", list.size(), StockDividends.PATH_FILE);

	}
	
	public String symbol          = null;
	
	public String amount          = null;
	public String type            = null;
	public String declarationDate = null;
	public String exOrEffDate     = null;
	public String recordDate      = null;
	public String paymentDate     = null;

	public StockDividends(
			String symbol,
			//
			String amount,
			String declarationDate,
			String exOrEffDate,
			String paymentDate,
			String recordDate,
			String type
			) {
		this.symbol          = symbol;
		//
		this.amount          = amount;
		this.declarationDate = declarationDate;
		this.exOrEffDate     = exOrEffDate;
		this.paymentDate     = paymentDate;
		this.recordDate      = recordDate;
		this.type            = type;
	}
	
	public StockDividends() {}

	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	@Override
	public String getPath() {
		return PATH_FILE;
	}
	@Override
	public String getKey() {
		return symbol;
	}
}

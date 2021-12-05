package yokwe.stock.us.nasdaq;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import yokwe.stock.us.Storage;
import yokwe.stock.us.nasdaq.api.Info;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;

public class StockInfo implements CSVUtil.Detail {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StockList.class);

	private static final String PATH_FILE = Storage.NASDAQ.getPath("stock-info.csv");
	
	public static void save(List<StockInfo> list) {
		CSVUtil.save(StockInfo.class, list);
	}
	public static List<StockInfo> getList() {
		return CSVUtil.getList(StockInfo.class);
	}
	public static Map<String, StockInfo> getMap() {
		return CSVUtil.getMap(StockInfo.class);
	}
	
	public static void download(List<StockList> stockList) {
		List<StockList> list = new ArrayList<>();
		for(var e: stockList) {
			File file = new File(Info.getPath(e.symbol));
			if (file.exists()) continue;
			list.add(e);
		}
		logger.info("StockInfo download {}", list.size());
		
		int countTotal = list.size();
		int count = 0;
		for(var e: list) {
			if ((count % 100) == 0) logger.info("{}", String.format("%5d / %5d  %s", count, countTotal, e.symbol));
			count++;
			
			Info.getInstance(e.symbol, e.assetClass);
		}
	}
	public static void update(List<StockList> stockList) {
		logger.info("StockInfo update {}", stockList.size());
		List<StockInfo> list = new ArrayList<>(stockList.size());
		
		int countTotal = stockList.size();
		int count = 0;
		for(var e: stockList) {
			if ((count % 1000) == 0) logger.info("{}", String.format("%5d / %5d  %s", count, countTotal, e.symbol));
			count++;
			
			Info info = Info.getInstance(e.symbol, e.assetClass);
			if (info.data == null) {
				logger.warn("data is null {}", e.symbol);
				continue;
			}
			
			String symbol           = e.symbol; // use normalized symbol
			String companyName      = info.data.companyName;
			String complianceStatus = info.data.complianceStatus == null ? "" : info.data.complianceStatus.message;
			String exchange         = info.data.exchange;

			String lastSalePrice      = info.data.primaryData.lastSalePrice;
			String lastTradeTimestamp = info.data.primaryData.lastTradeTimestamp;

			StockInfo stockInfo = new StockInfo(
				symbol, companyName, complianceStatus, exchange,
				lastSalePrice, lastTradeTimestamp);
			list.add(stockInfo);
		}
		
		StockInfo.save(list);
		logger.info("save {} {}", list.size(), StockInfo.PATH_FILE);
	}
			
	public String symbol           = null;
	public String companyName      = null;
	public String complianceStatus = null;
	public String exchange         = null;

	public String lastSalePrice      = null;
	public String lastTradeTimestamp = null;
	
	public StockInfo(
			String symbol,
			String companyName,
			String complianceStatus,
			String exchange,

			String lastSalePrice,
			String lastTradeTimestamp
		) {
		this.symbol           = symbol;
		this.companyName      = companyName;
		this.complianceStatus = complianceStatus;
		this.exchange         = exchange;

		this.lastSalePrice      = lastSalePrice;
		this.lastTradeTimestamp = lastTradeTimestamp;
	}
	public StockInfo() {}

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
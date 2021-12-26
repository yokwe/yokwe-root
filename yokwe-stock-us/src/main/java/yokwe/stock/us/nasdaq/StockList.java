package yokwe.stock.us.nasdaq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.stock.us.Storage;
import yokwe.stock.us.nasdaq.api.API;
import yokwe.stock.us.nasdaq.api.AssetClass;
import yokwe.stock.us.nasdaq.api.Screener;
import yokwe.util.CSVUtil;

public class StockList implements CSVUtil.Detail {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StockList.class);

	public static final String PATH_FILE = Storage.NASDAQ.getPath("stock-list.csv");

	public static void save(Collection<StockList> collection) {
		CSVUtil.save(StockList.class, collection);
	}
	public static List<StockList> getList() {
		return CSVUtil.getList(StockList.class);
	}
	public static Map<String, StockList> getMap() {
		return CSVUtil.getMap(StockList.class);
	}
	
	public static void update() {
		logger.info("StockList update");
		List<StockList> list = new ArrayList<>();
		
		// ETF
		{
			Screener.ETF instance = Screener.ETF.getInstance();
			logger.info("etf     {}", instance.data.data.rows.length);
			for(var e: instance.data.data.rows) {
				String symbol = API.normalizeSymbol(e.symbol.trim());
				list.add(new StockList(symbol, AssetClass.ETF));
			}
		}
		// Stock
		{
			Screener.Stock instance = Screener.Stock.getInstance();
			logger.info("stock   {}", instance.data.rows.length);
			for(var e: instance.data.rows) {
				String symbol = API.normalizeSymbol(e.symbol.trim());
				list.add(new StockList(symbol, AssetClass.STOCK));
			}
		}
		
		StockList.save(list);
		logger.info("save {} {}", list.size(), StockList.PATH_FILE);
	}
	
	public String symbol         = null;
	public AssetClass assetClass = null;
	
	public StockList(String symbol, AssetClass assetClass) {
		this.symbol     = symbol;
		this.assetClass = assetClass;
	}
	// Default constructor for newInstance
	public StockList() {}

	@Override
	public String getPath() {
		return PATH_FILE;
	}

	@Override
	public String getKey() {
		return symbol;
	}
}
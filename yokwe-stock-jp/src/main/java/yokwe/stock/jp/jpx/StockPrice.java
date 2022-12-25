package yokwe.stock.jp.jpx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;

public class StockPrice implements Comparable<StockPrice> {
	private static final String PATH_FILE = Storage.JPX.getPath("stock-price.csv");

	public static String getPath() {
		return PATH_FILE;
	}

	public static List<StockPrice> getList() {
		return ListUtil.getList(StockPrice.class, getPath());
	}
	public static void save(Collection<StockPrice> collection) {
		ListUtil.save(StockPrice.class, getPath(), collection);
		save(new ArrayList<>(collection));
	}
	public static void save(List<StockPrice> list) {
		ListUtil.save(StockPrice.class, getPath(), list);
	}
	
	public String date;      // YYYY-MM-DD -- taken from file modified time
	public String time;      // HH:mm      -- taken from file modified time
	public String stockCode; // 5 digits
	
	public String price;
	public String priceTime; // HH:mm or empty
	
	public String sell;
	public String sellTime;  // HH:mm or empty
	
	public String buy;
	public String buyTime;   // HH:mm or empty
	
	public String open;
	public String high;
	public String low;

	public String volume;
	public String trade;
	
	public String lastClose;
	
	public StockPrice(
			String date,
			String time,
			String stockCode,
			
			String price,
			String priceTime,
			
			String sell,
			String sellTime,
			
			String buy,
			String buyTime,
			
			String open,
			String high,
			String low,
			
			String volume,
			String trade,
			
			String lastClose
		) {
		this.date = date;
		this.time = time;
		this.stockCode = stockCode;
		
		this.price = price;
		this.priceTime = priceTime;
		
		this.sell = sell;
		this.sellTime = sellTime;
		
		this.buy = buy;
		this.buyTime = buyTime;
		
		this.open = open;
		this.high = high;
		this.low = low;
		
		this.volume = volume;
		this.trade = trade;
		
		this.lastClose = lastClose;
	}
	public StockPrice() {
		this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}
	
	@Override
	public int compareTo(StockPrice that) {
		int ret = 0;
		if (ret == 0) ret = this.stockCode.compareTo(that.stockCode);
		if (ret == 0) ret = this.date.compareTo(that.date);
		if (ret == 0) ret = this.time.compareTo(that.time);
		return ret;
	}
}

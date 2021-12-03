package yokwe.stock.us.nasdaq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import yokwe.stock.us.nasdaq.api.Info;
import yokwe.stock.us.nasdaq.api.Screener;
import yokwe.util.StringUtil;

public class UpdateStock {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateStock.class);
	
	public static String normalizeSymbol(String symbol) {
		// TRTN^A => TRTN-A
		// BRK/A  => BRK.A
		return symbol.replace('^', '-').replace('/', '.');
	}
	
	public static class Request {
		static Request getStock(String symbol, String name, String country, String industrial, String sector) {
			return new Request(false, symbol, name, country, industrial, sector);
		}
		static Request getETF(String symbol, String name) {
			return new Request(true, symbol, name, "", "", "");
		}
		
		public boolean isETF;
		public String  symbol;
		public String  name;
		
		public String country;
		public String industrial;
		public String sector;
		
		Request(boolean isETF, String symbol, String name, String country, String industrial, String sector) {
			this.isETF  = isETF;
			this.symbol = symbol.trim();
			this.name   = name.trim();
			
			this.country    = country.trim();
			this.industrial = industrial.trim();
			this.sector     = sector.trim();
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");		
		
		List<Stock> list = Stock.getList();
		logger.info("list    {}", list.size());

		List<Request> requestList = new ArrayList<>();
		
		// build requestList
		{
			Map<String, Stock> map = Stock.getMap();
			
			// ETF
			{
				Screener.ETF instance = Screener.ETF.getInstance();
				logger.info("etf     {}", instance.data.data.rows.length);
				for(var e: instance.data.data.rows) {
					String symbol = normalizeSymbol(e.symbol.trim());
					
					if (map.containsKey(symbol)) continue;				
					
					requestList.add(Request.getETF(symbol, e.companyName));
				}
			}
			
			// Stock
			{
				Screener.Stock instance = Screener.Stock.getInstance();
				logger.info("stock   {}", instance.data.rows.length);
				for(var e: instance.data.rows) {
					String symbol = normalizeSymbol(e.symbol.trim());
					
					if (map.containsKey(symbol)) continue;				
					
					requestList.add(Request.getStock(symbol, e.name, e.country, e.industry, e.sector));
				}
			}
		}
		
		logger.info("request {}", requestList.size());
		Collections.shuffle(requestList);
		
		int count = 0;
		int toatlCount = requestList.size();
		for(var e: requestList) {
			if ((count % 100) == 0) {
				logger.info("{}", String.format("%5d / %5d %s", count, toatlCount, e.symbol));
				Stock.save(list);
			}
			count++;
			
			Info info;
			if (e.isETF) {
				info = Info.getETF(e.symbol);
			} else {
				info = Info.getStock(e.symbol);
			}
			
			if (info == null) {
				// 	not found?
			} else {
				Stock stock = new Stock(
						e.symbol, info.data.assetClass, (info.data.complianceStatus == null) ? "" : info.data.complianceStatus.header,
						e.country, e.industrial, e.sector,
						info.data.companyName);
				list.add(stock);
			}
			
		}
		
		logger.info("save {} {}", Stock.getPath(), list.size());
		Stock.save(list);
		
		logger.info("STOP");
	}
}

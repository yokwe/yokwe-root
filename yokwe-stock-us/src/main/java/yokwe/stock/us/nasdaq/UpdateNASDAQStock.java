package yokwe.stock.us.nasdaq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.us.Stock;
import yokwe.stock.us.Stock.Market;
import yokwe.stock.us.Stock.Type;
import yokwe.stock.us.nasdaq.symbolDirectory.NASDAQListed;
import yokwe.stock.us.nasdaq.symbolDirectory.OtherListed;
import yokwe.stock.us.nyse.NYSEStock;
import yokwe.util.UnexpectedException;

public class UpdateNASDAQStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Map<String, Market> marketMap = new TreeMap<>();
	static {
		//   A = NYSE MKT
		//   N = New York Stock Exchange (NYSE)
		//   P = NYSE ARCA
		//   Z = BATS Global Markets (BATS)
		//   V = Investors' Exchange, LLC (IEXG)

		marketMap.put("A", Market.NYSE);   // NYSE MKT
		marketMap.put("N", Market.NYSE);   // New York Stock Exchange (NYSE)
		marketMap.put("P", Market.NYSE);   // NYSE ARCA
		marketMap.put("Z", Market.BATS);   // BATS Global Markets (BATS)
		marketMap.put("V", Market.IEXG);   // Investors' Exchange, LLC (IEXG)
	}

	public static void main(String[] args) {
		logger.info("START");
		
//		DownloadUtil.updateNASDAQListed();
//		DownloadUtil.updateOtherListed();
		
		Map<String, Stock> nyseMap = NYSEStock.getMap();
		logger.info("nyse {}", nyseMap.size());

		
		List<Stock> list = new ArrayList<>();
		{
			var nasdaqList = NASDAQListed.getList();
			logger.info("nasdaq list {}", nasdaqList.size());
			for(var e: nasdaqList) {
				if (e.isTestIssue()) continue; // skip test issue
				if (e.isRights())    continue; // skip right
				if (e.isUnits())     continue; // skip unit
				if (e.isWarrant())   continue; // skip warrant

				String symbol = e.symbol;
				Market market = Market.NASDAQ;
				Type   type;
				String name = e.name.replace(",", "");
				
				// sanity check
				if (nyseMap.containsKey(symbol)) {
					Stock nyseStock = nyseMap.get(symbol);
					if (nyseStock.market != Market.NASDAQ) {
						 logger.warn("nasdaq Unexpected market  {}  {}  {}", symbol, nyseStock.market, name);
					}
					// type
					if (e.etf.compareTo("Y") == 0) {
						if (nyseStock.type != Type.ETF) {
							 logger.warn("nasdaq type  {}", symbol, nyseStock.type, name);
						}
						type = Type.ETF;
					} else {
						type = nyseStock.type;
					}
				} else {
					if (name.toLowerCase().contains("warrant"))             continue; // skip warrant
					if (name.toLowerCase().contains("beneficial interest")) continue; // skip beneficial interest
					
					logger.warn("nasdaq skip unknown  {}  {}", symbol, name);
					continue;
				}

				list.add(new Stock(symbol, market, type, name));
			}
			
			var otherList = OtherListed.getList();
			logger.info("other  list {}", otherList.size());
			for(var e: otherList) {
				if (e.isTestIssue()) continue; // skip test issue
				if (e.isRights())    continue; // skip right
				if (e.isUnits())     continue; // skip unit
				if (e.isWarrant())   continue; // skip warrant
				
				String symbol = e.symbol;
				Market market;
				Type   type;
				String name = e.name.replace(",", "");

				if (marketMap.containsKey(e.exchange)) {
					market = marketMap.get(e.exchange);
				} else {
					logger.error("Unpexpected exchange");
					logger.error("  {}", e.exchange);
					throw new UnexpectedException("Unpexpected exchange");
				}

				// sanity check
				if (nyseMap.containsKey(symbol)) {
					Stock nyseStock = nyseMap.get(symbol);
					// market
					if (nyseStock.market != market) {
						 logger.warn("other Unexpected market  {}  nyse {}  nasdaq {}  {}", symbol, nyseStock.market, market, name);
					}
					// type
					type = nyseStock.type;
				} else {
					if (name.toLowerCase().contains("unit")) continue; // skip unit
					
					logger.warn("other skip unknown  {}  {}  {}", symbol, market, name);
					continue;
				}

				list.add(new Stock(symbol, market, type, name));
			}
		}
		
		logger.info("nasdaq {}", list.size());
		
		logger.info("STOP");
	}
}

package yokwe.stock.us.nasdaq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.us.Stock;
import yokwe.stock.us.Stock.Market;
import yokwe.stock.us.Stock.Type;
import yokwe.stock.us.nasdaq.symbolDirectory.DownloadUtil;
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
		
		DownloadUtil.updateNASDAQListed();
		DownloadUtil.updateOtherListed();
		
		Map<String, Stock> nyseMap = NYSEStock.getMap();
		logger.info("nyse {}", nyseMap.size());

		List<Stock> list = new ArrayList<>();
		{
			int countSkipNASDAQ = 0;
			var nasdaqList = NASDAQListed.getList();
			logger.info("nasdaq list {}", nasdaqList.size());
			for(var e: nasdaqList) {
				// skip test issue right unit warrant
				if (e.isTestIssue() || e.isRights() || e.isUnits() || e.isWarrant()) {
					countSkipNASDAQ++;
					continue;
				}
				
				String symbol = e.symbol;
				Market market = Market.NASDAQ;
				Type   type;
				String name = e.name.replace(",", "").toUpperCase(); // use upper case
				
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
					// skip warrant
					// skip beneficial interest
					if (name.contains("WARRANT") || name.contains("BENEFICIAL INTEREST")) {
						countSkipNASDAQ++;
						continue;
					}
					
					logger.warn("nasdaq skip unknown  {}  {}", symbol, name);
					countSkipNASDAQ++;
					continue;
				}

				list.add(new Stock(symbol, market, type, name));
			}
			
			int countSkipOther = 0;
			var otherList = OtherListed.getList();
			logger.info("other  list {}", otherList.size());
			for(var e: otherList) {
				// skip test issue right unit warrant
				if (e.isTestIssue() || e.isRights() || e.isUnits() || e.isWarrant()) {
					countSkipOther++;
					continue;
				}
				
				String symbol = e.symbol;
				Market market;
				Type   type;
				String name = e.name.replace(",", "").toUpperCase(); // use upper case

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
					// skip unit
					if (name.toLowerCase().contains("unit")) {
						countSkipOther++;
						continue;
					}
					
					logger.warn("other skip unknown  {}  {}  {}", symbol, market, name);
					countSkipOther++;
					continue;
				}

				list.add(new Stock(symbol, market, type, name));
			}
			logger.info("countSkipNASDAQ {}", countSkipNASDAQ);
			logger.info("countSkipOther  {}", countSkipOther);
		}
		
		
		logger.info("nasdaq {}", list.size());
		
		logger.info("STOP");
	}
}

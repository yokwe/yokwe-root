package yokwe.stock.jp.jpx;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.stock.jp.jpx.Stock.StockKind;
import yokwe.util.UnexpectedException;

public class UpdateStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
	public static void main(String[] args) {
		logger.info("START");
		
		Map<String, JPXETF>   etfMap   = JPXETF.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		Map<String, JPXETN>   etnMap   = JPXETN.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		Map<String, JPXInfra> infraMap = JPXInfra.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		Map<String, JPXREIT>  reitMap  = JPXREIT.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		
		Map<String, Stock> map = new TreeMap<>();
		{
			for(var jpxListing: JPXListing.getList()) {
				String    stockCode = jpxListing.stockCode;
				boolean   ignore    = false;
				StockKind stockKind = null;
				
				switch(jpxListing.market) {
				case CERTIFICATE:
					stockKind = StockKind.CERTIFICATE;
					break;
				case GROWTH:
					stockKind = StockKind.STOCK_GROWTH;
					break;
				case STANDARD:
					stockKind = StockKind.STOCK_STANDARD;
					break;
				case PRIME:
					stockKind = StockKind.STOCK_PRIME;
					break;
				case GROWTH_FOREIGN:
					stockKind = StockKind.FOREIGN_GROWTH;
					break;
				case STANDARD_FOREIGN:
					stockKind = StockKind.FOREIGN_STANDARD;
					break;
				case PRIME_FOREIGN:
					stockKind = StockKind.FOREIGN_PRIME;
					break;
				case ETF_ETN:
					if (etfMap.containsKey(stockCode)) stockKind = StockKind.ETF;
					if (etnMap.containsKey(stockCode)) stockKind = StockKind.ETN;
					break;
				case PRO_MARKET:
					ignore = true;
					break;
				case REIT_FUND:
					if (reitMap.containsKey(stockCode))  stockKind = StockKind.REIT;
					if (infraMap.containsKey(stockCode)) stockKind = StockKind.INFRA_FUND;
					break;
				default:
					logger.error("Unexpected");
					logger.error("  jpxListring  {}", jpxListing);
					throw new UnexpectedException("Unexpected");
				}
								
				if (ignore) continue;
				if (stockKind == null) {
					logger.error("Unexpected");
					logger.error("  jpxListring  {}", jpxListing);
					throw new UnexpectedException("Unexpected");
				}
				
				Stock stock = new Stock(jpxListing.stockCode, stockKind, jpxListing.sector33, jpxListing.sector17, jpxListing.scale, jpxListing.name);
				map.put(stock.stockCode, stock);
			}
			logger.info("map  {}", map.size());
		}
		{
			for(var e: etfMap.entrySet()) {
				var key   = e.getKey();
				var value = e.getValue();
				
				if (map.containsKey(key)) continue;
				logger.info("new ETF  {}  {}", value.stockCode, value.name);
				
				Stock stock = new Stock(value.stockCode, StockKind.ETF, "NEW", "NEW", "NEW", value.name);
				map.put(stock.stockCode, stock);
			}
			for(var e: etnMap.entrySet()) {
				var key   = e.getKey();
				var value = e.getValue();
				
				if (map.containsKey(key)) continue;
				logger.info("new ETN  {}  {}", value.stockCode, value.name);
				
				Stock stock = new Stock(value.stockCode, StockKind.ETN, "NEW", "NEW", "NEW", value.name);
				map.put(stock.stockCode, stock);
			}
			for(var e: infraMap.entrySet()) {
				var key   = e.getKey();
				var value = e.getValue();
				
				if (map.containsKey(key)) continue;
				logger.info("new infra  {}  {}", value.stockCode, value.name);
				
				Stock stock = new Stock(value.stockCode, StockKind.INFRA_FUND, "NEW", "NEW", "NEW", value.name);
				map.put(stock.stockCode, stock);
				
			}
			for(var e: reitMap.entrySet()) {
				var key   = e.getKey();
				var value = e.getValue();
				
				if (map.containsKey(key)) continue;
				logger.info("new reit  {}  {}", value.stockCode, value.name);
				
				Stock stock = new Stock(value.stockCode, StockKind.REIT, "NEW", "NEW", "NEW", value.name);
				map.put(stock.stockCode, stock);
			}
		}
		
		logger.info("save  {}  {}", map.size(), Stock.getPath());
		Stock.save(map.values());
		logger.info("STOP");
	}
}

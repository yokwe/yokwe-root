package yokwe.finance.provider.jpx;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.stock.JPXStockInfoJP;
import yokwe.finance.type.StockInfoJP;
import yokwe.finance.type.StockInfoJP.Topix;
import yokwe.util.UnexpectedException;

public class UpdateJPXStockInfoJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static Map<JPXListing.Kind, StockInfoJP.Kind> kindMap = new TreeMap<>();
	static {
		kindMap.put(JPXListing.Kind.DOMESTIC_GROWTH,   StockInfoJP.Kind.DOMESTIC_GROWTH);
		kindMap.put(JPXListing.Kind.DOMESTIC_STANDARD, StockInfoJP.Kind.DOMESTIC_STANDARD);
		kindMap.put(JPXListing.Kind.DOMESTIC_PRIME,    StockInfoJP.Kind.DOMESTIC_PRIME);
		kindMap.put(JPXListing.Kind.FOREIGN_GROWTH,    StockInfoJP.Kind.FOREIGN_GROWTH);
		kindMap.put(JPXListing.Kind.FOREIGN_STANDARD,  StockInfoJP.Kind.FOREIGN_STANDARD);
		kindMap.put(JPXListing.Kind.FOREIGN_PRIME,     StockInfoJP.Kind.FOREIGN_PRIME);
		//
//		kindMap.put(JPXListing.Kind.CERTIFICATE,       StockInfoJP.Kind.CERTIFICATE);
//		kindMap.put(JPXListing.Kind.PRO_MARKET,        StockInfoJP.Kind.PRO_MARKET);
	}
	
	private static Map<JPXListing.Topix, StockInfoJP.Topix> topixMap = new TreeMap<>();
	static {
		topixMap.put(JPXListing.Topix.CORE_30, StockInfoJP.Topix.CORE_30);
		topixMap.put(JPXListing.Topix.LARGE_70, StockInfoJP.Topix.LARGE_70);
		topixMap.put(JPXListing.Topix.MID_400, StockInfoJP.Topix.MID_400);
		topixMap.put(JPXListing.Topix.SMALL_1, StockInfoJP.Topix.SMALL_1);
		topixMap.put(JPXListing.Topix.SMALL_2, StockInfoJP.Topix.SMALL_2);
		topixMap.put(JPXListing.Topix.ETF_ETN, StockInfoJP.Topix.ETF_ENT);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		Map<String, JPXETF>   etfMap   = JPXETF.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		Map<String, JPXETN>   etnMap   = JPXETN.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		Map<String, JPXInfra> infraMap = JPXInfra.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		Map<String, JPXREIT>  reitMap  = JPXREIT.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		
		Map<String, JPXStockInfo> stockInfoMap = JPXStockInfo.getMap();
		
		Map<String, StockInfoJP> map = new TreeMap<>();
		{
			for(var jpxListing: JPXListing.getList()) {
				String stockCode = jpxListing.stockCode;
				
				// skip certificate and pro market
				if (jpxListing.kind == JPXListing.Kind.CERTIFICATE || jpxListing.kind == JPXListing.Kind.PRO_MARKET) continue;
				
				if (!stockInfoMap.containsKey(stockCode)) {
					logger.warn("no stockInfo  {}", stockCode);
				}
				JPXStockInfo jpxStockInfo = stockInfoMap.get(stockCode);

				StockInfoJP.Kind kind = null;
				if (kindMap.containsKey(jpxListing.kind)) {
					kind = kindMap.get(jpxListing.kind);
				} else {
					if (etfMap.containsKey(stockCode))   kind = StockInfoJP.Kind.ETF;
					if (etnMap.containsKey(stockCode))   kind = StockInfoJP.Kind.ETN;
					if (reitMap.containsKey(stockCode))  kind = StockInfoJP.Kind.REIT;
					if (infraMap.containsKey(stockCode)) kind = StockInfoJP.Kind.INFRA_FUND;
				}
				if (kind == null) {
					logger.error("Unexpected");
					logger.error("  jpxListring  {}", jpxListing);
					throw new UnexpectedException("Unexpected");
				}
				
				StockInfoJP.Topix topix = null;
				if (topixMap.containsKey(jpxListing.topix)) {
					topix = topixMap.get(jpxListing.topix);
				} else {
					logger.error("Unexpected");
					logger.error("  jpxListring  {}", jpxListing);
					throw new UnexpectedException("Unexpected");
				}
								
				StockInfoJP stockInfo = new StockInfoJP(jpxListing.stockCode, kind, jpxListing.sector33, jpxListing.sector17, topix, jpxStockInfo.isinCode, jpxStockInfo.tradeUnit, jpxStockInfo.issued, jpxListing.name);
				map.put(stockInfo.stockCode, stockInfo);
			}
			logger.info("map  {}", map.size());
		}
		{
			for(var e: etfMap.entrySet()) {
				var stockCode = e.getKey();
				var value     = e.getValue();
				
				if (map.containsKey(stockCode)) continue;
				
				JPXStockInfo jpxStockInfo = stockInfoMap.get(stockCode);
				if (jpxStockInfo == null) {
					logger.warn("no jpxStockInfo  {}", stockCode);
				}

				logger.info("new ETF    {}  {}", stockCode, value.name);
				
				StockInfoJP stockInfo = new StockInfoJP(value.stockCode, StockInfoJP.Kind.ETF, "NEW", "NEW", Topix.NEW, jpxStockInfo.isinCode, jpxStockInfo.tradeUnit, jpxStockInfo.issued, value.name);
				map.put(stockInfo.stockCode, stockInfo);
			}
			for(var e: etnMap.entrySet()) {
				var stockCode = e.getKey();
				var value     = e.getValue();
				
				if (map.containsKey(stockCode)) continue;
				
				JPXStockInfo jpxStockInfo = stockInfoMap.get(stockCode);
				if (jpxStockInfo == null) {
					logger.warn("no jpxStockInfo  {}", stockCode);
				}

				logger.info("new ETN    {}  {}", value.stockCode, value.name);
				
				StockInfoJP stockInfo = new StockInfoJP(value.stockCode, StockInfoJP.Kind.ETN, "NEW", "NEW", Topix.NEW, jpxStockInfo.isinCode, jpxStockInfo.tradeUnit, jpxStockInfo.issued, value.name);
				map.put(stockInfo.stockCode, stockInfo);
			}
			for(var e: infraMap.entrySet()) {
				var stockCode = e.getKey();
				var value     = e.getValue();
				
				if (map.containsKey(stockCode)) continue;
				
				JPXStockInfo jpxStockInfo = stockInfoMap.get(stockCode);
				if (jpxStockInfo == null) {
					logger.warn("no jpxStockInfo  {}", stockCode);
				}

				logger.info("new INFRA  {}  {}", value.stockCode, value.name);
				
				StockInfoJP stockInfo = new StockInfoJP(value.stockCode, StockInfoJP.Kind.INFRA_FUND, "NEW", "NEW", Topix.NEW, jpxStockInfo.isinCode, jpxStockInfo.tradeUnit, jpxStockInfo.issued, value.name);
				map.put(stockInfo.stockCode, stockInfo);
			}
			for(var e: reitMap.entrySet()) {
				var stockCode = e.getKey();
				var value     = e.getValue();
				
				if (map.containsKey(stockCode)) continue;
				
				JPXStockInfo jpxStockInfo = stockInfoMap.get(stockCode);
				if (jpxStockInfo == null) {
					logger.warn("no jpxStockInfo  {}", stockCode);
				}

				logger.info("new REIT   {}  {}", value.stockCode, value.name);
				
				StockInfoJP stockInfo = new StockInfoJP(value.stockCode, StockInfoJP.Kind.REIT, "NEW", "NEW", Topix.NEW, jpxStockInfo.isinCode, jpxStockInfo.tradeUnit, jpxStockInfo.issued, value.name);
				map.put(stockInfo.stockCode, stockInfo);
			}
		}
		
		logger.info("save  {}  {}", map.size(), JPXStockInfoJP.getPath());
		JPXStockInfoJP.save(map.values());
		logger.info("STOP");
	}
}

package yokwe.finance.provider.jpx;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.type.StockInfoJP;
import yokwe.finance.type.StockInfoJP.Kind;
import yokwe.finance.type.StockInfoJP.Topix;
import yokwe.util.UnexpectedException;

public class UpdateStockInfo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static Map<Listing.Kind, StockInfoJP.Kind> kindMap = new TreeMap<>();
	static {
		kindMap.put(Listing.Kind.DOMESTIC_GROWTH,   StockInfoJP.Kind.DOMESTIC_GROWTH);
		kindMap.put(Listing.Kind.DOMESTIC_STANDARD, StockInfoJP.Kind.DOMESTIC_STANDARD);
		kindMap.put(Listing.Kind.DOMESTIC_PRIME,    StockInfoJP.Kind.DOMESTIC_PRIME);
		kindMap.put(Listing.Kind.FOREIGN_GROWTH,    StockInfoJP.Kind.FOREIGN_GROWTH);
		kindMap.put(Listing.Kind.FOREIGN_STANDARD,  StockInfoJP.Kind.FOREIGN_STANDARD);
		kindMap.put(Listing.Kind.FOREIGN_PRIME,     StockInfoJP.Kind.FOREIGN_PRIME);
		//
//		kindMap.put(JPXListing.Kind.CERTIFICATE,       StockInfoJP.Kind.CERTIFICATE);
//		kindMap.put(JPXListing.Kind.PRO_MARKET,        StockInfoJP.Kind.PRO_MARKET);
	}
	
	private static Map<Listing.Topix, StockInfoJP.Topix> topixMap = new TreeMap<>();
	static {
		topixMap.put(Listing.Topix.CORE_30,  StockInfoJP.Topix.CORE_30);
		topixMap.put(Listing.Topix.LARGE_70, StockInfoJP.Topix.LARGE_70);
		topixMap.put(Listing.Topix.MID_400,  StockInfoJP.Topix.MID_400);
		topixMap.put(Listing.Topix.SMALL_1,  StockInfoJP.Topix.SMALL_1);
		topixMap.put(Listing.Topix.SMALL_2,  StockInfoJP.Topix.SMALL_2);
		topixMap.put(Listing.Topix.OTHER,  StockInfoJP.Topix.OTHER);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		Map<String, ETF>       etfMap   = ETF.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		Map<String, ETN>       etnMap   = ETN.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		Map<String, InfraFund> infraMap = InfraFund.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		Map<String, REIT>      reitMap  = REIT.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
				
		Map<String, StockInfoJP> map = new TreeMap<>();
		{
			for(var listing: Listing.getList()) {
				String stockCode = listing.stockCode;
				
				// skip certificate and pro market
				if (listing.kind == Listing.Kind.CERTIFICATE || listing.kind == Listing.Kind.PRO_MARKET) continue;
				
				StockInfoJP.Kind kind = null;
				if (kindMap.containsKey(listing.kind)) {
					kind = kindMap.get(listing.kind);
				} else {
					if (etfMap.containsKey(stockCode))   kind = StockInfoJP.Kind.ETF;
					if (etnMap.containsKey(stockCode))   kind = StockInfoJP.Kind.ETN;
					if (reitMap.containsKey(stockCode))  kind = StockInfoJP.Kind.REIT;
					if (infraMap.containsKey(stockCode)) kind = StockInfoJP.Kind.INFRA_FUND;
				}
				if (kind == null) {
					logger.error("Unexpected");
					logger.error("  jpxListring  {}", listing);
					throw new UnexpectedException("Unexpected");
				}
				
				StockInfoJP.Topix topix = null;
				if (topixMap.containsKey(listing.topix)) {
					topix = topixMap.get(listing.topix);
				} else {
					logger.error("Unexpected");
					logger.error("  jpxListring  {}", listing);
					throw new UnexpectedException("Unexpected");
				}
				
				StockInfoJP stockInfoJP = new StockInfoJP();
				stockInfoJP.stockCode = listing.stockCode;
				stockInfoJP.kind      = kind;
				stockInfoJP.sector33  = listing.sector33;
				stockInfoJP.sector17  = listing.sector17;
				stockInfoJP.topix     = topix;
				stockInfoJP.name      = listing.name;
				
				map.put(stockInfoJP.stockCode, stockInfoJP);
			}
			logger.info("map  {}", map.size());
		}
		{
			for(var e: etfMap.entrySet()) {
				var stockCode = e.getKey();
				var value     = e.getValue();
				
				if (map.containsKey(stockCode)) continue;
				
				logger.info("new ETF    {}  {}", stockCode, value.name);
				
				StockInfoJP stockInfoJP = new StockInfoJP();
				stockInfoJP.stockCode = value.stockCode;
				stockInfoJP.kind      = Kind.ETF;
				stockInfoJP.sector33  = "NEW";
				stockInfoJP.sector17  = "NEW";
				stockInfoJP.topix     = Topix.NEW;
				stockInfoJP.name      = value.name;
				
				map.put(stockInfoJP.stockCode, stockInfoJP);
			}
			for(var e: etnMap.entrySet()) {
				var stockCode = e.getKey();
				var value     = e.getValue();
				
				if (map.containsKey(stockCode)) continue;
				
				logger.info("new ETN    {}  {}", value.stockCode, value.name);
				
				StockInfoJP stockInfoJP = new StockInfoJP();
				stockInfoJP.stockCode = value.stockCode;
				stockInfoJP.kind      = Kind.ETN;
				stockInfoJP.sector33  = "NEW";
				stockInfoJP.sector17  = "NEW";
				stockInfoJP.topix     = Topix.NEW;
				stockInfoJP.name      = value.name;
				
				map.put(stockInfoJP.stockCode, stockInfoJP);
			}
			for(var e: infraMap.entrySet()) {
				var stockCode = e.getKey();
				var value     = e.getValue();
				
				if (map.containsKey(stockCode)) continue;
				
				logger.info("new INFRA  {}  {}", value.stockCode, value.name);
				
				StockInfoJP stockInfoJP = new StockInfoJP();
				stockInfoJP.stockCode = value.stockCode;
				stockInfoJP.kind      = Kind.INFRA_FUND;
				stockInfoJP.sector33  = "NEW";
				stockInfoJP.sector17  = "NEW";
				stockInfoJP.topix     = Topix.NEW;
				stockInfoJP.name      = value.name;
				
				map.put(stockInfoJP.stockCode, stockInfoJP);
			}
			for(var e: reitMap.entrySet()) {
				var stockCode = e.getKey();
				var value     = e.getValue();
				
				if (map.containsKey(stockCode)) continue;
				
				logger.info("new REIT   {}  {}", value.stockCode, value.name);
				
				StockInfoJP stockInfoJP = new StockInfoJP();
				stockInfoJP.stockCode = value.stockCode;
				stockInfoJP.kind      = Kind.REIT;
				stockInfoJP.sector33  = "NEW";
				stockInfoJP.sector17  = "NEW";
				stockInfoJP.topix     = Topix.NEW;
				stockInfoJP.name      = value.name;
				
				map.put(stockInfoJP.stockCode, stockInfoJP);
			}
		}
		
		logger.info("save  {}  {}", map.size(), StockInfo.getPath());
		StockInfo.save(map.values());
		logger.info("STOP");
	}
}

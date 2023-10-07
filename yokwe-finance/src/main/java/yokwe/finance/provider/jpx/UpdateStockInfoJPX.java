package yokwe.finance.provider.jpx;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.type.StockInfoJPType;
import yokwe.finance.type.StockInfoJPType.Kind;
import yokwe.finance.type.StockInfoJPType.Topix;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateStockInfoJPX {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static Map<Listing.Kind, StockInfoJPType.Kind> kindMap = new TreeMap<>();
	static {
		kindMap.put(Listing.Kind.DOMESTIC_GROWTH,   StockInfoJPType.Kind.DOMESTIC_GROWTH);
		kindMap.put(Listing.Kind.DOMESTIC_STANDARD, StockInfoJPType.Kind.DOMESTIC_STANDARD);
		kindMap.put(Listing.Kind.DOMESTIC_PRIME,    StockInfoJPType.Kind.DOMESTIC_PRIME);
		kindMap.put(Listing.Kind.FOREIGN_GROWTH,    StockInfoJPType.Kind.FOREIGN_GROWTH);
		kindMap.put(Listing.Kind.FOREIGN_STANDARD,  StockInfoJPType.Kind.FOREIGN_STANDARD);
		kindMap.put(Listing.Kind.FOREIGN_PRIME,     StockInfoJPType.Kind.FOREIGN_PRIME);
		//
//		kindMap.put(JPXListing.Kind.CERTIFICATE,       StockInfoJP.Kind.CERTIFICATE);
//		kindMap.put(JPXListing.Kind.PRO_MARKET,        StockInfoJP.Kind.PRO_MARKET);
	}
	
	private static Map<Listing.Topix, StockInfoJPType.Topix> topixMap = new TreeMap<>();
	static {
		topixMap.put(Listing.Topix.CORE_30,  StockInfoJPType.Topix.CORE_30);
		topixMap.put(Listing.Topix.LARGE_70, StockInfoJPType.Topix.LARGE_70);
		topixMap.put(Listing.Topix.MID_400,  StockInfoJPType.Topix.MID_400);
		topixMap.put(Listing.Topix.SMALL_1,  StockInfoJPType.Topix.SMALL_1);
		topixMap.put(Listing.Topix.SMALL_2,  StockInfoJPType.Topix.SMALL_2);
		topixMap.put(Listing.Topix.OTHER,    StockInfoJPType.Topix.OTHER);
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		// build map using listing, etf, etn, infrafund and reit of jpx
		Map<String, StockInfoJPType> map = new TreeMap<>();
		{
			var listingList = Listing.getList();
			var etfMap      = ETF.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
			var etnMap      = ETN.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
			var infraMap    = InfraFund.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
			var reitMap     = REIT.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
			logger.info("listing    {}", listingList.size());
			logger.info("etf        {}", etfMap.size());
			logger.info("etn        {}", etnMap.size());
			logger.info("infra      {}", infraMap.size());
			logger.info("reit       {}", reitMap.size());

			// from listing
			int countSkip = 0;
			for(var listing: listingList) {
				String stockCode = listing.stockCode;
				
				// skip certificate and pro market
				if (listing.kind == Listing.Kind.CERTIFICATE || listing.kind == Listing.Kind.PRO_MARKET) {
					countSkip++;
					continue;
				}
				
				StockInfoJPType.Kind kind = kindMap.get(listing.kind);
				if (kind == null) {
					if (etfMap.containsKey(stockCode))        kind = StockInfoJPType.Kind.ETF;
					else if (etnMap.containsKey(stockCode))   kind = StockInfoJPType.Kind.ETN;
					else if (reitMap.containsKey(stockCode))  kind = StockInfoJPType.Kind.REIT;
					else if (infraMap.containsKey(stockCode)) kind = StockInfoJPType.Kind.INFRA_FUND;
					else {
						logger.error("Unexpected");
						logger.error("  jpxListring  {}", listing);
						throw new UnexpectedException("Unexpected");
					}
				}
								
				StockInfoJPType.Topix topix = topixMap.get(listing.topix);
				if (topix == null) {
					logger.error("Unexpected");
					logger.error("  jpxListring  {}", listing);
					throw new UnexpectedException("Unexpected");
				}
				
				StockInfoJPType stockInfoJP = new StockInfoJPType();
				stockInfoJP.stockCode = listing.stockCode;
				stockInfoJP.isinCode  = "";
				stockInfoJP.tradeUnit = 0;
				stockInfoJP.issued    = 0;
				stockInfoJP.kind      = kind;
				stockInfoJP.sector33  = listing.sector33;
				stockInfoJP.sector17  = listing.sector17;
				stockInfoJP.topix     = topix;
				stockInfoJP.name      = listing.name;
				
				map.put(stockInfoJP.stockCode, stockInfoJP);
			}
			logger.info("skip       {}", countSkip);

			// not in listing but in etf
			for(var e: etfMap.entrySet()) {
				var stockCode = e.getKey();
				var value     = e.getValue();
				
				if (map.containsKey(stockCode)) continue;
				
				logger.info("new ETF    {}  {}", stockCode, value.name);
				
				StockInfoJPType stockInfoJP = new StockInfoJPType();
				stockInfoJP.stockCode = value.stockCode;
				stockInfoJP.isinCode  = "";
				stockInfoJP.tradeUnit = 0;
				stockInfoJP.issued    = 0;
				stockInfoJP.kind      = Kind.ETF;
				stockInfoJP.sector33  = "NEW";
				stockInfoJP.sector17  = "NEW";
				stockInfoJP.topix     = Topix.NEW;
				stockInfoJP.name      = value.name;
				
				map.put(stockInfoJP.stockCode, stockInfoJP);
			}
			// not in listing but in etn
			for(var e: etnMap.entrySet()) {
				var stockCode = e.getKey();
				var value     = e.getValue();
				
				if (map.containsKey(stockCode)) continue;
				
				logger.info("new ETN    {}  {}", value.stockCode, value.name);
				
				StockInfoJPType stockInfoJP = new StockInfoJPType();
				stockInfoJP.stockCode = value.stockCode;
				stockInfoJP.isinCode  = "";
				stockInfoJP.tradeUnit = 0;
				stockInfoJP.issued    = 0;
				stockInfoJP.kind      = Kind.ETN;
				stockInfoJP.sector33  = "NEW";
				stockInfoJP.sector17  = "NEW";
				stockInfoJP.topix     = Topix.NEW;
				stockInfoJP.name      = value.name;
				
				map.put(stockInfoJP.stockCode, stockInfoJP);
			}
			// not in listring but in infra
			for(var e: infraMap.entrySet()) {
				var stockCode = e.getKey();
				var value     = e.getValue();
				
				if (map.containsKey(stockCode)) continue;
				
				logger.info("new INFRA  {}  {}", value.stockCode, value.name);
				
				StockInfoJPType stockInfoJP = new StockInfoJPType();
				stockInfoJP.stockCode = value.stockCode;
				stockInfoJP.isinCode  = "";
				stockInfoJP.tradeUnit = 0;
				stockInfoJP.issued    = 0;
				stockInfoJP.kind      = Kind.INFRA_FUND;
				stockInfoJP.sector33  = "NEW";
				stockInfoJP.sector17  = "NEW";
				stockInfoJP.topix     = Topix.NEW;
				stockInfoJP.name      = value.name;
				
				map.put(stockInfoJP.stockCode, stockInfoJP);
			}
			// not in listing but in reit
			for(var e: reitMap.entrySet()) {
				var stockCode = e.getKey();
				var value     = e.getValue();
				
				if (map.containsKey(stockCode)) continue;
				
				logger.info("new REIT   {}  {}", value.stockCode, value.name);
				
				StockInfoJPType stockInfoJP = new StockInfoJPType();
				stockInfoJP.stockCode = value.stockCode;
				stockInfoJP.isinCode  = "";
				stockInfoJP.tradeUnit = 0;
				stockInfoJP.issued    = 0;
				stockInfoJP.kind      = Kind.REIT;
				stockInfoJP.sector33  = "NEW";
				stockInfoJP.sector17  = "NEW";
				stockInfoJP.topix     = Topix.NEW;
				stockInfoJP.name      = value.name;
				
				map.put(stockInfoJP.stockCode, stockInfoJP);
			}

			logger.info("map        {}", map.size());
		}
		// fix isinCode, tradeUnit and issued using current stock info
		{
			var unknownList = new ArrayList<StockInfoJPType>();

			var oldMap = StockInfoJPX.getMap();
			for(var entry: map.entrySet()) {
				var stockCode    = entry.getKey();
				var stockInfo    = entry.getValue();
				var oldStockInfo = oldMap.get(stockCode);
				
				if (oldStockInfo == null) {
					unknownList.add(stockInfo);
				} else {
					stockInfo.isinCode  = oldStockInfo.isinCode;
					stockInfo.tradeUnit = oldStockInfo.tradeUnit;
					stockInfo.issued    = oldStockInfo.issued;
				}
			}
			logger.info("unknown    {}", unknownList.size());
			
			String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit";
			String referer   = "https://www.jpx.co.jp/";

			for(var stockInfo: unknownList) {
				var stockCode = stockInfo.stockCode;
				String page;
				{
					String url = UpdateStockPriceJPX.getPageURL(stockCode);

					HttpUtil.Result result = HttpUtil.getInstance().withReferer(referer).withUserAgent(userAgent).download(url);
					if (result == null || result.result == null) {
						logger.error("Unexpected");
						logger.error("  result  {}", result);
						throw new UnexpectedException("Unexpected");
					}
					page = result.result;
				}
				
				if (page.contains("指定された銘柄が見つかりません")) {
					logger.info("skip       {}  {}", stockInfo.stockCode, stockInfo.name);
					map.remove(stockCode);
					continue;
				}
				
				var companyInfo = StockPage.CompanyInfo.getInstance(page);
				var tradeUnit   = StockPage.TradeUnit.getInstance(page);
				var issued      = StockPage.Issued.getInstance(page);
				
				if (companyInfo != null && tradeUnit != null && issued != null) {
					logger.info("update     {}  {}", stockInfo.stockCode, stockInfo.name);
					stockInfo.isinCode  = companyInfo.isin;
					stockInfo.tradeUnit = tradeUnit.value;
					stockInfo.issued    = issued.value;
				} else {
					logger.error("Unexpected page");
					logger.error("  stockCode {}", stockCode);

					if (companyInfo == null) logger.error("  companyInfo is null");
					if (tradeUnit   == null) logger.error("  tradeUnit is null");
					if (issued      == null) logger.error("  issued is null");
					throw new UnexpectedException("Unexpected page");
				}
			}
		}
		
		logger.info("save  {}  {}", map.size(), StockInfoJPX.getPath());
		StockInfoJPX.save(map.values());
		logger.info("STOP");
	}
}

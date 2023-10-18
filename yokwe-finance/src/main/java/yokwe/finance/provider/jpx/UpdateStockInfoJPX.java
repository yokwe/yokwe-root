package yokwe.finance.provider.jpx;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.type.StockInfoJPType;
import yokwe.finance.type.StockInfoJPType.Type;
import yokwe.finance.type.StockInfoJPType.Topix;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateStockInfoJPX {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static Map<ListingType.Type, StockInfoJPType.Type> typeMap = new TreeMap<>();
	static {
		typeMap.put(ListingType.Type.DOMESTIC_GROWTH,   StockInfoJPType.Type.DOMESTIC_GROWTH);
		typeMap.put(ListingType.Type.DOMESTIC_STANDARD, StockInfoJPType.Type.DOMESTIC_STANDARD);
		typeMap.put(ListingType.Type.DOMESTIC_PRIME,    StockInfoJPType.Type.DOMESTIC_PRIME);
		typeMap.put(ListingType.Type.FOREIGN_GROWTH,    StockInfoJPType.Type.FOREIGN_GROWTH);
		typeMap.put(ListingType.Type.FOREIGN_STANDARD,  StockInfoJPType.Type.FOREIGN_STANDARD);
		typeMap.put(ListingType.Type.FOREIGN_PRIME,     StockInfoJPType.Type.FOREIGN_PRIME);
		//
//		kindMap.put(JPXListing.Kind.CERTIFICATE,       StockInfoJP.Kind.CERTIFICATE);
//		kindMap.put(JPXListing.Kind.PRO_MARKET,        StockInfoJP.Kind.PRO_MARKET);
	}
	
	private static Map<ListingType.Topix, StockInfoJPType.Topix> topixMap = new TreeMap<>();
	static {
		topixMap.put(ListingType.Topix.CORE_30,  StockInfoJPType.Topix.CORE_30);
		topixMap.put(ListingType.Topix.LARGE_70, StockInfoJPType.Topix.LARGE_70);
		topixMap.put(ListingType.Topix.MID_400,  StockInfoJPType.Topix.MID_400);
		topixMap.put(ListingType.Topix.SMALL_1,  StockInfoJPType.Topix.SMALL_1);
		topixMap.put(ListingType.Topix.SMALL_2,  StockInfoJPType.Topix.SMALL_2);
		topixMap.put(ListingType.Topix.OTHER,    StockInfoJPType.Topix.OTHER);
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		// build map using listing, etf, etn, infrafund and reit of jpx
		Map<String, StockInfoJPType> map = new TreeMap<>();
		{
			var listingList = StorageJPX.Listing.getList();
			var etfMap      = StorageJPX.ETF.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
			var etnMap      = StorageJPX.ETN.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
			var infraMap    = StorageJPX.InfraFund.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
			var reitMap     = StorageJPX.REIT.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
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
				if (listing.type == ListingType.Type.CERTIFICATE || listing.type == ListingType.Type.PRO_MARKET) {
					countSkip++;
					continue;
				}
				
				StockInfoJPType.Type type = typeMap.get(listing.type);
				if (type == null) {
					if (etfMap.containsKey(stockCode))        type = StockInfoJPType.Type.ETF;
					else if (etnMap.containsKey(stockCode))   type = StockInfoJPType.Type.ETN;
					else if (reitMap.containsKey(stockCode))  type = StockInfoJPType.Type.REIT;
					else if (infraMap.containsKey(stockCode)) type = StockInfoJPType.Type.INFRA_FUND;
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
				stockInfoJP.type      = type;
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
				stockInfoJP.type      = Type.ETF;
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
				stockInfoJP.type      = Type.ETN;
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
				stockInfoJP.type      = Type.INFRA_FUND;
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
				stockInfoJP.type      = Type.REIT;
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

			var oldMap = StorageJPX.StockInfoJPX.getMap();
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
		
		logger.info("save  {}  {}", map.size(), StorageJPX.StockInfoJPX.getPath());
		StorageJPX.StockInfoJPX.save(map.values());
		logger.info("STOP");
	}
}

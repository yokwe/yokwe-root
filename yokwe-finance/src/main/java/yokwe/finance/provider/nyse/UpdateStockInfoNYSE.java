package yokwe.finance.provider.nyse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.finance.type.StockInfoUSType;
import yokwe.finance.type.StockInfoUSType.Market;
import yokwe.finance.type.StockInfoUSType.Type;
import yokwe.util.ListUtil; // FIXME
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateStockInfoNYSE {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String TYPE_STOCK = "EQUITY";
	private static final String TYPE_ETF   = "EXCHANGE_TRADED_FUND";
	
	public static final String URL          = "https://www.nyse.com/api/quotes/filter";
	public static final String BODY_FORMAT  = "{\"instrumentType\":\"%s\",\"pageNumber\":1,\"sortColumn\":\"NORMALIZED_TICKER\",\"sortOrder\":\"ASC\",\"maxResultsPerPage\":10000,\"filterToken\":\"\"}";
	public static final String CONTENT_TYPE = "application/json";
	
	
	// ISO 10381  MIC CODE
	//   https://www.iso20022.org/market-identifier-codes
	private static final String MIC_UNLISTED = "XXXX";
	private static final Map<String, Market> marketMap = new TreeMap<>();
	static {
		marketMap.put("ARCX", Market.NYSE);   // NYSE ARCA
		marketMap.put("BATS", Market.BATS);   // CBOE BZX U.S. EQUITIES EXCHANGE
		marketMap.put("XASE", Market.NYSE);   // NYSE MKT LLC
		marketMap.put("XNCM", Market.NASDAQ); // NASDAQ CAPITAL MARKET
		marketMap.put("XNGS", Market.NASDAQ); // NASDAQ/NGS (GLOBAL SELECT MARKET)
		marketMap.put("XNMS", Market.NASDAQ); // NASDAQ/NMS (GLOBAL MARKET)
		marketMap.put("XNYS", Market.NYSE);   // NEW YORK STOCK EXCHANGE, INC.
		marketMap.put("IEXG", Market.IEXG);   // INVESTORS EXCHANGE
	}
	private static final Map<String, Type> typeMap = new TreeMap<>();
	static {
		typeMap.put("CLOSED_END_FUND",              Type.CEF);
		typeMap.put("COMMON_STOCK",                 Type.COMMON);
		typeMap.put("DEPOSITORY_RECEIPT",           Type.ADR);
		typeMap.put("EXCHANGE_TRADED_FUND",         Type.ETF);
		typeMap.put("EXCHANGE_TRADED_NOTE",         Type.ETN);
		typeMap.put("LIMITED_PARTNERSHIP",          Type.LP);
		typeMap.put("PREFERRED_STOCK",              Type.PREF);
		typeMap.put("REIT",                         Type.REIT);
		typeMap.put("TRUST",                        Type.TRUST);
		typeMap.put("UNIT",                         Type.UNIT);
		typeMap.put("UNITS_OF_BENEFICIAL_INTEREST", Type.UBI);
	}


	public static List<Filter> downloadFilter(String instrumentType) {
		String body = String.format(BODY_FORMAT, instrumentType);
		
		HttpUtil.Result result = HttpUtil.getInstance().withPost(body, CONTENT_TYPE).download(URL);
		
		if (result == null) {
			throw new UnexpectedException("result == null");
		}
		if (result.result == null) {
			throw new UnexpectedException("result.result == null");
		}
		
		List<Filter> list = JSON.getList(Filter.class, result.result);
		return list;
	}
	
	private static void download() {
		var stockList = downloadFilter(TYPE_STOCK);
		var etfList   = downloadFilter(TYPE_ETF);
		
		List<Filter> list = new ArrayList<>();
		list.addAll(stockList);
		list.addAll(etfList);

		// sanity check
		{
			var map = ListUtil.checkDuplicate(list, o -> o.symbolTicker);
			logger.info("stock  {}", stockList.size());
			logger.info("etf    {}", etfList.size());
			logger.info("total  {}", stockList.size() + etfList.size());
			logger.info("map    {}", map.size());
			if (map.size() != stockList.size() + etfList.size()) {
				logger.error("Unexpected");
				throw new UnexpectedException("Unexpected");
			}
		}
		
		logger.info("save   {}  {}", list.size(), Filter.getPath());
		Filter.save(list);
	}
	
	private static void update() {
		List<StockInfoUSType> list = new ArrayList<>();
		
		int count = 0;
		int countSkip = 0;
		
		for(var e: Filter.getList()) {
			count++;
			if (e.symbolTicker.startsWith("E:")) {
				countSkip++;
				continue;
			}
			if (e.micCode.equals(MIC_UNLISTED)) {
				// this stock is unlisted stock
				countSkip++;
				continue;
			}
			
			String stockCode = e.symbolTicker;
			Market market    = marketMap.get(e.micCode);
			Type   type      = typeMap.get(e.instrumentType);
			String name      = e.instrumentName.replace(",", "").toUpperCase(); // use upper case
			
			if (market == null) {
				logger.error("Unpexpected micCode");
				logger.error("  {}  {}", stockCode, e.micCode);
				throw new UnexpectedException("Unpexpected micCode");
			}
			if (type == null) {
				logger.error("Unpexpected instrumentType");
				logger.error("  {}  {}", stockCode, e.instrumentType);
				throw new UnexpectedException("Unpexpected instrumentType");
			}
			
			if (type.isETF() || type.isStock()) {
				list.add(new StockInfoUSType(stockCode, market, type, name));
			} else {
				countSkip++;
			}
		}
		
		logger.info("total  {}", count);
		logger.info("skip   {}", countSkip);
		
		logger.info("save   {}  {}", list.size(), StorageNYSE.StockInfoNYSE.getPath());
		StorageNYSE.StockInfoNYSE.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		download();
		update();
		
		logger.info("STOP");
	}
}

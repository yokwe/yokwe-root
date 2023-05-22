package yokwe.stock.us.nyse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.us.Stock;
import yokwe.stock.us.Stock.Market;
import yokwe.stock.us.Stock.SimpleType;
import yokwe.stock.us.Stock.Type;
import yokwe.util.ListUtil;
import yokwe.util.UnexpectedException;


public class UpdateNYSEStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// ISO 10381  MIC CODE
	//   https://www.iso20022.org/market-identifier-codes
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

	public static void main(String[] args) {
		logger.info("START");
		
		Filter.STOCK.download();
		Filter.ETF.download();
		
		{			
			List<Filter.RawData> dataList = new ArrayList<>();
			{
				for(var e: Filter.STOCK.getList()) {
					dataList.add(e);
				}
				for(var e: Filter.ETF.getList()) {
					dataList.add(e);
				}
				logger.info("dataList {}", dataList.size());
			}
			
			List<Stock> list = new ArrayList<>();
			for(var data: dataList) {
				if (data.symbolTicker.startsWith("E:")) continue;
				
				String symbol = data.symbolTicker;
				Market market;
				Type   type;
				String name   = data.instrumentName.replace(",", "");
				
				if (marketMap.containsKey(data.micCode)) {
					market = marketMap.get(data.micCode);
				} else {
					logger.error("Unpexpected micCode");
					logger.error("  {}", data.micCode);
					throw new UnexpectedException("Unpexpected micCode");
				}
				if (typeMap.containsKey(data.instrumentType)) {
					type = typeMap.get(data.instrumentType);
				} else {
					logger.error("Unpexpected instrumentType");
					logger.error("  {}", data.instrumentType);
					throw new UnexpectedException("Unpexpected instrumentType");
				}
				
				if (type.simpleType == SimpleType.ETF || type.simpleType == SimpleType.STOCK) {
					list.add(new Stock(symbol, market, type, name));
				}
			}
			
			// sanity check
			ListUtil.checkDuplicate(list, Stock::getKey);
			
			Collections.sort(list);
			logger.info("save  {}  {}", list.size(), NYSEStock.getPath());
			NYSEStock.save(list);
		}
		logger.info("STOP");
	}
}

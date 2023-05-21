package yokwe.stock.us.nyse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yokwe.stock.us.nyse.NYSESymbol.Market;
import yokwe.stock.us.nyse.NYSESymbol.Type;
import yokwe.util.ListUtil;

public class UpdateNYSESymbol {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		Filter.Stock.download();
		Filter.ETF.download();
		
		{			
			List<Filter.Data> dataList = new ArrayList<>();
			{
				for(var e: Filter.Stock.getList()) {
					dataList.add(e);
				}
				for(var e: Filter.ETF.getList()) {
					dataList.add(e);
				}
				logger.info("dataList {}", dataList.size());
			}
			
			List<NYSESymbol> list = new ArrayList<>();
			for(var data: dataList) {
				if (data.symbolTicker.startsWith("E:")) continue;
				
				String symbol = data.symbolTicker;
				Market market = data.micCode.market;
				Type   type   = data.instrumentType.type;
				String name   = data.instrumentName.replace(",", "");
				
				if (type.etf || type.stock) {
					list.add(new NYSESymbol(symbol, market, type, name));
				}
			}
			
			// sanity check
			ListUtil.checkDuplicate(list, NYSESymbol::getKey);
			
			Collections.sort(list);
			logger.info("save  {}  {}", list.size(), NYSESymbol.getPath());
			NYSESymbol.save(list);
		}
		logger.info("STOP");
	}
}

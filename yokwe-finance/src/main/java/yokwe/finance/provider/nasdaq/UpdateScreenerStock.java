package yokwe.finance.provider.nasdaq;

import java.util.ArrayList;

import yokwe.finance.provider.nasdaq.api.API;
import yokwe.finance.provider.nasdaq.api.Screener;

public class UpdateScreenerStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static void update() {
		var list = new ArrayList<ScreenerStock>();
		
		var stock = Screener.Stock.getInstance();
		for(var e: stock.data.rows) {
			var screenerStock = new ScreenerStock();
			
			screenerStock.stockCode = API.normalizeSymbol(e.symbol);
			screenerStock.industry  = e.industry;
			screenerStock.sector    = e.sector;
			
			screenerStock.name      = e.name.replace(",", ""); // remove comma
			
			list.add(screenerStock);
		}
		
		logger.info("save  {}  {}", list.size(), ScreenerStock.getPath());
		ScreenerStock.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}

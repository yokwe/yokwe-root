package yokwe.finance.stock.jp;

import java.io.IOException;
import java.math.BigDecimal;

public class UpdateStockPrice {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		var stockInfoList = yokwe.finance.provider.jpx.StockInfo.getList();
		logger.info("stockInfoList  {}", stockInfoList.size());
		
		int count = 0;
		for(var stockInfo: stockInfoList) {
			count++;
			if ((count % 1000) == 1) logger.info("{}  /  {}", count, stockInfoList.size());
			
			String     stockCode = stockInfo.stockCode;
			BigDecimal lastClose = null;
			
			var list = yokwe.finance.provider.jpx.StockPrice.getList(stockCode);
			for(var e: list) {
				if (e.volume == 0) {
					if (lastClose == null) continue;
					
					e.open = e.high = e.low = e.close = lastClose;
				}
				lastClose = e.close;
			}
			StockPrice.save(stockCode, list);
		}
		
		logger.info("STOP");
	}
}

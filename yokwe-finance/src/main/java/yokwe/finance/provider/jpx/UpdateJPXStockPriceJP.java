package yokwe.finance.provider.jpx;

import java.io.IOException;
import java.math.BigDecimal;

import yokwe.finance.stock.JPXStockPriceJP;

public class UpdateJPXStockPriceJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		var stockInfoList = StockInfo.getList();
		logger.info("stockInfoList  {}", stockInfoList.size());
		
		int count = 0;
		for(var stockInfo: stockInfoList) {
			count++;
			if ((count % 1000) == 1) logger.info("{}  /  {}", count, stockInfoList.size());
			
			String     stockCode = stockInfo.stockCode;
			BigDecimal lastClose = null;
			
			var list = StockPrice.getList(stockCode);
			for(var e: list) {
				if (e.volume == 0) {
					if (lastClose == null) continue;
					
					e.open = e.high = e.low = e.close = lastClose;
				}
				lastClose = e.close;
			}
			JPXStockPriceJP.save(stockCode,list);
		}
		
		logger.info("STOP");
	}
}

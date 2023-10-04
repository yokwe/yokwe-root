package yokwe.finance.stock.jp;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;
import java.util.TreeSet;

import yokwe.util.FileUtil;

public class UpdateStockPrice {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static void update() {
		var stockInfoList = StockInfo.getList();
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
	}
	
	private static void moveUnknownFile() {
		Set<String> validNameSet = new TreeSet<>();
		for(var e: StockInfo.getList()) {
			File file = new File(StockPrice.getPath(e.stockCode));
			validNameSet.add(file.getName());
		}
		
		FileUtil.moveUnknownFile(validNameSet, StockPrice.getPath(), StockPrice.getPathDelist());
	}

	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		moveUnknownFile();
		
		update();
		
		logger.info("STOP");
	}
}

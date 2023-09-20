package yokwe.finance.provider.jpx;

import java.io.IOException;

import yokwe.finance.stock.JPXStockInfoJP;

public class UpdateJPXStockInfoJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		var stockInfoList  = StockInfo.getList();
		var stockDetailMap = StockDetail.getMap();
		logger.info("stockInfo    {}", stockInfoList.size());
		logger.info("stockDetail  {}", stockDetailMap.size());
		
		for(var stockInfo: stockInfoList) {
			String      stockCode   = stockInfo.stockCode;
			StockDetail stockDetail = stockDetailMap.get(stockCode);
			
			if (stockDetail != null) {
				stockInfo.isinCode  = stockDetail.isinCode;
				stockInfo.tradeUnit = stockDetail.tradeUnit;
				stockInfo.issued    = stockDetail.issued;
			} else {
				logger.warn("Unexpected stockCode  {}", stockCode);
			}
		}
		
		logger.info("save  {}  {}", stockInfoList.size(), JPXStockInfoJP.getPath());
		JPXStockInfoJP.save(stockInfoList);
		
		logger.info("STOP");
	}
}

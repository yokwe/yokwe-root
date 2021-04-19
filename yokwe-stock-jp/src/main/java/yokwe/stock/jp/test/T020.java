package yokwe.security.japan.test;

import java.io.IOException;

import org.slf4j.LoggerFactory;

public class T020 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T020.class);
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
//		{
//			String dirOLD = "tmp/data/price";
//			String dirNEW = "tmp/data/price-NEW";
//			
//			for(File file: FileUtil.listFile(dirOLD)) {
//				String stockCode4 = file.getName().substring(0, 4);
//				String stockCode5 = String.format("%s0", stockCode4);
//								
//				String oldPath = String.format("%s/%s.csv", dirOLD, stockCode4);
//				String newPath = String.format("%s/%s.csv", dirNEW, stockCode5);
//				
//				logger.info("{}  {}", oldPath, newPath);
//				
//				List<Price> list = CSVUtil.read(Price.class).file(oldPath);
//				for(Price e: list) {
//					if (e.stockCode.length() == 4) {
//						e.stockCode = String.format("%s0", e.stockCode);
//					}
//				}
//				CSVUtil.write(Price.class).file(newPath, list);
//			}
//		}
		
//		{
//			String fileOLD = "tmp/data/stock-info.csv";
//			String fileNEW = "tmp/data/stock-info-NEW.csv";
//			
//			List<StockInfo> list = CSVUtil.read(StockInfo.class).file(fileOLD);
//			for(StockInfo e: list) {
//				if (e.stockCode.length() == 4) {
//					e.stockCode = String.format("%s0", e.stockCode);
//				}
//			}
//			CSVUtil.write(StockInfo.class).file(fileNEW, list);
//		}
		
//		{
//			String dirOLD = "tmp/data/dividend";
//			String dirNEW = "tmp/data/dividend-NEW";
//			
//			for(File file: FileUtil.listFile(dirOLD)) {
//				String filename = file.getName();
//				if (!filename.endsWith(".csv")) continue;
//				
//				String stockCode4 = filename.replace(".csv", "");
//				String stockCode5 = Stock.toStockCode5(stockCode4);
//								
//				String oldPath = String.format("%s/%s.csv", dirOLD, stockCode4);
//				String newPath = String.format("%s/%s.csv", dirNEW, stockCode5);
//				
//				logger.info("{}  {}", oldPath, newPath);
//				
//				List<Dividend> list = CSVUtil.read(Dividend.class).file(oldPath);
//				for(Dividend e: list) {
//					e.stockCode = Stock.toStockCode5(e.stockCode);
//				}
//				CSVUtil.write(Dividend.class).file(newPath, list);
//			}
//		}
		
		
		logger.info("STOP");
	}
}

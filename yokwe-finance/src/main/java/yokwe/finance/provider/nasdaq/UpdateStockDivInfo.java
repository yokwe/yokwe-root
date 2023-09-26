package yokwe.finance.provider.nasdaq;

import java.io.File;
import java.util.ArrayList;

import yokwe.finance.provider.nasdaq.api.AssetClass;
import yokwe.finance.provider.nasdaq.api.Dividends;

public class UpdateStockDivInfo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static class Task {
		public String     stockCode;
		public AssetClass assetClass;
		public int        limit;
		
		Task(String stockCode, AssetClass assetClass, int limit) {
			this.stockCode  = stockCode;
			this.assetClass = assetClass;
			this.limit      = limit;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %d}", stockCode, assetClass, limit);
		}
	}
	private static void update() {
		// build taskList
		var taskList = new ArrayList<Task>();
		
		{
			var list = yokwe.finance.stock.us.StockInfo.getList();
			logger.info("list  {}", list.size());
			for(var stockInfo: list) {
				String     stockCode  = stockInfo.stockCode;
				AssetClass assetClass = stockInfo.type.isETF() ? AssetClass.ETF : AssetClass.STOCK;
				
				File file = new File(StockDivInfo.getPath(stockCode));
				if (file.exists()) continue;
				
				taskList.add(new Task(stockCode, assetClass, 9999));
			}
			logger.info("task  {}", taskList.size());
		}
		
		// process taskList
		{
			int count = 0;
			for(var task: taskList) {
				String stockCode = task.stockCode;
				if ((++count % 100) == 1) logger.info("{}  /  {}  {}", count, taskList.size(), stockCode);
				
				// FIXME
				{
					var list = StockDivInfo.getList(stockCode);
					if (!list.isEmpty()) continue;
				}
				
				var nasdaqSymbol = StockInfo.toNASDAQSymbol(stockCode);
				var div = Dividends.getInstance(nasdaqSymbol, task.assetClass, task.limit);
				if (div == null) {
					logger.warn("div is null  {}", task);
					continue;
				}
				if (div.data == null) {
					logger.warn("div.data is null  {}", task);
					continue;
				}
				if (div.data.dividends == null) {
					logger.warn("div.data.dividends is null  {}", task);
					continue;
				}
				if (div.data.dividends.rows == null) {
					logger.warn("div.data.tradesTable.rows is null  {}", task);
					// write empty data file
					StockDivInfo.save(stockCode, new ArrayList<>());
					continue;
				}
				
				var divInfoList = new ArrayList<StockDivInfo>();
				var rows = div.data.dividends.rows;
				for(var row: rows) {
					StockDivInfo divInfo = new StockDivInfo();
					divInfo.exOrEffDate     = row.exOrEffDate;
					divInfo.type            = row.type;
					divInfo.amount          = row.amount;
					divInfo.declarationDate = row.declarationDate;
					divInfo.recordDate      = row.recordDate;
					divInfo.paymentDate     = row.paymentDate;
					divInfo.currency        = row.currency;
					
					divInfoList.add(divInfo);
				}
				
				StockDivInfo.save(stockCode, divInfoList);
			}
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}

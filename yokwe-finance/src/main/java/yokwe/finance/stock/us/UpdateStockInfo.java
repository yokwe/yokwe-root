package yokwe.finance.stock.us;

import java.io.IOException;
import java.util.HashSet;

import yokwe.finance.type.StockInfoUS.Market;

public class UpdateStockInfo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static void update() {
		var list = yokwe.finance.provider.nasdaq.StockInfo.getList();
		logger.info("list        {}", list.size());
		{
			// make set of genuine stockCode from each market
			var batsList   = yokwe.finance.provider.bats.StockInfo.getList().stream().filter(o -> o.market == Market.BATS).map(o -> o.stockCode).toList();
			var nasdaqList = yokwe.finance.provider.nasdaq.StockInfo.getList().stream().filter(o -> o.market == Market.NASDAQ).map(o -> o.stockCode).toList();
			var nyseList   = yokwe.finance.provider.nyse.StockInfo.getList().stream().filter(o -> o.market == Market.NYSE).map(o -> o.stockCode).toList();
			logger.info("batsList    {}", batsList.size());
			logger.info("nasdaqList  {}", nasdaqList.size());
			logger.info("nyseList    {}", nyseList.size());
			
			var set = new HashSet<String>();		
			set.addAll(batsList);
			set.addAll(nasdaqList);
			set.addAll(nyseList);
			logger.info("set         {}", set.size());
			
			// remove entry from list if stockCode is not in set
			list.removeIf(o -> !set.contains(o.stockCode));
			logger.info("list2       {}", list.size());
		}
		{
			// make set of trading stockCode of each broakerage

			var monexList   = yokwe.finance.provider.monex.TradingStock.getList().stream().map(o -> o.stockCode).toList();
			var moomooList  = yokwe.finance.provider.moomoo.TradingStock.getList().stream().map(o -> o.stockCode).toList();
			var rakutenList = yokwe.finance.provider.rakuten.TradingStock.getList().stream().map(o -> o.stockCode).toList();
			var sbiList     = yokwe.finance.provider.sbi.TradingStock.getList().stream().map(o -> o.stockCode).toList();
			
			logger.info("monexList   {}", monexList.size());
			logger.info("moomooList  {}", moomooList.size());
			logger.info("rakutenList {}", rakutenList.size());
			logger.info("sbiList     {}", sbiList.size());
			var set = new HashSet<String>();		
			set.addAll(monexList);
			set.addAll(moomooList);
			set.addAll(rakutenList);
			set.addAll(sbiList);
			logger.info("set         {}", set.size());
			
			// remove entry from list if stockCode is not appeared in set
			list.removeIf(o -> !set.contains(o.stockCode));
			logger.info("list3       {}", list.size());
		}
		
		logger.info("save  {}  {}", list.size(), StockInfo.getPath());
		StockInfo.save(list);
	}
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}

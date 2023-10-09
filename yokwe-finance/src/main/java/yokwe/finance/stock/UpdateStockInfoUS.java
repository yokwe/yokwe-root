package yokwe.finance.stock;

import java.io.IOException;
import java.util.HashSet;

import yokwe.finance.provider.bats.StockInfoBATS;
import yokwe.finance.provider.monex.TradingStockMonex;
import yokwe.finance.provider.moomoo.TradingStockMoomoo;
import yokwe.finance.provider.nasdaq.StockInfoNasdaq;
import yokwe.finance.provider.nyse.StockInfoNYSE;
import yokwe.finance.provider.rakuten.TradingStockRakuten;
import yokwe.finance.provider.sbi.TradingStockSBI;
import yokwe.finance.type.StockInfoUSType.Market;

public class UpdateStockInfoUS {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static void update() {
		var list = StockInfoNasdaq.getList();
		logger.info("list        {}", list.size());
		{
			// make set of genuine stockCode from each market
			var batsList   = StockInfoBATS.getList().stream().filter(o -> o.market == Market.BATS).map(o -> o.stockCode).toList();
			var nasdaqList = StockInfoNasdaq.getList().stream().filter(o -> o.market == Market.NASDAQ).map(o -> o.stockCode).toList();
			var nyseList   = StockInfoNYSE.getList().stream().filter(o -> o.market == Market.NYSE).map(o -> o.stockCode).toList();
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
			var monexList   = TradingStockMonex.getList().stream().map(o -> o.stockCode).toList();
			var moomooList  = TradingStockMoomoo.getList().stream().map(o -> o.stockCode).toList();
			var rakutenList = TradingStockRakuten.getList().stream().map(o -> o.stockCode).toList();
			var sbiList     = TradingStockSBI.getList().stream().map(o -> o.stockCode).toList();
			
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
		
		logger.info("save  {}  {}", list.size(), StockInfoUS.getPath());
		StockInfoUS.save(list);
	}
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}

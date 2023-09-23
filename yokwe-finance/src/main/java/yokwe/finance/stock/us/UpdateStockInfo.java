package yokwe.finance.stock.us;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.type.StockInfoUS;
import yokwe.finance.type.StockInfoUS.Market;

public class UpdateStockInfo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		var batsList   = yokwe.finance.provider.bats.StockInfo.getList();
		var nasdaqList = yokwe.finance.provider.nasdaq.StockInfo.getList();
		var nyseList   = yokwe.finance.provider.nyse.StockInfo.getList();
		logger.info("batsList   {}", batsList.size());
		logger.info("nasdaqList {}", nasdaqList.size());
		logger.info("nyseList   {}", nyseList.size());

		var set = new HashSet<StockInfoUS>();
		{
						
			// bats
			long countBATS   = batsList.stream().filter(o -> o.market == Market.BATS).peek(o -> set.add(o)).count();
			long countNASDAQ = nasdaqList.stream().filter(o -> o.market == Market.NASDAQ).peek(o -> set.add(o)).count();
			long countNYSE   = nyseList.stream().filter(o -> o.market == Market.NYSE).peek(o -> set.add(o)).count();
			logger.info("countBATS   {}", countBATS);
			logger.info("countNASDAQ {}", countNASDAQ);
			logger.info("countNYSE   {}", countNYSE);
			logger.info("set         {}", set.size());
		}
		
		var list  = new ArrayList<StockInfoUS>();
		{
			var map = nyseList.stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
			
			for(var e: set) {
				var stockInfo = map.get(e.stockCode);
				if (stockInfo == null) {
					logger.warn("not found in nyse  {}  {}", e.stockCode, e.name);
				} else {
					list.add(stockInfo);
				}
			}
			logger.info("list        {}", list.size());
		}
		
		logger.info("save  {}  {}", list.size(), StockInfo.getPath());
		StockInfo.save(list);
		
		logger.info("STOP");
	}
}

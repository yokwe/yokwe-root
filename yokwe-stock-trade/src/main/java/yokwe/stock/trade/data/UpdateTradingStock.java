package yokwe.stock.trade.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.stock.trade.monex.MonexStock;
import yokwe.stock.trade.rakuten.RakutenStock;
import yokwe.stock.trade.sbi.SBIStock;
import yokwe.stock.us.Stock;
import yokwe.stock.us.Stock.Type;
import yokwe.stock.us.TradingStock;

public class UpdateTradingStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static void main(String[] args) {
		logger.info("START");
		
		var stockMap   = Stock.getMap();
		var monexSet   = MonexStock.getList().stream().map(o -> o.symbol).collect(Collectors.toSet());
		var sbiSet     = SBIStock.getList().stream().map(o -> o.symbol).collect(Collectors.toSet());
		var rakutenSet = RakutenStock.getList().stream().map(o -> o.symbol).collect(Collectors.toSet());
		
		Set<String> allSet = new TreeSet<>();
		allSet.addAll(monexSet);
		allSet.addAll(sbiSet);
		allSet.addAll(rakutenSet);
		logger.info("stock   {}", stockMap.size());
		logger.info("monex   {}", monexSet.size());
		logger.info("sbi     {}", sbiSet.size());
		logger.info("rakuten {}", rakutenSet.size());
		logger.info("all     {}", allSet.size());
		
		// trading symbol
		{
			List<TradingStock> list = new ArrayList<>();
			
			for(var e: allSet) {
				Stock stock = stockMap.get(e);
				if (stock == null) {
					logger.warn("Unexpeced symbol  {}", e);
					continue;
				}
				
				String symbol  = e;
				String monex   = "0";
				String sbi     = "0";
				String rakuten = "0";
				Type   type    = stock.type;
				String name    = stock.name;

				if (monexSet.contains(symbol)) {
					monex = "1";
				}
				if (sbiSet.contains(symbol)) {
					sbi = "1";
				}
				if (rakutenSet.contains(symbol)) {
					rakuten = "1";
				}

				var tradingSymbol = new TradingStock(symbol, monex, sbi, rakuten, type, name);
				list.add(tradingSymbol);
			}
			logger.info("trading {} {}", list.size(), TradingStock.getPath());
			TradingStock.save(list);
		}
		
		logger.info("STOP");
	}

}

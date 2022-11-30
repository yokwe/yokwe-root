package yokwe.stock.trade.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.stock.us.Symbol;
import yokwe.stock.us.nasdaq.NASDAQSymbol;

public class UpdateSymbol {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateSymbol.class);

	public static void main(String[] args) {
		logger.info("START");
		
		var nasdaqMap  = NASDAQSymbol.getList().stream().collect(Collectors.toMap(o -> o.symbol, o -> o.name));
		var extraSet   = Symbol.getListExtra().stream().map(o -> o.symbol).collect(Collectors.toSet());
		var monexMap   = yokwe.stock.trade.monex.UpdateSymbolName.getList().stream().collect(Collectors.toMap(o -> o.symbol, o -> o.name));
		var sbiMap     = yokwe.stock.trade.sbi.UpdateSymbolName.getList().stream().collect(Collectors.toMap(o -> o.symbol, o -> o.name));
		var rakutenMap = yokwe.stock.trade.rakuten.UpdateSymbolName.getList().stream().collect(Collectors.toMap(o -> o.symbol, o -> o.name));
		logger.info("nasdaq  {}", nasdaqMap.size());
		logger.info("extra   {}", extraSet.size());
		logger.info("monex   {}", monexMap.size());
		logger.info("sbi     {}", sbiMap.size());
		logger.info("rakuten {}", rakutenMap.size());
		
		Set<String> all = new TreeSet<>();
		all.addAll(extraSet);
		all.addAll(monexMap.keySet());
		all.addAll(sbiMap.keySet());
		all.addAll(rakutenMap.keySet());
		logger.info("all     {}", all.size());
		
		{
			var list = all.stream().map(o -> new Symbol(o)).collect(Collectors.toList());
			logger.info("save    {} {}", list.size(), Symbol.getPath());
			Symbol.save(list);
		}
		

		// trading symbol
		{
			List<SymbolTrading> list = new ArrayList<>();
			
			for(var e: all) {
				String symbol  = e;
				String nasdaq  = "0";
				String extra   = "0";
				String monex   = "0";
				String sbi     = "0";
				String rakuten = "0";
				String name    = "";

				if (nasdaqMap.containsKey(symbol)) {
					nasdaq = "1";
					name = nasdaqMap.get(symbol);
				}
				if (extraSet.contains(symbol)) {
					extra = "1";
				}
				if (monexMap.containsKey(symbol)) {
					monex = "1";
					if (name.isEmpty()) name = monexMap.get(symbol);
				}
				if (sbiMap.containsKey(symbol)) {
					sbi = "1";
					if (name.isEmpty()) name = sbiMap.get(symbol);
				}
				if (rakutenMap.containsKey(symbol)) {
					rakuten = "1";
					if (name.isEmpty()) name = rakutenMap.get(symbol);
				}

				var tradingSymbol = new SymbolTrading(symbol, nasdaq, extra, monex, sbi, rakuten, name);
				list.add(tradingSymbol);
			}
			logger.info("trading {} {}", list.size(), SymbolTrading.getPath());
			SymbolTrading.save(list);
		}
		
		
		// remove symbol that is not appeared in NASDAQSymbol
		{			
			var newSet = new TreeSet<>(all);
			int count = 0;
			for(var e: newSet) {
				if (nasdaqMap.containsKey(e)) {
					//
				} else {
					// not appeared in nasdaqSet
					// logger.warn("Unknown symbol {}", e);
					count++;
					all.remove(e);
				}
			}
			if (count != 0) {
				logger.warn("remove  {}", count);
				logger.info("all     {}", all.size());
				
				var list = all.stream().map(o -> new Symbol(o)).collect(Collectors.toList());
				logger.info("save    {} {}", list.size(), Symbol.getPath());
				Symbol.save(list);
			}
		}
		
		logger.info("STOP");
	}
}

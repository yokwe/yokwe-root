package yokwe.stock.trade.data;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.stock.us.Symbol;
import yokwe.stock.us.nasdaq.NASDAQSymbol;

public class UpdateSymbol {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateSymbol.class);

	public static void main(String[] args) {
		logger.info("START");
		
		var extraList   = Symbol.getListExtra();
		var monexList   = yokwe.stock.trade.monex.UpdateSymbolName.getList().stream().map(o -> new Symbol(o.symbol)).collect(Collectors.toList());
		var sbiList     = yokwe.stock.trade.sbi.UpdateSymbolName.getList().stream().map(o -> new Symbol(o.symbol)).collect(Collectors.toList());
		var rakutenList = yokwe.stock.trade.rakuten.UpdateSymbolName.getList().stream().map(o -> new Symbol(o.symbol)).collect(Collectors.toList());
		logger.info("extraList   {}", extraList.size());
		logger.info("monexList   {}", monexList.size());
		logger.info("sbiList     {}", sbiList.size());
		logger.info("rakutenList {}", rakutenList.size());
		
		Set<Symbol> set = new TreeSet<>();
		set.addAll(extraList);
		set.addAll(monexList);
		set.addAll(sbiList);
		set.addAll(rakutenList);
		logger.info("set         {}", set.size());
		
		// remove symbol that is not appeared in NASDAQSymbol
		{
			Set<Symbol> nasdaqSet = NASDAQSymbol.getList().stream().map(o -> new Symbol(o.symbol)).collect(Collectors.toSet());
			logger.info("nasdaqSet   {}", nasdaqSet.size());
			
			Set<Symbol> newSet = new TreeSet<>(set);
			int count = 0;
			for(Symbol e: newSet) {
				if (nasdaqSet.contains(e)) {
					//
				} else {
					// not appeared in nasdaqSet
					logger.warn("Unknown symbol {}", e.symbol);
					count++;
					set.remove(e);
				}
			}
			if (count != 0) {
				logger.warn("Remove count   {}", count);
			}
		}
		
		logger.info("save        {} {}", set.size(), Symbol.getPath());
		Symbol.save(set);
		
		logger.info("STOP");
	}
}

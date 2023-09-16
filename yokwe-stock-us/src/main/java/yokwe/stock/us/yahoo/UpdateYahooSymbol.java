package yokwe.stock.us.yahoo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import yokwe.stock.us.Stock;
import yokwe.util.yahoo.finance.Search;
import yokwe.util.yahoo.finance.Symbol;

public class UpdateYahooSymbol {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final long SLEEP_PERIOD_IN_MILLIS = 1000;
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<String> usSymbolList = Stock.getList().stream().map(o -> o.symbol).collect(Collectors.toList());
		logger.info("usSymbolList  {}", usSymbolList.size());
		
		Map<String, Symbol> symbolMap = YahooSymbol.getMap();
		logger.info("symbolMap   {}", symbolMap.size());
		{
			// remove entry that has not valid exchange from symbolMap
			var i = symbolMap.entrySet().iterator();
			boolean modified = false;
			while (i.hasNext()) {
				var entry = i.next();
				var exchange = entry.getValue().exchange;
				if (!Search.isValidExchange(exchange)) {
					logger.warn("invalid exchange  {}", entry);
					i.remove();
					modified = true;
				}
			}
			if (modified) {
				logger.info("save  {}  {}", symbolMap.size(), YahooSymbol.getPath());
				YahooSymbol.save(symbolMap.values());
			}
		}
		{
			// remove entry that has not valid symbol from symbolMap
			Set<String> set = usSymbolList.stream().map(o -> Stock.toYahooSymbol(o)).collect(Collectors.toSet());
			var i = symbolMap.entrySet().iterator();
			boolean modified = false;
			while (i.hasNext()) {
				var entry = i.next();
				var value = entry.getValue();
				if (!set.contains(value.symbol)) {
					logger.warn("invalid symbol  {}", entry);
					i.remove();
					modified = true;
				}
			}
			if (modified) {
				logger.info("save  {}  {}", symbolMap.size(), YahooSymbol.getPath());
				YahooSymbol.save(symbolMap.values());
			}
		}
		
		{
			// remove already processed symbol from usSymbol
			var i = usSymbolList.iterator();
			while (i.hasNext()) {
				var usSymbol = i.next();
				String yahooSymbol = Stock.toYahooSymbol(usSymbol);
				if (symbolMap.containsKey(yahooSymbol)) i.remove();
			}
		}
		logger.info("usSymbolList  {}", usSymbolList.size());
		
		// shuffle
		Collections.shuffle(usSymbolList);
		
		int countUpdate    = 0;
		int countProcessed = 0;
		for(var usSymbol: usSymbolList) {
			// only for common
			String yahooSymbol = Stock.toYahooSymbol(usSymbol);
			if (symbolMap.containsKey(yahooSymbol)) continue;
			
			Symbol symbol = Search.getSymbol(yahooSymbol);
			if (symbol == null) {
				logger.warn("no symbol  {}", yahooSymbol);
			} else {
				countProcessed++;
				
				symbolMap.put(symbol.symbol, symbol);
				countUpdate++;
				if ((countUpdate % 10) == 1) {
					logger.info("symbolMap  {} / {}", countProcessed, usSymbolList.size());
					YahooSymbol.save(symbolMap.values());
				}
				
				try {
					Thread.sleep(SLEEP_PERIOD_IN_MILLIS);
				} catch (InterruptedException e) {
					//
				}
			}
		}
		
		if (countUpdate != 0) {
			logger.info("save  {}  {}", symbolMap.size(), YahooSymbol.getPath());
			YahooSymbol.save(symbolMap.values());
		}

		logger.info("STOP");
	}
}

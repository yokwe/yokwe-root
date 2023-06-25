package yokwe.stock.us.nikko;

import java.util.stream.Collectors;

import yokwe.stock.us.Stock;
import yokwe.stock.us.Storage;
import yokwe.util.ListUtil;

public class UpdateNikkoStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static class Info implements Comparable<Info> {
		private static final String PATH_FILE = Storage.Nikko.getPath("stock-info.csv");
		private static String getPath() {
			return PATH_FILE;
		}

		public String symbol;
		public String nameJP;
		public String exchange;
		public String industry;
		public String flag;     // 売買禁止, 買禁止 or -
		
		@Override
		public int compareTo(Info that) {
			return this.symbol.compareTo(that.symbol);
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		var symbolSet = ListUtil.getList(Info.class, Info.getPath()).stream().filter(o -> o.flag.equals("-")).map(o -> o.symbol.replace("/", ".")).collect(Collectors.toSet());
		logger.info("tickerSet  {}", symbolSet.size());
		
		var stockList = Stock.getList();
		logger.info("stockList  {}", stockList.size());
		
		// sanity check of symbolSet
		{
			var stockSymbolSet = stockList.stream().map(o -> o.symbol).collect(Collectors.toSet());
			for(var e: symbolSet) {
				if (stockSymbolSet.contains(e)) continue;
				logger.info("unexpected symbol  {}", e);
			}
		}
		
		// remove stock that symbol not in symbolSet
		stockList.removeIf(o -> !symbolSet.contains(o.symbol));
		
		logger.info("save   {}  {}", stockList.size(), NikkoStock.getPath());
		NikkoStock.save(stockList);
		
		logger.info("STOP");
	}
}

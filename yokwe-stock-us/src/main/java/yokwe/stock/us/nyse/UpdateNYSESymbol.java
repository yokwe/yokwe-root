package yokwe.stock.us.nyse;

import java.util.List;
import java.util.stream.Collectors;

import yokwe.stock.us.nyse.Filter.Kind;
import yokwe.stock.us.nyse.NYSESymbol.Data;

public class UpdateNYSESymbol {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static void downloadStock() {
		List<Data> list = Filter.download(Kind.STOCK);
		List<Data> list2 = list.stream().filter(o -> !o.symbolTicker.startsWith("E:")).collect(Collectors.toList());
		
		logger.info("save  {}  {}", list2.size(), NYSESymbol.Stock.getPath());
		NYSESymbol.Stock.save(list2);
	}

	private static void downloadETF() {
		List<Data> list = Filter.download(Kind.ETF);
		List<Data> list2 = list.stream().filter(o -> !o.symbolTicker.startsWith("E:")).collect(Collectors.toList());

		logger.info("save  {}  {}", list2.size(), NYSESymbol.ETF.getPath());
		NYSESymbol.ETF.save(list2);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		downloadStock();
		downloadETF();
		
		logger.info("STOP");
	}
}

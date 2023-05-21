package yokwe.stock.us.nyse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.stock.us.nyse.Filter.Kind;
import yokwe.stock.us.nyse.NYSESymbol.Data;
import yokwe.stock.us.nyse.NYSESymbol.Symbol;
import yokwe.util.ListUtil;

public class UpdateNYSESymbol {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static void downloadStock() {
		List<Data> list = Filter.download(Kind.STOCK);
		List<Data> list2 = list.stream().filter(o -> !o.symbolTicker.startsWith("E:")).collect(Collectors.toList());
		for(var e: list2) {
			e.instrumentName = e.instrumentName.replace(",", "");
		}
		
		logger.info("save  {}  {}", list2.size(), NYSESymbol.Stock.getPath());
		NYSESymbol.Stock.save(list2);
	}

	public static void downloadETF() {
		List<Data> list = Filter.download(Kind.ETF);
		List<Data> list2 = list.stream().filter(o -> !o.symbolTicker.startsWith("E:")).collect(Collectors.toList());
		for(var e: list2) {
			e.instrumentName = e.instrumentName.replace(",", "");
		}

		logger.info("save  {}  {}", list2.size(), NYSESymbol.ETF.getPath());
		NYSESymbol.ETF.save(list2);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
//		downloadStock();
//		downloadETF();
		
		{
			List<Symbol> list = new ArrayList<>();
			
			for(var e: NYSESymbol.Stock.getList()) {
				list.add(new Symbol(e.symbolTicker, e.micCode, e.instrumentType, e.instrumentName));
			}
			for(var e: NYSESymbol.ETF.getList()) {
				list.add(new Symbol(e.symbolTicker, e.micCode, e.instrumentType, e.instrumentName));
			}
			logger.info("list   {}", list.size());
			
			List<Symbol> list2 = list.stream().filter(o -> o.type.stock || o.type.etf).collect(Collectors.toList());
			logger.info("list2  {}", list2.size());
			
			// sanity check
			ListUtil.checkDuplicate(list2, Symbol::getKey);
			
			Collections.sort(list2);
			logger.info("save  {}  {}", list2.size(), Symbol.getPath());
			Symbol.save(list2);
		}
		logger.info("STOP");
	}
}

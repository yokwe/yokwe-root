package yokwe.stock.trade.monex;

import java.util.List;

import yokwe.stock.us.Symbol;

//
// Update nasdaq.Symbol for nasdaq
//
public class UpdateSymbol {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateSymbol.class);

	public static void main(String[] args) {
		logger.info("START");
		
		List<Symbol> list = Symbol.getListExtra();
		logger.info("extra {} {}", list.size(), Symbol.getPathExtra());
		
		for(var e: StockUS.getList()) {
			list.add(new Symbol(e.symbol));
		}
		
		logger.info("save  {} {}", list.size(), Symbol.getPath());
		Symbol.save(list);
		
		logger.info("STOP");
	}
}

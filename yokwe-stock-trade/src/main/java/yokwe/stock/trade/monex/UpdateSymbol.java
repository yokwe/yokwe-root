package yokwe.stock.trade.monex;

import java.util.ArrayList;
import java.util.List;

import yokwe.stock.us.nasdaq.Symbol;

//
// Update nasdaq.Symbol for nasdaq
//
public class UpdateSymbol {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateSymbol.class);

	public static void main(String[] args) {
		logger.info("START");
		
		List<Symbol> list = new ArrayList<>();
		
		for(var e: StockUS.getList()) {
			Symbol symbol = new Symbol(e.symbol, e.name);
			list.add(symbol);
		}
		
		logger.info("save {} {}", list.size(), Symbol.getPath());
		Symbol.save(list);
		
		logger.info("STOP");
	}
}

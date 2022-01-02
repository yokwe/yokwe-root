package yokwe.stock.trade.monex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.trade.monex.StockUS.Type;
import yokwe.stock.us.nasdaq.Symbol;
import yokwe.stock.us.nasdaq.api.AssetClass;
import yokwe.util.UnexpectedException;

//
// Update nasdaq.Symbol for nasdaq
//
public class UpdateSymbol {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateSymbol.class);

	private static final Map<Type, AssetClass> map = new TreeMap<>();
	static {
		map.put(Type.ETF,   AssetClass.ETF);
		map.put(Type.Stock, AssetClass.STOCK);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<Symbol> list = Symbol.getListExtra();
		logger.info("extra {} {}", list.size(), Symbol.getPathExtra());
		
		for(var e: StockUS.getList()) {
			AssetClass assetClass;
			if (map.containsKey(e.type)) {
				assetClass = map.get(e.type);
			} else {
				logger.error("Unexpected");
				logger.error("  type {}", e.type);
				throw new UnexpectedException("Unexptected");
			}
			Symbol symbol = new Symbol(e.symbol, assetClass, e.name);
			list.add(symbol);
		}
		
		logger.info("save  {} {}", list.size(), Symbol.getPath());
		Symbol.save(list);
		
		logger.info("STOP");
	}
}

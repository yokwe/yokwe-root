package yokwe.finance.trade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeMap;

public class Portfolio {
//	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private TreeMap<String, Holding> holdingMap = new TreeMap<>();
	//              symbol  holding
	// FIXME needs rule for BOND
	
	// for sanity check
	public TreeMap<String, Holding> getHoldingMap() {
		return holdingMap;
	}
	
	public Holding getHolding(String symbol, String name, BigDecimal priceFactor) {
		var ret = holdingMap.get(symbol);
		if (ret == null) {
			ret = new Holding(symbol, name, priceFactor);
			holdingMap.put(symbol, ret);
		}
		return ret;
	}
	public Holding getHolding(String symbol, String name) {
		return getHolding(symbol, name, BigDecimal.ONE);
	}
	
	public int valueAsOf(LocalDate date) {
		int ret = 0;
		
		for(var e: holdingMap.entrySet()) {
			var holding = e.getValue();
			
			if (holding.totalUnits() == 0) continue;
			
			var value = holding.valueAsOf(date);
			if (value < 0) {
//				logger.warn("no data in priceMap  {}  {}  {}  {}  {}", holding.symbol(), holding.asset(), date, value, Integer.toHexString(value));
				return -1; // date is too old for holding, return minus one
			}
			
			ret += value;
		}
		
		return ret;
	}
}

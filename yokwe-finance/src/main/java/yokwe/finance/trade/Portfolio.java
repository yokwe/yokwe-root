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
	
	private Holding getHolding(String symbol, BigDecimal priceFactor) {
		var ret = holdingMap.get(symbol);
		if (ret == null) {
			ret = new Holding(symbol, priceFactor);
			holdingMap.put(symbol, ret);
		}
		return ret;
	}
	
	public void buy(String symbol, int units, int value, BigDecimal priceFactor) {
		var holding = getHolding(symbol, priceFactor);
		holding.buy(units, value);
	}
	public void buy(String symbol, int units, int value) {
		buy(symbol, units, value, BigDecimal.ONE);
	}
	// sell return cost of selling stock
	public int sell(String symbol, int units, BigDecimal priceFactor) {
		var holding = getHolding(symbol, priceFactor);
		return holding.sell(units);
	}
	public int sell(String symbol, int units) {
		return sell(symbol, units, BigDecimal.ONE);
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

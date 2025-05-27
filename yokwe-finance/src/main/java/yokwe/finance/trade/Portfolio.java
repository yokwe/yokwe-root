package yokwe.finance.trade;

import java.time.LocalDate;
import java.util.TreeMap;

public class Portfolio {
	private TreeMap<String, Holding> holdingMap = new TreeMap<>();
	
	// FIXME needs rule for BOND
	
	private Holding getHolding(String symbol) {
		var ret = holdingMap.get(symbol);
		if (ret == null) {
			ret = new Holding(symbol);
			holdingMap.put(symbol, ret);
		}
		return ret;
	}
		
	public void buy(String symbol, int units, int value) {
		var holding = getHolding(symbol);
		holding.buy(units, value);
	}
	// sell return cost of selling stock
	public int sell(String symbol, int units) {
		var holding = getHolding(symbol);
		return holding.sell(units);
	}
	
	public int valueAsOf(LocalDate date) {
		int ret = 0;
		
		for(var e: holdingMap.values()) {
			ret += e.valueAsOf(date);
		}
		
		return ret;
	}
}

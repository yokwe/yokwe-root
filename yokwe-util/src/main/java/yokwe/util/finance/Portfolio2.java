package yokwe.util.finance;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import yokwe.util.UnexpectedException;

public class Portfolio2 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	static class Holding {
		final String    name;
		final FundStats fundStats;
		
		int   quantity;
		
		Holding(String name, FundStats fundStats, int quantity) {
			this.name      = name;
			this.fundStats = fundStats;
			this.quantity  = quantity;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %d}", name, quantity);
		}

		void setQuantity(int newValue) {
			quantity = newValue;
		}
		
		Set<LocalDate> dateSet(int nMonth) {
			Set<LocalDate> set = new HashSet<>();
			for(var e: fundStats.dateArray(nMonth)) {
				set.add(e);
			}
			return set;
		}
	}
	
	static class Data {
		String   name;
		double   weight;
		double   rateOfReturn;
		double[] returnArray;
		
		Data(String name) {
			this.name         = name;
			this.weight       = 0;
			this.rateOfReturn = 0;
			this.returnArray  = null;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %.3f  %d}", name, weight, returnArray == null ? -1 : returnArray.length);
		}
	}
	
	private int                  nMonth     = 0;
	private Map<String, Holding> holdingMap = new TreeMap<>();
	//          name
	
	private Map<String, Data>    dataMap             = null;
	//          name
	private boolean              durationIsChanged   = true; // duration (nMonth) is changed
	private boolean              quantityIsChanged   = true; // quantity of holding is changed
	private boolean              holdingMapIsChanged = true; // new holding is added
	
	public Portfolio2(int nMonth) {
		this.nMonth = nMonth;
	}
	public Portfolio2() {
		this(0);
	}
	
	@Override
	public String toString() {
		return String.format("{%d  %s}", nMonth, holdingMap.values());
	}
	
	public Portfolio2 duration(int nMonth) {
		this.nMonth = nMonth;
		
		durationIsChanged = true;
		return this;
	}
	public int duration() {
		return this.nMonth;
	}
	public Portfolio2 quantity(String name, int quantity) {
		if (holdingMap.containsKey(name)) {
			Holding holding = holdingMap.get(name);
			holding.setQuantity(quantity);
		} else {
			logger.error("Unknown name");
			logger.error("  name  {}", name);
			throw new UnexpectedException("Unknown name");
		}
		
		quantityIsChanged = true;
		return this;
	}
	
	public Portfolio2 add(String name, DailyPriceDiv[] dailyPriceDivArray, int quantity) {
		FundStats fundStats = FundStats.getInstance(name, dailyPriceDivArray);
		Holding   holding   = new Holding(name, fundStats, quantity);
		holdingMap.put(name, holding);
		
		holdingMapIsChanged = true;
		return this;
	}
	public Portfolio2 add(String name, DailyPriceDiv[] dailyPriceDivArray) {
		return add(name, dailyPriceDivArray, 0);
	}
		
	private void prepare() {
		if (dataMap == null || holdingMapIsChanged) {
			dataMap = new TreeMap<>();
			for(var holding: holdingMap.values()) {
				String name = holding.name;
				Data   data = new Data(name);
				dataMap.put(name, data);
			}
			holdingMapIsChanged = false;
			//
			durationIsChanged   = true;
			quantityIsChanged   = true;
		}
		if (quantityIsChanged) {
			int totalQuantity = holdingMap.values().stream().mapToInt(o -> o.quantity).sum();
			for(var holding: holdingMap.values()) {
				String name = holding.name;
				Data   data = dataMap.get(name);
				// update data.weight
				data.weight = holding.quantity / (double)totalQuantity;
				logger.info("quantity  {}  {}", data, holding.quantity);
			}
			quantityIsChanged = false;
		}
		if (durationIsChanged) {
			// commonDateSet contains common date of all holding
			Set<LocalDate> commonDateSet = new HashSet<>();
			for(var holding: holdingMap.values()) {
				Set<LocalDate> dateSet = holding.dateSet(nMonth);
				if (commonDateSet.isEmpty()) {
					commonDateSet.addAll(dateSet);
				} else {
					commonDateSet.retainAll(dateSet);
				}
			}
			for(var holding: holdingMap.values()) {
				String name = holding.name;
				Data   data = dataMap.get(name);
				// update data.rateOfReturn
				data.rateOfReturn = holding.fundStats.rateOfReturn(nMonth);
				// update data.returnArray
				Map<LocalDate, Double> returnMap = holding.fundStats.returnMap(nMonth);
				{
					var i = returnMap.entrySet().iterator();
					while(i.hasNext()) {
						var entry = i.next();
						var date = entry.getKey();
						if (!commonDateSet.contains(date)) {
							// if date is not in commonDateSet, remove entry for Stats.standardDeviation(statsArray, weightArray);
							logger.warn("remove entry  {}  {}", name, date);
							i.remove();
						}
					}
				}
				data.returnArray = returnMap.values().stream().mapToDouble(o -> o).toArray();
				
				logger.info("duration  {}", data);
			}
			durationIsChanged = false;
		}
	}
	
	public double rateOfReturn() {
		prepare();
		
		double result = 0;
		for(var data: dataMap.values()) {
			result += data.weight * data.rateOfReturn;
		}
		return result;
	}
	public double risk() {
		prepare();

		int length = dataMap.size();
		
		double[] weightArray = new double[length];
		Stats[]  statsArray  = new Stats[length];
		
		int index = 0;
		for(var data: dataMap.values()) {
			weightArray[index] = data.weight;
			statsArray[index]  = new Stats(data.returnArray);
			
			// update for next iteration
			index++;
		}
		double risk = Stats.standardDeviation(statsArray, weightArray);
		return risk * FundStats.SQRT_DAY_IN_YEAR; // Convert daily value to annual value
	}
	
	public double rateOfReturn(String name) {
		if (holdingMap.containsKey(name)) {
			Holding holding = holdingMap.get(name);
			return holding.fundStats.rateOfReturn(nMonth);
		} else {
			logger.error("Unexpected name");
			logger.error("  name      {}", name);
			logger.error("  portfolio {}", toString());
			throw new UnexpectedException("Unexpected");
		}
	}
	public double risk(String name) {
		if (holdingMap.containsKey(name)) {
			Holding holding = holdingMap.get(name);
			return holding.fundStats.risk(nMonth);
		} else {
			logger.error("Unexpected name");
			logger.error("  name      {}", name);
			logger.error("  portfolio {}", toString());
			throw new UnexpectedException("Unexpected");
		}
	}
	public double riskDaily(String name) {
		if (holdingMap.containsKey(name)) {
			Holding holding = holdingMap.get(name);
			return holding.fundStats.riskDaily(nMonth);
		} else {
			logger.error("Unexpected name");
			logger.error("  name      {}", name);
			logger.error("  portfolio {}", toString());
			throw new UnexpectedException("Unexpected");
		}
	}
	public double riskMonthly(String name) {
		if (holdingMap.containsKey(name)) {
			Holding holding = holdingMap.get(name);
			return holding.fundStats.riskMonthly(nMonth);
		} else {
			logger.error("Unexpected name");
			logger.error("  name      {}", name);
			logger.error("  portfolio {}", toString());
			throw new UnexpectedException("Unexpected");
		}
	}
}

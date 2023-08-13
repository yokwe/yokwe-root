package yokwe.util.finance;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import yokwe.util.UnexpectedException;
import yokwe.util.finance.online.SimpleReturn;

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
		double[] priceArray;
		
		Data(String name, double weight, double rateOfReturn, double[] priceArray) {
			this.name         = name;
			this.weight       = weight;
			this.rateOfReturn = rateOfReturn;
			this.priceArray   = priceArray;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %.3f  %d}", name, weight, priceArray == null ? -1 : priceArray.length);
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
	
	private void fillPriceMap(String name, Set<LocalDate> dateSet, Map<LocalDate, Double> priceMap) {
		if (dateSet.equals(priceMap.keySet())) return;
		
		LocalDate[] dateArray = dateSet.toArray(LocalDate[]::new);
		Arrays.sort(dateArray);

		Set<LocalDate> mapDateSet = priceMap.keySet();
		LocalDate[] mapDateArray = priceMap.keySet().toArray(LocalDate[]::new);
		Arrays.sort(mapDateArray);
		
		for(int i = 0; i < dateArray.length; i++) {
			LocalDate date = dateArray[i];
			if (!mapDateSet.contains(date)) {
				LocalDate lastDate = dateArray[Math.max(0, i - 1)];
				if (priceMap.containsKey(lastDate)) {
					Double lastValue = priceMap.get(lastDate);
					priceMap.put(date, lastValue);
					logger.warn("fillPriceMap  {}  {}  {}", name, date, lastValue);
				} else {
					logger.error("Unexpected");
					logger.error("  date      {}", date);
					logger.error("  lastDate  {}", lastDate);
					throw new UnexpectedException("Unexpected");
				}
			}
		}
	}
	
	/*
	private void fillDivMap(Set<LocalDate> dateSet, Map<LocalDate, Double> divMap) {
		if (dateSet.equals(divMap.keySet())) return;
		
		LocalDate[] dateArray = dateSet.toArray(LocalDate[]::new);
		Arrays.sort(dateArray);

		Set<LocalDate> mapDateSet = divMap.keySet();
		LocalDate[] mapDateArray = mapDateSet.toArray(LocalDate[]::new);
		Arrays.sort(mapDateArray);
		
		Double zero = Double.valueOf(0);
		for(int i = 0; i < dateArray.length; i++) {
			LocalDate date = dateArray[i];
			if (!mapDateSet.contains(date)) {
				divMap.put(date, zero);
			}
		}
	}
	*/
	
	private void prepare() {
		if (dataMap == null || holdingMapIsChanged) {
			dataMap = new TreeMap<>();
			for(var holding: holdingMap.values()) {
				String name = holding.name;
				Data   data = new Data(name, 0, 0, null);
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
				// update weight
				data.weight = holding.quantity / (double)totalQuantity;
				logger.info("quantity  {}  data {}", holding.quantity, data);
			}
			quantityIsChanged = false;
		}
		if (durationIsChanged) {
			Set<LocalDate> dateSet = new HashSet<>();
			for(var holding: holdingMap.values()) {
				dateSet.addAll(holding.dateSet(nMonth));
			}
			for(var holding: holdingMap.values()) {
				String name = holding.name;
				Data   data = dataMap.get(name);
				// update rateOfReturn
				data.rateOfReturn = holding.fundStats.rateOfReturn(nMonth);
				// update priceArray
				Map<LocalDate, Double> priceMap = holding.fundStats.priceMap(nMonth);
				fillPriceMap(name, dateSet, priceMap);
				data.priceArray = priceMap.values().stream().mapToDouble(o -> o).toArray();
				
				logger.info("duration  data {}", data);
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
			double[] simpleReturnArray = DoubleArray.toDoubleArray(data.priceArray, new SimpleReturn());
			statsArray[index]  = new Stats(simpleReturnArray);
			double sd = Stats.standardDeviation(simpleReturnArray) * FundStats.SQRT_DAY_IN_YEAR;
			logger.info("XXX sd  {}  {}  {}", index, weightArray[index], sd);

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

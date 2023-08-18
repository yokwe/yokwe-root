package yokwe.util.finance;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import yokwe.util.UnexpectedException;

public class Portfolio {
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
		
		Set<LocalDate> dateSet(int nMonth, int nOffset) {
			Set<LocalDate> set = new HashSet<>();
			Arrays.stream(fundStats.dateArray(nMonth, nOffset)).forEach(o -> set.add(o));
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
	private int                  nOffset    = 0;
	private Map<String, Holding> holdingMap = new TreeMap<>();
	//          name
	
	private Map<String, Data>    dataMap             = null;
	//          name
	private boolean              durationIsChanged   = true; // duration (nMonth) is changed
	private boolean              quantityIsChanged   = true; // quantity of holding is changed
	private boolean              holdingMapIsChanged = true; // new holding is added
	
	public Portfolio(int nMonth) {
		this.nMonth = nMonth;
	}
	public Portfolio() {
		this(0);
	}
	
	@Override
	public String toString() {
		return String.format("{%d  %s}", nMonth, holdingMap.values());
	}
	
	public Portfolio duration(int nMonth, int nOffset) {
		if (nMonth <= 0 || nOffset < 0) {
			logger.error("Unexpected value");
			logger.error("  nMonth    {}", nMonth);
			logger.error("  nOffset   {}", nOffset);
			throw new UnexpectedException("Unexpected value");
		}

		this.nMonth  = nMonth;
		this.nOffset = nOffset;
		
		durationIsChanged = true;
		return this;
	}
	public int duration() {
		return this.nMonth;
	}
	public int offset() {
		return this.nOffset;
	}
	public int holdingDuration() {
		return holdingMap.values().stream().mapToInt(o -> o.fundStats.duration).min().getAsInt();
	}
	public int holdingDuration(String name) {
		return getHolding(name).fundStats.duration;
	}
	
	public Portfolio clearQuantity() {
		holdingMap.values().forEach(o -> o.quantity = 0);
		
		quantityIsChanged = true;
		return this;
	}
	public Portfolio quantity(String name, int quantity) {
		getHolding(name).setQuantity(quantity);
		
		quantityIsChanged = true;
		return this;
	}
	
	public Portfolio add(String name, DailyPriceDiv[] dailyPriceDivArray, int quantity) {
		FundStats fundStats = FundStats.getInstance(name, dailyPriceDivArray);
		return add(fundStats, quantity);
	}
	public Portfolio add(FundStats fundStats, int quantity) {
		Holding holding = new Holding(fundStats.code, fundStats, quantity);
		holdingMap.put(fundStats.code, holding);
		
		quantityIsChanged   = true; // Need to update weight of holding because total quantity is changed.
		holdingMapIsChanged = true;
		return this;
	}
	public Portfolio add(String name, DailyPriceDiv[] dailyPriceDivArray) {
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
//				logger.info("quantity  {}  {}", data, holding.quantity);
			}
			quantityIsChanged = false;
		}
		if (durationIsChanged) {
			// commonDateSet contains common date of all holding
			Set<LocalDate> commonDateSet = new HashSet<>();
			for(var holding: holdingMap.values()) {
				Set<LocalDate> dateSet = holding.dateSet(nMonth, nOffset);
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
				data.rateOfReturn = holding.fundStats.rateOfReturn(nMonth, nOffset);
				// update data.returnArray
				{
					LocalDate[] dateArray   = holding.fundStats.dateArray(nMonth, nOffset);
					double[]    returnArray = holding.fundStats.returnArray(nMonth, nOffset);
					
					double[]    tempArray   = new double[dateArray.length];
					int         tempIndex   = 0;
					
					for(int i = 0; i < dateArray.length; i++) {
						if (commonDateSet.contains(dateArray[i])) {
							// if dateArray[i] is in commonDateSet, add returnArray[i] to tempArray
							tempArray[tempIndex++] = returnArray[i];
						} else {
							// if dataArray[i] is not in commonDateSet, skip this data
							logger.info("skip data  {}  {}", name, dateArray[i]);
						}
					}
					data.returnArray = Arrays.copyOf(tempArray, tempIndex);
				}
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
	
	private Holding getHolding(String name) {
		if (holdingMap.containsKey(name)) {
			return holdingMap.get(name);
		} else {
			logger.error("Unexpected name");
			logger.error("  name      {}", name);
			logger.error("  portfolio {}", toString());
			throw new UnexpectedException("Unexpected");
		}
	}
	public double rateOfReturn(String name) {
		return getHolding(name).fundStats.rateOfReturn(nMonth, nOffset);
	}
	public double risk(String name) {
		return getHolding(name).fundStats.risk(nMonth, nOffset);
	}
	public double riskDaily(String name) {
		return getHolding(name).fundStats.riskDaily(nMonth, nOffset);
	}
	public double correlation(String nameA, String nameB) {
		Stats a = new Stats(getHolding(nameA).fundStats.priceArray(nMonth, nOffset));
		Stats b = new Stats(getHolding(nameB).fundStats.priceArray(nMonth, nOffset));
		return Stats.correlation(a, b);
	}
}

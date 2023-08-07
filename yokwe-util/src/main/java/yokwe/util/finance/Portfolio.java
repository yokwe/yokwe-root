package yokwe.util.finance;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;

import yokwe.util.UnexpectedException;
import yokwe.util.finance.online.NoReinvestedValue;
import yokwe.util.finance.online.ReinvestedValue;
import yokwe.util.finance.online.SimpleReturn;

public final class Portfolio {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final class Holding {
		final String         name;
		final MonthlyStats[] monthlyStatsArray;
		final int            durationInMonth;

		int      quantity;
		double   weight;
		
		double[] retPrice;
		double[] retReinvestment;
		double[] retNoReinvestment;
		
		Holding(String code, DailyPriceDiv[] dailyPriceDivArray, int quantity) {
			// sanity check
			{
				if (dailyPriceDivArray == null) {
					logger.error("dailyPriceDivArray is null");
					throw new UnexpectedException("dailyPriceDivArray is null");
				}
				if (dailyPriceDivArray.length == 0) {
					logger.error("dailyPriceDivArray.length == 0");
					throw new UnexpectedException("dailyPriceDivArray.length == 0");
				}
			}
			
			this.name              = code;
			this.monthlyStatsArray = MonthlyStats.monthlyStatsArray(code, dailyPriceDivArray, 99999);
			this.durationInMonth   = monthlyStatsArray.length;
			
			setQuantity(quantity);
		}
		
		@Override
		public String toString() {
//			return String.format("{%s  %d  %d}", name, quantity, durationInMonth);
			return String.format("{%s  %d}", name, quantity);
		}
		
		void setQuantity(int newValue) {
			// sanity check
			if (quantity < 0) {
				logger.error("quantity < 0");
				logger.error("  quiantity  {}", quantity);
				throw new UnexpectedException("quantity < 0");
			}
			
			quantity = newValue;
		}
		void setWeight(int totalQuantity) {
			// sanity check
			{
				if (totalQuantity <= 0) {
					logger.error("totalQuantity <= 0");
					throw new UnexpectedException("totalQuantity <= 0");
				}
				if (quantity < 0) {
					logger.error("quantity < 0");
					throw new UnexpectedException("quantity <= 0");
				}
				if (totalQuantity < quantity) {
					logger.error("totalQuantity < quantity");
					throw new UnexpectedException("totalQuantity < quantity");
				}
			}

			weight = (double)quantity / totalQuantity;
		}
		void setDuration(int nYear, Set<LocalDate> dateSet) {
			final int nMonth = nYear * 12;
			// sanity check
			{
				if (nYear <= 0) {
					logger.error("Unexpected value");
					logger.error("  nYear  {}", nYear);
					throw new UnexpectedException("Unexpected value");
				}
				if (durationInMonth < nMonth) {
					logger.error("durationInMonth < nMonth");
					logger.error("  nMonth           {}", nMonth);
					logger.error("  durationInMonth  {}", durationInMonth);
					throw new UnexpectedException("durationInMonth < nMonth");
				}
			}
						
			final MonthlyStats startMonth = monthlyStatsArray[nMonth - 1];
			final MonthlyStats endMonth   = monthlyStatsArray[0];

			final int startIndex       = startMonth.startIndex;
			final int stopIndexPlusOne = endMonth.stopIndexPlusOne;

			final LocalDate[] dateArray  = startMonth.dateArray;
			final double[]    priceArray = startMonth.priceArray;
			final double[]    divArray   = startMonth.divArray;
			
			final double[]    myPriceArray;
			final double[]    myDivArray;
			{
				Set<LocalDate> myDateSet = Arrays.stream(dateArray).collect(Collectors.toSet());
				if (dateSet.equals(myDateSet)) {
					myPriceArray = DoubleArray.toDoubleArray(priceArray, startIndex, stopIndexPlusOne, DoubleUnaryOperator.identity());
					myDivArray   = DoubleArray.toDoubleArray(divArray,   startIndex, stopIndexPlusOne, DoubleUnaryOperator.identity());
				} else {
					// There is missing data in this fund.
					//   If mother fund of the fund registered in foreign stock exchange, the fund cannot trade if foreign stock exchange is closed.
					Map<LocalDate, Double> priceMap = new TreeMap<>();
					Map<LocalDate, Double> divMap   = new TreeMap<>();
					
					// build priceMap and divMap
					for(int j = startIndex; j < stopIndexPlusOne; j++) {
						LocalDate date =  dateArray[j];
						double    price = priceArray[j];
						double    div   = divArray[j];
						
						priceMap.put(date, price);
						divMap.put(date, div);
					}
					
					int length = dateSet.size();
					myPriceArray = new double[length];
					myDivArray   = new double[length];
					
					int index = 0;
					for(var date: dateSet) {				
						if (priceMap.containsKey(date)) {
							myPriceArray[index] = priceMap.get(date);
							myDivArray[index]   = divMap.get(date);
						} else {
							if (index == 0) {
								myPriceArray[index] = priceArray[startIndex];
								myDivArray[index]   = 0;
							} else {
								myPriceArray[index] = myPriceArray[index - 1];
								myDivArray[index]   = 0;
							}
							logger.warn("Supply missing data  {}  {}  {}  {}", name, index, date, myPriceArray[index]);
						}
						// update for next iteration
						index++;
					}
				}
			}
			
			// price
			this.retPrice          = DoubleArray.toDoubleArray(myPriceArray, DoubleUnaryOperator.identity());
			// reinvested price
			this.retReinvestment   = DoubleArray.toDoubleArray(myPriceArray, myDivArray, new ReinvestedValue());
			// no reinvested price
			this.retNoReinvestment = DoubleArray.toDoubleArray(myPriceArray, myDivArray, new NoReinvestedValue());
		}
		
		Set<LocalDate> getDateSet(int nYear) {
			final int nMonth = nYear * 12;
			// sanity check
			{
				if (nYear <= 0) {
					logger.error("Unexpected value");
					logger.error("  nYear  {}", nYear);
					throw new UnexpectedException("Unexpected value");
				}
				if (durationInMonth < nMonth) {
					logger.error("durationInMonth < nMonth");
					logger.error("  nMonth           {}", nMonth);
					logger.error("  durationInMonth  {}", durationInMonth);
					throw new UnexpectedException("durationInMonth < nMonth");
				}
			}
						
			final MonthlyStats startMonth = monthlyStatsArray[nMonth - 1];
			final MonthlyStats endMonth   = monthlyStatsArray[0];

			final int startIndex       = startMonth.startIndex;
			final int stopIndexPlusOne = endMonth.stopIndexPlusOne;

			final LocalDate[] dateArray  = startMonth.dateArray;
			
			Set<LocalDate> set = new TreeSet<>();
			for(int i = startIndex; i < stopIndexPlusOne; i++) {
				set.add(dateArray[i]);
			}
			
			return set;
		}
	}
	
	private int                  nYear = 0;
	private Map<String, Holding> map   = new TreeMap<>();
	//          name
	private boolean needsSetDuration  = true;
	private boolean needsSetWeight    = true;

	@Override
	public String toString() {
		return String.format("{%d  %s}", nYear, map.values());
	}
	
	public Portfolio add(String name, DailyPriceDiv[] dailyPriceDivArray, int quantity) {
		Holding holding = new Holding(name, dailyPriceDivArray, quantity);
		var oldValue = map.put(name, holding);
		if (oldValue != null) {
			logger.error("Duplicate name");
			logger.error("  name  {}", name);
			logger.error("  map   {}", map.keySet());
			throw new UnexpectedException("Duplicate name");
		}
		//
		needsSetDuration = true;
		
		return this;
	}
	public Portfolio add(String name, DailyPriceDiv[] dailyPriceDivArray) {
		return add(name, dailyPriceDivArray, 0);
	}

	public Portfolio setQuantity(String name, int newValue) {
		Holding holding = map.get(name);
		if (holding != null) {
			holding.setQuantity(newValue);
		} else {
			logger.error("Unknown name");
			logger.error("  name  {}", name);
			logger.error("  map   {}", map.keySet());
			throw new UnexpectedException("Unknown name");
		}
		needsSetWeight = true;
		
		return this;
	}
	public Portfolio setDuration(int newValue) {
		// sanity check
		if (newValue <= 0) {
			logger.error("newValue <= 0");
			logger.error("  newValue  {}", newValue);
			throw new UnexpectedException("newValue <= 0");
		}
		
		nYear            = newValue;
		needsSetDuration = true;
		
		return this;
	}
	private void prepare() {
		if (needsSetWeight) {
			// call holding.setWeight
			int totalQuantity = map.values().stream().mapToInt(o -> o.quantity).sum();
			for(var holding: map.values()) {
				holding.setWeight(totalQuantity);
			}
			//
			needsSetWeight = false;
		}
		
		if (needsSetDuration) {
			Set<LocalDate> dateSet = new TreeSet<>();
			// build dateSet
			for(var holding: map.values()) {
				dateSet.addAll(holding.getDateSet(nYear));
			}
			// call holding.setDuration
			for(var holding: map.values()) {
				holding.setDuration(nYear, dateSet);
			}
			//
			needsSetDuration = false;
		}
	}
	
	public int durationInMonth() {
		var opt = map.values().stream().mapToInt(o -> o.durationInMonth).min();
		return opt.orElse(0);
	}
	
	// Rate of return of reinvestment -- invest dividend
	public double rorReinvestment() {
		prepare();
		
		double result = 0;
		for(var e: map.values()) {
			double[] array = e.retReinvestment;
			//
			double startValue = array[0];
			double endValue   = array[array.length - 1];
			result += e.weight * SimpleReturn.getValue(startValue, endValue);
		}
		return SimpleReturn.compoundAnnualReturn(result, nYear);
	}
	// Rate of return of no reinvestment -- just take dividend
	public double rorNoReinvestment() {
		prepare();

		double result = 0;
		for(var e: map.values()) {
			double[] array = e.retNoReinvestment;
			//
			double startValue = array[0];
			double endValue   = array[array.length - 1];
			result += e.weight * SimpleReturn.getValue(startValue, endValue);
		}
		return SimpleReturn.compoundAnnualReturn(result, nYear);
	}
	// Standard deviation of simple return of price
	public double standardDeviation() {
		prepare();

		int length = map.size();
		
		double[] weightArray = new double[length];
		Stats[]  statsArray  = new Stats[length];
		
		int index = 0;
		for(var e: map.values()) {
			weightArray[index] = e.weight;
			statsArray[index]  = new Stats(DoubleArray.toDoubleArray(e.retPrice, new SimpleReturn()));
			// update for next iteration
			index++;
		}
				
		return Finance.annualStandardDeviationFromDailyStandardDeviation(Stats.standardDeviation(statsArray, weightArray));
	}

}

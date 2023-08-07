package yokwe.util.finance;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.util.GenericArray;
import yokwe.util.UnexpectedException;
import yokwe.util.finance.online.NoReinvestedValue;
import yokwe.util.finance.online.ReinvestedValue;
import yokwe.util.finance.online.SimpleReturn;

public final class Portfolio {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final class Holding {
		public final String         code;              // can be isinCode, stockCode or ticker symbol
		public final MonthlyStats[] monthlyStatsArray;
		public final int            durationInMonth;

		private int     quantity;
		private double  weight;
		private boolean needsSetWeight;
		
		public int         nYear;
		public LocalDate[] dateArray;
		public double[]    retPrice;
		public double[]    retReinvestment;
		public double[]    retNoReinvestment;
		
		
		public Holding(String code, DailyPriceDiv[] dailyPriceDivArray, int quantity) {
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
			
			this.code              = code;
			this.monthlyStatsArray = MonthlyStats.monthlyStatsArray(code, dailyPriceDivArray, 99999);
			this.durationInMonth   = monthlyStatsArray.length;
			
			setQuantity(quantity);
		}
		public Holding(String code, DailyPriceDiv[] dailyPriceDivArray) {
			this(code, dailyPriceDivArray, 0);
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %d  %d  %d}", code, durationInMonth, quantity, nYear);
		}
		
		private void setQuantity(int newValue) {
			// sanity check
			if (quantity < 0) {
				logger.error("quantity < 0");
				logger.error("  quiantity  {}", quantity);
				throw new UnexpectedException("quantity < 0");
			}

			quantity       = newValue;
			needsSetWeight = true;
		}
		public void setWeight(int totalQuantity) {
			// sanity check
			{
				if (totalQuantity <= 0) {
					logger.error("totalQuantity <= 0");
					throw new UnexpectedException("totalQuantity <= 0");
				}
				if (quantity <= 0) {
					logger.error("quantity <= 0");
					throw new UnexpectedException("quantity <= 0");
				}
				if (totalQuantity < quantity) {
					logger.error("totalQuantity < quantity");
					throw new UnexpectedException("totalQuantity < quantity");
				}
			}

			weight = (double)quantity / totalQuantity;
			//
			needsSetWeight = false;
		}
		public void setDuration(int nYear, Set<LocalDate> dateSet) {
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
			
			final LocalDate[] myDateArray;
			final double[]    myPriceArray;
			final double[]    myDivArray;
			{
				Set<LocalDate> myDateSet = Arrays.stream(dateArray).collect(Collectors.toSet());
				if (dateSet.equals(myDateSet)) {
					myDateArray  = GenericArray.toArray(startMonth.dateArray, startIndex, stopIndexPlusOne, Function.identity(), LocalDate.class);
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
					myDateArray  = new LocalDate[length];
					myPriceArray = new double[length];
					myDivArray   = new double[length];
					
					int index = 0;
					for(var date: dateSet) {
						myDateArray[index] = date;
						
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
							logger.warn("Supply missing data  {}  {}  {}  {}", code, index, date, myPriceArray[index]);
						}
						// update for next iteration
						index++;
					}
				}
			}
			
			// nYear
			this.nYear             = nYear;
			// date
			this.dateArray         = myDateArray;
			// price
			this.retPrice          = DoubleArray.toDoubleArray(myPriceArray, DoubleUnaryOperator.identity());
			// reinvested price
			this.retReinvestment   = DoubleArray.toDoubleArray(myPriceArray, myDivArray, new ReinvestedValue());
			// no reinvested price
			this.retNoReinvestment = DoubleArray.toDoubleArray(myPriceArray, myDivArray, new NoReinvestedValue());
		}
		
		public Set<LocalDate> getDateSet(int nYear) {
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
	
	public int                  nYear = 0;
	public Map<String, Holding> map   = new TreeMap<>();
	//         name
	private boolean needsSetDuration  = true;
	
	public void put(String name, Holding holding) {
		if (map.containsKey(name)) {
			logger.error("Duplicate name");
			logger.error("  name  {}", name);
			logger.error("  map   {}", map.keySet());
			throw new UnexpectedException("Duplicate name");
		} else {
			map.put(name, holding);
		}
		//
		needsSetDuration = true;
	}
	public void put(String name, String code, DailyPriceDiv[] dailyPriceDivArray, int quantity) {
		Holding holding = new Holding(code, dailyPriceDivArray, quantity);
		put(name, holding);
	}
	public void put(String name, String code, DailyPriceDiv[] dailyPriceDivArray) {
		Holding holding = new Holding(code, dailyPriceDivArray);
		put(name, holding);
	}
	public Holding get(String name) {
		if (map.containsKey(name)) {
			return map.get(name);
		} else {
			logger.error("Unknown name");
			logger.error("  name  {}", name);
			logger.error("  map   {}", map.keySet());
			throw new UnexpectedException("Duplicate name");
		}
	}

	public void setQuantity(String name, int newValue) {
		if (map.containsKey(name)) {
			// sanity check
			{
				if (newValue < 0) {
					logger.error("newValue < 0");
					logger.error("  newValue  {}", newValue);
					throw new UnexpectedException("newValue < 0");
				}
			}

			Holding holding = map.get(name);
			holding.setQuantity(newValue);
		} else {
			logger.error("Unknown name");
			logger.error("  name  {}", name);
			logger.error("  map   {}", map.keySet());
			throw new UnexpectedException("Unknown name");
		}
	}
	public void setDuration(int nYear) {
		// sanity check
		{
			if (nYear <= 0) {
				logger.error("nYear <= 0");
				logger.error("  nYear  {}", nYear);
				throw new UnexpectedException("nYear <= 0");
			}
		}
		this.nYear = nYear;
		//
		needsSetDuration = true;
	}
	public void prepare() {
		{
			boolean needsSetWeight = false;
			for(var holding: map.values()) {
				if (holding.needsSetWeight) {
					needsSetWeight = true;
					break;
				}
			}
			if (needsSetWeight) {
				// call holding.setWeight
				int totalQuantity = map.values().stream().mapToInt(o -> o.quantity).sum();
				for(var holding: map.values()) {
					holding.setWeight(totalQuantity);
				}
			}
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

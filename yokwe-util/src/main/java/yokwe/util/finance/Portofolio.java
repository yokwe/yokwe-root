package yokwe.util.finance;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

public final class Portofolio {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final class Entry {
		public final String         code;              // can be isinCode, stockCode or ticker symbol
		public final int            quantity;
		public final MonthlyStats[] monthlyStatsArray;
		public final int            durationInMonth;
		public final double         weight;
		
		public Entry(String code, DailyPriceDiv[] dailyPriceDivArray, int quantity) {
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
				if (quantity <= 0) {
					logger.error("quantity <= 0");
					throw new UnexpectedException("quantity <= 0");
				}
			}
			
			this.code              = code;
			this.quantity          = quantity;
			this.monthlyStatsArray = MonthlyStats.monthlyStatsArray(code, dailyPriceDivArray, 99999);
			this.durationInMonth   = monthlyStatsArray.length;
			this.weight            = 0;
		}
		public Entry(String code, MonthlyStats[] monthlyStatsArray, int quantity, double weight) {
			this.code              = code;
			this.quantity          = quantity;
			this.monthlyStatsArray = monthlyStatsArray;
			this.durationInMonth   = monthlyStatsArray.length;
			this.weight            = weight;
		}
	}
	
	public static final class Builder {
		private final List<Entry> list = new ArrayList<>();
		
		private int totalQuantity = 0;
		
		public Builder add(String code, DailyPriceDiv[] dailyPriceDivArray, int quantity) {
			list.add(new Entry(code, dailyPriceDivArray, quantity));
			totalQuantity += quantity;
			return this;
		}

		public Portofolio build(int nYear) {
			if (list.isEmpty()) {
				logger.error("list is empty");
				throw new UnexpectedException("list is empty");
			}
			
			final int length = list.size();
			Entry[] entryArray = new Entry[length];
			for(int i = 0; i < length; i++) {
				Entry  entry  = list.get(i);
				double weigth = (double)entry.quantity / totalQuantity;
				entryArray[i] = new Entry(entry.code, entry.monthlyStatsArray, entry.quantity, weigth);
			}
			return new Portofolio(entryArray, nYear);
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Context {
		public final Entry       entry;
		public final LocalDate[] dateArray;
		public final double[]    retPrice;
		public final double[]    retReinvestment;
		public final double[]    retNoReinvestment;
		
		public Context(Entry entry, LocalDate[] dateArray, double[] retPrice, double[] retReinvestment, double[] retNoReinvestment) {
			this.entry             = entry;
			this.dateArray         = dateArray;
			this.retPrice          = retPrice;
			this.retReinvestment   = retReinvestment;
			this.retNoReinvestment = retNoReinvestment;
		}
	}
	

	private final Context[] contextArray;
	private final int       nYear;
	
	private Portofolio(Entry[] entryArray, int nYear) {
		final int nMonth = nYear * 12;
		
		// sanity check
		{
			if (nYear <= 0) {
				logger.error("Unexpected value");
				logger.error("  nYear  {}", nMonth);
				throw new UnexpectedException("Unexpected value");
			}
			for(var e: entryArray) {
				if (e.durationInMonth < nMonth) {
					logger.error("Not enough duration for nMonth");
					logger.error("  nMonth           {}", nMonth);
					logger.error("  durationInMonth  {}", e.durationInMonth);
					throw new UnexpectedException("Not enough duration for nMonth");
				}
			}
		}

		this.contextArray = new Context[entryArray.length];
		this.nYear        = nYear;
		
		Set<LocalDate> dateSet       = new TreeSet<>();
		{
			for(int i = 0; i < entryArray.length; i++) {
				Entry entry = entryArray[i];
				
				MonthlyStats startMonth = entry.monthlyStatsArray[nMonth - 1];
				MonthlyStats endMonth   = entry.monthlyStatsArray[0];

				int startIndex       = startMonth.startIndex;
				int stopIndexPlusOne = endMonth.stopIndexPlusOne;

				LocalDate[] dateArray = GenericArray.toArray(startMonth.dateArray, startIndex, stopIndexPlusOne, Function.identity(), LocalDate.class);
				Set<LocalDate> set = Arrays.stream(dateArray).collect(Collectors.toSet());
				dateSet.addAll(set);
			}
		}
		
		for(int i = 0; i < entryArray.length; i++) {
			Entry entry = entryArray[i];
			
			MonthlyStats startMonth = entry.monthlyStatsArray[nMonth - 1];
			MonthlyStats endMonth   = entry.monthlyStatsArray[0];
			
			int startIndex       = startMonth.startIndex;
			int stopIndexPlusOne = endMonth.stopIndexPlusOne;
			
			
			LocalDate[] dateArray  = startMonth.dateArray;
			double[]    priceArray = startMonth.priceArray;
			double[]    divArray   = startMonth.divArray;
			
			
			final LocalDate[] myDateArray;
			final double[]    myPriceArray;
			final double[]    myDivArray;
			{
				final int length = stopIndexPlusOne - startIndex;
				if (length == dateSet.size()) {
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
					
					int myLength = dateSet.size();
					myDateArray  = new LocalDate[myLength];
					myPriceArray = new double[myLength];
					myDivArray   = new double[myLength];
					
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
							logger.warn("Supply missing data  {}  {}  {}  {}", entry.code, index, date, myPriceArray[index]);
						}
						// update for next iteration
						index++;
					}
					
				}
			}
			
			// price
			double[]    retPrice          = DoubleArray.toDoubleArray(myPriceArray, DoubleUnaryOperator.identity());
			// reinvested price
			double[]    retReinvestment   = DoubleArray.toDoubleArray(myPriceArray, myDivArray, new ReinvestedValue());
			// no reinvested price
			double[]    retNoReinvestment = DoubleArray.toDoubleArray(myPriceArray, myDivArray, new NoReinvestedValue());

			contextArray[i] = new Context(entryArray[i], myDateArray, retPrice, retReinvestment, retNoReinvestment);
		}		
	}
	
	// return Portofolio for last nYear
	public Portofolio getInstance(int nYear) {
		Entry[] entryArray = GenericArray.toArray(contextArray, 0, contextArray.length, o -> o.entry, Entry.class);
		return new Portofolio(entryArray, nYear);
	}
	
	// Rate of return of reinvestment -- invest dividend
	public double rorReinvestment() {
		double result = 0;
		for(var e: contextArray) {
			double[] array = e.retReinvestment;
			//
			double startValue = array[0];
			double endValue   = array[array.length - 1];
			result += e.entry.weight * SimpleReturn.getValue(startValue, endValue);
		}
		return SimpleReturn.compoundAnnualReturn(result, nYear);
	}
	// Rate of return of no reinvestment -- just take dividend
	public double rorNoReinvestment() {
		double result = 0;
		for(var e: contextArray) {
			double[] array = e.retNoReinvestment;
			//
			double startValue = array[0];
			double endValue   = array[array.length - 1];
			result += e.entry.weight * SimpleReturn.getValue(startValue, endValue);
		}
		return SimpleReturn.compoundAnnualReturn(result, nYear);
	}
	// Standard deviation of simple return of price
	public double standardDeviation() {
		int length = contextArray.length;
		
		double[] weightArray = new double[length];
		Stats[]  statsArray  = new Stats[length];
		for(int i = 0; i < length; i++) {
			Context context = contextArray[i];
			weightArray[i] = context.entry.weight;
			
			// use simple return of price
			statsArray[i]  = new Stats(DoubleArray.toDoubleArray(context.retPrice, new SimpleReturn()));
		}
		
		return Finance.annualStandardDeviationFromDailyStandardDeviation(Stats.standardDeviation(statsArray, weightArray));
	}
	
	public double standardDeviation2() {
		int length = contextArray[0].retPrice.length;
		double[] data = new double[length];
		
		// create synthesize value from retPrice and weight
		for(var e: contextArray) {
			double[] simpleReturn = DoubleArray.toDoubleArray(e.retPrice, new SimpleReturn());
			for(int i = 0; i < length; i++) {
				data[i] += e.entry.weight * simpleReturn[i];
			}
		}
		
		// return standard deviation of synthesize value
		return Finance.annualStandardDeviationFromDailyStandardDeviation(Stats.standardDeviation(data));
	}
	
}

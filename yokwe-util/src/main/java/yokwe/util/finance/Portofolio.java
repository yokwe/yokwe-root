package yokwe.util.finance;

import java.util.ArrayList;
import java.util.List;

import yokwe.util.GenericArray;
import yokwe.util.UnexpectedException;
import yokwe.util.finance.online.ReinvestedValue;
import yokwe.util.finance.online.SimpleReturn;

public final class Portofolio {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final class Entry {
		public final String         isinCode;
		public final int            quantity;
		public final MonthlyStats[] monthlyStatsArray;
		public final int            durationInMonth;
		public final double         weight;
		
		public Entry(String isinCode, DailyPriceDiv[] dailyPriceDivArray, int quantity) {
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
			
			this.isinCode          = isinCode;
			this.quantity          = quantity;
			this.monthlyStatsArray = MonthlyStats.monthlyStatsArray(isinCode, dailyPriceDivArray, 99999);
			this.durationInMonth   = monthlyStatsArray.length;
			this.weight            = 0;
		}
		public Entry(String isinCode, MonthlyStats[] monthlyStatsArray, int quantity, double weight) {
			this.isinCode          = isinCode;
			this.quantity          = quantity;
			this.monthlyStatsArray = monthlyStatsArray;
			this.durationInMonth   = monthlyStatsArray.length;
			this.weight            = weight;
		}
	}
	
	public static final class Builder {
		private final List<Entry> list = new ArrayList<>();
		
		private int totalQuantity = 0;
		
		public Builder add(String isinCode, DailyPriceDiv[] dailyPriceDivArray, int quantity) {
			list.add(new Entry(isinCode, dailyPriceDivArray, quantity));
			totalQuantity += quantity;
			return this;
		}

		public Portofolio getInstance(int nYear) {
			if (list.isEmpty()) {
				logger.error("list is empty");
				throw new UnexpectedException("list is empty");
			}
			
			final int length = list.size();
			Entry[] entryArray = new Entry[length];
			for(int i = 0; i < length; i++) {
				Entry  entry  = list.get(i);
				double weigth = (double)entry.quantity / totalQuantity;
				entryArray[i] = new Entry(entry.isinCode, entry.monthlyStatsArray, entry.quantity, weigth);
			}
			return new Portofolio(entryArray, nYear);
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Context {
		public final Entry    entry;
		public final double[] retPrice;
		public final double[] retReinvestment;
		
		public Context(Entry entry, double[] retPrice, double[] retReinvestment) {
			this.entry           = entry;
			this.retPrice        = retPrice;
			this.retReinvestment = retReinvestment;
		}
	}
	

	private final Context[] contextArray;
	private final int       nYear;
	
	private Portofolio(Entry[] entryArray, int nYear) {
		final int nMonth = nYear * 12;
		
		// sanity check
		{
			if (nMonth <= 0) {
				logger.error("Unexpected value");
				logger.error("  nMonth  {}", nMonth);
				throw new UnexpectedException("Unexpected value");
			}
			for(var e: entryArray) {
				if (e.durationInMonth < nMonth) {
					logger.error("too short duration");
					logger.error("  nMonth           {}", nMonth);
					logger.error("  durationInMonth  {}", e.durationInMonth);
					throw new UnexpectedException("too short duration");
				}
			}
		}

		this.contextArray = new Context[entryArray.length];
		this.nYear        = nYear;
		for(int i = 0; i < entryArray.length; i++) {
			Entry entry = entryArray[i];
			
			MonthlyStats startMonth = entry.monthlyStatsArray[nMonth - 1];
			MonthlyStats endMonth   = entry.monthlyStatsArray[0];
			
			int startIndex       = startMonth.startIndex;
			int stopIndexPlusOne = endMonth.stopIndexPlusOne;
			
			double[] priceArray  = startMonth.priceArray;
			double[] divArray    = startMonth.divArray;
			
			// simple return of price
			double[] retPrice        = DoubleArray.toDoubleArray(priceArray, startIndex, stopIndexPlusOne, new SimpleReturn());
			// reinvesed price
			double[] retReinvestment = DoubleArray.toDoubleArray(priceArray, divArray, startIndex, stopIndexPlusOne, new ReinvestedValue());

			contextArray[i] = new Context(entryArray[i], retPrice, retReinvestment);
		}
	}
	
	
	public Portofolio getInstance(int nYear) {
		Entry[] entryArray = GenericArray.toArray(contextArray, 0, contextArray.length, o -> o.entry, Entry.class);
		return new Portofolio(entryArray, nYear);
	}
	
	public double rorReinvestment() {
		double result = 0;
		for(var e: contextArray) {
			double startValue = e.retReinvestment[0];
			double endValue   = e.retReinvestment[e.retReinvestment.length - 1];
			result += e.entry.weight * SimpleReturn.getValue(startValue, endValue);
		}
		return SimpleReturn.compoundAnnualReturn(result, nYear);
	}
	public double standardDeviation() {
		int length = contextArray.length;
		
		// FIXME standardDeviation use priceArray
		
		double[] weightArray = new double[length];
		Stats[]  statsArray  = new Stats[length];
		for(int i = 0; i < length; i++) {
			Context context = contextArray[i];
			weightArray[i] = context.entry.weight;
			statsArray[i]  = new Stats(context.retPrice);
		}
		
		return Finance.annualStandardDeviationFromDailyStandardDeviation(Stats.standardDeviation(statsArray, weightArray));
	}
	
}

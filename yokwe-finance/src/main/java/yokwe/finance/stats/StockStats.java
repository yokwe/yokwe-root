package yokwe.finance.stats;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import yokwe.finance.type.DailyValue;
import yokwe.finance.type.OHLCV;
import yokwe.util.finance.online.HV;
import yokwe.util.finance.online.LogReturn;
import yokwe.util.finance.online.Mean;
import yokwe.util.finance.online.RSI;
import yokwe.util.finance.online.Variance;

public class StockStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static double doubleOrElse(double value, double orElse) {
		return Double.isNaN(value) ? orElse : value;
	}
	
	public static StockStats getInstance(String stockCode, LocalDate dateStop, List<OHLCV> priceList, List<DailyValue> divList) {
		// NOTE dateStart an dateStop is inclusive
		var dateStartY1 = dateStop.minusYears(1).plusDays(1);
		var dateStartY3 = dateStop.minusYears(3).plusDays(1);

		// sanity check
		if (priceList == null) {
			logger.warn("price is null");
			return null;
		}
		if (divList == null) {
			logger.warn("div is null");
			return null;
		}
		
		OHLCV[] priceArray = priceList.stream().filter(o -> !o.date.isBefore(dateStartY1) && !o.date.isAfter(dateStop)).toArray(OHLCV[]::new);
		// sanity check
		if (priceArray.length < 4) {
			logger.warn("number of price in date range is too small {}  {}  {}", dateStartY1, dateStop, priceArray.length);
			return null;
		}
		double[] closeArray = Arrays.stream(priceArray).mapToDouble(o -> o.close.doubleValue()).toArray();
		double[] volArray   = Arrays.stream(priceArray).mapToDouble(o -> o.volume).toArray();
		
		DailyValue[] divArray;
		// build divArray from divList
		{
			// remove 0 dividend entries
			divList.removeIf(o -> o.value.doubleValue() == 0);
			if (divList.isEmpty()) {
				divArray = new DailyValue[0];
			} else {
				var last = divList.get(divList.size() - 1);
				if (!last.date.isBefore(dateStartY1)) {
					var divStartDate = last.date.minusYears(1);
					divArray = divList.stream().filter(o -> o.date.isAfter(divStartDate)).toArray(DailyValue[]::new);
				} else {
					divArray = new DailyValue[0];
				}
			}
		}
		
		
		// build stats
		StockStats stats = new StockStats();
		stats.date   = priceArray[priceArray.length - 1].date;
		stats.pricec = priceList.size();
		stats.price  = priceArray[priceArray.length - 1].close.doubleValue();
		
		{
			if (2 <= stats.pricec) {
				double lastClose = priceArray[priceArray.length - 2].close.doubleValue();
				stats.last = LogReturn.getValue(lastClose, stats.price);
			} else {
				stats.last = 0;
			}
		}
		
		// sd hv
		{
			if (30 <= stats.pricec) {
				{
					var logReturn = Arrays.stream(closeArray).map(new LogReturn()).toArray();
					Variance variance = new Variance();
					variance.accept(logReturn);
					stats.sd = doubleOrElse(variance.standardDeviation(), -1);
				}
				{
					HV hv = new HV();
					stats.hv = doubleOrElse(hv.applyAsDouble(closeArray), -1);
				}
			} else {
				stats.sd = -1;
				stats.hv = -1;
			}
		}
		
		// rsi
		{
			if (RSI.DEFAULT_SIZE <= stats.pricec) {
				RSI rsi = new RSI();
				stats.rsi = doubleOrElse(rsi.applyAsDouble(closeArray), -1);;
			} else {
				stats.rsi = -1;
			}
		}
		
		// min max
		{
			var min = Arrays.stream(priceArray).mapToDouble(o -> o.low.doubleValue()).min().getAsDouble();
			var max = Arrays.stream(priceArray).mapToDouble(o -> o.high.doubleValue()).max().getAsDouble();
			
			stats.min = LogReturn.getValue(min, stats.price);
			stats.max = LogReturn.getValue(stats.price, max);
		}
		{
			OHLCV[] priceArrayY3 = priceList.stream().filter(o -> !o.date.isBefore(dateStartY3) && !o.date.isAfter(dateStop)).toArray(OHLCV[]::new);
			var min = Arrays.stream(priceArrayY3).mapToDouble(o -> o.low.doubleValue()).min().getAsDouble();
			var max = Arrays.stream(priceArrayY3).mapToDouble(o -> o.high.doubleValue()).max().getAsDouble();
			
			stats.minY3 = LogReturn.getValue(min, stats.price);
			stats.maxY3 = LogReturn.getValue(stats.price, max);

		}
		
		// dividend
		{
			stats.divc     = divArray.length;
			
			if (divArray.length == 0) {
				stats.lastDiv       = 0;
				stats.forwardYield  = 0;
				stats.trailingYield = 0;
				stats.annualDiv     = 0;
			} else {
				stats.lastDiv  = divArray[divArray.length - 1].value.doubleValue();
				stats.forwardYield = (stats.lastDiv * stats.divc) / stats.price;
				
				stats.annualDiv = Arrays.stream(divArray).mapToDouble(o -> o.value.doubleValue()).sum();
				stats.trailingYield = stats.annualDiv / stats.price;
			}
		}
		
		// volume
		{
			int length = volArray.length;
			
			stats.vol = (long)volArray[length - 1];
			
			if (5 <= stats.pricec) {
				Mean mean = new Mean();
				stats.vol5 = (long)mean.applyAsDouble(volArray, length - 5, length);
			} else {
				stats.vol5 = -1;
			}
			
			if (21 <= stats.pricec) {
				Mean mean = new Mean();
				stats.vol21 = (long)mean.applyAsDouble(volArray, length - 21, length);
			} else {
				stats.vol21 = -1;
			}
		}
		
		return stats;
	}
	
	
	// current date, price and volume
	public LocalDate date;
	public int       pricec;
	public double    price;
	public double	 last;
	
	// stats - sd hv rsi
	//  30 < pricec
	public double sd;
	public double hv;
	// 15 <= pricec
	public double rsi;
	
	// min max
	public double min;
	public double max;
	public double minY3;
	public double maxY3;
	
	// dividend
	public int    divc;
	public double lastDiv;
	public double forwardYield;
	public double annualDiv;
	public double trailingYield;

	// volume
	public long   vol;
	// 5 <= pricec
	public long   vol5;
	// 20 <= pricec
	public long   vol21;
	
}

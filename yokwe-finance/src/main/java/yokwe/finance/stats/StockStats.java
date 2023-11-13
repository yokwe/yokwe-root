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
import yokwe.util.finance.online.SimpleReturn;
import yokwe.util.finance.online.Variance;

public class StockStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static double doubleOrElse(double value, double orElse) {
		return Double.isNaN(value) ? orElse : value;
	}
	
	public static StockStats getInstance(LocalDate dateStart, LocalDate dateStop, List<OHLCV> priceList, List<DailyValue> divList) {
		// NOTE dateStart an dateStop is inclusive
		
		// sanity check
		if (priceList == null) {
			logger.warn("price is null");
			return null;
		}
		if (divList == null) {
			logger.warn("div is null");
			return null;
		}
		
		OHLCV[] priceArray = priceList.stream().filter(o -> !o.date.isBefore(dateStart) && !o.date.isAfter(dateStop)).toArray(OHLCV[]::new);
		// sanity check
		if (priceArray.length < 4) {
			logger.warn("number of price in date range is too small {}  {}  {}", dateStart, dateStop, priceArray.length);
			return null;
		}
		double[] closeArray = Arrays.stream(priceArray).mapToDouble(o -> o.close.doubleValue()).toArray();
		double[] volArray   = Arrays.stream(priceArray).mapToDouble(o -> o.volume).toArray();
		
		double[] divArray;
		// build divArray from divList
		{
			// remove 0 dividend entries
			divList.removeIf(o -> o.value.doubleValue() == 0);
			if (divList.isEmpty()) {
				divArray = new double[0];
			} else {
				var last = divList.get(divList.size() - 1);
				if (last.date.isAfter(dateStart)) {
					var divStartDate = last.date.minusYears(1);
					divArray = divList.stream().filter(o -> o.date.isAfter(divStartDate) && !o.date.isAfter(dateStop)).mapToDouble(o -> o.value.doubleValue()).toArray();
				} else {
					divArray = new double[0];
				}
			}
		}
		
		
		// build stats
		StockStats stats = new StockStats();
		stats.date   = priceArray[priceArray.length - 1].date;
		stats.pricec = priceArray.length;
		stats.price  = priceArray[priceArray.length - 1].close.doubleValue();
		
		{
			double lastClose = priceArray[priceArray.length - 1].close.doubleValue();
			stats.last = SimpleReturn.getValue(lastClose, stats.price);
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
			
			stats.min = 1.0 - (min / stats.price);
			stats.max = (max / stats.price) - 1;
		}
		
		// dividend
		{
			stats.divc  = divArray.length;
			stats.yield = Arrays.stream(divArray).sum() / stats.price;
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
	
	// dividend
	public int    divc;
	public double yield;

	// volume
	public long   vol;
	// 5 <= pricec
	public long   vol5;
	// 20 <= pricec
	public long   vol21;
	
}

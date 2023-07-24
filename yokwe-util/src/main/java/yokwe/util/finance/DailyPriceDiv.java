package yokwe.util.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;

import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.finance.online.Mean;
import yokwe.util.finance.online.NoReinvestedValue;
import yokwe.util.finance.online.OnlineDoubleBinaryOperator;
import yokwe.util.finance.online.ReinvestedValue;

public final class DailyPriceDiv implements Comparable<DailyPriceDiv> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static double[] toDoubleArray(DailyPriceDiv[] array, int startIndex, int stopIndexPlusOne, ToDoubleFunction<DailyPriceDiv> op) {
		// Sanity check
		Util.checkIndex(array, startIndex, stopIndexPlusOne);

		int length = stopIndexPlusOne - startIndex;
		double[] result = new double[length];
		for(int i = 0, j = startIndex; i < length; i++, j++) {
			result[i] = op.applyAsDouble(array[j]);
		}
		return result;
	}
	private static <R> R[] toArray(DailyPriceDiv[] array, int startIndex, int stopIndexPlusOne, Function<DailyPriceDiv, R> op, Class<R> clazz) {
		// Sanity check
		Util.checkIndex(array, startIndex, stopIndexPlusOne);

		int length = stopIndexPlusOne - startIndex;
		R[] result;
		{
			IntFunction<R[]> generator = new Util.Generator<R>(clazz);
			result = generator.apply(length);
		}
		
		for(int i = 0, j = startIndex; i < length; i++, j++) {
			result[i] = op.apply(array[j]);
		}
		return result;
	}
	
	
	public static LocalDate[] toDateArray(DailyPriceDiv[] array, int startIndex, int stopIndexPlusOne) {
		return toArray(array, startIndex, stopIndexPlusOne, o -> o.date, LocalDate.class);
	}
	public static double[] toPriceArray(DailyPriceDiv[] array, int startIndex, int stopIndexPlusOne) {
		return toDoubleArray(array, startIndex, stopIndexPlusOne, o -> o.price);
	}
	public static double[] toDivArray(DailyPriceDiv[] array, int startIndex, int stopIndexPlusOne) {
		return toDoubleArray(array, startIndex, stopIndexPlusOne, o -> o.div);
	}
	//
	public static LocalDate[] toDateArray(DailyPriceDiv[] array) {
		return toDateArray(array, 0, array.length);
	}
	public static double[] toPriceArray(DailyPriceDiv[] array) {
		return toPriceArray(array, 0, array.length);
	}
	public static double[] toDivArray(DailyPriceDiv[] array) {
		return toDivArray(array, 0, array.length);
	}
	
	
	public static <T, U> DailyPriceDiv[] toDailyPriceDivArray(
		T[] priceArray, Function<T, LocalDate> opPriceDate, ToDoubleFunction<T> opPrice,
		U[] divArray,   Function<U, LocalDate> opDivDate,   ToDoubleFunction<U> opDiv,
		int startIndex, int stopIndexPlusOne
		) {
		// Sanity check
		Util.checkIndex(priceArray, divArray, startIndex, stopIndexPlusOne);
		
		Map<LocalDate, DailyPriceDiv> map = new TreeMap<>();
		
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			var entry = priceArray[i];
			LocalDate date  = opPriceDate.apply(entry);
			double    price = opPrice.applyAsDouble(entry);
			DailyPriceDiv dailyPriceDiv = new DailyPriceDiv(date, price, 0);
			if (map.containsKey(date)) {
				logger.error("Duplicate date");
				logger.error("  date  {}", date);
				logger.error("  entry {}", entry);
				throw new UnexpectedException("Duplicate date");
			} else {
				map.put(date, dailyPriceDiv);
			}
		}
		
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			var entry = divArray[i];
			LocalDate date = opDivDate.apply(entry);
			double    div  = opDiv.applyAsDouble(entry);
			if (map.containsKey(date)) {
				var oldEntry = map.get(date);
				if (oldEntry.div != 0) {
					logger.error("oldEntry contains div");
					logger.error("  oldEntry {}", oldEntry);
					logger.error("  entry    {}", entry);
					throw new UnexpectedException("oldEntry contains div");
				} else {
					var newEntry = new DailyPriceDiv(oldEntry.date, oldEntry.price, div);
					map.put(date, newEntry);
				}
			} else {
				logger.error("Unpexpected date");
				logger.error("  date  {}", date);
				logger.error("  entry {}", entry);
				throw new UnexpectedException("Unpexpected date");
			}
		}

		DailyPriceDiv[] array = map.values().toArray(new DailyPriceDiv[0]);
		Arrays.sort(array);
		return array;
	}
	public static <T, U> DailyPriceDiv[] toDailyPriceDivArray(
		T[] priceArray, Function<T, LocalDate> opPriceDate, ToDoubleFunction<T> opPrice,
		U[] divArray,   Function<U, LocalDate> opDivDate,   ToDoubleFunction<U> opDiv
		) {
		return toDailyPriceDivArray(priceArray, opPriceDate, opPrice, divArray, opDivDate, opDiv, 0, priceArray.length);
	}

	
	public final LocalDate date;
	public final double    price;
	public final double    div;
	
	public DailyPriceDiv(LocalDate date, double price, double div) {
		this.date  = date;
		this.price = price;
		this.div   = div;
		
		// sanity check
		if (Double.isInfinite(price)) {
			logger.error("price is infinite");
			logger.error("  {}", this);
			throw new UnexpectedException("price is infinite");
		}
		if (Double.isInfinite(div)) {
			logger.error("div is infinite");
			logger.error("  {}", this);
			throw new UnexpectedException("div is infinite");
		}
	}
	public DailyPriceDiv(LocalDate date, BigDecimal price, BigDecimal div) {
		this(date, price.doubleValue(), div.doubleValue());
	}
	public DailyPriceDiv(String dateString, double price, double div) {
		this(LocalDate.parse(dateString), price, div);
	}
	
	private LocalDate getKey() {
		return date;
	}

	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	@Override
	public int compareTo(DailyPriceDiv that) {
		return this.getKey().compareTo(that.getKey());
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof DailyPriceDiv) {
				DailyPriceDiv that = (DailyPriceDiv)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}
	@Override
	public int hashCode() {
		return this.getKey().hashCode();
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		{
			DailyPriceDiv[] array;
			{
				List<DailyPriceDiv> list = new ArrayList<>();
				list.add(new DailyPriceDiv("2023-01-01", 10, 0.00));
				list.add(new DailyPriceDiv("2023-01-02", 11, 0.01));
				list.add(new DailyPriceDiv("2023-01-02", 12, 0.00));
				list.add(new DailyPriceDiv("2023-01-02", 13, 0.01));
				
				array = list.toArray(new DailyPriceDiv[0]);
			}
			
			double[] priceArray = DailyPriceDiv.toPriceArray(array);
			double[] divArray = DailyPriceDiv.toDivArray(array);
			
			{
				OnlineDoubleBinaryOperator op = new ReinvestedValue();
				double[] values = DoubleArray.toDoubleArray(priceArray, divArray, op);
				for(var e: values) logger.info("XX  {}", e);
			}
			{
				OnlineDoubleBinaryOperator op = new NoReinvestedValue();
				double[] values = DoubleArray.toDoubleArray(priceArray, divArray, op);
				for(var e: values) logger.info("YY  {}", e);
			}
		}
		
		{
			double[] array = {10, 11, 12, 13, 14, 15, 16};
			var op = new Mean();
			double[] values = DoubleArray.toDoubleArray(array, op);

			for(int i = 0; i < array.length; i++) {
				logger.info("  {}  {}", array[i], values[i]);
			}
		}
		
		
		logger.info("STOP");
	}
}

package yokwe.util;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import yokwe.util.GenericArray.Collect;
import yokwe.util.GenericArray.CollectImpl;

public final class BigDecimalArray {
	//
	// GenericArray.toArray
	//
	
	
	//
	// create BigDecimal array from other type of array using map and op
	//
	public static <T> BigDecimal[] toArray(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> map, Function<BigDecimal, BigDecimal> op) {
		return GenericArray.toArray(array, startIndex, stopIndexPlusOne, map, op, BigDecimal.class);
	}
	public static <T> BigDecimal[] toArray(T[] array, Function<T, BigDecimal> function, Function<BigDecimal, BigDecimal> op) {
		// call above method
		return toArray(array, 0, array.length, function, op);
	}

	
	//
	// create simple ratio BigDecimal array from other type of array using map
	//
	private static final class SimpleReturnOp implements UnaryOperator<BigDecimal> {
		private boolean    firstTime = true;
		private BigDecimal previous  = null;
		
		@Override
		public BigDecimal apply(BigDecimal value) {
			if (firstTime) {
				// use first value as previous
				firstTime = false;
				previous  = value;
			}
			
			// ret = (value / previous) - 1
			BigDecimal ret   = BigDecimalUtil.toSimpleReturn(previous, value);
			
			// update for next iteration
			previous = value;
			
			return ret;
		}
	}
	public static <T> BigDecimal[] toSimpleReturn(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> map) {
		UnaryOperator<BigDecimal> op = new SimpleReturnOp();
		return toArray(array, startIndex, stopIndexPlusOne, map, op);
	}
	public static <T> BigDecimal[] toSimpleReturn(T[] array, Function<T, BigDecimal> function) {
		// call above method
		return toSimpleReturn(array, 0, array.length, function);
	}
	
	
	//
	// create log ratio BigDecimal array from other type of array using map
	//
	private static final class LogReturnOp implements UnaryOperator<BigDecimal> {
		private boolean    firstTime = true;
		private BigDecimal previousLog  = null;
		
		@Override
		public BigDecimal apply(BigDecimal value) {
			if (firstTime) {
				// use first value as previous
				firstTime   = false;
				previousLog = BigDecimalUtil.mathLog(value);
			}

			BigDecimal valueLog = BigDecimalUtil.mathLog(value);
			BigDecimal ret      = valueLog.subtract(previousLog);
			
			// update for next iteration
			previousLog = valueLog;
			
			return ret;
		}
	}
	public static <T> BigDecimal[] toLogReturn(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> map) {
		UnaryOperator<BigDecimal> op = new LogReturnOp();
		return toArray(array, startIndex, stopIndexPlusOne, map, op);
	}
	public static <T> BigDecimal[] toLogReturn(T[] array, Function<T, BigDecimal> function) {
		// call above method
		return toLogReturn(array, 0, array.length, function);
	}
	
	
	//
	// GenericArray.collect
	//
	
	
	//
	// calculate BigDecimal sum from other type of array using map
	//
	private static final class SumImpl implements CollectImpl<BigDecimal> {
		private BigDecimal sum = BigDecimal.ZERO;
		@Override
		public void accept(BigDecimal value) {
			sum  = sum.add(value);
		}
		@Override
		public BigDecimal get() {
			return sum;
		}
	}
	public static <T> BigDecimal sum(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> map) {
		var collector = new Collect<>(SumImpl::new);
		return GenericArray.collect(array, startIndex, stopIndexPlusOne, map, collector);
	}

	public static <T> BigDecimal sum(T[] array, Function<T, BigDecimal> map) {
		// call above method
		return sum(array, 0, array.length, map);
	}
	
	
	//
	// calculate BigDecimal arithmetic mean from other type of array using map
	//
	private static final class MeanImpl implements CollectImpl<BigDecimal> {
		private int        count = 0;
		private BigDecimal total   = BigDecimal.ZERO;
		@Override
		public void accept(BigDecimal value) {
			count++;
			total  = total.add(value);
		}
		@Override
		public BigDecimal get() {
			return BigDecimalUtil.divide(total, BigDecimal.valueOf(count));
		}
	}
	public static <T> BigDecimal mean(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> map) {
		var collector = new Collect<>(MeanImpl::new);
		return GenericArray.collect(array, startIndex, stopIndexPlusOne, map, collector);

	}
	public static <T> BigDecimal mean(T[] array, Function<T, BigDecimal> map) {
		// call above method
		return mean(array, 0, array.length, map);
	}
	
	
	//
	// calculate BigDecimal geometric mean from other type of array using map
	//
	private static final class GeometricMeanImpl implements CollectImpl<BigDecimal> {
		private int        count = 0;
		private BigDecimal total = BigDecimal.ZERO;
		
		@Override
		public void accept(BigDecimal value) {
			count++;
			total = total.add(BigDecimalUtil.mathLog(value));
		}

		@Override
		public BigDecimal get() {
			BigDecimal value = BigDecimalUtil.divide(total, BigDecimal.valueOf(count));;
			return BigDecimalUtil.mathExp(value);
		}
	}
	public static <T> BigDecimal geometricMean(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> map) {
		var collector = new Collect<>(GeometricMeanImpl::new);
		return GenericArray.collect(array, startIndex, stopIndexPlusOne, map, collector);
	}
	public static <T> BigDecimal geometricMean(T[] array, Function<T, BigDecimal> map) {
		// call above method
		return geometricMean(array, 0, array.length, map);
	}
	
	
	//
	// calculate BigDecimal variance from other type of array using map
	//
	private static final class VarianceImpl implements CollectImpl<BigDecimal> {
		private int        count = 0;
		private BigDecimal total = BigDecimal.ZERO;
		private BigDecimal mean;
		
		VarianceImpl(BigDecimal mean) {
			this.mean = mean;
		}
		@Override
		public void accept(BigDecimal value) {
			count++;
			// total += (mean - value) ^2
			total = total.add(BigDecimalUtil.square(mean.subtract(value)));
		}

		@Override
		public BigDecimal get() {
			// return total / count;
			return BigDecimalUtil.divide(total, BigDecimal.valueOf(count));
		}
	}
	public static <T> BigDecimal variance(T[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean, Function<T, BigDecimal> map) {
		var collector = new Collect<>(() -> new VarianceImpl(mean));
		return GenericArray.collect(array, startIndex, stopIndexPlusOne, map, collector);
	}
	public static <T> BigDecimal variance(T[] array, BigDecimal mean, Function<T, BigDecimal> map) {
		// call above method
		return variance(array, 0, array.length, mean, map);
	}
	
	
	//
	// calculate BigDecimal standard deviation from other type of array using map
	//
	public static <T> BigDecimal standardDeviation(T[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean, Function<T, BigDecimal> map) {
		return BigDecimalUtil.mathSqrt(variance(array, startIndex, stopIndexPlusOne, mean, map));
	}
	public static <T> BigDecimal standardDeviation(T[] array, BigDecimal mean, Function<T, BigDecimal> map) {
		// call above method
		return standardDeviation(array, 0, array.length, mean, map);
	}
	
	
	//
	// calculate BigDecimal RSI from other type of array using map
	//
	private static final class RSI_Wilder implements UnaryOperator<BigDecimal> {
		// See https://school.stockcharts.com/doku.php?id=technical_indicators:relative_strength_index_rsi
		private static final BigDecimal N_100 = BigDecimal.valueOf(100);
		
		private final int          size;
		
		private final BigDecimal   bdSize;
		private final BigDecimal   bdSizeMinusOne;
		
		private int        count = 0;
		private BigDecimal lastValue = null;
		private BigDecimal sumGain   = BigDecimal.ZERO;
		private BigDecimal sumLoss   = BigDecimal.ZERO;
		private BigDecimal avgGain   = null;
		private BigDecimal avgLoss   = null;
		
		public RSI_Wilder(int size_) {
			size           = size_;			
			bdSize         = BigDecimal.valueOf(size);
			bdSizeMinusOne = BigDecimal.valueOf(size - 1);
		}
		
		@Override
		public BigDecimal apply(BigDecimal value) {
			final BigDecimal change;
			final BigDecimal changeGain;
			final BigDecimal changeLoss;
			{
				change = lastValue == null ? BigDecimal.ZERO : value.subtract(lastValue);
				int compare = change.compareTo(BigDecimal.ZERO);
				
				changeGain = 0 < compare ? change : BigDecimal.ZERO;
				changeLoss = compare < 0 ? change.negate() : BigDecimal.ZERO;
			}
			
			if (count < size) {
				sumGain = sumGain.add(changeGain);
				sumLoss = sumLoss.add(changeLoss);
			} else if (count == size) {
				sumGain = sumGain.add(changeGain);
				sumLoss = sumLoss.add(changeLoss);
				
				avgGain = BigDecimalUtil.divide(sumGain, bdSize);
				avgLoss = BigDecimalUtil.divide(sumLoss, bdSize);
				sumGain = null;
				sumLoss = null;
			} else {
				avgGain = BigDecimalUtil.divide(BigDecimalUtil.multiply(avgGain, bdSizeMinusOne).add(changeGain), bdSize);
				avgLoss = BigDecimalUtil.divide(BigDecimalUtil.multiply(avgLoss, bdSizeMinusOne).add(changeLoss), bdSize);
			}
			
			// update for next iteration
			lastValue  = value;
			count++;
			
			if (avgLoss == null) {
				return BigDecimal.ONE.negate();
			} else {
				if (avgLoss.equals(BigDecimal.ZERO)) return BigDecimal.ONE.negate(); // avoid divide by zero
				BigDecimal rs  = BigDecimalUtil.divide(avgGain, avgLoss);                               // RS = average gain / average loss
				BigDecimal rsi = N_100.subtract(BigDecimalUtil.divide(N_100,  BigDecimal.ONE.add(rs))); // 100 - (100 / (1 + RS))
				
//				logger.info("accept {}",
//					String.format("%2d  %8.2f  %8.2f  %8.2f  %8.2f  %8.2f  %8.2f  %8.2f  %8.2f",
//					count, value.doubleValue(),
//					change.doubleValue(), changeGain.doubleValue(), changeLoss.doubleValue(),
//					avgGain.doubleValue(), avgLoss.doubleValue(),
//					rs.doubleValue(), rsi.doubleValue()));
				
				return rsi;
			}
		}
	}
	public static <T> BigDecimal[] toRSI(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> map) {
		UnaryOperator<BigDecimal> op = new RSI_Wilder(14);
		return toArray(array, startIndex, stopIndexPlusOne, map, op);
	}
	public static <T> BigDecimal[] toRSI(T[] array, Function<T, BigDecimal> function) {
		// call above method
		return toRSI(array, 0, array.length, function);
	}

	
	private static void testA() {
		// See http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:relative_strength_index_rsi
		BigDecimal data[] = {
			new BigDecimal("44.3389"),
			new BigDecimal("44.0902"),
			new BigDecimal("44.1497"),
			new BigDecimal("43.6124"),
			new BigDecimal("44.3278"),
			new BigDecimal("44.8264"),
			new BigDecimal("45.0955"),
			new BigDecimal("45.4245"),
			new BigDecimal("45.8433"),
			new BigDecimal("46.0826"),
			new BigDecimal("45.8931"),
			new BigDecimal("46.0328"),
			new BigDecimal("45.6140"),
			new BigDecimal("46.2820"),
			new BigDecimal("46.2820"),
			new BigDecimal("46.0028"),
			new BigDecimal("46.0328"),
			new BigDecimal("46.4116"),
			new BigDecimal("46.2222"),
			new BigDecimal("45.6439"),
			new BigDecimal("46.2122"),
			new BigDecimal("46.2521"),
			new BigDecimal("45.7137"),
			new BigDecimal("46.4515"),
			new BigDecimal("45.7835"),
			new BigDecimal("45.3548"),
			new BigDecimal("44.0288"),
			new BigDecimal("44.1783"),
			new BigDecimal("44.2181"),
			new BigDecimal("44.5672"),
			new BigDecimal("43.4205"),
			new BigDecimal("42.6628"),
			new BigDecimal("43.1314"),
		};
		
		var op = new RSI_Wilder(14);
		for(int i = 0; i < data.length; i++) {
			var rsi = op.apply(data[i]);
			logger.info("data {}", String.format("%2d  %6.4f  %6.4f", i + 1, data[i].doubleValue(), rsi.doubleValue()));
		}
		logger.info("last line should be 43.13  37.77");
	}
	
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	public static void main(String[] args) {
		logger.info("START");
		
		testA();
		
		logger.info("STOP");
	}
}

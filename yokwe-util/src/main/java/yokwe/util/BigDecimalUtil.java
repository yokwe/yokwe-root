package yokwe.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import yokwe.util.stats.DoubleArray;
import yokwe.util.stats.MA;

public final class BigDecimalUtil {
	private static final int          DOUBLE_PRECISION       = 15; // precision of double type
	private static final int          DEFAULT_PRECISION      = DOUBLE_PRECISION;
	private static final RoundingMode DEFAULT_ROUNDING_MODE  = RoundingMode.HALF_EVEN;
	private static final MathContext  DEFAULT_MATH_CONTEXT   = new MathContext(DEFAULT_PRECISION, DEFAULT_ROUNDING_MODE);
	
	public static final BigDecimal MINUS_1  = BigDecimal.valueOf(-1);
	public static final BigDecimal PLUS_100 = BigDecimal.valueOf(100);
	
	// 
	// ----------------------------
	// invoke static method of Math
	// ----------------------------
	//
	
	//
	// mathLog  -- Math.log
	//
	public static BigDecimal mathLog(BigDecimal x) {
		return BigDecimal.valueOf(Math.log(x.doubleValue())).round(DEFAULT_MATH_CONTEXT);
	}
	
	
	//
	// mathExp -- Math.exp
	//
	public static BigDecimal mathExp(BigDecimal x) {
		return BigDecimal.valueOf(Math.exp(x.doubleValue())).round(DEFAULT_MATH_CONTEXT);
	}
	
	
	//
	// mathPow  -- Math.pow
	//
	public static BigDecimal mathPow(BigDecimal x, BigDecimal y) {
		return BigDecimal.valueOf(Math.pow(x.doubleValue(), y.doubleValue())).round(DEFAULT_MATH_CONTEXT);
	}
	
	
	//
	// mathSqrt -- Math.sqrt
	//
	public static BigDecimal mathSqrt(BigDecimal x) {
		return BigDecimal.valueOf(Math.sqrt(x.doubleValue())).round(DEFAULT_MATH_CONTEXT);
	}

	
	// 
	// ------------------
	// convenience method
	// ------------------
	//
	
	//
	// setScale
	//
	public static BigDecimal setScale(BigDecimal value, int newScale) {
		return value.setScale(newScale, DEFAULT_ROUNDING_MODE);
	}
	
	
	//
	// round
	//
	public static BigDecimal round(BigDecimal value) {
		return value.round(DEFAULT_MATH_CONTEXT);
	}
	
	
	//
	// return a / b
	//
	public static BigDecimal divide(BigDecimal a, BigDecimal b) {
		return a.divide(b, DEFAULT_MATH_CONTEXT);
	}
	
	
	//
	// return a * b
	//
	public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
		return a.multiply(b, DEFAULT_MATH_CONTEXT);
	}
	
	
	//
	// return a + b
	//
	public static BigDecimal add(BigDecimal a, BigDecimal b) {
		return a.add(b, DEFAULT_MATH_CONTEXT);
	}
	
	
	//
	// return a - b
	//
	public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
		return a.subtract(b, DEFAULT_MATH_CONTEXT);
	}
	
	
	//
	// return simple return from previous and price
	//
	public static BigDecimal toSimpleReturn(BigDecimal previous, BigDecimal value) {
		// return (value / previous) - 1
		return divide(value, previous).subtract(BigDecimal.ONE);
	}
	
	
	//
	// return square of value -- value * value
	//
	public static BigDecimal square(BigDecimal value) {
		// return value * value
		return multiply(value, value);
	}
	
	
	//
	// return exponential moving average
	//
	public static final class EMA implements Supplier<BigDecimal>, Consumer<BigDecimal>, UnaryOperator<BigDecimal>  {
		public static final BigDecimal DEFAULT_DECAY_FACTOR = new BigDecimal("0.94");
		public static final BigDecimal DEFAULT_ALPHA        = getAlphaFromDecayFactor(DEFAULT_DECAY_FACTOR);
		
		private static final BigDecimal N_2 = BigDecimal.valueOf(2);
		public static BigDecimal getAlphaDataSize(int dataSize) {
			// From alpha = 2 / (N + 1)
			return divide(N_2, BigDecimal.valueOf(dataSize + 1));
		}
		public static BigDecimal getAlphaFromDecayFactor(BigDecimal decayFactor) {
			// 1 - decayFactor
			return BigDecimal.ONE.subtract(decayFactor);
		}
		
		private BigDecimal alpha;
		private BigDecimal average = null;
		
		public EMA(BigDecimal alpha) {
			this.alpha = alpha;
		}

		@Override
		public void accept(BigDecimal value) {
			if (average == null) average = value;
			// average = average + alpha * (value - average);
			average = average.add(multiply(alpha, value.subtract(average)));
		}
		@Override
		public BigDecimal get( ) {
			return average;
		}
		@Override
		public BigDecimal apply(BigDecimal value) {
			accept(value);
			return get();
		}
	}
	
	
	//
	// return historical volatility
	//
	public static final class HV implements Supplier<BigDecimal>, Consumer<BigDecimal>, UnaryOperator<BigDecimal> {
		public  static final BigDecimal CONFIDENCE_95_PERCENT = new BigDecimal("1.65");
		public  static final BigDecimal CONFIDENCE_99_PERCENT = new BigDecimal("2.33");
		
		public  static final BigDecimal TIME_HORIZON_DAY    = BigDecimal.valueOf(1);   // daily to daily
		public  static final BigDecimal TIME_HORIZON_WEEK   = BigDecimal.valueOf(5);   // daily to weekly
		public  static final BigDecimal TIME_HORIZON_MONTH  = BigDecimal.valueOf(21);  // daily to monthly
		public  static final BigDecimal TIME_HORIZON_YEAR   = BigDecimal.valueOf(252); // daily to annual
		
		public  static final BigDecimal DEFAULT_ALPHA        = new BigDecimal("0.06"); // 1 - 0.94
		
		private final EMA        ema;
		private final BigDecimal k;
		
		private BigDecimal previousLog = null;
		
		public HV(BigDecimal alpha, BigDecimal confidence, BigDecimal timeHorizon) {
			ema = new EMA(alpha);
			// k = confidence x sqrt(timeHorizon)
			k = multiply(confidence, mathSqrt(timeHorizon));
		}
		public HV(BigDecimal timeHorizon) {
			this(DEFAULT_ALPHA, CONFIDENCE_95_PERCENT, timeHorizon);
		}
		
		@Override
		public BigDecimal get() {
			return mathSqrt(multiply(ema.get(), k));
		}
		@Override
		public void accept(BigDecimal value) {
			if (previousLog == null) {
				previousLog = mathLog(value);
			}
			
			BigDecimal valueLog = mathLog(value);
			BigDecimal change   = valueLog.subtract(previousLog);
			ema.accept(square(change));
			
			// update for next iteration
			previousLog = valueLog;
		}
		@Override
		public BigDecimal apply(BigDecimal value) {
			accept(value);
			return get();
		}
	}
	
	
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	private static void testTable53() {
		// See Table 5.3 of page 82
		//   https://www.msci.com/documents/10199/5915b101-4206-4ba0-aee2-3449d5c7e95a
		//   https://elischolar.library.yale.edu/cgi/viewcontent.cgi?article=1546&context=ypfs-documents
		double alpha = 0.06; // 1 - 0.94 
		
		double[] data = {
			 0.633,
			 0.115,
			-0.459,
			 0.093,
			 0.176,
			-0.087,
			-0.142,
			 0.324,
			-0.943,
			-0.528,
			-0.107,
			-0.159,
			-0.445,
		 	 0.053,
			 0.152,
			-0.318,
			 0.424,
			-0.708,
			-0.105,
			-0.257,
			};
		BigDecimal[] array = Arrays.stream(data).mapToObj(o -> square(BigDecimal.valueOf(o))).toArray(BigDecimal[]::new);

		
		double     var_r[] = Arrays.stream(DoubleArray.multiply(data, data)).map(MA.EMA.ema(alpha)).toArray();
		EMA ema = new EMA(BigDecimal.valueOf(alpha));
		BigDecimal var_s[] = Arrays.stream(array).map(ema).toArray(BigDecimal[]::new);
		
		logger.info("");
		for(int i = 0; i < var_r.length; i++) {
			logger.info("Table 5.3 {}", String.format("%8.3f  %8.3f  %8.3f", data[i], var_r[i], var_s[i].setScale(4, DEFAULT_ROUNDING_MODE)));
		}
	}
	public static void main(String[] args) {
		testTable53();
		
		{
			BigDecimal[] a = {BigDecimal.valueOf(10), BigDecimal.valueOf(20), BigDecimal.valueOf(30), BigDecimal.valueOf(15)};
			BigDecimal[] b = BigDecimalArray.toSimpleReturn(a, Function.identity());
			
			for(var e: a) {
				logger.info("a  {}", e.stripTrailingZeros().toPlainString());
			}
			for(var e: b) {
				logger.info("b  {}", e.stripTrailingZeros().toPlainString());
			}
		}
	}

}

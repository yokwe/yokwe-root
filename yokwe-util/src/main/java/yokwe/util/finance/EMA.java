package yokwe.util.finance;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.UnaryOperator;

import static yokwe.util.BigDecimalUtil.*;

//
// return exponential moving average
//
public final class EMA implements UnaryOperator<BigDecimal>  {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private final BigDecimal alpha;
	
	private BigDecimal average = null;
	
	public EMA(BigDecimal alpha) {
		this.alpha = alpha;
	}

	@Override
	public BigDecimal apply(BigDecimal value) {
		if (average == null) average = value;
		
		// average = value * alpha + average * (1 - alpha)
		//         = average * (1 - alpha) + value * alpha
		//         = average - average * alpha + value * alpha
		//         = average + alpha * (value - average)
		// number of multiply is reduced
		average = average.add(multiply(alpha, value.subtract(average)));
		return average;
	}
	
	private static void testTable53() {
		// See Table 5.3 of page 82
		//   https://www.msci.com/documents/10199/5915b101-4206-4ba0-aee2-3449d5c7e95a
		//   https://elischolar.library.yale.edu/cgi/viewcontent.cgi?article=1546&context=ypfs-documents
		BigDecimal alpha = new BigDecimal("0.06"); // 1 - 0.94 
		
		BigDecimal[] data = {
			new BigDecimal("0.633"),
			new BigDecimal("0.115"),
			new BigDecimal("-0.459"),
			new BigDecimal("0.093"),
			new BigDecimal("0.176"),
			new BigDecimal("-0.087"),
			new BigDecimal("-0.142"),
			new BigDecimal("0.324"),
			new BigDecimal("-0.943"),
			new BigDecimal("-0.528"),
			new BigDecimal("-0.107"),
			new BigDecimal("-0.159"),
			new BigDecimal("-0.445"),
		 	new BigDecimal("0.053"),
			new BigDecimal("0.152"),
			new BigDecimal("-0.318"),
			new BigDecimal("0.424"),
			new BigDecimal("-0.708"),
			new BigDecimal("-0.105"),
			new BigDecimal("-0.257"),
		};
		
		BigDecimal[] var_r = Arrays.stream(data).map(o -> multiply(o, o)).map(new EMA(alpha)).toArray(BigDecimal[]::new);
		logger.info("");
		for(int i = 0; i < var_r.length; i++) {
			logger.info("Table 5.3 {}", String.format("%8.3f  %8.3f", data[i], var_r[i].doubleValue()));
		}
		logger.info("last line should be -0.257     0.224");
	}
	
	public static void main(String[] args) {
		logger.info("START");
		testTable53();
		logger.info("STOP");
	}

}
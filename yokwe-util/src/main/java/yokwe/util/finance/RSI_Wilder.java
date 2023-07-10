package yokwe.util.finance;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static yokwe.util.BigDecimalUtil.MINUS_1;
import static yokwe.util.BigDecimalUtil.PLUS_100;
import static yokwe.util.BigDecimalUtil.divide;
import static yokwe.util.BigDecimalUtil.multiply;
import static yokwe.util.BigDecimalUtil.add;
import static yokwe.util.BigDecimalUtil.subtract;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;

// Calculate RSI using Wilder methods
//   See https://school.stockcharts.com/doku.php?id=technical_indicators:relative_strength_index_rsi
public class RSI_Wilder implements UnaryOperator<BigDecimal> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final int DEFAULT_SIZE = 14;
	
	private final int          size;
	private final BigDecimal   alpha;
	
	private int        count = 0;
	private BigDecimal lastValue = null;
	private BigDecimal sumGain   = ZERO;
	private BigDecimal sumLoss   = ZERO;
	private BigDecimal avgGain   = null;
	private BigDecimal avgLoss   = null;
	
	public RSI_Wilder() {
		this(DEFAULT_SIZE);
	}
	public RSI_Wilder(int size_) {
		size  = size_;
		// alpha = 1 / size
		alpha = divide(ONE, BigDecimal.valueOf(size));
	}
	
	@Override
	public BigDecimal apply(BigDecimal value) {
		final BigDecimal change;
		final BigDecimal changeGain;
		final BigDecimal changeLoss;
		{
			change = lastValue == null ? ZERO : value.subtract(lastValue);
			int compare = change.compareTo(ZERO);
			
			changeGain = 0 < compare ? change : ZERO;
			changeLoss = compare < 0 ? change.negate() : ZERO;
		}
		
		if (count < size) {
			sumGain = add(sumGain, changeGain);
			sumLoss = add(sumLoss, changeLoss);
		} else if (count == size) {
			sumGain = add(sumGain, changeGain);
			sumLoss = add(sumLoss, changeLoss);
			
			avgGain = multiply(sumGain, alpha);
			avgLoss = multiply(sumLoss, alpha);
			sumGain = null;
			sumLoss = null;
		} else {
			// avg = value * alpha + avg * (1 - alpha)
			//     = avg * (1 - alpha) + value * alpha
			//     = avg - avg * alpha + value * alpha
			//     = avg + alpha * (value - avg)
			// number of multiply is reduced
			
			avgGain = add(avgGain, multiply(alpha, subtract(changeGain, avgGain)));
			avgLoss = add(avgLoss, multiply(alpha, subtract(changeLoss, avgLoss)));
		}
		
		// update for next iteration
		lastValue = value;
		count++;
		
		final BigDecimal rsi;
		{
			if (avgLoss == null) {
				rsi = MINUS_1;
			} else {
				// a = avgGain * 100
				BigDecimal a = multiply(avgGain, PLUS_100);
				// b = avgGain + avgLoss
				BigDecimal b = add(avgGain, avgLoss);
				
				if (b.equals(BigDecimal.ZERO)) {
					rsi = MINUS_1;
				} else {
					// rsi = a / b
					rsi = divide(a, b);
				}
			}
		}
		
		return rsi;
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
	
	public static void main(String[] args) {
		logger.info("START");
		
		testA();
		
		logger.info("STOP");
	}
}

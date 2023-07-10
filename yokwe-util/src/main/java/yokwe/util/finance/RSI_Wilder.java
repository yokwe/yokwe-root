package yokwe.util.finance;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;

import yokwe.util.BigDecimalUtil;

// Calculate RSI using Wilder methods
//   See https://school.stockcharts.com/doku.php?id=technical_indicators:relative_strength_index_rsi
public class RSI_Wilder implements UnaryOperator<BigDecimal> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final int DEFAULT_SIZE = 14;
	
	private final int          size;
	
	private final BigDecimal   bdSize;
	private final BigDecimal   bdSizeMinusOne;
	
	private int        count = 0;
	private BigDecimal lastValue = null;
	private BigDecimal sumGain   = BigDecimal.ZERO;
	private BigDecimal sumLoss   = BigDecimal.ZERO;
	private BigDecimal avgGain   = null;
	private BigDecimal avgLoss   = null;
	
	public RSI_Wilder() {
		this(DEFAULT_SIZE);
	}
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
			// avgGain = (avgGain * (size - 1) + changeGain) / size
			// avgLoss = (avgLoss * (size - 1) + changeGain) / size
			avgGain = BigDecimalUtil.divide(BigDecimalUtil.multiply(avgGain, bdSizeMinusOne).add(changeGain), bdSize);
			avgLoss = BigDecimalUtil.divide(BigDecimalUtil.multiply(avgLoss, bdSizeMinusOne).add(changeLoss), bdSize);
		}
		
		// update for next iteration
		lastValue = value;
		count++;
		
		final BigDecimal rsi;
		{
			// avoid null or divide by zero
			if (avgLoss == null || avgLoss.equals(BigDecimal.ZERO)) {
				rsi = BigDecimalUtil.MINUS_1;
			} else {
				// RS = average gain / average loss
				BigDecimal rs  = BigDecimalUtil.divide(avgGain, avgLoss);
				// RSI = 100 - (100 / (1 + RS))
				rsi = BigDecimalUtil.PLUS_100.subtract(BigDecimalUtil.divide(BigDecimalUtil.PLUS_100,  BigDecimal.ONE.add(rs)));
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

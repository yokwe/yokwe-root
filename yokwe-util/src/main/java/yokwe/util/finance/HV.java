package yokwe.util.finance;

import static yokwe.util.BigDecimalUtil.divide;
import static yokwe.util.BigDecimalUtil.mathLog;
import static yokwe.util.BigDecimalUtil.mathSqrt;
import static yokwe.util.BigDecimalUtil.multiply;
import static yokwe.util.BigDecimalUtil.square;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

//
// return historical volatility
//
public class HV implements UnaryOperator<BigDecimal>, Consumer<BigDecimal>, Supplier<BigDecimal> {
	public  static final BigDecimal CONFIDENCE_95_PERCENT = new BigDecimal("1.65");
	public  static final BigDecimal CONFIDENCE_99_PERCENT = new BigDecimal("2.33");
	
	public  static final int TIME_HORIZON_DAY     =   1;
	public  static final int TIME_HORIZON_WEEK    =   5;
	public  static final int TIME_HORIZON_MONTH   =  21;
	public  static final int TIME_HORIZON_YEAR    = 252;
	
	public  static final BigDecimal DEFAULT_ALPHA   = new BigDecimal("0.06"); // 1 - 0.94 
	
	private final EMA ema;
	
	private BigDecimal last = null;
	
	public HV(BigDecimal alpha) {
		ema = new EMA(alpha);
	}
	public HV() {
		this(DEFAULT_ALPHA);
	}
	
	public BigDecimal get(BigDecimal[] array) {
		ema.clear();
		for(var e: array) {
			accept(e);
		}
		return get();
	}
	
	public BigDecimal getVaR(BigDecimal confidence, int timeHorizon) {
		// 		return getValue() * confidence * Math.sqrt(timeHorizon);
		return multiply(get(), multiply(confidence, mathSqrt(BigDecimal.valueOf(timeHorizon))));
	}
	public BigDecimal getVaR95(int timeHorizon) {
		return getVaR(CONFIDENCE_95_PERCENT, timeHorizon);
	}
	public BigDecimal getVaR99(int timeHorizon) {
		return getVaR(CONFIDENCE_99_PERCENT, timeHorizon);
	}
	
	@Override
	public BigDecimal get() {
		// return Math.sqrt(ema.getValue());
		return mathSqrt(ema.get());
	}
	
	@Override
	public void accept(BigDecimal value) {
		if (last != null) {
			BigDecimal change = mathLog(divide(value, last));
			ema.accept(square(change));
		}
		
		last = value;
	}
	
	@Override
	public BigDecimal apply(BigDecimal value) {
		accept(value);
		return get();
	}
}

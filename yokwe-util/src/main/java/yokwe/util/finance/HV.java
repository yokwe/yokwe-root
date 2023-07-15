package yokwe.util.finance;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleUnaryOperator;

import yokwe.util.UnexpectedException;

//
// return historical volatility
//
public final class HV implements DoubleUnaryOperator, DoubleConsumer {
	public static final double CONFIDENCE_95_PERCENT = 1.65;
	public static final double CONFIDENCE_99_PERCENT = 2.33;
	
	public static final int TIME_HORIZON_DAY     =   1;
	public static final int TIME_HORIZON_WEEK    =   5;
	public static final int TIME_HORIZON_MONTH   =  21;
	public static final int TIME_HORIZON_YEAR    = 252;
	
	public static final double DEFAULT_ALPHA   = 0.06; // 1 - 0.94
	
	private final EMA ema;
	
	private boolean firstTime    = true;
	private double  lastLogValue = 0;
	
	public HV(double alpha) {
		ema = new EMA(alpha);
	}
	public HV() {
		this(DEFAULT_ALPHA);
	}
	
	public double getVaR(double confidence, int timeHorizon) {
		return get() * confidence * Math.sqrt(timeHorizon);
	}
	public double getVaR95(int timeHorizon) {
		return getVaR(CONFIDENCE_95_PERCENT, timeHorizon);
	}
	public double getVaR99(int timeHorizon) {
		return getVaR(CONFIDENCE_99_PERCENT, timeHorizon);
	}
	
	public double get() {
		return Math.sqrt(ema.getAsDouble());
	}
	
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			DoubleArray.logger.error("value is infinite");
			DoubleArray.logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}
		
		double logValue = Math.log(value);
		
		if (firstTime) {
			firstTime = false;
		} else {
			double change = logValue - lastLogValue;
			ema.accept(change * change);
		}
		
		// update for next iteration
		lastLogValue = logValue;
	}
	
	@Override
	public double applyAsDouble(double value) {
		accept(value);
		return get();
	}
}

package yokwe.util.finance;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;

import yokwe.util.BigDecimalUtil;

public final class LogReturn implements UnaryOperator<BigDecimal> {
	private boolean    firstTime    = true;
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

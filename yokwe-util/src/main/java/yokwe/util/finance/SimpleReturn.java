package yokwe.util.finance;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;

import yokwe.util.BigDecimalUtil;

public final class SimpleReturn implements UnaryOperator<BigDecimal> {
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

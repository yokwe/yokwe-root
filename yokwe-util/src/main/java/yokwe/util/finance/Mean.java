package yokwe.util.finance;

import yokwe.util.UnexpectedException;
import yokwe.util.finance.DoubleArray.ToDoubleImpl;

class Mean implements ToDoubleImpl {
	private int    count  = 0;
	private double result = 0;
	
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			DoubleArray.logger.error("value is infinite");
			DoubleArray.logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}
		
		count++;
		result += value;
	}

	@Override
	public double get() {
		return count == 0 ? Double.NaN : result / count;
	}
}
package yokwe.util.finance;

import java.util.function.DoubleUnaryOperator;

import yokwe.util.UnexpectedException;

//
// simple moving average
//
class SMA implements DoubleUnaryOperator {
	private final int      size;
	private final double[] data;

	private int      count = 0;
	private int      index = 0;
	private double   sum   = 0;
	
	public SMA(int size_) {
		size = size_;
		data = new double[size];
	}
	
	@Override
	public double applyAsDouble(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			DoubleArray.logger.error("value is infinite");
			DoubleArray.logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}
		
		final double result;
		if (count < size) {
			// write data
			data[index] = value;
			// update sum
			sum += value;
			// set result
			result = sum / (index + 1);
		} else {
			// adjust sum
			sum -= data[index];
			// overwrite data
			data[index] = value;
			// update sum
			sum += value;
			// set result
			result = sum / size;
		}
		
		// update for next iteration
		count++;
		index++;
		if (index == size) index = 0;
		
		return result;
	}
}
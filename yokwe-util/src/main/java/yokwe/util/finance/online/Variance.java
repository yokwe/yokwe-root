package yokwe.util.finance.online;

// Welford's online algorithm
//   https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Welford's_online_algorithm
public final class Variance implements OnlineDoubleUnaryOperator {
	private boolean firstTime = true;
	private int     count     = 0;
	
	private double lastM1 = Double.NaN;
	private double lastM2 = Double.NaN;
	
	@Override
	public void accept(double value) {
		count++;

		final double m1, m2;
		{
			if (firstTime) {
				m1 = value;
				m2 = 0;
				//
				firstTime = false;
			} else {
				double delta = value - lastM1;
				m1 = lastM1 + delta / count;
//				m2 = Math.fma(delta, value - m1, lastM2);
				m2 = lastM2 + delta * (value - m1);
			}
		}
		
		// update for next iteration
		lastM1 = m1;
		lastM2 = m2;
	}
	
	
	@Override
	public double getAsDouble() {
		return variance();
	}
	
	
	public double mean() {
		return lastM1;
	}
	public double variance() {
		return (firstTime) ? Double.NaN : (lastM2 / (count - 1));
	}
	public double biasedVariance() {
		return (firstTime) ? Double.NaN : (lastM2 / count);
	}
	public double standardDeviation() {
		return Math.sqrt(variance());
	}

}


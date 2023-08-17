package yokwe.util.finance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import yokwe.util.UnexpectedException;

public class Shrink {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	
	private static class ShrinkPattern {
		final static List<ShrinkPattern> list = new ArrayList<>();
		
		static ShrinkPattern get(double targetPercent) {
			if (list.size() < 5) {
				logger.error("Unexpected value");
				logger.error("  list  {}", list.size());
				throw new UnexpectedException("Unexpected value");
			}

			ShrinkPattern result = list.get(0);
			for(int i = 1; i < list.size(); i++) {
				ShrinkPattern shrinkPattern = list.get(i);
				double diff       = Math.abs(shrinkPattern.percent - targetPercent);
				double resultDiff = Math.abs(result.percent - targetPercent);
				if (diff < resultDiff) result = shrinkPattern;
			}
			
			return result;
		}
		static void add(boolean... pattern) {
			ShrinkPattern value = new ShrinkPattern(pattern);
			list.add(value);
			logger.info("ShrinkPattern {}", value);
		}
		
		final double    percent;
		final boolean[] pattern;
		
		private ShrinkPattern(boolean[] pattern) {
			int count = 0;
			for(var e: pattern) if (e) count++;

			this.percent = (double)count / pattern.length;
			this.pattern  = pattern;
		}
		
		@Override
		public String toString() {
			List<String> list = new ArrayList<>();
			for(var e: pattern) list.add(e ? "I" : "O");
			return String.format("{%.3f, %s}", percent, list);
		}
	}
	static {
		boolean I = true;
		boolean O = false;
		
		ShrinkPattern.add(I);
		ShrinkPattern.add(O, I, I, I, I, I, I, I, I, I);
		ShrinkPattern.add(O, I, I, I, I);
		ShrinkPattern.add(O, I, I, O, I, I, I, O, I, I);
		ShrinkPattern.add(O, I, I, O, I);
		ShrinkPattern.add(O, I);
		ShrinkPattern.add(O, O, I, O, I);
		ShrinkPattern.add(O, O, I, O, O, O, I, O, O, I);
		ShrinkPattern.add(O, O, O, O, I);
		ShrinkPattern.add(O, O, O, O, O, O, O, O, O, I);
	}
	
	static double[] toDoubleArray(double[] array, int startIndex, int stopIndexPlusOne, double ratio) {
		// sanity check
		Util.checkIndex(array, startIndex, stopIndexPlusOne);
		if (ratio < 0 || 1 < ratio) {
			logger.error("Unexpected value");
			logger.error("  ratio  {}", ratio);
			throw new UnexpectedException("Unexpected value");
		}
		ShrinkPattern shrinkPattern = ShrinkPattern.get(ratio);
		int patternArrayLengh = shrinkPattern.pattern.length;
		
		int length      = stopIndexPlusOne - startIndex;
		int resultLengh = (int)(length * shrinkPattern.percent + 0.5);
		if (resultLengh < 0) {
			logger.error("Unexpected value");
			logger.error("  ratio        {}", ratio);
			logger.error("  length       {}", length);
			logger.error("  resultLengh  {}", resultLengh);
			throw new UnexpectedException("Unexpected value");
		}
		
		double[] resultArray  = new double[resultLengh + 1];
		int      resultLength = 0;
		for(int i = 0; i < length; i++) {
			if (shrinkPattern.pattern[i % patternArrayLengh]) {
				resultArray[resultLength++] = array[startIndex + i];
			}
		}
		
		return Arrays.copyOf(resultArray, resultLength);
	}
	static double[] toDoubleArray(double[] array, double ratio) {
		return toDoubleArray(array, 0, array.length, ratio);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		double[] array = new double[10000];
		Arrays.fill(array, 1);
		
		{
			double ratio = 1;
			double[] shrinkArray = toDoubleArray(array, ratio);
			logger.info("{}  {}", ratio, shrinkArray.length);
		}
		{
			double ratio = 0.5;
			double[] shrinkArray = toDoubleArray(array, ratio);
			logger.info("{}  {}", ratio, shrinkArray.length);
		}
		
		logger.info("STOP");
	}
}

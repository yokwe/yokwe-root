package yokwe.util.finance;

import java.util.Arrays;

import yokwe.util.UnexpectedException;

public class Shrink {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean[][] patternArrays;
	static {
		boolean I = true;
		boolean O = false;
		patternArrays = new boolean[10][];
		//                                0  1  2  3  4  5  6  7  8  9
		patternArrays[9] = new boolean[] {I, I, I, I, I, O, I, I, I, I}; //  9 / 10      0 1 2 3 4   6 7 8 9 0 1 2 3 4   6 7 8 9
		patternArrays[8] = new boolean[] {I, I, I, O, I, I, I, I, O, I}; //  8 / 10      0 1 2   4 5 6 7   9 0 1 2   4 5 6 7   9
		patternArrays[7] = new boolean[] {I, I, I, O, I, I, O, I, I, O}; //  7 / 10      0 1 2   4 5   7 8   0 1 2   4 5   7 8  
		patternArrays[6] = new boolean[] {I, O, I, O, I, I, O, I, O, I}; //  6 / 10      0   2   4 5   7   9 0   2   4 5   7   9
		
		patternArrays[5] = new boolean[] {O, I, O, I, O, I, O, I, O, I}; //  5 / 10        1   3   5   7   9   1   3   5   7   9

		patternArrays[4] = new boolean[10]; // !pattern[6]
		patternArrays[3] = new boolean[10]; // !pattern[7]
		patternArrays[2] = new boolean[10]; // !pattern[8]
		patternArrays[1] = new boolean[10]; // !pattern[9]
		patternArrays[0] = new boolean[10];
		for(int i = 1; i <= 4; i++) {
			for(int j = 0; j < 10; j++) {
				patternArrays[i][j] = !patternArrays[10 - i][j];
			}
		}
	}
	
	double[] toDoubleArray(double[] array, int startIndex, int stopIndexPlusOne, double ratio) {
		// sanity check
		Util.checkIndex(array, startIndex, stopIndexPlusOne);
		if (ratio < 0 || 1 < ratio) {
			logger.error("Unexpected value");
			logger.error("  ratio  {}", ratio);
			throw new UnexpectedException("Unexpected value");
		}
		
		int ratioIndex = (int)(ratio * 10 + 0.5);
		if (ratioIndex < 1) {
			logger.error("Unexpected value");
			logger.error("  ratio        {}", ratio);
			logger.error("  ratioIndex   {}", ratioIndex);
			throw new UnexpectedException("Unexpected value");
		}
		if (10 <= ratioIndex) {
			return Arrays.copyOfRange(array, startIndex, stopIndexPlusOne);
		}
		boolean[] patternArray = patternArrays[ratioIndex];
		
		int length      = stopIndexPlusOne - startIndex;
		int resultLengh = (length * 10) / ratioIndex;
		if (resultLengh < 100) {
			logger.error("Unexpected value");
			logger.error("  ratio        {}", ratio);
			logger.error("  length       {}", length);
			logger.error("  resultLengh  {}", resultLengh);
			throw new UnexpectedException("Unexpected value");
		}
		
		double[] resultArray  = new double[resultLengh + 1];
		int      resultLength = 0;
		for(int i = 0; i < length; i++) {
			if (patternArray[i % 10]) {
				resultArray[resultLength++] = array[startIndex + i];
			}
		}
		
		return Arrays.copyOf(resultArray, resultLength);
	}
}

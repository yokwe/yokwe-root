package yokwe.util.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;

import yokwe.util.UnexpectedException;

//Morningstar
//https://web.stanford.edu/~wfsharpe/art/stars/stars2.htm

//GIPS
//https://www.pwc.ch/en/publications/2020/PwC-GIPS-2020.pdf
//https://www.gipsstandards.org/wp-content/uploads/2021/03/calculation_methodology_gs_2011.pdf
//https://www.nbim.no/contentassets/98750345a7e641558d820573c52e4a8e/2020-gips-manual-in-english.pdf
//https://www.cfainstitute.org/-/media/documents/code/gips/gips-standards-for-firms-explanation-of-provisions-section-2.ashx

//Modified Dietz method
//https://en.wikipedia.org/wiki/Modified_Dietz_method

public final class Finance {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	//
	// calculate reinvested price
	//
	public static class ReinvestedPrice {
		private BigDecimal previousPrice;
		private BigDecimal previousReinvestedPrice;
		
		public ReinvestedPrice(BigDecimal initialPrice) {
			previousPrice = previousReinvestedPrice = initialPrice;
		}
		
		public BigDecimal apply(BigDecimal price, BigDecimal div) {
			// 日次リターンを「（当日の基準価格＋分配金）÷ 前営業日基準価格 -1」として計算
			//	<計算式>前営業日の分配金再投資基準価格 × (1+日次リターン)
			BigDecimal reinvestedPrice = previousReinvestedPrice.multiply(price.add(div)).divide(previousPrice, BigDecimalUtil.DEFAULT_MATH_CONTEXT);
			
			// update for next iteration
			previousPrice           = price;
			previousReinvestedPrice = reinvestedPrice;
			
			return reinvestedPrice;
		}
	}
	
	
	//
	// create reinvested array from price and dividend array
	//
	public static DailyValue[] toReinvested(DailyValue[] priceArray, DailyValue[] divArray) {
		DailyValue[] ret = new DailyValue[priceArray.length];
		
		Map<LocalDate, BigDecimal> divMap = DailyValue.toMap(divArray);
		ReinvestedPrice reinvestedPrice = new ReinvestedPrice(priceArray[0].value);

		for(int i = 0; i < priceArray.length; i++) {
			LocalDate  date  = priceArray[i].date;
			BigDecimal price = priceArray[i].value;
			BigDecimal div   = divMap.getOrDefault(date, BigDecimal.ZERO);
			
			ret[i] = new DailyValue(date, reinvestedPrice.apply(price, div));
		}
		
		return ret;
	}
	public static BigDecimal[] toReinvestedValue(DailyValue[] priceArray, DailyValue[] divArray) {
		BigDecimal[] ret = new BigDecimal[priceArray.length];
		
		Map<LocalDate, BigDecimal> divMap = DailyValue.toMap(divArray);
		ReinvestedPrice reinvestedPrice = new ReinvestedPrice(priceArray[0].value);

		for(int i = 0; i < priceArray.length; i++) {
			LocalDate  date  = priceArray[i].date;
			BigDecimal price = priceArray[i].value;
			BigDecimal div   = divMap.getOrDefault(date, BigDecimal.ZERO);
			
			ret[i] = reinvestedPrice.apply(price, div);
		}
		
		return ret;
	}
	
	
	//
	// annualized return from monthly return
	//
	private static final BigDecimal N_MONTH_PER_YEAR = BigDecimal.valueOf(12);
	
	public static <T> BigDecimal annualizeMonthlyReturn(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		int nMonth = stopIndexPlusOne - startIndex;
		// sanity check
		if ((nMonth % 12) != 0) {
			logger.error("number of element must be multiple of 12");
			logger.error("  array.length      {}", array.length);
			throw new UnexpectedException("number of element must be multiple of 12");
		}

		BigDecimal nYear = BigDecimal.valueOf(nMonth).divide(N_MONTH_PER_YEAR, BigDecimalUtil.DEFAULT_MATH_CONTEXT);
				
		BigDecimal base = BigDecimal.ONE;
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			base = base.multiply(function.apply(array[i]).add(BigDecimal.ONE), BigDecimalUtil.DEFAULT_MATH_CONTEXT);
		}
		BigDecimal exponent = BigDecimal.ONE.divide(nYear, BigDecimalUtil.DEFAULT_MATH_CONTEXT);
		return BigDecimalUtil.mathPow(base, exponent).subtract(BigDecimal.ONE);
	}
	
}

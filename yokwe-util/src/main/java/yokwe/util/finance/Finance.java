package yokwe.util.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;

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
	//
	// calculate reinvested price
	//
	public static class ReinvestedPrice {
		// https://www.nikkei.com/help/contents/markets/fund/
		// 分配金再投資基準価格
		// 分配金を受け取らず、その分を元本に加えて運用を続けたと想定して算出する基準価格です。
		// 複数の投資信託の運用実績を比較するときなどは、分配金再投資ベースの基準価格の騰落率を使うのが一般的です。
		//
		// 【計算内容】
		// <計算式>前営業日の分配金再投資基準価格 × (1+日次リターン)
		// <例>前営業日の分配金再投資基準価格が15000、日次リターンが1%の場合、15000×(1+0.01)=15150。
		//
		// 設定日の場合、前営業日の分配金再投資基準価格と基準価格を通常10000(※1)、日次リターンを「設定日の基準価格÷前営業日基準価格-1」として計算する。
		// 決算日に分配金が出た場合は、日次リターンを「（当日の基準価格＋分配金）÷前営業日基準価格 -1」として計算する。分配金は税引き前。
		// （※1）当初元本の設定がある場合は当初元本価格を使用。

		private BigDecimal previousPrice;
		private BigDecimal previousReinvestedPrice;
		
		public ReinvestedPrice(BigDecimal initialPrice) {
			previousPrice = previousReinvestedPrice = initialPrice;
		}
		
		public BigDecimal apply(BigDecimal price, BigDecimal div) {
			BigDecimal dailyReturn     = price.add(div).divide(previousPrice, BigDecimalUtil.DEFAULT_MATH_CONTEXT);
			BigDecimal reinvestedPrice = previousReinvestedPrice.multiply(dailyReturn).round(BigDecimalUtil.DEFAULT_MATH_CONTEXT);
			
			// update for next iteration
			previousPrice           = price;
			previousReinvestedPrice = reinvestedPrice;
			
			return reinvestedPrice;
		}
	}
	
	
	//
	// create reinvested array from price and dividend array
	//
	public static DailyValue[] toReinvestedPrice(DailyValue[] priceArray, DailyValue[] divArray) {
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
	public static <T> BigDecimal cumulativeReturn(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		BigDecimal value = BigDecimal.ONE;
		
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			// value = value * (1 + function.apply(array[i]))
			value = value.multiply(BigDecimal.ONE.add(function.apply(array[i])), BigDecimalUtil.DEFAULT_MATH_CONTEXT);
		}
		
		return value.subtract(BigDecimal.ONE);
	}
	
	
	//
	// annualized return
	//
	public static BigDecimal annualizeReturn(BigDecimal absoluteReturn, int nYear) {
		// ((1 + absoluteReturnWithReinvest) ^ (1 / nYear)) - 1
		BigDecimal base     = BigDecimal.ONE.add(absoluteReturn);
		BigDecimal exponent = BigDecimal.ONE.divide(BigDecimal.valueOf(nYear), BigDecimalUtil.DEFAULT_MATH_CONTEXT);
		
		return BigDecimalUtil.mathPow(base, exponent).subtract(BigDecimal.ONE);
	}
}

package yokwe.util.finance;

import java.math.BigDecimal;
import java.util.function.BinaryOperator;

import yokwe.util.BigDecimalUtil;

//
// calculate reinvested price
//
public final class ReinvestedPrice implements BinaryOperator<BigDecimal>{
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
	
	@Override
	public BigDecimal apply(BigDecimal price, BigDecimal div) {
		BigDecimal dailyReturn     = BigDecimalUtil.divide(price.add(div), previousPrice);
		BigDecimal reinvestedPrice = BigDecimalUtil.multiply(previousReinvestedPrice, dailyReturn);
		
		// update for next iteration
		previousPrice           = price;
		previousReinvestedPrice = reinvestedPrice;
		
		return reinvestedPrice;
	}
}
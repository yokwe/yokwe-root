package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class PriceDiv implements Comparable<PriceDiv> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static class DailyValue {
		public static DailyValue getInstance(LocalDate date, BigDecimal value) {
			return new DailyValue(date, value);
		}
		
		public LocalDate  date;
		public BigDecimal value;

		public DailyValue(LocalDate date, BigDecimal value) {
			this.date  = date;
			this.value = value;
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static Map<LocalDate, PriceDiv> getMap(List<DailyValue> priceList, List<DailyValue> divList) {
		Map<LocalDate, PriceDiv> map = new TreeMap<>();
		// date
		for(var e: priceList) {
			if (map.containsKey(e.date)) {
				// duplicate
				logger.error("Duplicate date");
				logger.error("  old  {}", map.get(e.date));
				logger.error("  new  {}", e);
				throw new UnexpectedException("Duplicate date");
			} else {
				map.put(e.date, new PriceDiv(e.date, e.value));
			}
		}
		for(var e: divList) {
			if (map.containsKey(e.date)) {
				var priceMap = map.get(e.date);
				priceMap.div = e.value;
			} else {
				// duplicate
				logger.error("Unexpected div date");
				logger.error("  new  {}", e);
				throw new UnexpectedException("Duplicate div date");
			}
		}
		
		calculateReinvestedPrice(map.values());
		return map;
	}
	
	// https://www.nikkei.com/help/contents/markets/fund/#qf5
	// 分配金再投資基準価格
	//	分配金を受け取らず、その分を元本に加えて運用を続けたと想定して算出する基準価格です。
	//	複数の投資信託の運用実績を比較するときなどは、分配金再投資ベースの基準価格の騰落率を使うのが一般的です。
	//	【計算内容】
	//	<計算式>前営業日の分配金再投資基準価格 × (1+日次リターン)
	//	<例>前営業日の分配金再投資基準価格が15000、日次リターンが1%の場合、15000×(1+0.01)=15150。
	//	設定日の場合、前営業日の分配金再投資基準価格と基準価格を通常10000(※1)、日次リターンを「設定日の基準価格÷前営業日基準価格-1」として計算する。
	//	決算日に分配金が出た場合は、日次リターンを「（当日の基準価格＋分配金）÷前営業日基準価格 -1」として計算する。分配金は税引き前。
	//	（※1）当初元本の設定がある場合は当初元本価格を使用。
	
	public static final int          DEFAULT_SCALE         = 15;
	public static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

	public static void calculateReinvestedPrice(Collection<PriceDiv> collection) {
		List<PriceDiv> list = new ArrayList<>();
		list.addAll(collection);
		calculateReinvestedPrice(list);
	}
	public static void calculateReinvestedPrice(List<PriceDiv> list) {
		Collections.sort(list);
		PriceDiv first = list.get(0);		
		BigDecimal previousPrice          = first.price;
		BigDecimal previousReinvestedPrce = first.price;
		
		for(var e: list) {
			// 日次リターンを「（当日の基準価格＋分配金）÷前営業日基準価格 -1」として計算
			BigDecimal dailyReturnPlusOne = e.price.add(e.div).divide(previousPrice, DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
			//	<計算式>前営業日の分配金再投資基準価格 × (1+日次リターン)
			BigDecimal reinvestedPrice = previousReinvestedPrce.multiply(dailyReturnPlusOne).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
			e.reinvestedPrice = reinvestedPrice.setScale(2, DEFAULT_ROUNDING_MODE);
			
			// update for next iteration
			previousPrice          = e.price;
			previousReinvestedPrce = reinvestedPrice;
		}
		
	}
	
	
	public LocalDate  date;
	public BigDecimal price;
	public BigDecimal div;
	public BigDecimal reinvestedPrice;
	
	public PriceDiv(LocalDate date, BigDecimal price, BigDecimal div, BigDecimal reinvestedPrice) {
		this.date            = date;
		this.price           = price;
		this.div             = div;
		this.reinvestedPrice = reinvestedPrice;
	}
	public PriceDiv(LocalDate date, BigDecimal price, BigDecimal div) {
		this(date, price, div, BigDecimal.ZERO);
	}
	public PriceDiv(LocalDate date, BigDecimal price) {
		this(date, price, BigDecimal.ZERO);
	}

	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	@Override
	public int compareTo(PriceDiv that) {
		return this.date.compareTo(that.date);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof PriceDiv) {
				PriceDiv that = (PriceDiv)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}
	@Override
	public int hashCode() {
		return this.date.hashCode();
	}

}

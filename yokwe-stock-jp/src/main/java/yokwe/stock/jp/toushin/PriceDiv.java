package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
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
		
		ReinvestedPrice reinvestedPrice = new ReinvestedPrice();
		for(var e: map.values()) {
			e.reinvestedPrice = reinvestedPrice.apply(e.price, e.div);
		}
		
		return map;
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

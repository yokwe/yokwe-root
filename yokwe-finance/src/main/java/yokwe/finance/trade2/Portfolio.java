package yokwe.finance.trade2;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.trade2.Transaction.Asset;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.DailyValueMap;
import yokwe.util.UnexpectedException;

public class Portfolio {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static MathContext ROUND_UP_4 = new MathContext(4, RoundingMode.UP);
	
	
	private Map<String, Entry> entryMap = new TreeMap<>();
	//          code
	private Entry getEntry(Transaction transaction) {
		var code = transaction.code;
		Entry entry;
		if (entryMap.containsKey(code)) {
			entry = entryMap.get(code);
		} else {
			entry = Entry.getInstance(transaction);
			entryMap.put(code, entry);
		}
		return entry;
	}
	
	public void buy(Transaction transaction) {
		var entry = getEntry(transaction);
		entry.buy(transaction);
	}
	public int sell(Transaction transaction) {
		var entry = getEntry(transaction);
		return entry.sell(transaction);
	}
	public int valueAsOf(LocalDate date) {
		int ret = 0;
		for(var e: entryMap.values()) {
			var value = e.valueAsOf(date);
			if (value < 0) return -1;
			ret += value;
		}
		return ret;
	}
	
	
	public interface Entry extends Comparable<Entry> {
		public Asset  asset();
		public String code();
		
		public void buy(Transaction transaction);
		// sell returns sell cost
		public int sell(Transaction transaction);
		public int valueAsOf(LocalDate date);
		
		@Override
		default int compareTo(Entry that) {
			int ret = this.asset().compareTo(that.asset());
			if (ret == 0) this.code().compareTo(that.code());
			return ret;
		}
		
		
		static Map<Asset, Function<Transaction, Entry>> map = Map.ofEntries(
			Map.entry(Asset.STOCK_JP, o -> new Entry.STOCK_JP(o)),
			Map.entry(Asset.FUND_JP,  o -> new Entry.FUND_JP(o)),
			Map.entry(Asset.STOCK_US, o -> new Entry.STOCK_US(o)),
			Map.entry(Asset.BOND_US,  o -> new Entry.BOND_US(o)),
			Map.entry(Asset.MMF_US,   o -> new Entry.MMF_US(o))
		);

		static Entry getInstance(Transaction transaction) {
			return map.get(transaction.asset).apply(transaction);
		}
		
		
		abstract class Base implements Entry {
			final Asset  asset;
			final String code;
			
			int totalUnits = 0;
			int totalCost  = 0;
			
			Base(Transaction transaction) {
				this.asset = transaction.asset;
				this.code  = transaction.code;
			}
			
			@Override
			public Asset asset() {
				return asset;
			}
			@Override
			public String code() {
				return code;
			}
			
			@Override
			public void buy(Transaction transaction) {
				var units = transaction.units;
				var cost  = transaction.amount;
				totalUnits += units;
				totalCost  += cost;
			}
			@Override
			public int sell(Transaction transaction) {
				// sanity check
				if (totalUnits < 0 || totalCost < 0) {
					logger.error("Unexpected totalUnits");
					logger.error("  transaction  {}", transaction.toString());
					logger.error("  etnry        {}", this.toString());
					throw new UnexpectedException("Unexpected totalUnits");
				}
				
				var units = transaction.units;
				
				int sellCost;
				if (units < totalUnits) {
					var unitPrice = BigDecimal.valueOf(totalCost).divide(BigDecimal.valueOf(totalUnits), ROUND_UP_4);
					sellCost = unitPrice.multiply(BigDecimal.valueOf(units)).intValue();
					
					totalUnits -= units;
					totalCost  -= sellCost;
				} else {
					if (totalUnits < units) {
						logger.warn("totalUnits  {}  units  {}", totalUnits, units);
					}
					sellCost = totalCost;
					
					totalUnits = 0;
					totalCost  = 0;
				}
				return sellCost;
			}
		}
		abstract class BaseMap extends Base {
			protected Map<String, DailyValueMap> map = new TreeMap<>();
			
			BaseMap(Transaction transaction) {
				super(transaction);
			}
			
			abstract protected List<DailyValue> getList(String code);

			@Override
			public int valueAsOf(LocalDate date) {
				if (totalUnits == 0) return 0;
				
				DailyValueMap dailyValueMap;
				if (map.containsKey(code)) {
					dailyValueMap = map.get(code);
				} else {
					var list = getList(code);
					if (list.isEmpty()) {
						logger.error("list is empty");
						logger.error("  {}", code);
						throw new UnexpectedException("list is empty");
					}
					dailyValueMap = new DailyValueMap(list);
					map.put(code, dailyValueMap);
				}
				var price = dailyValueMap.get(date);
				if (price == null) return -1;
				return price.multiply(BigDecimal.valueOf(totalUnits), ROUND_UP_4).intValue();
			}
		}

		public class STOCK_JP extends BaseMap {
			private STOCK_JP(Transaction transaction) {
				super(transaction);
			}
			@Override
			protected List<DailyValue> getList(String code) {
				return StorageStock.StockPriceJP.getList(code).stream().map(o -> new DailyValue(o.date, o.close)).toList();
			}
		}
		public class FUND_JP extends BaseMap {
			private FUND_JP(Transaction transaction) {
				super(transaction);
			}
			@Override
			protected List<DailyValue> getList(String code) {
				return StorageFund.FundPrice.getList(code).stream().map(o -> new DailyValue(o.date, o.price)).toList();
			}
		}
		public class STOCK_US extends BaseMap {
			private STOCK_US(Transaction transaction) {
				super(transaction);
			}
			@Override
			protected List<DailyValue> getList(String code) {
				return StorageStock.StockPriceUS.getList(code).stream().map(o -> new DailyValue(o.date, o.close)).toList();
			}
		}
		public class BOND_US extends Base {
			private BOND_US(Transaction transaction) {
				super(transaction);
			}
			@Override
			public int valueAsOf(LocalDate date) {
				return totalUnits;
			}
		}
		public class MMF_US extends Base {
			private MMF_US(Transaction transaction) {
				super(transaction);
			}
			@Override
			public int valueAsOf(LocalDate date) {
				return totalUnits;
			}
		}
	}
}

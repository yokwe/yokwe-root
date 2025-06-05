package yokwe.finance.trade2;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import yokwe.finance.trade2.Transaction.Asset;
import yokwe.util.UnexpectedException;

public class Portfolio {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static MathContext ROUND_UP_4 = new MathContext(4, RoundingMode.UP);
	
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
			Map.entry(Asset.STOCK_JP, new Functions.STOCK_JP()),
			Map.entry(Asset.FUND_JP,  new Functions.FUND_JP()),
			Map.entry(Asset.STOCK_US, new Functions.STOCK_US()),
			Map.entry(Asset.BOND_US,  new Functions.BOND_US()),
			Map.entry(Asset.MMF_US,   new Functions.MMF_US())
		);
		static class Functions {
			static class STOCK_JP implements Function<Transaction, Entry> {
				@Override
				public Entry apply(Transaction transaction) {
					return new Entry.STOCK_JP(transaction);
				}
			}
			static class FUND_JP implements Function<Transaction, Entry> {
				@Override
				public Entry apply(Transaction transaction) {
					return new Entry.FUND_JP(transaction);
				}
			}
			static class STOCK_US implements Function<Transaction, Entry> {
				@Override
				public Entry apply(Transaction transaction) {
					return new Entry.STOCK_US(transaction);
				}
			}
			static class BOND_US implements Function<Transaction, Entry> {
				@Override
				public Entry apply(Transaction transaction) {
					return new Entry.BOND_US(transaction);
				}
			}
			static class MMF_US implements Function<Transaction, Entry> {
				@Override
				public Entry apply(Transaction transaction) {
					return new Entry.MMF_US(transaction);
				}
			}
		}

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
		public class STOCK_JP extends Base {
			private STOCK_JP(Transaction transaction) {
				super(transaction);
			}
			@Override
			public int valueAsOf(LocalDate date) {
				return 0; // FIXME
			}
		}
		public class FUND_JP extends Base {
			private FUND_JP(Transaction transaction) {
				super(transaction);
			}
			@Override
			public int valueAsOf(LocalDate date) {
				return 0; // FIXME
			}
		}
		public class STOCK_US extends Base {
			private STOCK_US(Transaction transaction) {
				super(transaction);
			}
			@Override
			public int valueAsOf(LocalDate date) {
				return 0; // FIXME
			}
		}
		public class BOND_US extends Base {
			private BOND_US(Transaction transaction) {
				super(transaction);
			}
			@Override
			public int valueAsOf(LocalDate date) {
				return 1; // FIXME
			}
		}
		public class MMF_US extends Base {
			private MMF_US(Transaction transaction) {
				super(transaction);
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
				var units = transaction.units;
				return units;
			}
			@Override
			public int valueAsOf(LocalDate date) {
				return 1; // FIXME
			}
		}
	}	
	
	
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
}

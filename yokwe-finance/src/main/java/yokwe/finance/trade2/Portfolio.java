package yokwe.finance.trade2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.trade2.Transaction.Asset;
import yokwe.finance.trade2.rakuten.StorageRakuten;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.LocalDateMap.DailyValueMap;
import yokwe.util.UnexpectedException;

public class Portfolio {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static class Trade {
		public enum Type {
			BUY,
			SELL,
			;
		}
		
		public static Trade buy(Transaction transaction) {
			if (transaction.type != Transaction.Type.BUY) {
				logger.error("Unexpected type");
				logger.error("  transaction  {}", transaction.toString());
				throw new UnexpectedException("Unexpected type");
			}
			var ret = new Trade();
			
			ret.settlemenDate = transaction.settlementDate;
			ret.tradeDate     = transaction.tradeDate;
			ret.type          = Type.BUY;
			ret.asset         = transaction.asset;
			ret.code          = transaction.code;
			ret.units         = transaction.units;
			ret.cost          = transaction.amount;
			
			
			return ret;
		}
		
		LocalDate settlemenDate;
		LocalDate tradeDate;
		Type      type;
		Asset     asset;
		String    code;
		int       units;
		int       cost;
		int       totalCost;
		int       totalUnits;
	}
	
	
	
	private Map<String, Entry> entryMap = new TreeMap<>();
	//          code
	
	
	private Entry getEntry(Transaction transaction) {
		String key;
		switch (transaction.asset) {
		case STOCK_JP:
		case FUND_JP:
		case STOCK_US:
			key = transaction.code;
			break;
		case BOND_US:
		case MMF_US:
			key = transaction.comment;
			break;
		default:
			logger.error("Unexpected asset");
			logger.error("  {}", transaction);
			throw new UnexpectedException("Unexpected asset");
		}
		
		Entry entry;
		if (entryMap.containsKey(key)) {
			entry = entryMap.get(key);
		} else {
			entry = Entry.getInstance(transaction);
			entryMap.put(key, entry);
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
		public int    totalUnits();
		public int    totalCost();
		
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
			final String name;
			
			int totalUnits = 0;
			int totalCost  = 0;
			
			Base(Transaction transaction) {
				this.asset = transaction.asset;
				this.code  = transaction.code;
				this.name  = transaction.comment;
			}
			
			@Override
			public String toString() {
				return String.format("{%s  %s  %d  %d  %s}",  asset, code, totalUnits, totalCost, name);
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
			public int totalUnits() {
				return totalUnits;
			}
			@Override
			public int totalCost() {
				return totalCost;
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
					logger.error("  this         {}  {}  {}  {}", asset, code, totalUnits, totalCost);
					throw new UnexpectedException("Unexpected totalUnits");
				}
				
				var units = transaction.units;
				
				int sellCost;
				if (units < totalUnits) {
					var unitPrice = BigDecimal.valueOf(totalCost).divide(BigDecimal.valueOf(totalUnits), 0, RoundingMode.UP);
					sellCost = unitPrice.multiply(BigDecimal.valueOf(units)).setScale(0, RoundingMode.HALF_UP).intValue();
					
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
			public String toString() {
				return String.format("{%s  %s  %d  %d  %s}",  asset, code, totalUnits, totalCost, name);
			}
			
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
				return price.multiply(BigDecimal.valueOf(totalUnits)).setScale(0, RoundingMode.HALF_UP).intValue();
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
			private Map<String, FundPriceInfoJPType> fundPriceInfoMap = StorageRakuten.FundPriceInfoJP.getList().stream().collect(Collectors.toMap(o -> o.code, Function.identity()));
			//          code
			private FUND_JP(Transaction transaction) {
				super(transaction);
			}
			@Override
			protected List<DailyValue> getList(String code) {
				var shift = fundPriceInfoMap.get(code).shift;
				// apply movePointLeft(shift) to adjust price value for one unit
				return StorageFund.FundPrice.getList(code).stream().map(o -> new DailyValue(o.date, o.price.movePointLeft(shift))).toList();
			}
		}
		public class STOCK_US extends BaseMap {
			private STOCK_US(Transaction transaction) {
				super(transaction);
			}
			@Override
			protected List<DailyValue> getList(String code) {
				// change o.close from dollar value to cent value
				return StorageStock.StockPriceUS.getList(code).stream().map(o -> new DailyValue(o.date, o.close.movePointRight(2))).toList();
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

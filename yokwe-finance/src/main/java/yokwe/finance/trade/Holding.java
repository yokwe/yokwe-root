package yokwe.finance.trade;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.DailyValueMap;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;

public class Holding {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static MathContext UP_0 = new MathContext(0, RoundingMode.UP);
	private static MathContext UP_4 = new MathContext(4, RoundingMode.UP);
	
	private enum AssetType {
		// JPY
		STOCK_JP,
		FUND_JP,
		// USD
		STOCK_US,
		BOND_US,
		MMF_US,
		;
	}
	private static Set<String> stockCodeJPSet = StorageStock.StockInfoJP.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
	private static Set<String> stockCodeUSSet = StorageStock.StockInfoUSTrading.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
	private static Set<String> fundCodeJPSet = StorageFund.FundInfo.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
	public static AssetType toAssetType(String code, String name) {
		if (stockCodeJPSet.contains(code))     return AssetType.STOCK_JP;
		if (fundCodeJPSet.contains(code))      return AssetType.FUND_JP;
		if (stockCodeUSSet.contains(code))     return AssetType.STOCK_US;
		if (name.contains("米ドル建債券"))     return AssetType.BOND_US;
		if (name.contains("GS米ドルファンド")) return AssetType.MMF_US;
		logger.error("Unexpected symbol");
		logger.error("  symbol  {}", code);
		throw new UnexpectedException("Unexpected symbol");
	}
	
	public static class Transaction implements Comparable<Transaction> {
		public enum Type {
			BUY,
			SELL,
			;
		}
		
		public static Transaction buy(LocalDate date, int units, int buyCost) {
			return new Transaction(date, Type.BUY, units, buyCost, 0);
		}
		public static Transaction sell(LocalDate date, int units, int sellCost, int sellValue) {
			return new Transaction(date, Type.SELL, units, sellCost, sellValue);
		}
		
		public final LocalDate date;
		public final Type      type;
		public final int       units;
		public final int       cost;  // buy cost or sell cost
		public final int       value; // sell value
		
		private Transaction(LocalDate date, Type type, int units, int cost, int value) {
			this.date  = date;
			this.type  = type;
			this.units = units;
			this.cost  = cost;
			this.value = value;
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}

		@Override
		public int compareTo(Transaction that) {
			int ret = this.date.compareTo(that.date);
			if (ret == 0) ret = this.type.compareTo(that.type);
			if (ret == 0) ret = Integer.compare(this.units, that.units);
			if (ret == 0) ret = Integer.compare(this.cost,  that.cost);
			if (ret == 0) ret = Integer.compare(this.value, that.value);
			return ret;
		}
	}
	
	public final AssetType  assetType;
	public final String     code;
	public final String     name;
	public final BigDecimal priceFactor;
	
	private int totalUnits;
	private int totalCost;
	
	private List<Transaction> history;
	
	
	public Holding(String code, String name, BigDecimal priceFactor) {
		this.assetType   = toAssetType(code, name);
		this.code        = code;
		this.name        = name;
		this.priceFactor = priceFactor;
		this.totalUnits  = 0;
		this.totalCost   = 0;
		this.history     = new ArrayList<>();
	}
	public Holding(String code, String name) {
		this(code, name, BigDecimal.ONE);
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	public int totalUnits() {
		return totalUnits;
	}
	public int totalCost() {
		return totalCost;
	}
	
	public void buy(LocalDate date, int units, int buyCost) {
		totalUnits += units;
		totalCost  += buyCost;
		
		history.add(Transaction.buy(date, units, buyCost));
	}
	
	// sell returns cost of selling stock
	public int sell(LocalDate date, int units, int sellValue) {
		// sanity check
		if (totalUnits <= 0 || totalUnits < units|| totalCost <= 0) {
			logger.error("Unexpected totalUnits");
			logger.error("  units    {}", units);
			logger.error("  holding  {}", this.toString());
			throw new UnexpectedException("Unexpected totalUnits");
		}
		
		int sellCost;
		if (totalUnits == units) {
			sellCost = totalCost;
			
			totalUnits = 0;
			totalCost  = 0;
		} else {
			var unitPrice = BigDecimal.valueOf(totalCost).divide(BigDecimal.valueOf(totalUnits), UP_4);
			sellCost = unitPrice.multiply(BigDecimal.valueOf(units)).intValue();
			
			totalUnits -= units;
			totalCost  -= sellCost;
		}
		history.add(Transaction.sell(date, units, sellCost, sellValue));
		return sellCost;
	}
	
	private static Map<String, DailyValueMap> priceMap = new TreeMap<>();
	//                 symbol
	public int valueAsOf(LocalDate date) {
		if (totalUnits == 0) return 0;
		
		if (assetType == AssetType.BOND_US) return totalCost;
		if (assetType == AssetType.MMF_US)  return totalCost;
		
		var map = getPriceMap();
		var unitPrice = map.get(date);
		if (unitPrice == null) {
//			logger.warn("no data in priceMap  {}  {}", symbol, date);
			logger.warn("no data in priceMap  {}  {}  {}  --  {}", code, date, map.firstKey(), map.lastKey());
			return -1;
		}
		var value = unitPrice.multiply(BigDecimal.valueOf(totalUnits), UP_0).divide(priceFactor, UP_0).intValue();
		return value;
	}
	
	private DailyValueMap getPriceMap() {
		var map = priceMap.get(code);
		if (map == null) {
			List<DailyValue> list;
			switch(assetType) {
			case STOCK_JP:
				list = StorageStock.StockPriceJP.getList(code).stream().map(o -> new DailyValue(o.date, o.close)).toList();
				break;
			case FUND_JP:
				list = StorageFund.FundPrice.getList(code).stream().map(o -> new DailyValue(o.date, o.price)).toList();
				break;
			case STOCK_US:
				list = StorageStock.StockPriceUS.getList(code).stream().map(o -> new DailyValue(o.date, o.close.movePointRight(2))).toList(); // change unit dollar to unit cent
				break;
			default:
				logger.error("Unexpected asset");
				logger.error("  asset   {}", assetType);
				logger.error("  symbol  {}!", code);
				throw new UnexpectedException("Unexpected asset");
			}
			// sanity check
			if (list.isEmpty()) {
				logger.error("list is empty");
				logger.error("  asset   {}",  assetType);
				logger.error("  symbol  {}!", code);
				throw new UnexpectedException("list is empty");
			}
			
			map = new DailyValueMap(list);
			priceMap.put(code, map);
		}
		
		return map;
	}

}
package yokwe.finance.trade;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
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
	
	private enum Asset {
		BOND,
		STOCK_JP,
		STOCK_US,
		FUND_JP,
		;
	}
	
	private static Set<String> stockCodeJPSet = StorageStock.StockInfoJP.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
	private static Set<String> stockCodeUSSet = StorageStock.StockInfoUSTrading.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
	private static Set<String> fundCodeJPSet = StorageFund.FundInfo.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
	public static Asset toAsset(String symbol) {
		if (stockCodeJPSet.contains(symbol)) return Asset.STOCK_JP;
		if (stockCodeUSSet.contains(symbol)) return Asset.STOCK_US;
		if (fundCodeJPSet.contains(symbol))  return Asset.FUND_JP;
		logger.error("Unexpected symbol");
		logger.error("  symbol  {}", symbol);
		throw new UnexpectedException("Unexpected symbol");
	}
	
	private final Asset      asset;
	private final String     symbol;
	private final BigDecimal priceFactor;
	
	private int totalUnits;
	private int totalCost;
	
	public Holding(String symbol, BigDecimal priceFactor) {
		this.asset       = toAsset(symbol);
		this.symbol      = symbol;
		this.priceFactor = priceFactor;
		this.totalUnits  = 0;
		this.totalCost   = 0;
	}
	public Holding(String symbol) {
		this(symbol, BigDecimal.ONE);
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	public Asset asset() {
		return asset;
	}
	public String symbol() {
		return symbol;
	}
	public int totalUnits() {
		return totalUnits;
	}
	public int totalCost() {
		return totalCost;
	}
	
	public void buy(int units, int cost) {
		// adjust totalCost
		totalCost += cost;
		
		// adjust totalUnits
		totalUnits += units;
	}
	
	// sell returns cost of selling stock
	public int sell(int units) {
		// sanity check
		if (totalUnits <= 0 || totalUnits < units|| totalCost <= 0) {
			logger.error("Unexpected totalUnits");
			logger.error("  units    {}", units);
			logger.error("  holding  {}", this.toString());
			throw new UnexpectedException("Unexpected totalUnits");
		}
		
		// adjust totalCost
		var unitPrice = BigDecimal.valueOf(totalCost).divide(BigDecimal.valueOf(totalUnits), UP_4);
		int cost = getValue(unitPrice, BigDecimal.valueOf(units));
		totalCost -= cost;
		
		// adjust totalUnits
		totalUnits -= units;
		return cost;
	}
	
	private int getValue(BigDecimal unitPrice, BigDecimal units) {
		return unitPrice.multiply(units, UP_0).divide(priceFactor, UP_0).intValue();
	}
	
	private static Map<String, DailyValueMap> priceMap = new TreeMap<>();
	//                 symbol
	public int valueAsOf(LocalDate date) {
		if (totalUnits == 0) return 0;
		
		if (asset == Asset.BOND) return totalCost;
		
		var map = getPriceMap();
		var unitPrice = map.get(date);
		if (unitPrice == null) {
//			logger.warn("no data in priceMap  {}  {}", symbol, date);
			logger.warn("no data in priceMap  {}  {}  {}  --  {}", symbol, date, map.firstKey(), map.lastKey());
			return -1;
		}
		var value = getValue(unitPrice, BigDecimal.valueOf(totalUnits));
		return value;
	}
	
	private DailyValueMap getPriceMap() {
		var map = priceMap.get(symbol);
		if (map == null) {
			List<DailyValue> list;
			switch(asset) {
			case STOCK_JP:
				list = StorageStock.StockPriceJP.getList(symbol).stream().map(o -> new DailyValue(o.date, o.close)).toList();
				break;
			case STOCK_US:
				list = StorageStock.StockPriceUS.getList(symbol).stream().map(o -> new DailyValue(o.date, o.close)).toList();
				break;
			case FUND_JP:
				list = StorageFund.FundPrice.getList(symbol).stream().map(o -> new DailyValue(o.date, o.price)).toList();
				break;
			default:
				logger.error("Unexpected asset");
				logger.error("  asset   {}", asset);
				logger.error("  symbol  {}!", symbol);
				throw new UnexpectedException("Unexpected asset");
			}
			// sanity check
			if (list.isEmpty()) {
				logger.error("list is empty");
				logger.error("  asset   {}",  asset);
				logger.error("  symbol  {}!", symbol);
				throw new UnexpectedException("list is empty");
			}
			
			map = new DailyValueMap(list);
			priceMap.put(symbol, map);
			logger.info("XX  {}  {}  {}  {}", symbol, asset, map.firstKey(), map.lastKey());
		}
		
		return map;
	}

}
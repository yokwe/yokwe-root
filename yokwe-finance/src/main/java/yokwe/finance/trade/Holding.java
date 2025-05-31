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
	public static Asset toAsset(String code, String name) {
		if (stockCodeJPSet.contains(code))     return Asset.STOCK_JP;
		if (fundCodeJPSet.contains(code))      return Asset.FUND_JP;
		if (stockCodeUSSet.contains(code))     return Asset.STOCK_US;
		if (name.contains("米ドル建債券"))     return Asset.BOND_US;
		if (name.contains("GS米ドルファンド")) return Asset.MMF_US;
		logger.error("Unexpected symbol");
		logger.error("  symbol  {}", code);
		throw new UnexpectedException("Unexpected symbol");
	}
	
	private final Asset      asset;
	private final String     code;
	private final String     name;
	private final BigDecimal priceFactor;
	
	private int totalUnits;
	private int totalCost;
	
	public Holding(String code, String name, BigDecimal priceFactor) {
		this.asset       = toAsset(code, name);
		this.code        = code;
		this.name        = name;
		this.priceFactor = priceFactor;
		this.totalUnits  = 0;
		this.totalCost   = 0;
	}
	public Holding(String code, String name) {
		this(code, name, BigDecimal.ONE);
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	public Asset asset() {
		return asset;
	}
	public String code() {
		return code;
	}
	public String name() {
		return name;
	}
	public int totalUnits() {
		return totalUnits;
	}
	public int totalCost() {
		return totalCost;
	}
	
	public void buy(int units, int cost) {
		totalUnits += units;
		totalCost  += cost;
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
		
		if (totalUnits == units) {
			var cost = totalCost;
			
			totalUnits = 0;
			totalCost = 0;
			return cost;
		} else {
			var unitPrice = BigDecimal.valueOf(totalCost).divide(BigDecimal.valueOf(totalUnits), UP_4);
			var cost = unitPrice.multiply(BigDecimal.valueOf(units)).intValue();
			
			totalUnits -= units;
			totalCost  -= cost;
			return cost;
		}
	}
	
	private static Map<String, DailyValueMap> priceMap = new TreeMap<>();
	//                 symbol
	public int valueAsOf(LocalDate date) {
		if (totalUnits == 0) return 0;
		
		if (asset == Asset.BOND_US) return totalCost;
		if (asset == Asset.MMF_US)  return totalCost;
		
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
			switch(asset) {
			case STOCK_JP:
				list = StorageStock.StockPriceJP.getList(code).stream().map(o -> new DailyValue(o.date, o.close)).toList();
				break;
			case STOCK_US:
				list = StorageStock.StockPriceUS.getList(code).stream().map(o -> new DailyValue(o.date, o.close)).toList();
				break;
			case FUND_JP:
				list = StorageFund.FundPrice.getList(code).stream().map(o -> new DailyValue(o.date, o.price)).toList();
				break;
			default:
				logger.error("Unexpected asset");
				logger.error("  asset   {}", asset);
				logger.error("  symbol  {}!", code);
				throw new UnexpectedException("Unexpected asset");
			}
			// sanity check
			if (list.isEmpty()) {
				logger.error("list is empty");
				logger.error("  asset   {}",  asset);
				logger.error("  symbol  {}!", code);
				throw new UnexpectedException("list is empty");
			}
			
			map = new DailyValueMap(list);
			priceMap.put(code, map);
		}
		
		return map;
	}

}
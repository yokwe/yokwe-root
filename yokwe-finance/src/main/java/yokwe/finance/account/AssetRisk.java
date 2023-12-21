package yokwe.finance.account;

import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.account.prestia.FundPrestia;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.FundInfoJP;
import yokwe.finance.type.StockInfoJPType;
import yokwe.finance.type.StockInfoUSType;
import yokwe.util.UnexpectedException;

public interface AssetRisk {
	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final Storage storage = Storage.account.base;

	public Risk  getRisk(String code);
	default public boolean isSafe(String code) {
		return getRisk(code).isSafe();
	}

	public static final class Entry implements Comparable<Entry> {
	    public String code;
	    public Risk   risk;
	    public String name;
	    
	    public Entry(String code, Risk risk, String name) {
	    	this.code = code;
	    	this.risk = risk;
	    	this.name = name;
	    }
	    
		@Override
		public int compareTo(Entry that) {
			return this.code.compareTo(that.code);
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %s}", code, risk, name);
		}
	}
	
	public static final AssetRisk fundCode    = new ImplFundCode();
	public static final AssetRisk fundPrestia = new ImplFundPrestia();
	public static final AssetRisk stockUS     = new ImplStockUS();
	public static final AssetRisk stockJP     = new ImplStockJP();
	
	public class ImplFundCode implements AssetRisk {
		public static final Storage.LoadSave<Entry, String> ASSET_RISK_FUND =
			new Storage.LoadSave.Impl<>(Entry.class,  o -> o.code, storage, "asset-risk-fund.csv");
		
		private static final Map<String, Entry>      entryMap = ASSET_RISK_FUND.getMap();
		private static final Map<String, FundInfoJP> fundMap  = StorageFund.FundInfo.getMap();
		
		@Override
		public Risk getRisk(String isinCode) {
			Entry entry;
			{
				if (entryMap.containsKey(isinCode)) {
					entry = entryMap.get(isinCode);
				} else {
					if (fundMap.containsKey(isinCode)) {
						// add new entry using fundMap
						var fund = fundMap.get(isinCode);
						entry = new Entry(fund.isinCode, Risk.UNKNOWN, fund.name);
						entryMap.put(entry.code, entry);
						ASSET_RISK_FUND.save(entryMap.values());
					} else {
						logger.error("Unexpected isinCode");
						logger.error("  {}!", isinCode);
						throw new UnexpectedException("Unexpected isinCode");
					}
				}
			}
			if (entry.risk == Risk.UNKNOWN) {
				logger.warn("risk is unknown  {}  {}", entry.code, entry.name);
			}
			return entry.risk;
		}
	}
	public class ImplFundPrestia implements AssetRisk {
		public static final Storage.LoadSave<Entry, String> ASSET_RISK_FUND_PRESTIA =
			new Storage.LoadSave.Impl<>(Entry.class,  o -> o.code, storage, "asset-risk-fund-prestia.csv");
		
		private static final Map<String, Entry>       entryMap = ASSET_RISK_FUND_PRESTIA.getMap();
		private static final Map<String, FundPrestia> fundMap  = FundPrestia.FUND_PRESTIA.getMap();
		
		@Override
		public Risk getRisk(String fundCode) {
			Entry entry;
			{
				if (entryMap.containsKey(fundCode)) {
					entry = entryMap.get(fundCode);
				} else {
					if (fundMap.containsKey(fundCode)) {
						// add new entry using fundMap
						var fund = fundMap.get(fundCode);
						entry = new Entry(fund.fundCode, Risk.UNKNOWN, fund.fundName);
						entryMap.put(entry.code, entry);
						ASSET_RISK_FUND_PRESTIA.save(entryMap.values());
					} else {
						logger.error("Unexpected fundCode");
						logger.error("  {}!", fundCode);
						throw new UnexpectedException("Unexpected fundCode");
					}
				}
			}
			if (entry.risk == Risk.UNKNOWN) {
				logger.warn("risk is unknown  {}  {}", entry.code, entry.name);
			}
			return entry.risk;
		}
	}
	public class ImplStockUS implements AssetRisk {
		public static final Storage.LoadSave<Entry, String> ASSET_RISK_STOCK_US =
			new Storage.LoadSave.Impl<>(Entry.class,  o -> o.code, storage, "asset-risk-stock-us.csv");
		
		private static final Map<String, Entry>            entryMap   = ASSET_RISK_STOCK_US.getMap();
		private static final Map<String, StockInfoUSType>  stockUSMap = StorageStock.StockInfoUSTrading.getMap();

		@Override
		public Risk getRisk(String stockCode) {
			Entry entry;
			{
				if (entryMap.containsKey(stockCode)) {
					entry = entryMap.get(stockCode);
				} else {
					if (stockUSMap.containsKey(stockCode)) {
						// add new entry using fundMap
						var stockUS = stockUSMap.get(stockCode);
						entry = new Entry(stockUS.stockCode, Risk.UNKNOWN, stockUS.name);
						entryMap.put(entry.code, entry);
						ASSET_RISK_STOCK_US.save(entryMap.values());
					} else {
						logger.error("Unexpected stockCode");
						logger.error("  {}!", stockCode);
						throw new UnexpectedException("Unexpected stockCode");
					}
				}
			}
			if (entry.risk == Risk.UNKNOWN) {
				logger.warn("risk is unknown  {}  {}", entry.code, entry.name);
			}
			return entry.risk;
		}		
	}
	public class ImplStockJP implements AssetRisk {
		public static final Storage.LoadSave<Entry, String> ASSET_RISK_STOCK_JP =
			new Storage.LoadSave.Impl<>(Entry.class,  o -> o.code, storage, "asset-risk-stock-jp.csv");
			
		private static final Map<String, Entry>            entryMap   = ASSET_RISK_STOCK_JP.getMap();
		private static final Map<String, StockInfoJPType>  stockJPMap = StorageStock.StockInfoJP.getMap();

		@Override
		public Risk getRisk(String stockCode) {
			Entry entry;
			{
				if (entryMap.containsKey(stockCode)) {
					entry = entryMap.get(stockCode);
				} else {
					if (stockJPMap.containsKey(stockCode)) {
						// add new entry using fundMap
						var stockJP = stockJPMap.get(stockCode);
						entry = new Entry(stockJP.stockCode, Risk.UNKNOWN, stockJP.name);
						entryMap.put(entry.code, entry);
						ASSET_RISK_STOCK_JP.save(entryMap.values());
					} else {
						logger.error("Unexpected stockCode");
						logger.error("  {}!", stockCode);
						throw new UnexpectedException("Unexpected stockCode");
					}
				}
			}
			if (entry.risk == Risk.UNKNOWN) {
				logger.warn("risk is unknown  {}  {}", entry.code, entry.name);
			}
			return entry.risk;
		}		
	}
}

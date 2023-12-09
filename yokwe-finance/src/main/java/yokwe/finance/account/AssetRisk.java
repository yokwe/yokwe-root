package yokwe.finance.account;

import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.FundInfoJP;
import yokwe.finance.type.StockInfoJPType;
import yokwe.finance.type.StockInfoUSType;
import yokwe.util.UnexpectedException;

public interface AssetRisk {
	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final Storage storage = Storage.account.base;

	public Status  getStatus(String code);
	default public boolean isSafe(String code) {
		var status = getStatus(code);
		return status.isSafe();
	}

	public enum Status {
		UNKNOWN,
		SAFE,
		UNSAFE;
		
		public boolean isSafe() {
			return this.equals(SAFE);
		}
	}
	
	public static final class Entry implements Comparable<Entry> {
	    public String code;
	    public Status status;
	    public String name;
	    
	    public Entry(String code, Status status, String name) {
	    	this.code   = code;
	    	this.status = status;
	    	this.name   = name;
	    }
	    
		@Override
		public int compareTo(Entry that) {
			return this.code.compareTo(that.code);
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %s}", code, status, name);
		}
	}
	
	public static final AssetRisk fund    = new ImplFund();
	public static final AssetRisk stockUS = new ImplStockUS();
	public static final AssetRisk stockJP = new ImplStockJP();
	
	public class ImplFund implements AssetRisk {
		public static final Storage.LoadSave<Entry, String> ASSET_RISK_FUND =
			new Storage.LoadSave.Impl<>(Entry.class,  o -> o.code, storage, "asset-risk-fund.csv");
		
		private static final Map<String, Entry>      entryMap = ASSET_RISK_FUND.getMap();
		private static final Map<String, FundInfoJP> fundMap  = StorageFund.FundInfo.getMap();
		
		@Override
		public Status getStatus(String isinCode) {
			Entry entry;
			{
				if (entryMap.containsKey(isinCode)) {
					entry = entryMap.get(isinCode);
				} else {
					if (fundMap.containsKey(isinCode)) {
						// add new entry using fundMap
						var fund = fundMap.get(isinCode);
						entry = new Entry(fund.isinCode, Status.UNKNOWN, fund.name);
						entryMap.put(entry.code, entry);
						ASSET_RISK_FUND.save(entryMap.values());
					} else {
						logger.error("Unexpected isinCode");
						logger.error("  {}!", isinCode);
						throw new UnexpectedException("Unexpected isinCode");
					}
				}
			}
			if (entry.status == Status.UNKNOWN) {
				logger.warn("status is unknown  {}  {}", entry.code, entry.name);
			}
			return entry.status;
		}
	}
	public class ImplStockUS implements AssetRisk {
		public static final Storage.LoadSave<Entry, String> ASSET_RISK_STOCK_US =
			new Storage.LoadSave.Impl<>(Entry.class,  o -> o.code, storage, "asset-risk-stock-us.csv");
		
		private static final Map<String, Entry>            entryMap   = ASSET_RISK_STOCK_US.getMap();
		private static final Map<String, StockInfoUSType>  stockUSMap = StorageStock.StockInfoUSTrading.getMap();

		@Override
		public Status getStatus(String stockCode) {
			Entry entry;
			{
				if (entryMap.containsKey(stockCode)) {
					entry = entryMap.get(stockCode);
				} else {
					if (stockUSMap.containsKey(stockCode)) {
						// add new entry using fundMap
						var stockUS = stockUSMap.get(stockCode);
						entry = new Entry(stockUS.stockCode, Status.UNKNOWN, stockUS.name);
						entryMap.put(entry.code, entry);
						ASSET_RISK_STOCK_US.save(entryMap.values());
					} else {
						logger.error("Unexpected stockCode");
						logger.error("  {}!", stockCode);
						throw new UnexpectedException("Unexpected stockCode");
					}
				}
			}
			if (entry.status == Status.UNKNOWN) {
				logger.warn("status is unknown  {}  {}", entry.code, entry.name);
			}
			return entry.status;
		}		
	}
	public class ImplStockJP implements AssetRisk {
		public static final Storage.LoadSave<Entry, String> ASSET_RISK_STOCK_JP =
				new Storage.LoadSave.Impl<>(Entry.class,  o -> o.code, storage, "asset-risk-stock-jp.csv");
			
		private static final Map<String, Entry>            entryMap   = ASSET_RISK_STOCK_JP.getMap();
		private static final Map<String, StockInfoJPType>  stockJPMap = StorageStock.StockInfoJP.getMap();

		@Override
		public Status getStatus(String stockCode) {
			Entry entry;
			{
				if (entryMap.containsKey(stockCode)) {
					entry = entryMap.get(stockCode);
				} else {
					if (stockJPMap.containsKey(stockCode)) {
						// add new entry using fundMap
						var stockJP = stockJPMap.get(stockCode);
						entry = new Entry(stockJP.stockCode, Status.UNKNOWN, stockJP.name);
						entryMap.put(entry.code, entry);
						ASSET_RISK_STOCK_JP.save(entryMap.values());
					} else {
						logger.error("Unexpected stockCode");
						logger.error("  {}!", stockCode);
						throw new UnexpectedException("Unexpected stockCode");
					}
				}
			}
			if (entry.status == Status.UNKNOWN) {
				logger.warn("status is unknown  {}  {}", entry.code, entry.name);
			}
			return entry.status;
		}		
	}
}

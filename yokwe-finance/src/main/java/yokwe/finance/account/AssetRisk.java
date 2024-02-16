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
	
	public Entry getEntry(String code);
	
	public static final class Entry implements Comparable<Entry> {
	    public final String code;
	    public final Risk   assetRisk;
	    public final Risk   currencyRisk;
	    public final String name;
	    
	    public Entry(String code, Risk assetRisk, Risk currencyRisk, String name) {
	    	this.code         = code;
	    	this.assetRisk    = assetRisk;
	    	this.currencyRisk = currencyRisk;
	    	this.name         = name;
	    }
	    
		@Override
		public int compareTo(Entry that) {
			return this.code.compareTo(that.code);
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %s  %s}", code, assetRisk, currencyRisk, name);
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
		public Entry getEntry(String isinCode) {
			Entry entry;
			{
				if (entryMap.containsKey(isinCode)) {
					entry = entryMap.get(isinCode);
				} else {
					if (fundMap.containsKey(isinCode)) {
						// add new entry using fundMap
						var fund = fundMap.get(isinCode);
						entry = new Entry(fund.isinCode, Risk.UNKNOWN, Risk.UNKNOWN, fund.name);
						entryMap.put(entry.code, entry);
						ASSET_RISK_FUND.save(entryMap.values());
					} else {
						logger.error("Unexpected isinCode");
						logger.error("  {}!", isinCode);
						throw new UnexpectedException("Unexpected isinCode");
					}
				}
			}
			// sanity check
			if (entry.assetRisk == Risk.UNKNOWN) {
				logger.warn("assetRisk is unknown  {}", entry.toString());
			}
			if (entry.currencyRisk == Risk.UNKNOWN) {
				logger.warn("currencyRisk is unknown  {}", entry.toString());
			}
			return entry;
		}
	}
	public class ImplFundPrestia implements AssetRisk {
		public static final Storage.LoadSave<Entry, String> ASSET_RISK_FUND_PRESTIA =
			new Storage.LoadSave.Impl<>(Entry.class,  o -> o.code, storage, "asset-risk-fund-prestia.csv");
		
		private static final Map<String, Entry>       entryMap = ASSET_RISK_FUND_PRESTIA.getMap();
		private static final Map<String, FundPrestia> fundMap  = FundPrestia.FUND_PRESTIA.getMap();
		
		@Override
		public Entry getEntry(String fundCode) {
			Entry entry;
			{
				if (entryMap.containsKey(fundCode)) {
					entry = entryMap.get(fundCode);
				} else {
					if (fundMap.containsKey(fundCode)) {
						// add new entry using fundMap
						var fund = fundMap.get(fundCode);
						entry = new Entry(fund.fundCode, Risk.UNKNOWN, Risk.UNKNOWN, fund.fundName);
						entryMap.put(entry.code, entry);
						ASSET_RISK_FUND_PRESTIA.save(entryMap.values());
					} else {
						logger.error("Unexpected fundCode");
						logger.error("  {}!", fundCode);
						throw new UnexpectedException("Unexpected fundCode");
					}
				}
			}
			// sanity check
			if (entry.assetRisk == Risk.UNKNOWN) {
				logger.warn("assetRisk is unknown  {}", entry.toString());
			}
			if (entry.currencyRisk == Risk.UNKNOWN) {
				logger.warn("currencyRisk is unknown  {}", entry.toString());
			}
			return entry;
		}
	}
	public class ImplStockUS implements AssetRisk {
		public static final Storage.LoadSave<Entry, String> ASSET_RISK_STOCK_US =
			new Storage.LoadSave.Impl<>(Entry.class,  o -> o.code, storage, "asset-risk-stock-us.csv");
		
		private static final Map<String, Entry>            entryMap   = ASSET_RISK_STOCK_US.getMap();
		private static final Map<String, StockInfoUSType>  stockUSMap = StorageStock.StockInfoUSTrading.getMap();

		@Override
		public Entry getEntry(String stockCode) {
			Entry entry;
			{
				if (entryMap.containsKey(stockCode)) {
					entry = entryMap.get(stockCode);
				} else {
					if (stockUSMap.containsKey(stockCode)) {
						// add new entry using fundMap
						var stockUS = stockUSMap.get(stockCode);
						entry = new Entry(stockUS.stockCode, Risk.UNKNOWN, Risk.UNKNOWN, stockUS.name);
						entryMap.put(entry.code, entry);
						ASSET_RISK_STOCK_US.save(entryMap.values());
					} else {
						logger.error("Unexpected stockCode");
						logger.error("  {}!", stockCode);
						throw new UnexpectedException("Unexpected stockCode");
					}
				}
			}
			// sanity check
			if (entry.assetRisk == Risk.UNKNOWN) {
				logger.warn("assetRisk is unknown  {}", entry.toString());
			}
			if (entry.currencyRisk == Risk.UNKNOWN) {
				logger.warn("currencyRisk is unknown  {}", entry.toString());
			}
			return entry;
		}		
	}
	public class ImplStockJP implements AssetRisk {
		public static final Storage.LoadSave<Entry, String> ASSET_RISK_STOCK_JP =
			new Storage.LoadSave.Impl<>(Entry.class,  o -> o.code, storage, "asset-risk-stock-jp.csv");
			
		private static final Map<String, Entry>            entryMap   = ASSET_RISK_STOCK_JP.getMap();
		private static final Map<String, StockInfoJPType>  stockJPMap = StorageStock.StockInfoJP.getMap();
		
		@Override
		public Entry getEntry(String stockCode) {
			Entry entry;
			{
				if (entryMap.containsKey(stockCode)) {
					entry = entryMap.get(stockCode);
				} else {
					if (stockJPMap.containsKey(stockCode)) {
						// add new entry using fundMap
						var stockJP = stockJPMap.get(stockCode);
						entry = new Entry(stockJP.stockCode, Risk.UNKNOWN, Risk.UNKNOWN, stockJP.name);
						entryMap.put(entry.code, entry);
						ASSET_RISK_STOCK_JP.save(entryMap.values());
					} else {
						logger.error("Unexpected stockCode");
						logger.error("  {}!", stockCode);
						throw new UnexpectedException("Unexpected stockCode");
					}
				}
			}
			// sanity check
			if (entry.assetRisk == Risk.UNKNOWN) {
				logger.warn("assetRisk is unknown  {}", entry.toString());
			}
			if (entry.currencyRisk == Risk.UNKNOWN) {
				logger.warn("currencyRisk is unknown  {}", entry.toString());
			}
			return entry;
		}		
	}
}

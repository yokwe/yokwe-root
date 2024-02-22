package yokwe.finance.account;

import java.util.Map;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.account.prestia.FundPrestia;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.stock.StorageStock;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class AssetInfo implements Comparable<AssetInfo> {
	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public interface Provider {
		AssetInfo getAssetInfo(String code);
	}

    public final String code;
    public final Risk   assetRisk;
    public final Risk   currencyRisk;
    public final String name;
    
    public AssetInfo(String code, Risk assetRisk, Risk currencyRisk, String name) {
    	this.code         = code;
    	this.assetRisk    = assetRisk;
    	this.currencyRisk = currencyRisk;
    	this.name         = name;
    }
    public AssetInfo(String code, String name) {
    	this(code, Risk.UNKNOWN, Risk.UNKNOWN, name);
    }
    
	@Override
	public int compareTo(AssetInfo that) {
		return this.code.compareTo(that.code);
	}
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
    
	private static final Storage storage = Storage.account.base;
	
	public static final Provider fundCode;
	public static final Provider fundPrestia;
	public static final Provider stockUS;
	public static final Provider stockJP;
	static {
		{
			var loadSave = new Storage.LoadSave.Impl<>(AssetInfo.class,  o -> o.code, storage, "asset-info-fund.csv");
			var nameMap  = StorageFund.FundInfo.getList().stream().collect(Collectors.toMap(o -> o.isinCode, o -> o.name));
			
			fundCode = new ProviderImpl(loadSave, nameMap);
		}
		{
			var loadSave = new Storage.LoadSave.Impl<>(AssetInfo.class,  o -> o.code, storage, "asset-info-fund-prestia.csv");			
			var nameMap  = FundPrestia.FUND_PRESTIA.getList().stream().collect(Collectors.toMap(o -> o.fundCode, o -> o.fundName));
			
			fundPrestia = new ProviderImpl(loadSave, nameMap);
		}
		{
			var loadSave = new Storage.LoadSave.Impl<>(AssetInfo.class,  o -> o.code, storage, "asset-info-stock-us.csv");			
			var nameMap  = StorageStock.StockInfoUSTrading.getList().stream().collect(Collectors.toMap(o -> o.stockCode, o -> o.name));
			
			stockUS = new ProviderImpl(loadSave, nameMap);
		}
		{
			var loadSave = new Storage.LoadSave.Impl<>(AssetInfo.class,  o -> o.code, storage, "asset-info-stock-jp.csv");			
			var nameMap  = StorageStock.StockInfoJP.getList().stream().collect(Collectors.toMap(o -> o.stockCode, o -> o.name));
			
			stockJP = new ProviderImpl(loadSave, nameMap);
		}
	}


    private static class ProviderImpl implements Provider {
    	private final Storage.LoadSave<AssetInfo, String> loadSave;
		private final Map<String, AssetInfo> assetInfoMap;
		private final Map<String, String>    nameMap;
		
		ProviderImpl(Storage.LoadSave<AssetInfo, String> loadSave, Map<String, String> nameMap) {
			this.loadSave     = loadSave;
			this.assetInfoMap = loadSave.getMap();
			this.nameMap      = nameMap;
		}
		
		@Override
		public AssetInfo getAssetInfo(String code) {
			var ret = assetInfoMap.get(code);
			if (ret == null) {
				var name = nameMap.get(code);
				if (name == null) {
					logger.error("Unexpected code");
					logger.error("code  {}!", code);
					throw new UnexpectedException("Unexpected code");
				}
				ret = new AssetInfo(code, name);
				assetInfoMap.put(code, ret);
				// save changes
				loadSave.save(assetInfoMap.values());
			}
			// sanity check
			if (ret.assetRisk == Risk.UNKNOWN) {
				logger.warn("assetRisk is unknown  {}", ret.toString());
			}
			if (ret.currencyRisk == Risk.UNKNOWN) {
				logger.warn("currencyRisk is unknown  {}", ret.toString());
			}
			return ret;
		}
    }
}

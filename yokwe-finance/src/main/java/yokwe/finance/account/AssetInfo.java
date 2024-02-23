package yokwe.finance.account;

import java.util.Map;

import yokwe.finance.Storage;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class AssetInfo implements Comparable<AssetInfo> {
	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
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
	
	
	public static AssetInfo getInstance(Asset asset) {
		switch (asset.product) {
		case DEPOSIT:
			return AssetInfo.DEPOSIT;
		case TERM_DEPOSIT:
			return AssetInfo.TERM_DEPOSIT;
		case STOCK:
			switch(asset.currency) {
			case JPY:
				return AssetInfo.stockJP.getAssetInfo(asset);
			case USD:
				return AssetInfo.stockUS.getAssetInfo(asset);
			default:
				logger.error("Uexpected product");
				logger.error("  asset  {}", asset);
				throw new UnexpectedException("Uexpected product");
			}
		case FUND:
			return AssetInfo.fundCode.getAssetInfo(asset);
		case BOND:
			return AssetInfo.BOND;
		default:
			logger.error("Uexpected product");
			logger.error("  asset  {}", asset);
			throw new UnexpectedException("Uexpected product");
		}
	}
	
	private static final AssetInfo DEPOSIT      = new AssetInfo("", Risk.SAFE, Risk.SAFE, "DEPOSIT");
	private static final AssetInfo TERM_DEPOSIT = new AssetInfo("", Risk.SAFE, Risk.SAFE, "TERM_DEPOSIT");
	private static final AssetInfo BOND         = new AssetInfo("", Risk.SAFE, Risk.SAFE, "BOND");
	
	private interface Provider {
		AssetInfo getAssetInfo(Asset asset);
	}
	private static final Provider fundCode;
	private static final Provider stockUS;
	private static final Provider stockJP;
	
	static {
		Storage storage = Storage.account.base;
		
		{
			var loadSave = new Storage.LoadSave.Impl<>(AssetInfo.class,  o -> o.code, storage, "asset-info-fund.csv");			
			fundCode = new ProviderImpl(loadSave);
		}
		{
			var loadSave = new Storage.LoadSave.Impl<>(AssetInfo.class,  o -> o.code, storage, "asset-info-stock-us.csv");			
			stockUS = new ProviderImpl(loadSave);
		}
		{
			var loadSave = new Storage.LoadSave.Impl<>(AssetInfo.class,  o -> o.code, storage, "asset-info-stock-jp.csv");			
			stockJP = new ProviderImpl(loadSave);
		}
	}

    private static class ProviderImpl implements Provider {
    	private final Storage.LoadSave<AssetInfo, String> loadSave;
		private final Map<String, AssetInfo> assetInfoMap;
		
		ProviderImpl(Storage.LoadSave<AssetInfo, String> loadSave) {
			this.loadSave     = loadSave;
			this.assetInfoMap = loadSave.getMap();
		}
		
		@Override
		public AssetInfo getAssetInfo(Asset asset) {
			var code = asset.code;
			var name = asset.name;
			
			var ret = assetInfoMap.get(code);
			if (ret == null) {
				// add new entry to assetInfoMap
				ret = new AssetInfo(code, name);
				assetInfoMap.put(code, ret);
				// save changes
				loadSave.save(assetInfoMap.values());
			}
			// sanity check
			if (ret.assetRisk == Risk.UNKNOWN || ret.currencyRisk == Risk.UNKNOWN) {
				logger.warn("assetInfo has UNKNOWN  {}", ret);
			}
			return ret;
		}
    }
}

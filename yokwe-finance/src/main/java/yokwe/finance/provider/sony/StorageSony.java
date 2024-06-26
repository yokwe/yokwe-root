package yokwe.finance.provider.sony;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingFundType;

public class StorageSony {
	public static final Storage storage = Storage.provider.sony;
	
	// trading-fund-sony
	public static final Storage.LoadSave<TradingFundType, String> TradingFundSony =
		new Storage.LoadSave.Impl<>(TradingFundType.class,  o -> o.isinCode, storage, "trading-fund-sony.csv");
	
	// trading-fund-sony
	public static final Storage.LoadSave<FundInfoSony, String> FundInfoSony =
		new Storage.LoadSave.Impl<>(FundInfoSony.class,  o -> o.isinCode, storage, "fund-info-sony.csv");
}

package yokwe.finance.provider.smtb;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingFundType;

public class StorageSMTB {
	public static final Storage storage = Storage.provider.smtb;
	
	// trading-fund-smtb
	public static final Storage.LoadSave<TradingFundType, String> TradingFundSMTB =
		new Storage.LoadSave.Impl<>(TradingFundType.class,  o -> o.isinCode, storage, "trading-fund-smtb.csv");
}

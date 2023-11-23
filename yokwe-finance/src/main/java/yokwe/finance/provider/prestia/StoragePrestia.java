package yokwe.finance.provider.prestia;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingFundType;

public class StoragePrestia {
	public static final Storage storage = Storage.provider.prestia;
	
	// trading-fund-nomura
	public static final Storage.LoadSave<TradingFundType, String> TradingFundPrestia =
		new Storage.LoadSave.Impl<>(TradingFundType.class,  o -> o.isinCode, storage, "trading-fund-prestia.csv");
}

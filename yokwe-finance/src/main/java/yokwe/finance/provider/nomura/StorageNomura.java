package yokwe.finance.provider.nomura;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingFundType;

public class StorageNomura {
	public static final Storage storage = Storage.provider.nomura;
	
	// trading-fund-nomura
	public static final Storage.LoadSave<TradingFundType, String> TradingFundNomura =
		new Storage.LoadSave.Impl<>(TradingFundType.class,  o -> o.isinCode, storage, "trading-fund-nomura.csv");
}

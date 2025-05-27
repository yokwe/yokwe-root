package yokwe.finance.provider.rakuten;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingFundType;
import yokwe.finance.type.TradingStockType;

public class StorageRakuten {
	public static final Storage storage = Storage.provider.rakuten;
	
	// trading-fund-rakuten
	public static final Storage.LoadSave<TradingFundType, String> TradingFundRakuten =
		new Storage.LoadSave.Impl<>(TradingFundType.class,  o -> o.isinCode, storage, "trading-fund-rakuten.csv");
	// trading-stock-rakuten
	public static final Storage.LoadSave<TradingStockType, String> TradingStockRakuten =
		new Storage.LoadSave.Impl<>(TradingStockType.class,  o -> o.stockCode, storage, "trading-stock-rakuten.csv");
	// fund-code-name
	public static final Storage.LoadSave<FundCodeName, String> FundCodeNameRakuten =
		new Storage.LoadSave.Impl<>(FundCodeName.class,  o -> o.isinCode, storage, "fund-code-name-rakuten.csv");
}

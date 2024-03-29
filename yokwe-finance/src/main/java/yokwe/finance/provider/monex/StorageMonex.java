package yokwe.finance.provider.monex;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingFundType;
import yokwe.finance.type.TradingStockType;

public class StorageMonex {
	public static final Storage storage = Storage.provider.monex;
	
	// trading-fund-monex
	public static final Storage.LoadSave<TradingFundType, String> TradingFundMonex =
		new Storage.LoadSave.Impl<>(TradingFundType.class,  o -> o.isinCode, storage, "trading-fund-monex.csv");
	// trading-stock-monex
	public static final Storage.LoadSave<TradingStockType, String> TradingStockMonex =
		new Storage.LoadSave.Impl<>(TradingStockType.class,  o -> o.stockCode, storage, "trading-stock-monex.csv");
}

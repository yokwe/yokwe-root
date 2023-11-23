package yokwe.finance.provider.moomoo;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingStockType;

public class StorageMoomoo {
	public static final Storage storage = Storage.provider.moomoo;
	
	// trading-stock-monex
	public static final Storage.LoadSave<TradingStockType, String> TradingStockMoomoo =
		new Storage.LoadSave.Impl<>(TradingStockType.class,  o -> o.stockCode, storage, "trading-stock-moomoo.csv");
}

package yokwe.finance.provider.rakuten;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingFundType;
import yokwe.finance.type.TradingStockType;

public class StorageRakuten {
	public static void initialize() {}
	
	private static final Storage storage = new Storage.Impl(Storage.provider, "rakuten");
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
	
	// trading-fund-monex
	public static final Storage.LoadSave<TradingFundType, String> TradingFundRakuten =
		new Storage.LoadSave.Impl<>(TradingFundType.class,  o -> o.isinCode, storage, "trading-fund-rakuten.csv");
	// trading-stock-monex
	public static final Storage.LoadSave<TradingStockType, String> TradingStockRakuten =
		new Storage.LoadSave.Impl<>(TradingStockType.class,  o -> o.stockCode, storage, "trading-stock-rakuten.csv");
}

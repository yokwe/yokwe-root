package yokwe.finance.provider.sbi;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingFundType;
import yokwe.finance.type.TradingStockType;

public class StorageSBI {
	private static final Storage storage = Storage.provider.sbi;
	
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
	public static final Storage.LoadSave<TradingFundType, String> TradingFundSBI =
		new Storage.LoadSave.Impl<>(TradingFundType.class,  o -> o.isinCode, storage, "trading-fund-sbi.csv");
	// trading-stock-monex
	public static final Storage.LoadSave<TradingStockType, String> TradingStockSBI =
		new Storage.LoadSave.Impl<>(TradingStockType.class,  o -> o.stockCode, storage, "trading-stock-sbi.csv");
}

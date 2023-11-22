package yokwe.finance.provider.moomoo;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingStockType;

public class StorageMoomoo {
	private static final Storage storage = Storage.provider.moomoo;
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
	
	// trading-stock-monex
	public static final Storage.LoadSave<TradingStockType, String> TradingStockMoomoo =
		new Storage.LoadSave.Impl<>(TradingStockType.class,  o -> o.stockCode, storage, "trading-stock-moomoo.csv");
}

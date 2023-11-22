package yokwe.finance.provider.nikko;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingFundType;
import yokwe.finance.type.TradingStockType;

public class StorageNikko {
	private static final Storage storage = Storage.provider.nikko;
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
	
	// trading-fund-nikko
	public static final Storage.LoadSave<TradingFundType, String> TradingFundNikko =
		new Storage.LoadSave.Impl<>(TradingFundType.class,  o -> o.isinCode, storage, "trading-fund-nikko.csv");
	// trading-stock-nikko
	public static final Storage.LoadSave<TradingStockType, String> TradingStockNikko =
		new Storage.LoadSave.Impl<>(TradingStockType.class,  o -> o.stockCode, storage, "trading-stock-nikko.csv");
}

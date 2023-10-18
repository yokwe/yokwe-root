package yokwe.finance.provider.nyse;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoUSType;

public class StorageNYSE {
	public static void initialize() {}
	
	private static final Storage storage = new Storage.Impl(Storage.provider, "nyse");
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
	
	// stock-info-nasdaq
	public static final Storage.LoadSave<StockInfoUSType, String> StockInfoNYSE =
		new Storage.LoadSave.Impl<>(StockInfoUSType.class,  o -> o.stockCode, storage, "stock-info-nyse.csv");
	
	// stock-info-nasdaq
	public static final Storage.LoadSave<FilterType, String> Filter =
		new Storage.LoadSave.Impl<>(FilterType.class,  o -> o.normalizedTicker, storage, "filter.csv");
}

package yokwe.finance.provider.click;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingFundType;

public class StorageClick {
	public static void initialize() {}
	
	private static final Storage storage = new Storage.Impl(Storage.provider, "click");
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
	
	// stock-info-bats
	public static final Storage.LoadSave<TradingFundType, String> TradingFundClick =
		new Storage.LoadSave.Impl<>(TradingFundType.class,  o -> o.isinCode, storage, "trading-fund-click.csv");
}

package yokwe.finance.provider.sony;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingFundType;

public class StorageSony {
	public static void initialize() {}
	
	private static final Storage storage = new Storage.Impl(Storage.provider, "sony");
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
	
	// trading-fund-nomura
	public static final Storage.LoadSave<TradingFundType, String> TradingFundSony =
		new Storage.LoadSave.Impl<>(TradingFundType.class,  o -> o.isinCode, storage, "trading-fund-sony.csv");
}

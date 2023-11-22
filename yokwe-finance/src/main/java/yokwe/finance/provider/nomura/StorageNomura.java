package yokwe.finance.provider.nomura;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingFundType;

public class StorageNomura {
	private static final Storage storage = Storage.provider.nomura;
	
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
	public static final Storage.LoadSave<TradingFundType, String> TradingFundNomura =
		new Storage.LoadSave.Impl<>(TradingFundType.class,  o -> o.isinCode, storage, "trading-fund-nomura.csv");
}

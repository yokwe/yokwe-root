package yokwe.finance.account.prestia;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;

public class StoragePrestia {
	private static final Storage storage = Storage.account.prestia;
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
	
	// asset
	public static final Storage.LoadSaveList<Asset> Asset =
		new Storage.LoadSaveList.Impl<>(Asset.class, storage, "asset.csv");
}

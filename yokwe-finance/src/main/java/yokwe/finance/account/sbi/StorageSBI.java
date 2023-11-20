package yokwe.finance.account.sbi;

import yokwe.finance.Storage;

public class StorageSBI {
	public static void initialize() {}
	
	private static final Storage storage = new Storage.Impl(Storage.account, "sbi");
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
}

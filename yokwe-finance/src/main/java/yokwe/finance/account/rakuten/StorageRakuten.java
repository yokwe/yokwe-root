package yokwe.finance.account.rakuten;

import yokwe.finance.Storage;

public class StorageRakuten {
	public static void initialize() {}
	
	private static final Storage storage = new Storage.Impl(Storage.account, "rakuten");
	
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

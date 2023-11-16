package yokwe.finance.account.smbc;

import yokwe.finance.Storage;

public class StorageSMBC {
	public static void initialize() {}
	
	private static final Storage storage = new Storage.Impl(Storage.account, "smbc");
	
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

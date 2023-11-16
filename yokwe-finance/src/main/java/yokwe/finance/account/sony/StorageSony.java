package yokwe.finance.account.sony;

import yokwe.finance.Storage;

public class StorageSony {
	public static void initialize() {}
	
	private static final Storage storage = new Storage.Impl(Storage.account, "sony");
	
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

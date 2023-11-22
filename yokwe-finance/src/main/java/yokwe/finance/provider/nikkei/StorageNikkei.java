package yokwe.finance.provider.nikkei;

import yokwe.finance.Storage;

public class StorageNikkei {
	private static final Storage storage = Storage.provider.nikkei;
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
	
	// div score
	public static final Storage.LoadSave<DivScoreType, String> DivScore =
		new Storage.LoadSave.Impl<>(DivScoreType.class,  o -> o.isinCode, storage, "div-score.csv");
}

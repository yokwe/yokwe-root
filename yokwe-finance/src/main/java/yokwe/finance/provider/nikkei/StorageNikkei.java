package yokwe.finance.provider.nikkei;

import yokwe.finance.Storage;

public class StorageNikkei {
	public static final Storage storage = Storage.provider.nikkei;
	
	// div score
	public static final Storage.LoadSave<DivScoreType, String> DivScore =
		new Storage.LoadSave.Impl<>(DivScoreType.class,  o -> o.isinCode, storage, "div-score.csv");
}

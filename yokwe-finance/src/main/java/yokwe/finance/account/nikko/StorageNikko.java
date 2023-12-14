package yokwe.finance.account.nikko;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;

public class StorageNikko {
	public static final Storage storage = Storage.account.nikko;
	
	// asset
	public static final Storage.LoadSaveList<Asset> Asset =
		new Storage.LoadSaveList.Impl<>(Asset.class, storage, "asset.csv");
}

package yokwe.finance.account.sony;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;

public class StorageSony {
	public static final Storage storage = Storage.account.sony;
	
	// asset
	public static final Storage.LoadSaveList<Asset> Asset =
		new Storage.LoadSaveList.Impl<>(Asset.class, storage, "asset.csv");
}

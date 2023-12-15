package yokwe.finance.account.sbi;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;

public class StorageSBI {
	public static final Storage storage = Storage.account.sbi;
	
	// asset
	public static final Storage.LoadSaveList<Asset> Asset =
		new Storage.LoadSaveList.Impl<>(Asset.class, storage, "asset.csv");
}

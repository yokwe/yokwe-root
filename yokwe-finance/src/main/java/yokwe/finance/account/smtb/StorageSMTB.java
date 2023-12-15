package yokwe.finance.account.smtb;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;

public class StorageSMTB {
	public static final Storage storage = Storage.account.smtb;
	
	// asset
	public static final Storage.LoadSaveList<Asset> Asset =
		new Storage.LoadSaveList.Impl<>(Asset.class, storage, "asset.csv");
}

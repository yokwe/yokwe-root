package yokwe.finance.account.rakuten;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;

public class StorageRakuten {
	public static final Storage storage = Storage.account.rakuten;
	
	// asset
	public static final Storage.LoadSaveList<Asset> Asset =
		new Storage.LoadSaveList.Impl<>(Asset.class, storage, "asset.csv");
}

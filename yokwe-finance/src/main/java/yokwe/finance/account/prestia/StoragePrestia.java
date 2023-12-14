package yokwe.finance.account.prestia;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;

public class StoragePrestia {
	public static final Storage storage = Storage.account.prestia;
	
	// asset
	public static final Storage.LoadSaveList<Asset> Asset =
		new Storage.LoadSaveList.Impl<>(Asset.class, storage, "asset.csv");
	
	// fund prestia
	public static final Storage.LoadSave<FundPrestia, String> FundPrestia =
		new Storage.LoadSave.Impl<>(FundPrestia.class, o -> o.fundCode, storage, "fund-prestia.csv");
}

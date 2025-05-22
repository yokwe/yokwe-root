package yokwe.finance.trade.rakuten;

import yokwe.finance.Storage;
import yokwe.finance.trade.AccountHistory;

public class StorageRakuten {
	public static final Storage storage = Storage.trade.rakuten;
	
	// account-history
	public static final Storage.LoadSaveList<AccountHistory> AccountHistory =
		new Storage.LoadSaveList.Impl<AccountHistory>(AccountHistory.class,  storage, "account-history-rakuten.csv");
}

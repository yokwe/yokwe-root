package yokwe.finance.trade2.rakuten;

import yokwe.finance.Storage;
import yokwe.finance.trade2.Transaction;

public class StorageRakuten {
	public static final Storage storage = Storage.trade.rakuten;
	
	// account-history
	public static final Storage.LoadSaveList<Transaction> TransactionList =
		new Storage.LoadSaveList.Impl<Transaction>(Transaction.class,  storage, "transaction-list-rakuten.csv");
}

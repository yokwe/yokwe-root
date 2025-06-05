package yokwe.finance.trade2.rakuten;

import yokwe.finance.Storage;
import yokwe.finance.trade2.FundPriceInfoJPType;
import yokwe.finance.trade2.Transaction;

public class StorageRakuten {
	public static final Storage storage = Storage.trade.rakuten;
	
	// transaction-list
	public static final Storage.LoadSaveList<Transaction> TransactionList =
		new Storage.LoadSaveList.Impl<Transaction>(Transaction.class,  storage, "transaction-rakuten.csv");
	
	// fund-price-factor
	public static final Storage.LoadSaveList<FundPriceInfoJPType> FundPriceInfoJP =
		new Storage.LoadSaveList.Impl<FundPriceInfoJPType>(FundPriceInfoJPType.class,  storage, "fund-price-info-jp-rakuten.csv");
}

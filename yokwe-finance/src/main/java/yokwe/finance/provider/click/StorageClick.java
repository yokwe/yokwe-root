package yokwe.finance.provider.click;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingFundType;

public class StorageClick {
	public static final Storage storage = Storage.provider.click;
	
	// stock-info-bats
	public static final Storage.LoadSave<TradingFundType, String> TradingFundClick =
		new Storage.LoadSave.Impl<>(TradingFundType.class,  o -> o.isinCode, storage, "trading-fund-click.csv");
}

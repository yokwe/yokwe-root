package yokwe.finance.provider.rakuten;

import yokwe.finance.Storage;
import yokwe.finance.type.NisaFundType;
import yokwe.finance.type.StockCodeType;
import yokwe.finance.type.TradingFundType;
import yokwe.finance.type.TradingStockType;

public class StorageRakuten {
	private static final Storage storage = Storage.provider.rakuten;
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
	
	// trading-fund-rakuten
	public static final Storage.LoadSave<TradingFundType, String> TradingFundRakuten =
		new Storage.LoadSave.Impl<>(TradingFundType.class,  o -> o.isinCode, storage, "trading-fund-rakuten.csv");
	// trading-stock-rakuten
	public static final Storage.LoadSave<TradingStockType, String> TradingStockRakuten =
		new Storage.LoadSave.Impl<>(TradingStockType.class,  o -> o.stockCode, storage, "trading-stock-rakuten.csv");
	// nisa-rakuten
	public static final Storage.LoadSave<NisaFundType, String> NisaFundRakuten =
		new Storage.LoadSave.Impl<>(NisaFundType.class,  o -> o.isinCode, storage, "nisa-fund-rakuten.csv");
	// nisa-etf-jp-rakuten
	public static final Storage.LoadSave<StockCodeType, String> NisaETFJPRakuten =
		new Storage.LoadSave.Impl<>(StockCodeType.class,  o -> o.stockCode, storage, "nisa-etf-jp-rakuten.csv");
	// nisa-etf-us-rakuten
	public static final Storage.LoadSave<StockCodeType, String> NisaETFUSRakuten =
		new Storage.LoadSave.Impl<>(StockCodeType.class,  o -> o.stockCode, storage, "nisa-etf-us-rakuten.csv");
}

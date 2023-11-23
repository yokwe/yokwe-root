package yokwe.finance.provider.nyse;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoUSType;

public class StorageNYSE {
	public static final Storage storage = Storage.provider.nyse;
	
	// stock-info-nasdaq
	public static final Storage.LoadSave<StockInfoUSType, String> StockInfoNYSE =
		new Storage.LoadSave.Impl<>(StockInfoUSType.class,  o -> o.stockCode, storage, "stock-info-nyse.csv");
	
	// stock-info-nasdaq
	public static final Storage.LoadSave<FilterType, String> Filter =
		new Storage.LoadSave.Impl<>(FilterType.class,  o -> o.normalizedTicker, storage, "filter.csv");
}

package yokwe.finance.provider.yahoo;

import java.time.chrono.ChronoLocalDate;

import yokwe.finance.Storage;
import yokwe.finance.type.CompanyInfoType;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.OHLCV;

public class StorageYahoo {
	public static final Storage storage = Storage.provider.yahoo;
	
	// company-info-jp-yahoo
	public static final Storage.LoadSave<CompanyInfoType, String> CompanyInfoJPYahoo =
		new Storage.LoadSave.Impl<>(CompanyInfoType.class,  o -> o.stockCode, storage, "company-info-jp-yahoo.csv");
	// company-info-us-yahoo
	public static final Storage.LoadSave<CompanyInfoType, String> CompanyInfoUSYahoo =
		new Storage.LoadSave.Impl<>(CompanyInfoType.class,  o -> o.stockCode, storage, "company-info-us-yahoo.csv");
	
	// stock-div-jp-yahoo
	public static final Storage.LoadSave2<DailyValue, ChronoLocalDate> StockDivJPYahoo =
		new Storage.LoadSave2.Impl<>(DailyValue.class,  o -> o.date, storage, "stock-div-jp-yahoo", o -> o + ".csv");
	
	// stock-price-jp-yahoo
	public static final Storage.LoadSave2<OHLCV, ChronoLocalDate> StockPriceJPYahoo =
		new Storage.LoadSave2.Impl<>(OHLCV.class,  o -> o.date, storage, "stock-price-jp-yahoo", o -> o + ".csv");
	
	// stock-split-jp-yahoo
	public static final Storage.LoadSave2<Split, ChronoLocalDate> StockSplitJPYahoo =
		new Storage.LoadSave2.Impl<>(Split.class,  o -> o.date, storage, "stock-split-jp-yahoo", o -> o + ".csv");
}

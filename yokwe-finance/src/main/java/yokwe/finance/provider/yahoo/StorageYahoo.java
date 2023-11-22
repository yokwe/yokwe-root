package yokwe.finance.provider.yahoo;

import java.time.chrono.ChronoLocalDate;

import yokwe.finance.Storage;
import yokwe.finance.type.CompanyInfoType;
import yokwe.finance.type.DailyValue;

public class StorageYahoo {
	private static final Storage storage = Storage.provider.yahoo;
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
	
	// company-info-jp-yahoo
	public static final Storage.LoadSave<CompanyInfoType, String> CompanyInfoJPYahoo =
		new Storage.LoadSave.Impl<>(CompanyInfoType.class,  o -> o.stockCode, storage, "company-info-jp-yahoo.csv");
	// company-info-us-yahoo
	public static final Storage.LoadSave<CompanyInfoType, String> CompanyInfoUSYahoo =
		new Storage.LoadSave.Impl<>(CompanyInfoType.class,  o -> o.stockCode, storage, "company-info-us-yahoo.csv");
	
	// stock-div-jp-yahoo
	public static final Storage.LoadSave2<DailyValue, ChronoLocalDate> StockDivJPYahoo =
		new Storage.LoadSave2.Impl<>(DailyValue.class,  o -> o.date, storage, "stock-div-jp-yahoo", o -> o + ".csv");
}

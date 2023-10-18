package yokwe.finance.provider.nasdaq;

import yokwe.finance.Storage;
import yokwe.finance.type.CompanyInfoType;
import yokwe.finance.type.StockInfoUSType;

public class StorageNasdaq {
	public static void initialize() {}
	
	private static final Storage storage = new Storage.Impl(Storage.provider, "nasdaq");
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
	
	// stock-info-nasdaq
	public static final Storage.LoadSave<StockInfoUSType, String> StockInfoNasdaq =
		new Storage.LoadSave.Impl<>(StockInfoUSType.class,  o -> o.stockCode, storage, "stock-info-nasdaq.csv");
	
	// company-info-nasdaq
	public static final Storage.LoadSave<CompanyInfoType, String> CompanyInfoNasdaq =
		new Storage.LoadSave.Impl<>(CompanyInfoType.class,  o -> o.stockCode, storage, "company-info-nasdaq.csv");
}

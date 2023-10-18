package yokwe.finance.provider.bats;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoUSType;

public class StorageBATS {
	public static void initialize() {}
	
	private static final Storage storage = new Storage.Impl(Storage.provider, "bats");
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
	
	public static final String PATH_TXT = getPath("listed-security-report.txt");

	// stock-info-bats
	public static final Storage.LoadSave<StockInfoUSType, String> StockInfoBATS =
		new Storage.LoadSave.Impl<>(StockInfoUSType.class,  o -> o.stockCode, storage, "stock-info-bats.csv");
	
	// listed-security-report
	public static final Storage.LoadSave<ListedSecurityReportType, String> ListedSecurityReport =
		new Storage.LoadSave.Impl<>(ListedSecurityReportType.class,  o -> o.symbol, storage, "listed-security-report.csv");
}

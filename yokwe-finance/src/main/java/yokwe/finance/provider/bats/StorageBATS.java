package yokwe.finance.provider.bats;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoUSType;

public class StorageBATS {
	public static final Storage storage = Storage.provider.bats;
	
	public static final String PATH_TXT = storage.getPath("listed-security-report.txt");

	// stock-info-bats
	public static final Storage.LoadSave<StockInfoUSType, String> StockInfoBATS =
		new Storage.LoadSave.Impl<>(StockInfoUSType.class,  o -> o.stockCode, storage, "stock-info-bats.csv");
	
	// listed-security-report
	public static final Storage.LoadSave<ListedSecurityReportType, String> ListedSecurityReport =
		new Storage.LoadSave.Impl<>(ListedSecurityReportType.class,  o -> o.symbol, storage, "listed-security-report.csv");
}

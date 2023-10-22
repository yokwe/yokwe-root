package yokwe.finance.report;

import yokwe.finance.Storage;

public class StorageReport {
	public static void initialize() {}
	
	private static final Storage storage = Storage.report;
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
	
	// stock-stats-jp
	public static final Storage.LoadSave<StockStatsJP, String> StockStatsJP =
		new Storage.LoadSave.Impl<>(StockStatsJP.class,  o -> o.stockCode, storage, "stock-stats-jp.csv");
	// stock-stats-us
	public static final Storage.LoadSave<StockStatsUS, String> StockStatsUS =
		new Storage.LoadSave.Impl<>(StockStatsUS.class,  o -> o.stockCode, storage, "stock-stats-us.csv");
	// fund-stats
	public static final Storage.LoadSave<FundStatsType, String> FundStats =
		new Storage.LoadSave.Impl<>(FundStatsType.class,  o -> o.isinCode, storage, "fund-stats.csv");
}

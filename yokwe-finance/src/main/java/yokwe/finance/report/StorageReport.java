package yokwe.finance.report;

import yokwe.finance.Storage;

public class StorageReport {
	public static void initialize() {}
	
	public static final Storage storage = Storage.report;
	
	// stock-stats-jp
	public static final Storage.LoadSave<StockStatsJP, String> StockStatsJP =
		new Storage.LoadSave.Impl<>(StockStatsJP.class,  o -> o.stockCode, storage, "stock-stats-jp.csv");
	// stock-stats-us
	public static final Storage.LoadSave<StockStatsUS, String> StockStatsUS =
		new Storage.LoadSave.Impl<>(StockStatsUS.class,  o -> o.stockCode, storage, "stock-stats-us.csv");
	// fund-stats
	public static final Storage.LoadSave<FundStatsType, String> FundStats =
		new Storage.LoadSave.Impl<>(FundStatsType.class,  o -> o.isinCode, storage, "fund-stats.csv");
	// fund-stats
	public static final Storage.LoadSave<StockStatsUSMonthly, String> StockStatsUSMonthly =
		new Storage.LoadSave.Impl<>(StockStatsUSMonthly.class,  o -> o.stockCode, storage, "stock-stats-us-mothly.csv");
}

package yokwe.finance.provider.jpx;

import java.time.chrono.ChronoLocalDate;

import yokwe.finance.Storage;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.OHLCV;
import yokwe.finance.type.StockInfoJPType;

public class StorageJPX {
	private static final Storage storage = Storage.provider.jpx;
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
	
	// etf
	public static final Storage.LoadSave<StockNameType, String> ETF =
		new Storage.LoadSave.Impl<>(StockNameType.class,  o -> o.stockCode, storage, "etf.csv");
	// etn
	public static final Storage.LoadSave<StockNameType, String> ETN =
			new Storage.LoadSave.Impl<>(StockNameType.class,  o -> o.stockCode, storage, "etn.csv");
	// foreign stock
	public static final Storage.LoadSave<StockNameType, String> ForeignStock =
		new Storage.LoadSave.Impl<>(StockNameType.class,  o -> o.stockCode, storage, "foreign-stock.csv");
	// infra fund
	public static final Storage.LoadSave<StockNameType, String> InfraFund =
		new Storage.LoadSave.Impl<>(StockNameType.class,  o -> o.stockCode, storage, "infra-fund.csv");
	// listing
	public static final Storage.LoadSave<ListingType, String> Listing =
		new Storage.LoadSave.Impl<>(ListingType.class,  o -> o.stockCode, storage, "listing.csv");
	// reit
	public static final Storage.LoadSave<StockNameType, String> REIT =
		new Storage.LoadSave.Impl<>(StockNameType.class,  o -> o.stockCode, storage, "reit.csv");
	// stock detail
	public static final Storage.LoadSave<StockDetailType, String> StockDetail =
		new Storage.LoadSave.Impl<>(StockDetailType.class,  o -> o.stockCode, storage, "stock-detail.csv");
		
	// stock div jpx
	public static final Storage.LoadSave2<DailyValue, ChronoLocalDate> StockDivJPX =
		new Storage.LoadSave2.Impl<>(DailyValue.class, o -> o.date, storage, "stock-div-jpx", o -> o + ".csv");
	
	// stock info jpx
	public static final Storage.LoadSave<StockInfoJPType, String> StockInfoJPX =
		new Storage.LoadSave.Impl<>(StockInfoJPType.class,  o -> o.stockCode, storage, "stock-info-jpx.csv");

	// stock price jpxs
	public static final Storage.LoadSave2<OHLCV, ChronoLocalDate> StockPriceJPX =
		new Storage.LoadSave2.Impl<>(OHLCV.class, o -> o.date, storage, "stock-price-jpx", o -> o + ".csv");
}

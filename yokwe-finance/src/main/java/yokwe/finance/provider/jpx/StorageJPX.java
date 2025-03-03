package yokwe.finance.provider.jpx;

import java.time.chrono.ChronoLocalDate;

import yokwe.finance.Storage;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.OHLCV;
import yokwe.finance.type.StockInfoJPType;

public class StorageJPX {
	public static final Storage storage = Storage.provider.jpx;
	
	// etf
	public static final Storage.LoadSave<StockNameType, String> ETF =
		new Storage.LoadSave.Impl<>(StockNameType.class,  StockNameType::getKey, storage, "etf.csv");
	// etn
	public static final Storage.LoadSave<StockNameType, String> ETN =
			new Storage.LoadSave.Impl<>(StockNameType.class,  StockNameType::getKey, storage, "etn.csv");
	// foreign stock
	public static final Storage.LoadSave<StockNameType, String> ForeignStock =
		new Storage.LoadSave.Impl<>(StockNameType.class,  StockNameType::getKey, storage, "foreign-stock.csv");
	// infra fund
	public static final Storage.LoadSave<StockNameType, String> InfraFund =
		new Storage.LoadSave.Impl<>(StockNameType.class,  StockNameType::getKey, storage, "infra-fund.csv");
	// listing
	public static final Storage.LoadSave<ListingType, String> Listing =
		new Storage.LoadSave.Impl<>(ListingType.class,  o -> o.stockCode, storage, "listing.csv");
	// reit
	public static final Storage.LoadSave<StockNameType, String> REIT =
		new Storage.LoadSave.Impl<>(StockNameType.class,  StockNameType::getKey, storage, "reit.csv");
		
	// stock div jpx
	public static final Storage.LoadSave2<DailyValue, ChronoLocalDate> StockDivJPX =
		new Storage.LoadSave2.Impl<>(DailyValue.class, DailyValue::getKey, storage, "stock-div-jpx", o -> o + ".csv");
	
	// stock info jpx
	public static final Storage.LoadSave<StockInfoJPType, String> StockInfoJPX =
		new Storage.LoadSave.Impl<>(StockInfoJPType.class,  StockInfoJPType::getKey, storage, "stock-info-jpx.csv");

	// stock price jpxs
	public static final Storage.LoadSave2<OHLCV, ChronoLocalDate> StockPriceJPX =
		new Storage.LoadSave2.Impl<>(OHLCV.class, OHLCV::getKey, storage, "stock-price-jpx", o -> o + ".csv");
	
	// stock split
	public static final Storage.LoadSave<StockSplitType, String> StockSplit =
		new Storage.LoadSave.Impl<>(StockSplitType.class, StockSplitType::getKey, storage, "stock-split.csv");
	
	// stock list
	public static final Storage.LoadSave<StockListType, String> StockList =
		new Storage.LoadSave.Impl<>(StockListType.class, StockListType::getKey, storage, "stockList.csv");
	public static final Storage.LoadSaveText2 StockListJSON =
		new Storage.LoadSaveText2.Impl(storage, "stockList", o -> o + ".json");
	// stock detail
	public static final Storage.LoadSaveText2 StockDetailJSON =
		new Storage.LoadSaveText2.Impl(storage, "stockDetail",  o -> o + ".json");
	
}

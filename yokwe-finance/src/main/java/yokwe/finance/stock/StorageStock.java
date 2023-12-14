package yokwe.finance.stock;

import java.time.chrono.ChronoLocalDate;

import yokwe.finance.Storage;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.OHLCV;
import yokwe.finance.type.StockInfoJPType;
import yokwe.finance.type.StockInfoUSType;

public class StorageStock {
	public static final Storage storage = Storage.stock;
	
	// stock-div-XX
	public static final Storage.LoadSave2<DailyValue, ChronoLocalDate> StockDivJP =
		new Storage.LoadSave2.Impl<>(DailyValue.class, o -> o.date, storage, "stock-div-jp", o -> o + ".csv");
	public static final Storage.LoadSave2<DailyValue, ChronoLocalDate> StockDivUS =
		new Storage.LoadSave2.Impl<>(DailyValue.class, o -> o.date, storage, "stock-div-us", o -> o + ".csv");
	
	// stock-info-jp
	public static final Storage.LoadSave<StockInfoJPType, String> StockInfoJP =
		new Storage.LoadSave.Impl<>(StockInfoJPType.class,  o -> o.stockCode, storage, "stock-info-jp.csv");
	// stock-info-us
	public static final Storage.LoadSave<StockInfoUSType, String> StockInfoUSAll =
		new Storage.LoadSave.Impl<>(StockInfoUSType.class,  o -> o.stockCode, storage, "stock-info-us-all.csv");
	public static final Storage.LoadSave<StockInfoUSType, String> StockInfoUSTrading =
		new Storage.LoadSave.Impl<>(StockInfoUSType.class,  o -> o.stockCode, storage, "stock-info-us-trading.csv");
		
	// stock-price-XX
	public static final Storage.LoadSave2<OHLCV, ChronoLocalDate> StockPriceJP =
		new Storage.LoadSave2.Impl<>(OHLCV.class, o -> o.date, storage, "stock-price-jp", o -> o + ".csv");
	public static final Storage.LoadSave2<OHLCV, ChronoLocalDate> StockPriceUS =
		new Storage.LoadSave2.Impl<>(OHLCV.class, o -> o.date, storage, "stock-price-us", o -> o + ".csv");
}

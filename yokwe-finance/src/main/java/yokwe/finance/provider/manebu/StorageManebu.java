package yokwe.finance.provider.manebu;

import java.time.chrono.ChronoLocalDate;

import yokwe.finance.Storage;
import yokwe.finance.type.DailyValue;

public class StorageManebu {
	public static final Storage storage = Storage.provider.manebu;
	
	// reit-div
	public static final Storage.LoadSave2<DailyValue, ChronoLocalDate> ETFDiv =
		new Storage.LoadSave2.Impl<>(DailyValue.class, o -> o.date, storage, "etf-div", o -> o + ".csv");
	
	// reit-info-XX
	public static final Storage.LoadSave<ETFInfoType, String> ETFInfo =
		new Storage.LoadSave.Impl<>(ETFInfoType.class,  o -> o.stockCode, storage, "etf-info.csv");
}

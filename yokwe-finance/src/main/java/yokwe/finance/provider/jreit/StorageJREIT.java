package yokwe.finance.provider.jreit;

import java.time.chrono.ChronoLocalDate;

import yokwe.finance.Storage;
import yokwe.finance.type.DailyValue;

public class StorageJREIT {
	public static final Storage storage = Storage.provider.jreit;
	
	// reit-div
	public static final Storage.LoadSave2<DailyValue, ChronoLocalDate> JREITDiv =
		new Storage.LoadSave2.Impl<>(DailyValue.class, o -> o.date, storage, "jreit-div", o -> o + ".csv");
	
	// reit-info-XX
	public static final Storage.LoadSave<JREITInfoType, String> JREITInfo =
		new Storage.LoadSave.Impl<>(JREITInfoType.class,  o -> o.stockCode, storage, "jreit-info.csv");
}

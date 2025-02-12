package yokwe.finance.fund;

import java.time.chrono.ChronoLocalDate;

import yokwe.finance.Storage;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.FundInfoJP;
import yokwe.finance.type.FundPriceJP;
import yokwe.finance.type.NISAInfoType;

public class StorageFund {
	private static final Storage storage = Storage.fund;
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
	
	// fund-div
	public static final Storage.LoadSave2<DailyValue, ChronoLocalDate> FundDiv =
		new Storage.LoadSave2.Impl<>(DailyValue.class, o -> o.date, storage, "fund-div", o -> o + ".csv");
	
	// fund-info
	public static final Storage.LoadSave<FundInfoJP, String> FundInfo =
			new Storage.LoadSave.Impl<>(FundInfoJP.class,  o -> o.isinCode, storage, "fund-info.csv");
		
	// fund-price
	public static final Storage.LoadSave2<FundPriceJP, ChronoLocalDate> FundPrice =
		new Storage.LoadSave2.Impl<>(FundPriceJP.class, o -> o.date, storage, "fund-price", o -> o + ".csv");
	
	// nisa-info
	public static final Storage.LoadSave<NISAInfoType, String> NISAInfo =
		new Storage.LoadSave.Impl<>(NISAInfoType.class,  o -> o.isinCode, storage, "nisa-info.csv");
}

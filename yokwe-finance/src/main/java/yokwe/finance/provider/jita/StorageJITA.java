package yokwe.finance.provider.jita;

import java.time.chrono.ChronoLocalDate;

import yokwe.finance.Storage;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.FundInfoJP;
import yokwe.finance.type.FundPriceJP;

public class StorageJITA {
	public static final Storage storage = Storage.provider.jita;
	
	// fund-div
	public static final Storage.LoadSave2<DailyValue, ChronoLocalDate> FundDivJITA =
		new Storage.LoadSave2.Impl<>(DailyValue.class, o -> o.date, storage, "fund-div-jita", o -> o + ".csv");
	
	// fund-info
	public static final Storage.LoadSave<FundInfoJP, String> FundInfoJITA =
			new Storage.LoadSave.Impl<>(FundInfoJP.class,  o -> o.stockCode, storage, "fund-info-jita.csv");
		
	// fund-price
	public static final Storage.LoadSave2<FundPriceJP, ChronoLocalDate> FundPriceJITA =
		new Storage.LoadSave2.Impl<>(FundPriceJP.class, o -> o.date, storage, "fund-price-jita", o -> o + ".csv");
}

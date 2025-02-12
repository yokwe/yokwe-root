package yokwe.finance.provider.jita;

import java.time.chrono.ChronoLocalDate;

import yokwe.finance.Storage;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.FundInfoJP;
import yokwe.finance.type.FundPriceJP;
import yokwe.finance.type.NISAInfoType;

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
	
	// nisa-info
	public static final Storage.LoadSave<NISAInfoType, String> NISAInfoJITA =
		new Storage.LoadSave.Impl<>(NISAInfoType.class,  o -> o.isinCode, storage, "nisa-info-jita.csv");
	
	// listed_fund_for_investor
	public static final Storage.LoadSave<UpdateNISAInfoJITA.ListedFundForInvestor, String> ListedFundForInvestor =
		new Storage.LoadSave.Impl<>(UpdateNISAInfoJITA.ListedFundForInvestor.class,  o -> o.stockCode, storage, "listed_fund_for_investor.csv");
	// unlisted_fund_for_investor
	public static final Storage.LoadSave<UpdateNISAInfoJITA.UnlistedFundForInvestor, String> UnlistedFundForInvestor =
		new Storage.LoadSave.Impl<>(UpdateNISAInfoJITA.UnlistedFundForInvestor.class,  o -> o.fundCode, storage, "unlisted_fund_for_investor.csv");
}

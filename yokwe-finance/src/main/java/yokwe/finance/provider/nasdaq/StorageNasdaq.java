package yokwe.finance.provider.nasdaq;

import yokwe.finance.Storage;
import yokwe.finance.type.CompanyInfoType;
import yokwe.finance.type.StockInfoUSType;

public class StorageNasdaq {
	public static final Storage storage = Storage.provider.nasdaq;
	
	// stock-info-nasdaq
	public static final Storage.LoadSave<StockInfoUSType, String> StockInfoNasdaq =
		new Storage.LoadSave.Impl<>(StockInfoUSType.class,  o -> o.stockCode, storage, "stock-info-nasdaq.csv");
	
	// company-info-nasdaq
	public static final Storage.LoadSave<CompanyInfoType, String> CompanyInfoNasdaq =
		new Storage.LoadSave.Impl<>(CompanyInfoType.class,  o -> o.stockCode, storage, "company-info-nasdaq.csv");
	
	// nasdqqlisted
	public static final Storage.LoadSave<NasdaqListedType, String> NasdaqListed =
		new Storage.LoadSave.Impl<>(NasdaqListedType.class,  o -> o.symbol, storage, "nasdaqlisted.csv");
	
	public static final String NasdaqListed_TXT = storage.getPath("nasdaqlisted.txt");
	
	// otherlisted
	public static final Storage.LoadSave<OtherListedType, String> OtherListed =
		new Storage.LoadSave.Impl<>(OtherListedType.class,  o -> o.symbol, storage, "otherlisted.csv");
	
	public static final String OtherListed_TXT = storage.getPath("otherlisted.txt");

}

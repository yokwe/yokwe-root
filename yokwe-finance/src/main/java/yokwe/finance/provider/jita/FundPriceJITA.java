package yokwe.finance.provider.jita;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.FundPriceJP;
import yokwe.util.ListUtil;

public class FundPriceJITA {
	private static final String PREFIX = "fund-price-jita";
	
	private static final Storage storage = Storage.provider_jita;
	
	public static String getPath() {
		return storage.getPath(PREFIX);
	}
	public static String getPath(String stockCode) {
		return storage.getPath(PREFIX, stockCode + ".csv");
	}
	
	public static void save(String stockCode, Collection<FundPriceJP> collection) {
		ListUtil.save(FundPriceJP.class, getPath(stockCode), collection);
	}
	public static void save(String stockCode, List<FundPriceJP> list) {
		ListUtil.save(FundPriceJP.class, getPath(stockCode), list);
	}
	
	public static List<FundPriceJP> getList(String stockCode) {
		return ListUtil.getList(FundPriceJP.class, getPath(stockCode));
	}
	public static Map<LocalDate, FundPriceJP> getMap(String stockCode) {
		return ListUtil.checkDuplicate(getList(stockCode), o -> o.date);
	}	
}

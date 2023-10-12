package yokwe.finance.fund;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.FundPriceJP;
import yokwe.util.ListUtil;

public class FundPrice {
	private static final String PREFIX = "fund-price";
	
	private static final Storage storage = Storage.fund;
	
	public static String getPath() {
		return storage.getPath(PREFIX);
	}
	public static String getPath(String isinCode) {
		return storage.getPath(PREFIX, isinCode + ".csv");
	}
	
	public static void save(String isinCode, Collection<FundPriceJP> collection) {
		ListUtil.save(FundPriceJP.class, getPath(isinCode), collection);
	}
	public static void save(String isinCode, List<FundPriceJP> list) {
		ListUtil.save(FundPriceJP.class, getPath(isinCode), list);
	}
	
	public static List<FundPriceJP> getList(String isinCode) {
		return ListUtil.getList(FundPriceJP.class, getPath(isinCode));
	}
	public static Map<LocalDate, FundPriceJP> getMap(String isinCode) {
		return ListUtil.checkDuplicate(getList(isinCode), o -> o.date);
	}	
}

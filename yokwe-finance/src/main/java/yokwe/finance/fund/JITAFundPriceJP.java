package yokwe.finance.fund;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.FundPriceJP;
import yokwe.util.ListUtil;

public class JITAFundPriceJP {
	private static final String PREFIX = "jita-fund-price-jp";
	public static String getPath() {
		return Storage.Fund.getPath(PREFIX);
	}
	public static String getPath(String stockCode) {
		return Storage.Fund.getPath(PREFIX, stockCode + ".csv");
	}
	
	private static final String PREFIX_DELIST = PREFIX + "-delist";
	public static String getPathDelist() {
		return Storage.Fund.getPath(PREFIX_DELIST);
	}

	public static void save(String stockCode, Collection<FundPriceJP> collection) {
		String path = getPath(stockCode);
		ListUtil.save(FundPriceJP.class, path, collection);
	}
	public static void save(String stockCode, List<FundPriceJP> list) {
		String path = getPath(stockCode);
		ListUtil.save(FundPriceJP.class, path, list);
	}
	
	public static List<FundPriceJP> getList(String stockCode) {
		String path = getPath(stockCode);
		return ListUtil.getList(FundPriceJP.class, path);
	}
	public static Map<LocalDate, FundPriceJP> getMap(String stockCode) {
		var list = getList(stockCode);
		return ListUtil.checkDuplicate(list, o -> o.date);
	}	
}

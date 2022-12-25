package yokwe.stock.jp.edinet;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.jp.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.ListUtil;
import yokwe.util.UnexpectedException;

public class EDINETInfo implements Comparable<EDINETInfo> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final String PATH_FILE = Storage.EDINET.getPath("edinet-info.csv");
	public static final String getPath() {
		return PATH_FILE;
	}
	
	public static final void save(List<EDINETInfo> list) {
		ListUtil.save(EDINETInfo.class, getPath(), list);
	}
	
	public static List<EDINETInfo> load() {
		return ListUtil.load(EDINETInfo.class, getPath());
	}
	
	private static List<EDINETInfo> list = null;
	public static List<EDINETInfo> getList() {
		if (list == null) {
			list = ListUtil.getList(EDINETInfo.class, getPath());
		}
		return list;
	}
	
	private static Map<String, EDINETInfo> map = null;
	//                 edinetCode
	public static Map<String, EDINETInfo> getMap() {
		if (map == null) {
			map = new TreeMap<>();
			for(EDINETInfo e: getList()) {
				String key = e.edinetCode;
				if (map.containsKey(key)) {
					logger.error("Duplicate edinetCode");
					logger.error("  old  {}", map.get(key));
					logger.error("  new  {}", e);
					throw new UnexpectedException("Duplicate edinetCode");
				} else {
					map.put(key, e);
				}
			}
		}
		return map;
	}
	public static EDINETInfo get(String edinetCode) {
		Map<String, EDINETInfo> map = getMap();
		if (map.containsKey(edinetCode)) {
			return map.get(edinetCode);
		} else {
			return null;
		}
	}
	
	private static Map<String, EDINETInfo> stockCodeMap = null;
	//                 stockCode
	public static Map<String, EDINETInfo> getStockCodeMap() {
		if (stockCodeMap == null) {
			stockCodeMap = new TreeMap<>();
			for(EDINETInfo e: getList()) {
				String key = e.stockCode;
				if (key == null || key.isEmpty()) continue;
				if (stockCodeMap.containsKey(key)) {
					logger.error("Duplicate edinetCode");
					logger.error("  old  {}", stockCodeMap.get(key));
					logger.error("  new  {}", e);
					throw new UnexpectedException("Duplicate edinetCode");
				} else {
					stockCodeMap.put(key, e);
				}
			}
		}
		return stockCodeMap;
	}
	public static EDINETInfo getFromStockCode(String stockCode) {
		Map<String, EDINETInfo> map = getStockCodeMap();
		if (map.containsKey(stockCode)) {
			return map.get(stockCode);
		} else {
			return null;
		}
	}

	@CSVUtil.ColumnName("ＥＤＩＮＥＴコード")
	public String edinetCode;
	
	@CSVUtil.ColumnName("提出者種別")
	public String category;

	@CSVUtil.ColumnName("上場区分")
	public String listingType;

	@CSVUtil.ColumnName("連結の有無")
	public String hasConsolidateSubsidary;

	@CSVUtil.ColumnName("資本金")
	public String capital;
	
	@CSVUtil.ColumnName("決算日")
	public String closingDate;
	
	@CSVUtil.ColumnName("提出者名")
	public String name;
	
	@CSVUtil.ColumnName("提出者名（英字）")
	public String nameEnglish;
	
	@CSVUtil.ColumnName("提出者名（ヨミ）")
	public String nameRuby;
	
	@CSVUtil.ColumnName("所在地")
	public String address;
	
	@CSVUtil.ColumnName("提出者業種")
	public String sector;
	
	@CSVUtil.ColumnName("証券コード")
	public String stockCode; // can be NNNN0 or empty
	
	@CSVUtil.ColumnName("提出者法人番号")
	public String corporateCode;
	
	@Override
	public String toString() {
		return String.format("%s %s %s %s %s %s %s %s %s %s %s %s %s",
				edinetCode, category, listingType, hasConsolidateSubsidary, capital, closingDate,
				name, nameEnglish, nameRuby, address, sector, stockCode, corporateCode);
	}
	
	@Override
	public int compareTo(EDINETInfo that) {
		int ret = this.edinetCode.compareTo(that.edinetCode);
		return ret;
	}
}

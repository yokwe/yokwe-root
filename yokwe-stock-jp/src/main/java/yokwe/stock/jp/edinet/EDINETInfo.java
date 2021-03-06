package yokwe.stock.jp.edinet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;

public class EDINETInfo implements Comparable<EDINETInfo> {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EDINETInfo.class);

	private static final String PATH_DATA = getPath();	
	public static String getPath() {
		return EDINET.getPath("edinet-info.csv");
	}
	
	public static final void save(List<EDINETInfo> list) {
		// Sort before write
		Collections.sort(list);
		CSVUtil.write(EDINETInfo.class).file(PATH_DATA, list);
	}
	
	public static List<EDINETInfo> load() {
		return CSVUtil.read(EDINETInfo.class).file(PATH_DATA);
	}
	
	private static List<EDINETInfo> list = null;
	public static List<EDINETInfo> getList() {
		if (list == null) {
			list = CSVUtil.read(EDINETInfo.class).file(PATH_DATA);
			if (list == null) {
				list = new ArrayList<>();
			}
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

	@CSVUtil.ColumnName("???????????????????????????")
	public String edinetCode;
	
	@CSVUtil.ColumnName("???????????????")
	public String category;

	@CSVUtil.ColumnName("????????????")
	public String listingType;

	@CSVUtil.ColumnName("???????????????")
	public String hasConsolidateSubsidary;

	@CSVUtil.ColumnName("?????????")
	public String capital;
	
	@CSVUtil.ColumnName("?????????")
	public String closingDate;
	
	@CSVUtil.ColumnName("????????????")
	public String name;
	
	@CSVUtil.ColumnName("????????????????????????")
	public String nameEnglish;
	
	@CSVUtil.ColumnName("????????????????????????")
	public String nameRuby;
	
	@CSVUtil.ColumnName("?????????")
	public String address;
	
	@CSVUtil.ColumnName("???????????????")
	public String sector;
	
	@CSVUtil.ColumnName("???????????????")
	public String stockCode; // can be NNNN0 or empty
	
	@CSVUtil.ColumnName("?????????????????????")
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

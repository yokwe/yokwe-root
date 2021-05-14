package yokwe.stock.jp.fsa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.util.CSVUtil;

public class Fund implements Comparable<Fund> {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Fund.class);

	private static final String PATH_DATA = getPath();
	
	public static final String getPath() {
		return FSA.getPath("fund.csv");
	}
	
	public static final void save(List<Fund> list) {
		// Sort before write
		Collections.sort(list);
		CSVUtil.write(Fund.class).file(PATH_DATA, list);
	}
	
	public static List<Fund> load() {
		return CSVUtil.read(Fund.class).file(PATH_DATA);
	}
	
	private static List<Fund> list = null;
	public static List<Fund> getList() {
		if (list == null) {
			list = CSVUtil.read(Fund.class).file(PATH_DATA);
			if (list == null) {
				list = new ArrayList<>();
			}
		}
		return list;
	}
	
	private static Map<String, Fund> map = null;
	//                 fundCode
	public static Map<String, Fund> getMap() {
		if (map == null) {
			map = new TreeMap<>();
			for(Fund e: getList()) {
				String key = e.fundCode;
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
	public static Fund get(String edinetCode) {
		Map<String, Fund> map = getMap();
		if (map.containsKey(edinetCode)) {
			return map.get(edinetCode);
		} else {
			return null;
		}
	}
	
	private static Map<String, Fund> nameMap = null;
	//                 stockCode-fundName
	public static Map<String, Fund> getStockCodeNameMap() {
		if (nameMap == null) {
			nameMap = new TreeMap<>();
			for(Fund e: getList()) {
				if (e.stockCode.isEmpty()) continue;
				
				String key = String.format("%s-%s", e.stockCode, e.fundName);
				if (key == null || key.isEmpty()) continue;
				if (nameMap.containsKey(key)) {
					logger.error("Duplicate edinetCode  {}", key);
					logger.error("  old  {}", nameMap.get(key));
					logger.error("  new  {}", e);
					throw new UnexpectedException("Duplicate edinetCode");
				} else {
					nameMap.put(key, e);
				}
			}
		}
		return nameMap;
	}
	public static Fund getFromStockCodeFundName(String stockCode, String fundName) {
		Map<String, Fund> map = getStockCodeNameMap();
		String key = String.format("%s-%s", stockCode, fundName);
		if (map.containsKey(key)) {
			return map.get(key);
		} else {
			return null;
		}
	}


	
	@CSVUtil.ColumnName("ファンドコード")
	public String fundCode;
	
	@CSVUtil.ColumnName("証券コード")
	public String stockCode; // can be NNNN0 or empty

	@CSVUtil.ColumnName("ファンド名")
	public String fundName;

	@CSVUtil.ColumnName("ファンド名（ヨミ）")
	public String fundNameRuby;

	@CSVUtil.ColumnName("特定有価証券区分名")
	public String category;
	
	@CSVUtil.ColumnName("特定期1")
	public String specialDate1;
	
	@CSVUtil.ColumnName("特定期2")
	public String specialDate2;
	
	@CSVUtil.ColumnName("ＥＤＩＮＥＴコード")
	public String edinetCode;
	
	@CSVUtil.ColumnName("発行者名")
	public String submitterName;
	
	
	@Override
	public String toString() {
		return String.format("%s %s %s %s %s %s %s %s %s",
				fundCode, stockCode, fundName, fundNameRuby, category, specialDate1, specialDate2, edinetCode, submitterName);
	}
	
	@Override
	public int compareTo(Fund that) {
		int ret = this.fundCode.compareTo(that.fundCode);
		return ret;
	}
}

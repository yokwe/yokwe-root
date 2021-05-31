package yokwe.stock.jp.sony;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;

public class Fund implements Comparable<Fund> {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Fund.class);

	public static final String PATH_FILE = "tmp/data/sony/fund.csv"; // FIXME

	private static List<Fund> list = null;
	public static List<Fund> getList() {
		if (list == null) {
			list = CSVUtil.read(Fund.class).file(PATH_FILE);
			if (list == null) {
				list = new ArrayList<>();
			}
		}
		return list;
	}
	
	private static Map<String, Fund> map = null;
	public static Map<String, Fund> getMap() {
		if (map == null) {
			map = new TreeMap<>();
			for(var e: getList()) {
				map.put(e.isinCode, e);
			}
		}
		return map;
	}

	public static void save(Collection<Fund> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<Fund> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(Fund.class).file(PATH_FILE, list);
	}

	public static enum Region {
		WORLD        ("01", "世界全体"),
		JAPAN        ("10", "日本"),
		NORTH_AMERICA("20", "北米"),
		EUROPE       ("30", "欧州"),
		ASIA         ("40", "アジア"),
		OCEANIA      ("50", "オセアニア"),
		OTHER        ("60", "その他");
		
		public final String code;
		public final String name;
		Region(String code, String name) {
			this.code = code;
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
		
		public static Region get(String code) {
			for(Region e: Region.values()) {
				if (e.code.equals(code)) return e;
			}
			logger.error("Unexpected code {}!", code);
			throw new UnexpectedException("Unexpected value");
		}
	}
	
	public static enum Target {
		STOCK   ("01", "株式"),
		BOND    ("10", "債券"),
		REIT    ("20", "REIT"),
		BALANCE ("30", "バランス"),
		COMODITY("40", "コモディティ・資源");
		
		public final String code;
		public final String name;
		Target(String code, String name) {
			this.code = code;
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
		
		public static Target get(String code) {
			for(Target e: Target.values()) {
				if (e.code.equals(code)) return e;
			}
			logger.error("Unexpected code {}!", code);
			throw new UnexpectedException("Unexpected value");
		}
	}
	
	public LocalDateTime dateTime; // 2020-04-14 23:00:33
	public String        isinCode; // IE0030804631
	//
	public String        category; // 国際REIT型
	public String        fundName; // ワールド・リート・オープン（毎月決算型）
    public Company       company;  // 081
    public String        divFreq;  // 12
    public Region        region;   // 01
    public Target        target;   // 20
    public Currency      currency; // JPY

	public Fund() {
		this.dateTime = null;
		this.isinCode = null;
		//
	    this.category = null;
	    this.fundName = null;
	    this.company  = null;
	    this.divFreq  = null;
	    this.region   = null;
	    this.target   = null;
	    this.currency = null;
	}
	
	@Override
	public int compareTo(Fund that) {
		int ret = this.dateTime.compareTo(that.dateTime);
		if (ret == 0) ret = this.isinCode.compareTo(that.isinCode);
		return ret;
	}
}

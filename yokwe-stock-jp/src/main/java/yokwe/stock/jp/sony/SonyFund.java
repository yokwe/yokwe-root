package yokwe.stock.jp.sony;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.UnexpectedException;

public class SonyFund implements Comparable<SonyFund> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final String PATH_FILE = Storage.Sony.getPath("sony-fund.csv");
	public static final String getPath() {
		return PATH_FILE;
	}

	private static List<SonyFund> list = null;
	public static List<SonyFund> getList() {
		if (list == null) {
			list = ListUtil.getList(SonyFund.class, getPath());
		}
		return list;
	}

	private static Map<String, SonyFund> map = null;
	public static Map<String, SonyFund> getMap() {
		if (map == null) {
			var list = getList();
			map = list.stream().collect(Collectors.toMap(o -> o.isinCode, o -> o));
		}
		return map;
	}

	public static void save(Collection<SonyFund> collection) {
		ListUtil.save(SonyFund.class, getPath(), collection);
	}
	public static void save(List<SonyFund> list) {
		ListUtil.save(SonyFund.class, getPath(), list);
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
    public String        divFreq;  // 12
    public String        rating;   // - 1 2 3 4 5
    public Region        region;   // 01
    public Target        target;   // 20
    public Currency      currency; // JPY

	public SonyFund() {
		this.dateTime = null;
		this.isinCode = null;
		//
	    this.category = null;
	    this.fundName = null;
	    this.divFreq  = null;
	    this.region   = null;
	    this.target   = null;
	    this.currency = null;
	}
	
	@Override
	public int compareTo(SonyFund that) {
		int ret = this.dateTime.compareTo(that.dateTime);
		if (ret == 0) ret = this.isinCode.compareTo(that.isinCode);
		return ret;
	}
}

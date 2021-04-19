package yokwe.stock.jp.sony;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;

public class FundInfo implements Comparable<FundInfo> {
	public static final String PATH_FILE = "tmp/data/sony/fund-info.csv";

	private static List<FundInfo> list = null;
	public static List<FundInfo> getList() {
		if (list == null) {
			list = CSVUtil.read(FundInfo.class).file(PATH_FILE);
			if (list == null) {
				list = new ArrayList<>();
			}
		}
		return list;
	}
	
	private static Map<String, FundInfo> map = null;
	public static Map<String, FundInfo> getMap() {
		if (map == null) {
			map = new TreeMap<>();
			for(var e: getList()) {
				map.put(e.isinCode, e);
			}
		}
		return map;
	}

	public static void save(Collection<FundInfo> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<FundInfo> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(FundInfo.class).file(PATH_FILE, list);
	}
	
	// from Fund
	public String   isinCode;
	public Company  company;
	public String   fundName;
	public Currency currency;
	
	// var msFundCode = '2013121001';
	public String msFundCode;
	
	// 設定日
	public String inceptionDate;
	
	// 償還日
	public String redemptionDate;
	
	// 決算日
	public String closingDate;
	
	// 購入単位
	public String purchaseUnit;

	// 購入約定日
	public String purchaseDate;

	// 購入価格
	public String purchasePrice;

	// 解約単位
	public String cancelUnit;

	// 解約価格
	public String cancelPrice;

	// 解約代金支払日
	public String cancelPaymentDate;

	// 購入・解約の申込締切
	public String deadline;

	// 販売手数料（税込）
	public String salesFeeA;
	public String salesFeeB;
	public String salesFeeC;

	// 信託報酬（税込）
	public String trustFee;
	
	// 実質信託報酬（税込）
	public String realTrustFee;

	// 信託財産留保額
	public String cancelFee;

	// ファンドの特色
	public String description;
	
	
	@Override
	public int compareTo(FundInfo that) {
		int ret = this.isinCode.compareTo(that.isinCode);
		return ret;
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
}

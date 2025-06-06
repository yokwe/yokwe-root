package yokwe.stock.jp.sony;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.ToString;

public class SonyFundInfo implements Comparable<SonyFundInfo> {
	private static final String PATH_FILE = Storage.Sony.getPath("sony-fund-info.csv");
	public static final String getPath() {
		return PATH_FILE;
	}

	private static List<SonyFundInfo> list = null;
	public static List<SonyFundInfo> getList() {
		if (list == null) {
			list = ListUtil.getList(SonyFundInfo.class, getPath());
		}
		return list;
	}
	
	private static Map<String, SonyFundInfo> map = null;
	public static Map<String, SonyFundInfo> getMap() {
		if (map == null) {
			var list = getList();
			map = list.stream().collect(Collectors.toMap(o -> o.isinCode, o -> o));
		}
		return map;
	}

	public static void save(Collection<SonyFundInfo> collection) {
		ListUtil.save(SonyFundInfo.class, getPath(), collection);
	}
	public static void save(List<SonyFundInfo> list) {
		ListUtil.save(SonyFundInfo.class, getPath(), list);
	}
	
	// from Fund
	public String   isinCode;
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
	public int compareTo(SonyFundInfo that) {
		int ret = this.isinCode.compareTo(that.isinCode);
		return ret;
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
}

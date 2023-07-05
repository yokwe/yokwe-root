package yokwe.stock.jp.nomura;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.toushin.Seller;
import yokwe.stock.jp.toushin.UpdateStats;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class NomuraFund implements Comparable<NomuraFund> {
	public static final String SELLER_NAME = "野村證券";
	
	private static final String PATH_FILE = Storage.Nomura.getPath("nomura-fund.csv");
	public static final String getPath() {
		return PATH_FILE;
	}

	private static List<NomuraFund> list = null;
	public static List<NomuraFund> getList() {
		if (list == null) {
			list = ListUtil.getList(NomuraFund.class, getPath());
		}
		return list;
	}

	private static Map<String, NomuraFund> map = null;
	public static Map<String, NomuraFund> getMap() {
		if (map == null) {
			var list = getList();
			map = list.stream().collect(Collectors.toMap(o -> o.isinCode, o -> o));
		}
		return map;
	}

	public static void save(Collection<NomuraFund> collection) {
		ListUtil.save(NomuraFund.class, getPath(), collection);
	}
	public static void save(List<NomuraFund> list) {
		ListUtil.save(NomuraFund.class, getPath(), list);
	}
	
	public static BigDecimal getSalesFee(String isinCode, BigDecimal defaultValue) {
		BigDecimal salesFee = Seller.getSalesFee(isinCode, SELLER_NAME);
		return salesFee == null ? defaultValue : salesFee.multiply(UpdateStats.CONSUMPTION_TAX_RATE);
	}

	public String isinCode; // IE0030804631
	public String fundCode; // IE0030804631
	public String name;
	
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(NomuraFund that) {
		return this.isinCode.compareTo(that.isinCode);
	}

}

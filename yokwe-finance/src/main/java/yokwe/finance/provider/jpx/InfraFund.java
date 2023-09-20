package yokwe.finance.provider.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class InfraFund implements Comparable<InfraFund> {
	private static final String PATH_FILE = Storage.Provider.JPX.getPath("infra-fund.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<InfraFund> getList() {
		return ListUtil.getList(InfraFund.class, getPath());
	}
	public static Map<String, InfraFund> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<InfraFund> collection) {
		ListUtil.save(InfraFund.class, getPath(), collection);
	}
	public static void save(List<InfraFund> list) {
		ListUtil.save(InfraFund.class, getPath(), list);
	}
	
	
	public String stockCode;
	public String name;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(InfraFund that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof InfraFund) {
			InfraFund that = (InfraFund)o;
			return 
				this.stockCode.equals(that.stockCode) &&
				this.name.equals(that.name);
		} else {
			return false;
		}
	}
}

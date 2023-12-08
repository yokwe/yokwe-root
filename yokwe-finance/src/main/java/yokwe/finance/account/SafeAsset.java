package yokwe.finance.account;

import java.util.Map;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;

public class SafeAsset implements Comparable<SafeAsset> {
	private static final String PATH = Storage.account.base.getPath("safe-asset.csv");
	public static final String getPath() {
		return PATH;
	}
	
	private static Map<String, String> map;
	static {
		map = ListUtil.getList(SafeAsset.class, getPath()).stream().collect(Collectors.toMap(o -> o.code, o -> o.name));
	}
	public static boolean isSafeAsset(String code) {
		return map.containsKey(code);
	}
	
	public String code;
	public String name;

	@Override
	public int compareTo(SafeAsset that) {
		return this.code.compareTo(that.code);
	}
	
	@Override
	public String toString() {
		return code;
	}
}

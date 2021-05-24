package yokwe.stock.jp.xbrl.tdnet;

import yokwe.stock.jp.xbrl.XBRL;

public class TDNET {
	private static final String DIR_BASE = XBRL.getPath("tdnet");
	public static String getPath() {
		return DIR_BASE;
	}
	public static String getPath(String path) {
		return String.format("%s/%s", DIR_BASE, path);
	}

}

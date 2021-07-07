package yokwe.stock.jp.xbrl.edinet;

import yokwe.stock.jp.xbrl.XBRL;

public class EDINET {
	private static final String DIR_BASE = XBRL.getPath("edinet");
	public static String getPath() {
		return DIR_BASE;
	}
	public static String getPath(String path) {
		return String.format("%s/%s", DIR_BASE, path);
	}

}

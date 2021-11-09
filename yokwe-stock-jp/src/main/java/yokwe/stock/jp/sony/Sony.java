package yokwe.stock.jp.sony;

import yokwe.stock.jp.Storage;

public final class Sony {
	private static final String DIR_BASE = Storage.getPath("sony");
	public static String getPath() {
		return DIR_BASE;
	}
	public static String getPath(String path) {
		return String.format("%s/%s", DIR_BASE, path);
	}

}

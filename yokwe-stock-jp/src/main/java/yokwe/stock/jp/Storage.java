package yokwe.stock.jp;

public final class Storage {
	public static final String PATH_BASE = "/mnt/stock/jp";
	
	public static String getPath(String path) {
		return String.format("%s/%s", PATH_BASE, path);
	}
	
	public static String getPath(String prefix, String path) {
		return getPath(String.format("%s/%s", prefix, path));
	}

	// MoneyBuJPX
	public static final class MoneyBuJPX {
		public static final String PREFIX = "moneybujpx";
		
		public static String getPath() {
			return Storage.getPath(PREFIX);
		}
		public static String getPath(String path) {
			return Storage.getPath(PREFIX, path);
		}
		public static String getPath(String prefix, String path) {
			return getPath(String.format("%s/%s", prefix, path));
		}
	}
	
	// REIT
	public static final class JapanREIT {
		public static final String PREFIX = "japanreit";
		
		public static String getPath() {
			return Storage.getPath(PREFIX);
		}
		public static String getPath(String path) {
			return Storage.getPath(PREFIX, path);
		}
		public static String getPath(String prefix, String path) {
			return getPath(String.format("%s/%s", prefix, path));
		}
	}
	
}

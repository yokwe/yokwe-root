package yokwe.stock.us;

import yokwe.util.SystemUtil;

public final class Storage {
	public static final String PREFIX    = "stock/us";
	public static final String PATH_BASE = SystemUtil.getMountPoint(PREFIX);

	public static String getPath(String path) {
		return String.format("%s/%s", PATH_BASE, path);
	}
	
	public static String getPath(String prefix, String path) {
		return getPath(String.format("%s/%s", prefix, path));
	}
	
	// bats
	public static class BATS {
		public static final String PREFIX = "bats";
		
		public static String getPath(String path) {
			return Storage.getPath(PREFIX, path);
		}
		public static String getPath(String prefix, String path) {
			return getPath(String.format("%s/%s", prefix, path));
		}
	}
	
	// nasdaq
	public static class NASDAQ {
		public static final String PREFIX = "nasdaq";
		
		public static String getPath(String path) {
			return Storage.getPath(PREFIX, path);
		}
		public static String getPath(String prefix, String path) {
			return getPath(String.format("%s/%s", prefix, path));
		}
	}
	
	// nyse
	public static class NYSE {
		public static final String PREFIX = "nyse";
		
		public static String getPath(String path) {
			return Storage.getPath(PREFIX, path);
		}
		public static String getPath(String prefix, String path) {
			return getPath(String.format("%s/%s", prefix, path));
		}
	}
}

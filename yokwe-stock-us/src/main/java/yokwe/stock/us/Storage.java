package yokwe.stock.us;

import yokwe.util.StorageUtil;

public final class Storage {
	public static final String PATH_BASE = StorageUtil.getPath();
	
	public static String getPath(String path) {
		return String.format("%s/%s", PATH_BASE, path);
	}
	
	public static String getPath(String prefix, String path) {
		return getPath(String.format("%s/%s", prefix, path));
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
}

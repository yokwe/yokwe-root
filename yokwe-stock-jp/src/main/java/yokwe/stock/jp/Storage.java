package yokwe.stock.jp;

public final class Storage {
	public static final String PATH_BASE = "/mnt/stock/jp";
	
	public static String getPath(String path) {
		return String.format("%s/%s", PATH_BASE, path);
	}
	
	public static String getPath(String prefix, String path) {
		return getPath(String.format("%s/%s", prefix, path));
	}

	// EDINET
	public static final class EDINET {
		public static final String PREFIX = "edinet";
		
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
	
	// JAPAN REIT
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
	
	// JASDEC
	public static final class JASDEC {
		public static final String PREFIX = "jasdec";
		
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

	// JPX
	public static final class JPX {
		public static final String PREFIX = "jpx";
		
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
	
	// Sony
	public static final class Sony {
		public static final String PREFIX = "sony";
		
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
	
	// Toushin
	public static final class Toushin {
		public static final String PREFIX = "toushin";
		
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

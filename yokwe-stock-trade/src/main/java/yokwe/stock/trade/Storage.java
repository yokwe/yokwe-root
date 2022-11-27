package yokwe.stock.trade;

public final class Storage {
	public static final String PATH_BASE = "/mnt/stock/trade";
	
	private static String getPath(String path) {
		return String.format("%s/%s", PATH_BASE, path);
	}
	private static String getPath(String prefix, String path) {
		return getPath(String.format("%s/%s", prefix, path));
	}
	
	// data
	public static final class Data {
		public static final String PREFIX = "data";
		
		public static String getPath(String path) {
			return Storage.getPath(PREFIX, path);
		}
		public static String getPath(String prefix, String path) {
			return getPath(String.format("%s/%s", prefix, path));
		}
	}
	
	// activity
	public static final class Activity {
		public static final String PREFIX = "activity";
		
		public static String getPath(String path) {
			return Storage.getPath(PREFIX, path);
		}
		public static String getPath(String prefix, String path) {
			return getPath(String.format("%s/%s", prefix, path));
		}
	}
	
	// report
	public static final class Report {
		public static final String PREFIX = "report";
		
		public static String getPath(String path) {
			return Storage.getPath(PREFIX, path);
		}
		public static String getPath(String prefix, String path) {
			return getPath(String.format("%s/%s", prefix, path));
		}
	}
	
	// gmo
	public static final class GMO {
		public static final String PREFIX = "gmo";
		
		public static String getPath(String path) {
			return Storage.getPath(PREFIX, path);
		}
		public static String getPath(String prefix, String path) {
			return getPath(String.format("%s/%s", prefix, path));
		}
	}

	// monex
	public static final class Monex {
		public static final String PREFIX = "monex";
		
		public static String getPath(String path) {
			return Storage.getPath(PREFIX, path);
		}
		public static String getPath(String prefix, String path) {
			return getPath(String.format("%s/%s", prefix, path));
		}
	}

	// sbi
	public static final class SBI {
		public static final String PREFIX = "sbi";
		
		public static String getPath(String path) {
			return Storage.getPath(PREFIX, path);
		}
		public static String getPath(String prefix, String path) {
			return getPath(String.format("%s/%s", prefix, path));
		}
	}

	// rakuten
	public static final class Rakuten {
		public static final String PREFIX = "rakuten";
		
		public static String getPath(String path) {
			return Storage.getPath(PREFIX, path);
		}
		public static String getPath(String prefix, String path) {
			return getPath(String.format("%s/%s", prefix, path));
		}
	}

}

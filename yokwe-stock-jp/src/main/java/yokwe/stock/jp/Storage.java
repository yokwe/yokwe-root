package yokwe.stock.jp;

import yokwe.util.SystemUtil;

public final class Storage {
	public static final String PREFIX    = "stock/jp";
	public static final String PATH_BASE = SystemUtil.getMountPoint(PREFIX);
	
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
	
	// TDNET
	public static final class TDNET {
		public static final String PREFIX = "tdnet";
		
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
	
	// XBRL
	public static final class XBRL {
		public static final String PREFIX = "xbrl";
		
		public static String getPath() {
			return Storage.getPath(PREFIX);
		}
		public static String getPath(String path) {
			return Storage.getPath(PREFIX, path);
		}
		public static String getPath(String prefix, String path) {
			return getPath(String.format("%s/%s", prefix, path));
		}
		
		// XBRL EDINET
		public static final class EDINET {
			public static final String PREFIX = "edinet";
			
			public static String getPath() {
				return Storage.XBRL.getPath(PREFIX);
			}
			public static String getPath(String path) {
				return Storage.XBRL.getPath(PREFIX, path);
			}
			public static String getPath(String prefix, String path) {
				return getPath(String.format("%s/%s", prefix, path));
			}
		}
		
		// XBRL TDNET
		public static final class TDNET {
			public static final String PREFIX = "tdnet";
			
			public static String getPath() {
				return Storage.XBRL.getPath(PREFIX);
			}
			public static String getPath(String path) {
				return Storage.XBRL.getPath(PREFIX, path);
			}
			public static String getPath(String prefix, String path) {
				return getPath(String.format("%s/%s", prefix, path));
			}
		}
	}
	
	// Nikkei
	public static final class Nikkei {
		public static final String PREFIX = "nikkei";
		
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
	
	// Nikko
	public static final class Nikko {
		public static final String PREFIX = "nikko";
		
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
	
	// Nomura
	public static final class Nomura {
		public static final String PREFIX = "nomura";
		
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
	
	// GMO
	public static final class GMO {
		public static final String PREFIX = "gmo";
		
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

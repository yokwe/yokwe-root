package yokwe.finance;

import yokwe.util.SystemUtil;

public class Storage {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final String PREFIX    = "finance";
	public static final String PATH_BASE = SystemUtil.getMountPoint(PREFIX);
	
	public static String getPath(String path) {
		return String.format("%s/%s", PATH_BASE, path);
	}
	
	public static String getPath(String prefix, String path) {
		return getPath(String.format("%s/%s", prefix, path));
	}
	
	
	// Provider
	public static final class Provider {
		public static final String PREFIX = "provider";
		
		public static String getPath() {
			return Storage.getPath(PREFIX);
		}
		public static String getPath(String path) {
			return Storage.getPath(PREFIX, path);
		}
		public static String getPath(String prefix, String path) {
			return getPath(String.format("%s/%s", prefix, path));
		}
		
		// Provider jpx
		public static final class JPX {
			public static final String PREFIX = "jpx";
			
			public static String getPath() {
				return Storage.Provider.getPath(PREFIX);
			}
			public static String getPath(String path) {
				return Storage.Provider.getPath(PREFIX, path);
			}
			public static String getPath(String prefix, String path) {
				return getPath(String.format("%s/%s", prefix, path));
			}
		}
		
		// Provider jita
		public static final class JITA {
			public static final String PREFIX = "jita";
			
			public static String getPath() {
				return Storage.Provider.getPath(PREFIX);
			}
			public static String getPath(String path) {
				return Storage.Provider.getPath(PREFIX, path);
			}
			public static String getPath(String prefix, String path) {
				return getPath(String.format("%s/%s", prefix, path));
			}
		}
	}
	
	// Stock
	public static final class Stock {
		public static final String PREFIX = "stock";
		
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

	// Fund
	public static final class Fund {
		public static final String PREFIX = "fund";
		
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

	
	public static void main(String[] args) {
		logger.info("START");
		logger.info("PATH_BASE     {}", PATH_BASE);
		logger.info("Provider      {}", Storage.Provider.getPath());
		logger.info("Provider.JPX  {}", Storage.Provider.JPX.getPath());
		logger.info("STOP");
	}
}

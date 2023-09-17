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
	
	public static void main(String[] args) {
		logger.info("START");
		logger.info("PATH_BASE  {}", PATH_BASE);
		logger.info("STOP");
	}
}

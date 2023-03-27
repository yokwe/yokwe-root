package yokwe.util;

public class SystemUtil {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public  static final String MOUNT_POINT;
	static {
		String osName = System.getProperty("os.name");
		
		MOUNT_POINT = switch(osName) {
			case "FreeBSD"  -> "/mnt";
			case "Mac OS X" -> "/Volumes";
			default -> {
				logger.error("Unexpected OS_NAME");
				logger.error("  os.name {}", osName);
				throw new UnexpectedException("Unexpected OS_NAME");
			}
		};
	}
}

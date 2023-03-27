package yokwe.util;

public final class SystemUtil {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final String PROPS_MACOS_DRIVE = "yokwe.mount.macos.drive.name";
	
	private static final String OS_NAME_FREEBSD = "FreeBSD";
	private static final String OS_NAME_MACOS   = "Mac OS X";
	
	private static final String MOUNT_POINT_FREEBSD = "/mnt";
	private static final String MOUNT_POINT_MACOS   = "/Volumes";

	public  static final String MOUNT_POINT;
	static {
		String osName = System.getProperty("os.name");
		
		if (osName.equals(OS_NAME_FREEBSD)) {
			MOUNT_POINT = MOUNT_POINT_FREEBSD;
		} else if (osName.equals(OS_NAME_MACOS)) {
			String driveName = System.getProperty(PROPS_MACOS_DRIVE);
			if (driveName == null) {
				logger.error("No driveName");
				logger.error("  {}", PROPS_MACOS_DRIVE);
				throw new UnexpectedException("No driveName");
			}
			MOUNT_POINT = MOUNT_POINT_MACOS + "/" + driveName;
		} else {
			logger.error("Unexpected OS_NAME");
			logger.error("  os.name {}", osName);
			throw new UnexpectedException("Unexpected OS_NAME");
		}
		logger.info("MOUNT_POINT {}", MOUNT_POINT);
	}
	
	public static String getMountPoint(String prefix) {
		return String.format("%s/%s", MOUNT_POINT, prefix);
	}
}

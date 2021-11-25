package yokwe.stock.us;

public final class Storage {
	public static final String PATH_BASE = "/mnt/stock/us";
	
	public static String getPath(String path) {
		return String.format("%s/%s", PATH_BASE, path);
	}
	
	public static String getPath(String prefix, String path) {
		return getPath(String.format("%s/%s", prefix, path));
	}
}

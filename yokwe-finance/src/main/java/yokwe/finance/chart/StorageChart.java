package yokwe.finance.chart;

import java.io.File;

import yokwe.finance.Storage;

public class StorageChart {
	private static final Storage storage = Storage.chart;
	
	public static File getFile() {
		return storage.getFile();
	}
	public static File getFile(String path) {
		return storage.getFile(path);
	}
	public static File getFile(String prefix, String path) {
		return storage.getFile(prefix, path);
	}
	
	public static String getPath() {
		return storage.getPath();
	}
	public static String getPath(String path) {
		return storage.getPath(path);
	}
	public static String getPath(String prefix, String path) {
		return storage.getPath(prefix, path);
	}
}

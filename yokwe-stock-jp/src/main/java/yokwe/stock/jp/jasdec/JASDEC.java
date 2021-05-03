package yokwe.stock.jp.jasdec;

import yokwe.stock.jp.Storage;

public class JASDEC {
	public static final String PREFIX = "jasdec";
	
	public static final String getPath(String path) {
		return Storage.getPath(PREFIX, path);
	}
}

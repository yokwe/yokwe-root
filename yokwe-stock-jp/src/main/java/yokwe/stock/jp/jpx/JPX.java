package yokwe.stock.jp.jpx;

import yokwe.stock.jp.Storage;

public class JPX {
	public static final String PREFIX = "jpx";
	
	public static final String getPath(String path) {
		return Storage.getPath(PREFIX, path);
	}

}

package yokwe.stock.jp.edinet;

import yokwe.stock.jp.Storage;

public class EDINET {
	public static final String PREFIX = "edinet";
	
	public static final String getPath(String path) {
		return Storage.getPath(PREFIX, path);
	}

}

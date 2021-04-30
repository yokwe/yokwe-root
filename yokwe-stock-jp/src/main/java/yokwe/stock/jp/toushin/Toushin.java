package yokwe.stock.jp.toushin;

import yokwe.stock.jp.Storage;

public class Toushin {
	public static final String PREFIX = "toushin";
	
	public static final String getPath(String path) {
		return Storage.getPath(PREFIX, path);
	}

}

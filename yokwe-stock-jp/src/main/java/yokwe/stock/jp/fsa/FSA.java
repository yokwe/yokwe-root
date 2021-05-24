package yokwe.stock.jp.fsa;

import yokwe.stock.jp.Storage;

public class FSA {
	public static final String PREFIX = "fsa";
	
	public static final String getPath(String path) {
		return Storage.getPath(PREFIX, path);
	}
}

package yokwe.stock.jp.xbrl.edinet;

import yokwe.stock.jp.Storage;

public class Taxonomy {
	// https://disclosure.edinet-fsa.go.jp/E01EW/BLMainController.jsp?uji.bean=ee.bean.W1E62071.EEW1E62071Bean&uji.verb=W1E62071InitDisplay&TID=W1E62071&PID=W0EZ0001&SESSIONKEY=&lgKbn=2&dflg=0&iflg=0
	// EDINETタクソノミ
	//   00 . 全様式一括
	//     2021年版EDINETタクソノミ
	public static final String URL_ALL_20201101 = "https://disclosure.edinet-fsa.go.jp/E01EW/download?uji.verb=W1E62071TaxonomyDownload&uji.bean=ee.bean.W1E62071.EEW1E62071Bean&TID=W1E62071&PID=W1E62071&SESSIONKEY=1624352505206&downloadFileName=ALL_20201101.zip&lgKbn=2&dflg=0&iflg=0&dispKbn=1";
	
	
	public static final String PATH_DIR = Storage.XBRL.EDINET.getPath("ALL_20201101/taxonomy");
	
	public static final String getPath(String path) {
		return String.format("%s/%s", PATH_DIR, path);
	}
	
	// Directory location = /mnt/stock/jp/xbrl/edinet/ALL_20201101/taxonomy/jpcrp/2020-11-01/label
	public static final String PATH_DIR_JPCRP = Storage.XBRL.EDINET.getPath("ALL_20201101/taxonomy/jpcrp/2020-11-01/label");
}

package yokwe.stock.jp.sony;

public class MSFundCode {
	public static final String URL = "https://apl.morningstar.co.jp/webasp/sonybk/detail/JP90C0009VE0.html";
	
	public static final String getURL(String isinCode) {
		return String.format("https://apl.morningstar.co.jp/webasp/sonybk/detail/%s.html", isinCode);
	}
	// To get msFundCode from isinCode, fetch page using above getURL() and find line like "var msFundCode = '2013121001';"

	// Fund list
	//   https://moneykit.net/data/fund/SFBA1700F471.js

	// Fund basic info
	//   https://apl.morningstar.co.jp/webasp/funddataxml/basic/basic_2013121001.xml
	// Fund historical price info
	//   https://apl.morningstar.co.jp/xml/chart/funddata/2013121001.xml
}

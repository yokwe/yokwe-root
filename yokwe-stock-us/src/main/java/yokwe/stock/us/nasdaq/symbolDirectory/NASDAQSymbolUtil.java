package yokwe.stock.us.nasdaq.symbolDirectory;

public final class NASDAQSymbolUtil {
	// See page below
	//   Ticker Symbol Convention
	//   https://www.nasdaqtrader.com/trader.aspx?id=CQSsymbolconvention
	
	public static final String SUFFIX_WARRANT     = "+";
	public static final String SUFFIX_RIGHTS      = "^";
	public static final String SUFFIX_UNITS       = "=";
	public static final String SUFFIX_WHEN_ISSUED = "#";
	public static final String SUFFIX_CALLED      = "*";
	
	public static boolean isWarrant(String nasdaqSymbol) {
		return nasdaqSymbol.contains(SUFFIX_WARRANT);
	}
	public static boolean isRights(String nasdaqSymbol) {
		return nasdaqSymbol.contains(SUFFIX_RIGHTS);
	}
	public static boolean isUnits(String nasdaqSymbol) {
		return nasdaqSymbol.contains(SUFFIX_UNITS);
	}
	public static boolean isWhenIssed(String nasdaqSymbol) {
		return nasdaqSymbol.contains(SUFFIX_WHEN_ISSUED);
	}
	public static boolean isCalled(String nasdaqSymbol) {
		return nasdaqSymbol.contains(SUFFIX_CALLED);
	}
	
	public static boolean isStock(String nasdaqSymbol) {
		if (isWarrant(nasdaqSymbol)) return false;
		if (isRights(nasdaqSymbol)) return false;
		if (isUnits(nasdaqSymbol)) return false;
		
		return true;
	}
	public static String normalizedSymbol(String nasdaqSymbol) {
		return nasdaqSymbol.replace(SUFFIX_WHEN_ISSUED, "").replace(SUFFIX_CALLED, "");
	}

}

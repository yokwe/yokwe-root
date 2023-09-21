package yokwe.finance.provider.nasdaq;

import java.util.Collection;
import java.util.List;

import yokwe.finance.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class OtherListed implements Comparable<OtherListed> {
	// ACT Symbol|Security Name|Exchange|CQS Symbol|ETF|Round Lot Size|Test Issue|NASDAQ Symbol
	// A|Agilent Technologies, Inc. Common Stock|N|A|N|100|N|A
	// ZYME|Zymeworks Inc. Common Shares|N|ZYME|N|100|N|ZYME
	// File Creation Time: 1122202221:31||||||
	
	public static final String LAST_LINE_STARTS_WITH = "File Creation Time: ";
	public static final String LAST_LINE_ENDS_WITH   = "||||||";

	// See below URL for detail
	//   http://www.nasdaqtrader.com/trader.aspx?id=symboldirdefs
	
	// ACT Symbol
	//   Identifier for each security used in ACT and CTCI connectivity protocol.
	//   Typical identifiers have 1-5 character root symbol and then 1-3 characters for suffixes. Allow up to 14 characters.
	
	// Exchange	
	//   The listing stock exchange or market of a security.
	//   Allowed values are:
	//   A = NYSE MKT
	//   N = New York Stock Exchange (NYSE)
	//   P = NYSE ARCA
	//   Z = BATS Global Markets (BATS)
	//   V = Investors' Exchange, LLC (IEXG)
	
	// CQS Symbol	
	//   Identifier of the security used to disseminate data via the SIAC Consolidated Quotation System (CQS) and Consolidated Tape System (CTS) data feeds.
	//   Typical identifiers have 1-5 character root symbol and then 1-3 characters for suffixes. Allow up to 14 characters.
	
	// ETF
	//   Identifies whether the security is an exchange traded fund (ETF). Possible values:
	//   Y = Yes, security is an ETF
	//   N = No, security is not an ETF
	
	// Round Lot Size
	//   Indicates the number of shares that make up a round lot for the given security. Allow up to 6 digits.
	
	// Test Issue
	//   Indicates whether the security is a test security.
	//   Y = Yes, it is a test issue.
	//   N = No, it is not a test issue
	
	// NASDAQ Symbol
	//   Identifier of the security used to in various NASDAQ connectivity protocols and NASDAQ market data feeds.
	//   Typical identifiers have 1-5 character root symbol and then 1-3 characters for suffixes. Allow up to 14 characters.
	//   See below link for explanation
	//     https://www.nasdaqtrader.com/trader.aspx?id=CQSsymbolconvention
	
	public static final String URL = "ftp://anonymous:anonymous@ftp.nasdaqtrader.com/symboldirectory/otherlisted.txt";
	
	public static final String PATH_TXT = Storage.Provider.NASDAQ.getPath("otherlisted.txt");
	public static final String PATH_CSV = Storage.Provider.NASDAQ.getPath("otherlisted.csv");
		
	
	public static String getPath() {
		return PATH_CSV;
	}
	
	public static void save(Collection<OtherListed> collection) {
		// sanity check
		ListUtil.checkDuplicate(collection, o -> o.symbol);
		ListUtil.save(OtherListed.class, getPath(), collection);
	}
	public static void save(List<OtherListed> list) {
		// sanity check
		ListUtil.checkDuplicate(list, o -> o.symbol);
		ListUtil.save(OtherListed.class, getPath(), list);
	}
	
	public static List<OtherListed> load() {
		var list = ListUtil.load(OtherListed.class, getPath());
		// sanity check
		ListUtil.checkDuplicate(list, o -> o.symbol);
		return list;
	}
	public static List<OtherListed> getList() {
		var list = ListUtil.getList(OtherListed.class, getPath());
		// sanity check
		ListUtil.checkDuplicate(list, o -> o.symbol);
		return list;
	}
	
	
	public static final String SUFFIX_WARRANT     = "+";
	public static final String SUFFIX_RIGHTS      = "^";
	public static final String SUFFIX_UNITS       = "=";
	public static final String SUFFIX_WHEN_ISSUED = "#";
	public static final String SUFFIX_CALLED      = "*";

	
	// ACT Symbol|Security Name|Exchange|CQS Symbol|ETF|Round Lot Size|Test Issue|NASDAQ Symbol
	@CSVUtil.ColumnName("ACT Symbol")      public String actSymbol;
	@CSVUtil.ColumnName("Security Name")   public String name;
	@CSVUtil.ColumnName("Exchange")        public String exchange;
	@CSVUtil.ColumnName("CQS Symbol")      public String cqsSymbol;
	@CSVUtil.ColumnName("ETF")             public String etf;
	@CSVUtil.ColumnName("Round Lot Size")  public String roundLotSize;
	@CSVUtil.ColumnName("Test Issue")      public String testIssue;
	@CSVUtil.ColumnName("NASDAQ Symbol")   public String symbol;
		
	public boolean isTestIssue() {
		return !testIssue.equals("N");
	}
	
	public boolean isWarrant() {
		return symbol.contains(SUFFIX_WARRANT);
	}
	public boolean isRights() {
		return symbol.contains(SUFFIX_RIGHTS);
	}
	public boolean isUnits() {
		return symbol.contains(SUFFIX_UNITS);
	}
	public boolean isWhenIssed() {
		return symbol.contains(SUFFIX_WHEN_ISSUED);
	}
	public boolean isCalled() {
		return symbol.contains(SUFFIX_CALLED);
	}
	public boolean isStock() {
		if (isWarrant()) return false;
		if (isRights())  return false;
		if (isUnits())   return false;
		
		return true;
	}
	
	// Remove suffix of issued and called
	public String normalizedSymbol() {
		return symbol.replace(SUFFIX_WHEN_ISSUED, "").replace(SUFFIX_CALLED, "");
	}

	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(OtherListed that) {
		return this.actSymbol.compareTo(that.actSymbol);
	}

}

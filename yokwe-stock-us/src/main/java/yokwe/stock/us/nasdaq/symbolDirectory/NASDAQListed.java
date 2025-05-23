package yokwe.stock.us.nasdaq.symbolDirectory;

import java.util.Collection;
import java.util.List;

import yokwe.stock.us.Storage;
import yokwe.util.ListUtil;
import yokwe.util.ToString;

public class NASDAQListed implements Comparable<NASDAQListed> {
	// Symbol|Security Name|Market Category|Test Issue|Financial Status|Round Lot Size|ETF|NextShares
	// AACG|ATA Creativity Global - American Depositary Shares, each representing two common shares|G|N|N|100|N|N
	// ZYXI|Zynex, Inc. - Common Stock|Q|N|N|100|N|N
	// File Creation Time: 1122202221:30|||||||
	
	public static final String LAST_LINE_STARTS_WITH = "File Creation Time: ";
	public static final String LAST_LINE_ENDS_WITH   = "|||||||";
	
	// See below URL for detail
	//   http://www.nasdaqtrader.com/trader.aspx?id=symboldirdefs
	
	// Market Category	The category assigned to the issue by NASDAQ based on Listing Requirements. Values:
	//  Q = NASDAQ Global Select MarketSM
	// 	G = NASDAQ Global MarketSM
	// 	S = NASDAQ Capital Market
	
	// Test Issue	Indicates whether or not the security is a test security. Values:
	//   Y = yes, it is a test issue.
	//   N = no, it is not a test issue.
	
	// Financial Status	Indicates when an issuer has failed to submit its regulatory filings on a timely basis,
	// has failed to meet NASDAQ's continuing listing standards, and/or has filed for bankruptcy. Values include:
	//   D = Deficient: Issuer Failed to Meet NASDAQ Continued Listing Requirements
	//   E = Delinquent: Issuer Missed Regulatory Filing Deadline
	//   Q = Bankrupt: Issuer Has Filed for Bankruptcy
	//   N = Normal (Default): Issuer Is NOT Deficient, Delinquent, or Bankrupt.
	//   G = Deficient and Bankrupt
	//   H = Deficient and Delinquent
	//   J = Delinquent and Bankrupt
	//   K = Deficient, Delinquent, and Bankrupt
	
	// Round Lot	Indicates the number of shares that make up a round lot for the given security.
	

	public static final String URL = "ftp://ftp.nasdaqtrader.com/symboldirectory/nasdaqlisted.txt";
	
	public static final String PATH_TXT_FILE = Storage.NASDAQ.getPath("nasdaqlisted.txt");
	public static final String PATH_CSV_FILE = Storage.NASDAQ.getPath("nasdaqlisted.csv");
	
	public static final String HEADER = "Symbol|Security Name|Market Category|Test Issue|Financial Status|Round Lot Size|ETF|NextShares";
	
	public static String getPath() {
		return PATH_CSV_FILE;
	}
	
	public static void save(Collection<NASDAQListed> collection) {
		// sanity check
		ListUtil.checkDuplicate(collection, o -> o.symbol);
		ListUtil.save(NASDAQListed.class, getPath(), collection);
	}
	public static void save(List<NASDAQListed> list) {
		// Sanity check
		ListUtil.checkDuplicate(list, o -> o.symbol);
		ListUtil.save(NASDAQListed.class, getPath(), list);
	}
	
	public static List<NASDAQListed> load() {
		var list = ListUtil.load(NASDAQListed.class, getPath());
		// Sanity check
		ListUtil.checkDuplicate(list, o -> o.symbol);
		return list;
	}
	public static List<NASDAQListed> getList() {
		var list = ListUtil.getList(NASDAQListed.class, getPath());
		// Sanity check
		ListUtil.checkDuplicate(list, o -> o.symbol);
		return list;
	}
	
	// NASDAQ Integrated Platform Suffix
	// See page below
	//   Ticker Symbol Convention
	//   https://www.nasdaqtrader.com/trader.aspx?id=CQSsymbolconvention


	
	public String symbol;
	public String name;
	public String marketCategory;
	public String testIssue;
	public String financialStatus;
	public String roundLotSize;
	public String etf;
	public String nextShare;
	
	public NASDAQListed(
		String symbol,
		String name,
		String marketCategory,
		String testIssue,
		String financialStatus,
		String roundLotSize,
		String etf,
		String nextShare
		) {
		this.symbol          = symbol;
		this.name            = name;
		this.marketCategory  = marketCategory;
		this.testIssue       = testIssue;
		this.financialStatus = financialStatus;
		this.roundLotSize    = roundLotSize;
		this.etf             = etf;
		this.nextShare       = nextShare;
	}
	public NASDAQListed() {
		this(null, null, null, null, null, null, null, null);
	}
	
	public boolean isWarrant() {
		return (symbol.length() == 5 && symbol.charAt(4) == 'W');
	}
	public boolean isRights() {
		return (symbol.length() == 5 && symbol.charAt(4) == 'R');
	}
	public boolean isUnits() {
		return (symbol.length() == 5 && symbol.charAt(4) == 'U');
	}

	public boolean isStock() {
		if (isWarrant()) return false;
		if (isRights())  return false;
		if (isUnits())   return false;
		
		return true;
	}

	public boolean isTestIssue() {
		return !testIssue.equals("N");
	}

	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	@Override
	public int compareTo(NASDAQListed that) {
		return this.symbol.compareTo(that.symbol);
	}

}

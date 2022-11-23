package yokwe.stock.us.nasdaq.symbolDirectory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yokwe.stock.us.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class NASDAQListed implements Comparable<NASDAQListed> {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NASDAQListed.class);

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
	
	private static void checkDuplicate(List<NASDAQListed> list) {
		Map<String, NASDAQListed> map = new HashMap<>();
		for(var e: list) {
			String symbol = e.symbol;
			if (map.containsKey(symbol)) {
				logger.error("Duplicate symbol");
				logger.error("  old {}", map.get(symbol));
				logger.error("  new {}", e);
				throw new UnexpectedException("Duplicate symbol");
			} else {
				map.put(symbol, e);
			}
		}
	}

	public static void save(Collection<NASDAQListed> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<NASDAQListed> list) {
		// sanity check
		checkDuplicate(list);
		
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(NASDAQListed.class).file(getPath(), list);
	}
	
	public static List<NASDAQListed> load() {
		var list = CSVUtil.read(NASDAQListed.class).file(getPath());
		// sanity check
		if (list != null) checkDuplicate(list);
		
		return list;
	}
	public static List<NASDAQListed> getList() {
		List<NASDAQListed> ret = load();
		return ret == null ? new ArrayList<>() : ret;
	}

	
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
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(NASDAQListed that) {
		return this.symbol.compareTo(that.symbol);
	}

}

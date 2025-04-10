package yokwe.stock.us.bats;

import java.util.Collection;
import java.util.List;

import yokwe.stock.us.Storage;
import yokwe.util.ListUtil;
import yokwe.util.ToString;

public class ListedSecurityReport implements Comparable<ListedSecurityReport> {
	// Daily Listed Securities Report
	// Web Page
	//   https://www.cboe.com/us/equities/market_statistics/listed_securities/
	// File Location
	//   ftp://ftp.batstrading.com/bzx-equities/listed-securities/bzx_equities_listed_security_rpt_20230519.txt
	//   https://www.cboe.com/us/equities/market_statistics/listed_securities/2023/05/bzx_equities_listed_security_rpt_20230519.txt-dl
	// Data format
	//   https://cdn.cboe.com/resources/membership/BATS_BZX_Exchange_US_Listings_Corporate_Actions_Specification.pdf
	// CSM Symbology
	//   https://cdn.cboe.com/resources/membership/US_Symbology_Reference.pdf
	
	// From Cboe BZX Exchange US Listings Corporate Actions Specification
	//   https://cdn.cboe.com/resources/membership/BATS_BZX_Exchange_US_Listings_Corporate_Actions_Specification.pdf
	//
	// Daily Listed Securities Report Fields
	//
	// Symbol
	//   Symbol of Listed Security (upper case) in CMS Symbology.
	//   See below page for CMS Symbology
	//   https://cdn.cboe.com/resources/membership/US_Symbology_Reference.pdf
	// CUSIP
	//   The nine-character CUSIP assigned to the symbol on the day the report is published.
	// Issue Name
	//   A text field representing the name of the issue.
	// Issue Type
	//   The type of issue: common, preferred, etc. Allowed values:
	//   Commodity Futures Trust Shares
	//   Commodity Index Trust Shares
	//   Commodity-Based Trust Shares
	//   Commodity-Linked Securities
	//   Convertible Debt
	//   Currency Trust Shares
	//   Currency Warrants
	//   Derivative Securities Traded under UTP
	//   Equity Gold Shares
	//   Equity Index-Linked Securities
	//   Exchange-Traded Fund Shares
	//   Fixed Income Index-Linked Securities
	//   Futures-Linked Securities
	//   Index Fund Shares
	//   Index Warrants
	//   Index-Linked Exchangeable Notes
	//   Managed Fund Shares
	//   Managed Portfolio Shares
	//   Managed Trust Securities
	//   Multifactor Index-Linked Securities
	//   Other Securities
	//   Partnership Units
	//   Portfolio Depository Receipts
	//   Preferred Stock
	//   Primary Equity
	//   Right
	//   Secondary Class of Common
	//   Selected Equity-linked Debt Securities (SEEDS)
	//   Tracking Fund Shares
	//   Trust Certificates
	//   Trust Issued Receipts
	//   Trust Units
	//   Units
	//   Warrant
	// Currency
	//   Currency of the issue (ISO code)
	// Outstanding Shares
	//   Integer representing the number of shares outstanding for the issue.
	//   Will be “N/A” for all ETPs.
	// Test Symbol
	//   Indicates whether or not the issue is a test symbol. Allowed values:
	//   Y = Yes
	//   N = No
	// Market Category
	//   The Market Category of a security. Allowed values:
	//   Tier 1
	//   Tier 2
	// First Date Traded
	//   The date the Issue first traded as a BZX Listed Issue
	// IPO Flag
	//   Indicates if the issue conducted an Initial Public Offering on Cboe. Allowed values:
	//   Y = Yes
	//   N = No
	// Expiration Date
	//   Date (in YYYY-MM-DD format) that a security (warrant, when issued, etc.) expires.
	// Separation Date
	//   Date (in YYYY-MM-DD format) that a unit or warrant is separating from the associated common stock.
	// When Issued Flag
	//   Indicates if the issue is in a "when-issued" status. Allowed values:
	//   Y = Yes
	//   N = No
	// When Distributed Flag
	//   Indicates if the issue is in a "when-distributed" status. Allowed values:
	//   Y = Yes
	//   N = No
	// Round Lot Quantity
	//   Integer indicating the number of shares that define a round lot.
	// Notes
	//   Free-form text field for notes pertaining to the issue.
	// Financial Status
	//   Values:
	//   0 = Normal
	//   1 = Bankrupt
	//   2 = Below Continuing Listing Standards
	//   3 = Bankrupt & Below Continuing Listing Standards
	//   4 = Late Filing
	//   5 = Bankrupt & Late Filing
	//   6 = Below Continuing Listing Standards & Late Filing
	//   7 = Bankrupt, Below Continuing Listing Standards & Late Filing 8 = Creations Suspended (for Exchange Traded Product)
	//   9 = Redemptions Suspended (for Exchange Traded Product)
	//   A = Liquidation (for Exchange Traded Product)

	// First three lines of file
	// ----
	// Environment=PROD|Report Date=2023-05-19|Record Count=619
	// Symbol|Issue Name|Issue Type|Currency|Outstanding Shares|Test Symbol|Market Category|First Date Traded|IPO Flag|Expiration Date|Separation Date|When Issued Flag|When Distributed Flag|Round Lot Quantity|Notes|Financial Status
	// AAAU|Goldman Sachs Physical Gold ETF Shares|Commodity-Based Trust Shares|USD|n/a|N|Tier 1|2022-02-03|N|||N|N|100||0
	// ----
	
	// Last three lines of file
	// ----
	// ZIVB|-1x Short VIX Mid-Term Futures Strategy ETF|Exchange-Traded Fund Shares|USD|n/a|N|Tier 1|2023-04-19|Y|||N|N|100||0
	// ZTEST|Cboe Test|Primary Equity|USD|0|Y|Tier 1|2018-08-22|N|||N|N|100||0
	// ZTST|MPS Test Symbol|Managed Portfolio Shares|USD|n/a|Y|Tier 1|2020-01-15|Y|||N|N|100||0
	// ----
	
	public static final String PATH_FILE = Storage.BATS.getPath("listed-security-report.csv");
	
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static void save(Collection<ListedSecurityReport> collection) {
		// sanity check
		ListUtil.save(ListedSecurityReport.class, getPath(), collection);
	}
	public static void save(List<ListedSecurityReport> list) {
		// sanity check
		ListUtil.save(ListedSecurityReport.class, getPath(), list);
	}
	
	public static List<ListedSecurityReport> load() {
		return ListUtil.load(ListedSecurityReport.class, getPath());
	}
	public static List<ListedSecurityReport> getList() {
		return ListUtil.getList(ListedSecurityReport.class, getPath());
	}

	
	public String symbol;
	public String cusip;
	public String issueName;
	public String issueType;
	public String currency;
	public String outstandingShares;
	public String testSymbol;
	public String marketCategory;
	public String firstDateTraded;
	public String ipoFlag;
	public String expirationDate;
	public String separationDate;
	public String whenIssuedFlag;
	public String whenDistributedFlag;
	public String roundLotQuantity;
	public String notes;
	public String financialStatus;
	
	public ListedSecurityReport(
		String symbol,
		String cusip,
		String issueName,
		String issueType,
		String currency,
		String outstandingShares,
		String testSymbol,
		String marketCategory,
		String firstDateTraded,
		String ipoFlag,
		String expirationDate,
		String separationDate,
		String whenIssuedFlag,
		String whenDistributedFlag,
		String roundLotQuantity,
		String notes,
		String financialStatus
	) {
		this.symbol              = symbol;
		this.cusip               = cusip;
		this.issueName           = issueName;
		this.issueType           = issueType;
		this.currency            = currency;
		this.outstandingShares   = outstandingShares;
		this.testSymbol          = testSymbol;
		this.marketCategory      = marketCategory;
		this.firstDateTraded     = firstDateTraded;
		this.ipoFlag             = ipoFlag;
		this.expirationDate      = expirationDate;
		this.separationDate      = separationDate;
		this.whenIssuedFlag      = whenIssuedFlag;
		this.whenDistributedFlag = whenDistributedFlag;
		this.roundLotQuantity    = roundLotQuantity;
		this.notes               = notes;
		this.financialStatus     = financialStatus;
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	public String getKey() {
		return this.symbol;
	}
	@Override
	public int compareTo(ListedSecurityReport that) {
		return this.getKey().compareTo(that.getKey());
	}
	@Override
	public int hashCode() {
		return this.getKey().hashCode();
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof ListedSecurityReport) {
				ListedSecurityReport that = (ListedSecurityReport)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}

}

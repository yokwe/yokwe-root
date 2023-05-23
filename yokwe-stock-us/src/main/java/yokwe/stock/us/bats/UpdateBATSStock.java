package yokwe.stock.us.bats;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.us.Stock;
import yokwe.stock.us.Stock.Market;
import yokwe.stock.us.Stock.Type;
import yokwe.stock.us.Storage;
import yokwe.stock.us.nyse.NYSEStock;
import yokwe.util.FTPUtil;
import yokwe.util.FileUtil;
import yokwe.util.MarketHoliday;
import yokwe.util.UnexpectedException;

public class UpdateBATSStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final String URL_PATTERN = "ftp://ftp.batstrading.com/bzx-equities/listed-securities/bzx_equities_listed_security_rpt_%d%02d%02d.txt";
	public static final String PATH_FILE = Storage.BATS.getPath("listed-security-report.txt");
	
	private static String headerLine(LocalDate date, int lines) {
		return String.format("Environment=PROD|Report Date=%s|Record Count=%d", date.toString(), lines);
	}

	public static void download(LocalDate date) {
		String urlString = String.format(URL_PATTERN, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
		logger.info("url {}", urlString);
		
		byte[] data = FTPUtil.download(urlString);
		if (data == null) {
			logger.error("Download failed  {}", urlString);
			throw new UnexpectedException("Download failed");
		}
		
		FileUtil.rawWrite().file(PATH_FILE, data);
		logger.info("save {} {}", data.length, PATH_FILE);
	}
	
	public static void update(LocalDate date) {
		String content = FileUtil.read().file(PATH_FILE);
		String[] lines = content.split("[\r\n]+");
		logger.info("lines {}", lines.length);
		
		// check line 0
		{
			// Environment=PROD|Report Date=2023-05-19|Record Count=619
			String expect = headerLine(date, lines.length);
			String actual = lines[0];
			if (actual.compareTo(expect) != 0) {
				logger.error("Unexpected line");
				logger.error("  expect {}", expect);
				logger.error("  actual {}", actual);
				throw new UnexpectedException("Unexpected line");
			}
		}
		// check line 1
		{
			// Symbol|CUSIP|Issue Name|Issue Type|Currency|Outstanding Shares|Test Symbol|Market Category|First Date Traded|IPO Flag|Expiration Date|Separation Date|When Issued Flag|When Distributed Flag|Round Lot Quantity|Notes|Financial Status
			String expect = "Symbol|CUSIP|Issue Name|Issue Type|Currency|Outstanding Shares|Test Symbol|Market Category|First Date Traded|IPO Flag|Expiration Date|Separation Date|When Issued Flag|When Distributed Flag|Round Lot Quantity|Notes|Financial Status";
			String actual = lines[1];
			if (actual.compareTo(expect) != 0) {
				logger.error("Unexpected line");
				logger.error("  expect {}", expect);
				logger.error("  actual {}", actual);
				throw new UnexpectedException("Unexpected line");
			}
		}
		
		List<ListedSecurityReport> list = new ArrayList<>();
		for(int i = 2; i < lines.length; i++) {
			String line = lines[i];
			String[] token = line.split("[\\|]");
			if (token.length != 17) {
				logger.error("Unexpected line");
				logger.error("  token {}", token.length);
				logger.error("  line  {}  {}", i, line);
				throw new UnexpectedException("Unexpected line");
			}
			
			String symbol              = token[0];
			String cusip               = token[1];
			String issueName           = token[2].replace(",", "");
			String issueType           = token[3];
			String currency            = token[4];
			String outstandingShares   = token[5];
			String testSymbol          = token[6];
			String marketCategory      = token[7];
			String firstDateTraded     = token[8];
			String ipoFlag             = token[9];
			String expirationDate      = token[10];
			String separationDate      = token[11];
			String whenIssuedFlag      = token[12];
			String whenDistributedFlag = token[13];
			String roundLotQuantity    = token[14];
			String notes               = token[15];
			String financialStatus     = token[16];
			
			ListedSecurityReport report = new ListedSecurityReport(
				symbol,
				cusip,
				issueName,
				issueType,
				currency,
				outstandingShares,
				testSymbol,
				marketCategory,
				firstDateTraded,
				ipoFlag,
				expirationDate,
				separationDate,
				whenIssuedFlag,
				whenDistributedFlag,
				roundLotQuantity,
				notes,
				financialStatus
			);
			list.add(report);
		}
		
		logger.info("save {} {}", list.size(), ListedSecurityReport.getPath());
		ListedSecurityReport.save(list);
	}
	
	private static boolean needsDownload(LocalDate date) {
		File txtFile = new File(PATH_FILE);
		
		if (txtFile.exists()) {
			String   content = FileUtil.read().file(txtFile);
			String[] lines   = content.split("[\r\n]+");

			String headerLine = headerLine(date, lines.length);
			if (lines[0].compareTo(headerLine) == 0) {
				// file is already exists for date
				return false;
			}
		}
		return true;
	}
	private static boolean needsUpdate() {
		File csvFile = new File(ListedSecurityReport.PATH_FILE);
		return !csvFile.exists();
	}
	
	private static final Map<String, Type> typeMap = new TreeMap<>();
	static {
		typeMap.put("Commodity-Based Trust Shares",   Type.ETF);
		typeMap.put("Exchange-Traded Fund Shares",    Type.ETF);
		typeMap.put("Managed Portfolio Shares",       Type.ETF);
		typeMap.put("Tracking Fund Shares",           Type.ETF);
		typeMap.put("Trust Issued Receipts",          Type.ETF);
		//
		typeMap.put("Equity Index-Linked Securities", Type.ETN);
		typeMap.put("Futures-Linked Securities",      Type.ETN);
		//
		typeMap.put("Primary Equity",                 Type.COMMON);

	}
	
	private static void updateStock() {
		Map<String, Stock> nyseMap = NYSEStock.getMap();
		logger.info("nyse {}", nyseMap.size());
		
		List<Stock> list = new ArrayList<>();
		List<ListedSecurityReport> reportList = ListedSecurityReport.getList();
		logger.info("repo {}", reportList.size());

		int countTest = 0;
		for(var e: reportList) {
			// skip test
			if (e.testSymbol.compareTo("Y") == 0) {
				countTest++;
				continue;
			}
			
			String symbol = e.symbol;
			Market market = Market.BATS;
			Type   type;
			String name   = e.issueName.toUpperCase(); // use upper case
			
			// type
			if (typeMap.containsKey(e.issueType)) {
				type = typeMap.get(e.issueType);
			} else {
				logger.error("Unexpected issueType");
				logger.error("  symbol     {}!", e.symbol);
				logger.error("  issueType  {}!", e.issueType);
				logger.error("  issueName  {}!", e.issueName);
				throw new UnexpectedException("Unexpected issueType");
			}
			
			// sanity check with nyse
			if (nyseMap.containsKey(symbol)) {
				Stock nyseStock = nyseMap.get(symbol);
				if (nyseStock.type != type) {
					logger.error("Unexpected type");
					logger.error("  bats    {}  {}  {}", e.symbol, e.issueType, e.issueName);
					logger.error("  type  {}", type);
					logger.error("  nyse  {}", nyseStock.type);
					throw new UnexpectedException("Unexpected type");
				}
				if (nyseStock.market != Market.BATS) {
					logger.error("Unexpected market");
					logger.error("  bats    {}  {}  {}", e.symbol, e.issueType, e.issueName);
					logger.error("  market  {}", market);
					logger.error("  nyse    {}", nyseStock.market);
					throw new UnexpectedException("Unexpected market");
				}
			}
			
			list.add(new Stock(symbol, market, type, name));
		}
		logger.info("test {}", countTest);
		
		logger.info("save {} {}", list.size(), BATSStock.getPath());
		BATSStock.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		LocalDate date = MarketHoliday.US.getLastTradingDate();

		if (needsDownload(date)) {
			download(date);
		} else {
			logger.info("no need to download");
		}
		
		update(date);
		updateStock();
		
		logger.info("STOP");
	}
}

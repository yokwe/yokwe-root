package yokwe.finance.provider.bats;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoUS;
import yokwe.finance.type.StockInfoUS.Market;
import yokwe.finance.type.StockInfoUS.Type;
import yokwe.util.CSVUtil;
import yokwe.util.FTPUtil;
import yokwe.util.FileUtil;
import yokwe.util.MarketHoliday;
import yokwe.util.UnexpectedException;

public class UpdateStockInfo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_PATTERN = "ftp://ftp.batstrading.com/bzx-equities/listed-securities/bzx_equities_listed_security_rpt_%d%02d%02d.txt";
	
	private static final LocalDate TARGET_DATE = MarketHoliday.US.getLastTradingDate();
	
	private static final String PATH_TXT = Storage.Provider.BATS.getPath("listed-security-report.txt");
	
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
	
	
	private static void downloadFile() {
		String urlString = String.format(URL_PATTERN, TARGET_DATE.getYear(), TARGET_DATE.getMonthValue(), TARGET_DATE.getDayOfMonth());
		logger.info("url {}", urlString);
		
		byte[] data = FTPUtil.download(urlString);
		if (data == null) {
			logger.error("Download failed  {}", urlString);
			throw new UnexpectedException("Download failed");
		}
		
		// save txt file
		FileUtil.rawWrite().file(PATH_TXT, data);
		logger.info("save  {}  {}", data.length, PATH_TXT);
	}
	private static void saveCSVFile() {
		List<ListedSecurityReport>	list;
		{
			String txtString = FileUtil.read().file(PATH_TXT);
			String[] lines = txtString.split("[\\r\\n]+");
			
			// remove first line
			String string = String.join("\n", Arrays.copyOfRange(lines, 1, lines.length)) + "\n";
			// read string as csv file
			list = CSVUtil.read(ListedSecurityReport.class).withSeparator('|').file(new StringReader(string));
		}
		
		// save csv file
		logger.info("save  {}  {}", list.size(), ListedSecurityReport.getPath());
		ListedSecurityReport.save(list);
	}
	private static void update() {
		List<StockInfoUS> list = new ArrayList<>();
		for(var e: ListedSecurityReport.getList()) {
			
			String symbol = e.symbol;
			Market market = Market.BATS;
			Type   type   = typeMap.get(e.issueType);
			String name   = e.issueName.replace(",", "").toUpperCase(); // use upper case
			
			if (type == null) {
				logger.error("Unexpected issueType");
				logger.error("  symbol     {}!", e.symbol);
				logger.error("  issueType  {}!", e.issueType);
				logger.error("  issueName  {}!", e.issueName);
				throw new UnexpectedException("Unexpected issueType");
			}

			list.add(new StockInfoUS(symbol, market, type, name));
		}
		
		logger.info("save  {}  {}", list.size(), StockInfo.getPath());
		StockInfo.save(list);
	}

	public static void main(String[] args) {
		logger.info("START");
		
		downloadFile();
		saveCSVFile();
		update();
		
		logger.info("STOP");
	}

}

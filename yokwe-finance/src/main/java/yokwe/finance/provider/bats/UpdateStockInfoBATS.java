package yokwe.finance.provider.bats;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.finance.type.StockInfoUSType;
import yokwe.finance.type.StockInfoUSType.Market;
import yokwe.finance.type.StockInfoUSType.Type;
import yokwe.util.CSVUtil;
import yokwe.util.FTPUtil;
import yokwe.util.FileUtil;
import yokwe.util.MarketHoliday;
import yokwe.util.UnexpectedException;

public class UpdateStockInfoBATS {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
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
		//
		typeMap.put("Warrant",                        Type.WARRANT);
	}
	
	private static void update() {
		byte[] data;
		{
			var date = MarketHoliday.US.getLastTradingDate();
			logger.info("lastTradingDate  {}", date);

			String url = String.format(
				"ftp://ftp.batstrading.com/bzx-equities/listed-securities/bzx_equities_listed_security_rpt_%d%02d%02d.txt",
				date.getYear(), date.getMonthValue(), date.getDayOfMonth());
//			logger.info("url              {}", url);
			
			data = FTPUtil.download(url);
			if (data == null) {
				logger.error("Download failed  {}", url);
				throw new UnexpectedException("Download failed");
			}
		}
		
		logger.info("save  {}  {}", data.length, StorageBATS.PATH_TXT);
		FileUtil.rawWrite().file(StorageBATS.PATH_TXT, data);
		
		List<ListedSecurityReportType>	reportList;
		{
			String string;
			{
				String dataString = new String(data, StandardCharsets.UTF_8);
				String[] lines = dataString.split("[\\r\\n]+");
				
				// remove first line
				string = String.join("\n", Arrays.copyOfRange(lines, 1, lines.length)) + "\n";
			}
			// read string as csv file
			reportList = CSVUtil.read(ListedSecurityReportType.class).withSeparator('|').file(new StringReader(string));
		}
		// save csv file
		logger.info("save  {}  {}", reportList.size(), StorageBATS.ListedSecurityReport.getPath());
		StorageBATS.ListedSecurityReport.save(reportList);
		
		
		List<StockInfoUSType> list = new ArrayList<>();
		{
			int countSkip = 0;
			for(var e: reportList) {
				if (e.isTestSymbol()) {
					logger.info("skip  test symbol          {}  {}", e.symbol, e.issueName);
					countSkip++;
					continue;
				}
				if (!e.isFinancialNormal()) {
					logger.info("skip  financial status  {}  {}  {}", e.financialStatus, e.symbol, e.issueName);
					countSkip++;
					continue;
				}
				
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

				list.add(new StockInfoUSType(symbol, market, type, name));
			}
			logger.info("skip  {}", countSkip);
		}
		// save csv file
		logger.info("save  {}  {}", list.size(), StorageBATS.StockInfoBATS.getPath());
		StorageBATS.StockInfoBATS.save(list);
	}

	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}

}

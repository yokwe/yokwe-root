package yokwe.finance.provider.nasdaq;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.finance.type.StockInfoUS;
import yokwe.finance.type.StockInfoUS.Market;
import yokwe.finance.type.StockInfoUS.Type;
import yokwe.util.CSVUtil;
import yokwe.util.FTPUtil;
import yokwe.util.FileUtil;
import yokwe.util.ListUtil;
import yokwe.util.UnexpectedException;

public class UpdateStockInfo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Map<String, Market> marketMap = new TreeMap<>();
	static {
		//   A = NYSE MKT
		//   N = New York Stock Exchange (NYSE)
		//   P = NYSE ARCA
		//   Z = BATS Global Markets (BATS)
		//   V = Investors' Exchange, LLC (IEXG)

		marketMap.put("A", Market.NYSE);   // NYSE MKT
		marketMap.put("N", Market.NYSE);   // New York Stock Exchange (NYSE)
		marketMap.put("P", Market.NYSE);   // NYSE ARCA
		marketMap.put("Z", Market.BATS);   // BATS Global Markets (BATS)
		marketMap.put("V", Market.IEXG);   // Investors' Exchange, LLC (IEXG)
	}
	
	
	private static void downloadFile(String url, String path) {
		byte[] data = FTPUtil.download(url);
		if (data == null) {
			logger.error("Download failed  {}", NASDAQListed.URL);
			throw new UnexpectedException("Download failed");
		}
		
		// save txt file
		FileUtil.rawWrite().file(path, data);
		logger.info("save  {}  {}", data.length, path);
	}
	private static <E extends Comparable<E>> void saveCSVFile(Class<E> clazz, String pathText, String pathCSV) {
		List<E>	list;
		{
			String txtString = FileUtil.read().file(pathText);
			String[] lines = txtString.split("[\\r\\n]+");
			
			// remove last line
			String string = String.join("\n", Arrays.copyOfRange(lines, 0, lines.length - 1)) + "\n";
			// read string as csv file
			list = CSVUtil.read(clazz).withSeparator('|').file(new StringReader(string));
		}
		
		// save csv file
		logger.info("save  {}  {}", list.size(), pathCSV);
		ListUtil.save(clazz, pathCSV, list);
	}
	private static void download() {
		// download txt file
		downloadFile(NASDAQListed.URL, NASDAQListed.PATH_TXT);
		downloadFile(OtherListed.URL,  OtherListed.PATH_TXT);
		// save txt file as csv file
		saveCSVFile(NASDAQListed.class, NASDAQListed.PATH_TXT, NASDAQListed.PATH_CSV);
		saveCSVFile(OtherListed.class,  OtherListed.PATH_TXT,  OtherListed.PATH_CSV);
	}
	
	private static void update() {
		// FIXME
		List<StockInfoUS> list = new ArrayList<>();
		
		int countTotal = 0;
		int countSkip  = 0;
		{
			for(var e: NASDAQListed.getList()) {
				countTotal++;
				
				// skip test issue, right, unit and warrant
				if (e.isTestIssue() || e.isRights() || e.isUnits() || e.isWarrant()) {
					countSkip++;
					continue;
				}
				// skip warrant and beneficial interest
				var upperCaseName = e.name.toUpperCase();
				if (upperCaseName.contains("WARRANT") || upperCaseName.contains("BENEFICIAL INTEREST")) {
					countSkip++;
					continue;
				}
				// skip financial status is not normal
				if (!e.isFinancialNormal()) {
					logger.info("skip  financial status  {}  {}  {}", e.financialStatus, e.symbol, e.name);
					countSkip++;
					continue;
				}
				
				String symbol = e.symbol;
				Market market = Market.NASDAQ;
				Type   type   = e.etf.equals("Y") ? Type.ETF : Type.COMMON; // FIXME only ETF or other
				String name   = e.name.replace(",", "").toUpperCase(); // use upper case

				list.add(new StockInfoUS(symbol, market, type, name));
			}
			for(var e: OtherListed.getList()) {
				countTotal++;
				
				// skip test issue, right, unit and warrant
				if (e.isTestIssue() || e.isRights() || e.isUnits() || e.isWarrant()) {
					countSkip++;
					continue;
				}
				
				String symbol = e.symbol;
				Market market = marketMap.get(e.exchange);
				Type   type   = e.etf.equals("Y") ? Type.ETF : Type.COMMON; // FIXME only ETF or other
				String name   = e.name.replace(",", "").toUpperCase(); // use upper case
				
				list.add(new StockInfoUS(symbol, market, type, name));
			}
		}
		
		logger.info("total  {}", countTotal);
		logger.info("skip   {}", countSkip);
		
		// save
		logger.info("save   {}  {}", list.size(), StockInfo.getPath());
		StockInfo.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		download();
		update();
		
		logger.info("STOP");
	}
}

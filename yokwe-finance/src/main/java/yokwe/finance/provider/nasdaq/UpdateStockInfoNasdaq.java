package yokwe.finance.provider.nasdaq;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoUSType;
import yokwe.finance.type.StockInfoUSType.Market;
import yokwe.finance.type.StockInfoUSType.Type;
import yokwe.util.CSVUtil;
import yokwe.util.FTPUtil;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class UpdateStockInfoNasdaq {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final String URL_NASDAQLISTED = "ftp://ftp.nasdaqtrader.com/symboldirectory/nasdaqlisted.txt";
		
	public static final String URL_OTHERLISTED = "ftp://anonymous:anonymous@ftp.nasdaqtrader.com/symboldirectory/otherlisted.txt";
		

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
	
	
	private static <E extends Comparable<E>> void download(String url, String path, Class<E> clazz, Storage.LoadSave<E, String> loadSave) {
		String string;
		{
			byte[] data = FTPUtil.download(url);
			if (data == null) {
				logger.error("Download failed  {}", url);
				throw new UnexpectedException("Download failed");
			}
			
			string = new String(data, StandardCharsets.US_ASCII);
			
			// save txt file
			FileUtil.write().file(path, string);
			logger.info("save  {}  {}", string.length(), path);
		}
		List<E>	list;
		{
			String[] lines = string.split("[\\r\\n]+");
			
			// remove last line
			String csvString = String.join("\n", Arrays.copyOfRange(lines, 0, lines.length - 1)) + "\n";
			// read string as csv file
			list = CSVUtil.read(clazz).withSeparator('|').file(new StringReader(csvString));
		}
				
		// save csv file
		logger.info("save  {}  {}", list.size(), loadSave.getPath());
		loadSave.save(list);
	}
	private static void download() {
		download(URL_NASDAQLISTED, StorageNasdaq.NasdaqListed_TXT, NasdaqListedType.class, StorageNasdaq.NasdaqListed);
		download(URL_OTHERLISTED,  StorageNasdaq.OtherListed_TXT,  OtherListedType.class,  StorageNasdaq.OtherListed);
	}
	
	private static void update() {
		List<StockInfoUSType> list = new ArrayList<>();
		
		int countTotal = 0;
		int countSkip  = 0;
		{
			for(var e: StorageNasdaq.NasdaqListed.getList()) {
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
				Type   type   = e.etf.equals("Y") ? Type.ETF : Type.COMMON; // just ETF or COMMON for now
				String name   = e.name.replace(",", "").toUpperCase(); // use upper case

				list.add(new StockInfoUSType(symbol, market, type, name));
			}
			for(var e: StorageNasdaq.OtherListed.getList()) {
				countTotal++;
				
				// skip test issue, right, unit and warrant
				if (e.isTestIssue() || e.isRights() || e.isUnits() || e.isWarrant()) {
					countSkip++;
					continue;
				}
				
				String symbol = e.symbol;
				Market market = marketMap.get(e.exchange);
				Type   type   = e.etf.equals("Y") ? Type.ETF : Type.COMMON;  // just ETF or COMMON for now
				String name   = e.name.replace(",", "").toUpperCase(); // use upper case
				
				list.add(new StockInfoUSType(symbol, market, type, name));
			}
		}
		
		logger.info("total  {}", countTotal);
		logger.info("skip   {}", countSkip);
		
		// save
		logger.info("save   {}  {}", list.size(), StorageNasdaq.StockInfoNasdaq.getPath());
		StorageNasdaq.StockInfoNasdaq.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		download();
		update();
		
		logger.info("STOP");
	}
}

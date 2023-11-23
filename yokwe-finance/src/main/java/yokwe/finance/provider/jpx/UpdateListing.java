package yokwe.finance.provider.jpx;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.finance.provider.jpx.ListingType.Type;
import yokwe.finance.type.StockInfoJPType;
import yokwe.util.FileUtil;
import yokwe.util.HashCode;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateListing {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL = "https://www.jpx.co.jp/markets/statistics-equities/misc/tvdivq0000001vg2-att/data_j.xls";
	
	private static final String PATH_DATAFILE = StorageJPX.storage.getPath("data_j.xls");
	private static final String URL_DATAFILE  = StringUtil.toURLString(PATH_DATAFILE);
	
	private static final long GRACE_PERIOD_IN_DAY = 30;
	
	private static void update() {
		logger.info("grace period  {} days", GRACE_PERIOD_IN_DAY);
		
		{
			File file = new File(PATH_DATAFILE);
			
			byte[] oldHashCode;
			// check last modified date of file
			{
				if (file.canRead()) {
					Instant  now          = Instant.now();
					Instant  lastModified = FileUtil.getLastModified(PATH_DATAFILE);
					Duration duration     = Duration.between(lastModified, now);
					if (duration.toDays() <= GRACE_PERIOD_IN_DAY) {
						logger.info("No need to update  --  within grace period  {} days", GRACE_PERIOD_IN_DAY);
						return;
					}
					oldHashCode = HashCode.getHashCode(file);
				} else {
					oldHashCode = new byte[] {0};
				}
			}
			
			logger.info("download {}", URL);
			HttpUtil http = HttpUtil.getInstance().withRawData(true);
			HttpUtil.Result result = http.download(URL);
			
			if (result != null && result.rawData != null) {
				byte[] newHashCode = HashCode.getHashCode(result.rawData);
				
				logger.info("oldHashCode  {}", StringUtil.toHexString(oldHashCode));
				logger.info("newHashCode  {}", StringUtil.toHexString(newHashCode));
				
				if (Arrays.equals(oldHashCode, newHashCode)) {
					logger.info("No need to update  --  same file contents");
					return;
				}
				logger.info("save  {}  {}", result.rawData.length, PATH_DATAFILE);
				FileUtil.rawWrite().file(file, result.rawData);
			} else {
				logger.error("Unexpected result");
				logger.error("  result  {}", result);
				throw new UnexpectedException("Unexpected result");
			}
		}

		logger.info("open  {}", URL_DATAFILE);
		List<ListingType> list = new ArrayList<>();
		
		// Build newList
		try (SpreadSheet spreadSheet = new SpreadSheet(URL_DATAFILE, true)) {
			List<ListingType> rawDataList = Sheet.extractSheet(spreadSheet, ListingType.class);
			logger.info("read  {}", rawDataList.size());
			
			for(ListingType rawData: rawDataList) {
				// Trim space of rawData
				String date = rawData.date.trim();
				if (date.length() != 8) {
					logger.error("Unexpected date");
					logger.error("  datte {}!", date);
					throw new UnexpectedException("Unexpected date");
				}
				
				ListingType value = new ListingType();
				
				value.date         = String.format("%s-%s-%s", date.substring(0, 4), date.substring(4, 6), date.substring(6, 8));;
				value.stockCode    = StockInfoJPType.toStockCode5(rawData.stockCode.trim());
				value.name         = rawData.name.trim();
				value.type         = rawData.type;
				value.sector33Code = rawData.sector33Code.trim();
				value.sector33     = rawData.sector33.trim();
				value.sector17Code = rawData.sector17Code.trim();
				value.sector17     = rawData.sector17.trim();
				value.topix        = rawData.topix;
				value.scaleCode    = rawData.scaleCode.trim();
				
				list.add(value);
			}
			
			// output count of data for each market
			{
				Map<Type, Integer> countMap = new TreeMap<>();
				for(var e: Type.values()) {
					countMap.put(e, 0);
				}
				for(var e: list) {
					int count = countMap.get(e.type);
					countMap.put(e.type, count + 1);
				}
				for(var e: countMap.entrySet()) {
					String name  = e.getKey().name();
					int    count = e.getValue();
					logger.info("market {}", String.format("%-18s %4d", name, count));
				}
			}
			
			// Sanity check
			if (list.isEmpty()) {
				logger.error("Empty data");
				throw new UnexpectedException("Empty data");
			}
			
			// save
			logger.info("save  {}  {}", list.size(), StorageJPX.Listing.getPath());
			StorageJPX.Listing.save(list);
		}
	}
	
	public static void main(String[] args) {
		try {
			logger.info("START");
			
			LibreOffice.initialize();
			
			update();
			
			logger.info("STOP");
		} catch (Throwable e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
		} finally {
			LibreOffice.terminate();
		}
	}
}

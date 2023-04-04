package yokwe.stock.jp.jpx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.jpx.Stock.Market;
import yokwe.util.FileUtil;
import yokwe.util.HashCode;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_DATAFILE  = "https://www.jpx.co.jp/markets/statistics-equities/misc/tvdivq0000001vg2-att/data_j.xls";
	
	private static final String PATH_DATAFILE      = Storage.JPX.getPath("data_j.xls");
	private static final String URL_DATAFILE_LOCAL = StringUtil.toURLString(PATH_DATAFILE);

	private static void processRequest() {
		logger.info("download {}", URL_DATAFILE);
		HttpUtil http = HttpUtil.getInstance().withRawData(true);
		HttpUtil.Result result = http.download(URL_DATAFILE);
		
		// Check file and download contents
		{
			boolean needsWrite = true;
			File file = new File(PATH_DATAFILE);
			if (file.exists()) {
				final String hashOfDownload       = StringUtil.toHexString(HashCode.getHashCode(result.rawData));
				final String hashOfDownlaodedFile = StringUtil.toHexString(HashCode.getHashCode(file));

				if (hashOfDownlaodedFile.equals(hashOfDownload)) {
					logger.info("Download same contents {}", hashOfDownload);
					logger.info("  download  {}  {}", result.rawData.length, hashOfDownload);
					logger.info("  file      {}  {}", file.length(), hashOfDownlaodedFile);
					
					// if file and download has same contents, no needs to write
					needsWrite = false;
				}
			}
			
			if (needsWrite) {
				logger.info("write {} {}", PATH_DATAFILE, result.rawData.length);
				FileUtil.rawWrite().file(file, result.rawData);
			}
		}

		logger.info("open {}", URL_DATAFILE_LOCAL);
		List<Stock> newList = new ArrayList<>();
		
		// Build newList
		try (SpreadSheet spreadSheet = new SpreadSheet(URL_DATAFILE_LOCAL, true)) {
			List<Stock> rawDataList = Sheet.extractSheet(spreadSheet, Stock.class);
			logger.info("read {}", rawDataList.size());
			
			// Trim space
			for(Stock value: rawDataList) {
				Stock newValue = new Stock();
				
				String date = value.date.trim();
				if (date.length() != 8) {
					logger.error("Unexpected date value {}!", date);
					throw new UnexpectedException("Unexpected date value");
				}
				String newDate = String.format("%s-%s-%s", date.substring(0, 4), date.substring(4, 6), date.substring(6, 8));
				
				newValue.date         = newDate;
				newValue.stockCode    = value.stockCode.trim();
				newValue.name         = value.name.trim();
				newValue.market       = value.market;
				newValue.sector33Code = value.sector33Code.trim();
				newValue.sector33     = value.sector33.trim();
				newValue.sector17Code = value.sector17Code.trim();
				newValue.sector17     = value.sector17.trim();
				newValue.scale        = value.scale.trim();
				newValue.scaleCode    = value.scaleCode.trim();
				
				// Make stockCode 5 digits
				newValue.stockCode = Stock.toStockCode5(newValue.stockCode);
				
				newList.add(newValue);
			}
			
			{
				Map<Market, Integer> countMap = new TreeMap<>();
				for(var e: Market.values()) {
					countMap.put(e, 0);
				}
				for(var e: newList) {
					int count = countMap.get(e.market);
					countMap.put(e.market, count + 1);
				}
				for(var e: countMap.entrySet()) {
					String name  = e.getKey().name();
					int    count = e.getValue();
					logger.info("market {}", String.format("%-16s %4d", name, count));
				}
			}
			
			// Sanity check
			if (newList.isEmpty()) {
				logger.error("Empty data");
				throw new UnexpectedException("Empty data");
			}
			
			// Save if necessary
			{
				boolean sameData = false;
				String  newDate  = newList.get(0).date;
				
				List<Stock> oldList = Stock.getList();
				if (oldList.isEmpty()) {
					sameData = false;
				} else {
					// check date of first data
					String oldDate = oldList.get(0).date;
					sameData = newDate.equals(oldDate);
				}
				
				if (sameData) {
					logger.warn("same data  {}  {}", newDate, newList.size());
				} else {
					logger.info("save {}  {}  {}", newDate, newList.size(), Stock.getPath());
					Stock.save(newList);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			logger.info("START");
			
			processRequest();
			
			logger.info("STOP");
		} finally {
			System.exit(0);
		}
	}
}

package yokwe.stock.jp.jpx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.jpx.JPXListing.Market;
import yokwe.util.FileUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateJPXListing {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL = "https://www.jpx.co.jp/markets/statistics-equities/misc/tvdivq0000001vg2-att/data_j.xls";
	
	private static final String PATH_DATAFILE = Storage.JPX.getPath("data_j.xls");
	private static final String URL_DATAFILE  = StringUtil.toURLString(PATH_DATAFILE);

	private static void processRequest() {
		logger.info("download {}", URL);
		HttpUtil http = HttpUtil.getInstance().withRawData(true);
		HttpUtil.Result result = http.download(URL);
		
		if (result != null && result.rawData != null) {
			logger.info("write {} {}", PATH_DATAFILE, result.rawData.length);
			FileUtil.rawWrite().file(PATH_DATAFILE, result.rawData);
		} else {
			logger.error("Unexpected result");
			logger.error("  result  {}", result);
			throw new UnexpectedException("Unexpected result");
		}

		logger.info("open {}", URL_DATAFILE);
		List<JPXListing> list = new ArrayList<>();
		
		// Build newList
		try (SpreadSheet spreadSheet = new SpreadSheet(URL_DATAFILE, true)) {
			List<JPXListing> rawDataList = Sheet.extractSheet(spreadSheet, JPXListing.class);
			logger.info("read {}", rawDataList.size());
			
			for(JPXListing rawData: rawDataList) {
				// Trim space of rawData
				String date = rawData.date.trim();
				if (date.length() != 8) {
					logger.error("Unexpected date");
					logger.error("  datte {}!", date);
					throw new UnexpectedException("Unexpected date");
				}
				
				JPXListing value = new JPXListing();
				
				value.date         = String.format("%s-%s-%s", date.substring(0, 4), date.substring(4, 6), date.substring(6, 8));;
				value.stockCode    = Stock.toStockCode5(rawData.stockCode.trim());
				value.name         = rawData.name.trim();
				value.market       = rawData.market;
				value.sector33Code = rawData.sector33Code.trim();
				value.sector33     = rawData.sector33.trim();
				value.sector17Code = rawData.sector17Code.trim();
				value.sector17     = rawData.sector17.trim();
				value.scale        = rawData.scale.trim();
				value.scaleCode    = rawData.scaleCode.trim();
				
				list.add(value);
			}
			
			// output count of data for each market
			{
				Map<Market, Integer> countMap = new TreeMap<>();
				for(var e: Market.values()) {
					countMap.put(e, 0);
				}
				for(var e: list) {
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
			if (list.isEmpty()) {
				logger.error("Empty data");
				throw new UnexpectedException("Empty data");
			}
			
			// save
			logger.info("save  {}  {}", list.size(), JPXListing.getPath());
			JPXListing.save(list);
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

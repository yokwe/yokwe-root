package yokwe.finance.provider.jpx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.finance.Storage;
import yokwe.finance.provider.jpx.Listing.Kind;
import yokwe.finance.type.StockInfoJP;
import yokwe.util.FileUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateListing {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL = "https://www.jpx.co.jp/markets/statistics-equities/misc/tvdivq0000001vg2-att/data_j.xls";
	
	private static final String PATH_DATAFILE = Storage.Provider.JPX.getPath("data_j.xls");
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
		List<Listing> list = new ArrayList<>();
		
		// Build newList
		try (SpreadSheet spreadSheet = new SpreadSheet(URL_DATAFILE, true)) {
			List<Listing> rawDataList = Sheet.extractSheet(spreadSheet, Listing.class);
			logger.info("read {}", rawDataList.size());
			
			for(Listing rawData: rawDataList) {
				// Trim space of rawData
				String date = rawData.date.trim();
				if (date.length() != 8) {
					logger.error("Unexpected date");
					logger.error("  datte {}!", date);
					throw new UnexpectedException("Unexpected date");
				}
				
				Listing value = new Listing();
				
				value.date         = String.format("%s-%s-%s", date.substring(0, 4), date.substring(4, 6), date.substring(6, 8));;
				value.stockCode    = StockInfoJP.toStockCode5(rawData.stockCode.trim());
				value.name         = rawData.name.trim();
				value.kind       = rawData.kind;
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
				Map<Kind, Integer> countMap = new TreeMap<>();
				for(var e: Kind.values()) {
					countMap.put(e, 0);
				}
				for(var e: list) {
					int count = countMap.get(e.kind);
					countMap.put(e.kind, count + 1);
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
			logger.info("save  {}  {}", list.size(), Listing.getPath());
			Listing.save(list);
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
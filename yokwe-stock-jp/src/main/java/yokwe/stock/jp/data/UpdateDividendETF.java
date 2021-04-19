package yokwe.stock.jp.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.LoggerFactory;

import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

//
// Make DividendETF from dividend-etf.ods
//

public class UpdateDividendETF {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateDividendETF.class);
	
	public static final String URL_DIVIDEND_ETF = "file:///home/hasegawa/Dropbox/Trade/dividend-etf.ods";

	public static void main(String[] args) {
		logger.info("START");
		
		try (SpreadSheet dividendETF = new SpreadSheet(URL_DIVIDEND_ETF, true)) {
			List<String> sheetNameList = dividendETF.getSheetNameList();
			Collections.sort(sheetNameList);
			
			List<DividendETF> all = new ArrayList<>();
			
			for(String sheetName: sheetNameList) {
				if (!sheetName.matches("^20[0-9][0-9]$")) {
					logger.warn("Sheet skip {}", sheetName);
					continue;
				}
				
				List<DividendETF> list = Sheet.extractSheet(dividendETF, DividendETF.class, sheetName);
				logger.info("Sheet read {} {}", sheetName, list.size());
				
				for(DividendETF e: list) {
					// Remove ".0" suffix from stockCode
					if (e.stockCode.endsWith(".0")) {
						e.stockCode = e.stockCode.substring(0, e.stockCode.length() - 2);
					}
				}
				all.addAll(list);
			}
			Collections.sort(all);
			
			// FIXME add sanity check
			
			logger.info("save {} {}", DividendETF.PATH_FILE, all.size());
			DividendETF.save(all);
		}

		logger.info("STOP");
		System.exit(0);
	}
}

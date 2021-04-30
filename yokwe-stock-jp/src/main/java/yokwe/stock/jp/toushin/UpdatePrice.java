package yokwe.stock.jp.toushin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import yokwe.util.FileUtil;

public class UpdatePrice {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdatePrice.class);

	private static final String  CSV_HEADER  = "年月日,基準価額(円),純資産総額（百万円）,分配金,決算期";
	
	public static void update() {
		logger.info("update");
		
		int count = 0;
		File dir = new File(DownloadFile.getPathPrice(""));
		for(var file: dir.listFiles()) {
			if (file.isDirectory()) continue;
			String isinCode = file.getName();
			
			String string = FileUtil.read().file(file);
			
			String[] lines = string.split("[\\r\\n]+");
			if (lines[0].equals(CSV_HEADER)) {
				List<Price> priceList = new ArrayList<>();
				
				for(int i = 1; i < lines.length; i++) {
					String line = lines[i];
					String[] fields = line.split(",", -1);
					if (fields.length != 5) {
						logger.warn("Unexpected field");
						logger.warn("  {} - {}!", fields.length, line);
						System.exit(0); // FIXME
					} else {
						String date          = fields[0]; // 年月日
						String basePrice     = fields[1]; // 基準価額(円) = 純資産総額 / (総口数 * 10,000)
						String netAssetValue = fields[2]; // 純資産総額（百万円）
						String dividend      = fields[3]; // 分配金
						String period        = fields[4]; // 決算期
						
						date = date.replace("年", "-").replace("月", "-").replace("日", "");

						Price fundPrice = new Price(date, basePrice, netAssetValue, dividend, period);
						priceList.add(fundPrice);
					}
				}
				Price.save(isinCode, priceList);
				count++;
			} else {
				logger.warn("Unpexpected header");
				logger.warn("  {}!", lines[0]);
			}
		}
		
		logger.info("count {}", count);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}

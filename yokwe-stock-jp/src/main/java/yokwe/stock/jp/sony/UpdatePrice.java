package yokwe.stock.jp.sony;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.DataBindingException;
import jakarta.xml.bind.JAXB;
import yokwe.stock.jp.Storage;
import yokwe.util.FileUtil;
import yokwe.util.http.HttpUtil;

public class UpdatePrice {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	//https://apl.morningstar.co.jp/xml/chart/funddata/2013121001.xml
	
	private static String URL_BASE_FUNDDATA = "https://apl.morningstar.co.jp/xml/chart/funddata";
	public static String getURL(String msFundCode) {
		return String.format("%s/%s.xml", URL_BASE_FUNDDATA, msFundCode);
	}
	
	private static final String PREFIX = "page";
	public static final String getPath(String isinCode) {
		return Storage.Sony.getPath(PREFIX, isinCode + ".xml");
	}


	public static void main(String[] arsg) {
		logger.info("START");

		for(FundInfo fundInfo: FundInfo.getList()) {
			String isinCode   = fundInfo.isinCode;
			String msFundCode = fundInfo.msFundCode;
			if (msFundCode.isEmpty()) continue;
			
			logger.info("{} {} {}", isinCode, msFundCode, fundInfo.fundName);
			
			final yokwe.stock.jp.sony.xml.ChartFundData chartFundData;
			{
				File file = new File(getPath(msFundCode));
				
				try {
					byte[] byteArray;
					{
						if (file.exists()) {
							byteArray = FileUtil.rawRead().file(file);
						} else {
							String url = getURL(msFundCode);
							logger.info("  download {}", url);
							HttpUtil.Result result = HttpUtil.getInstance().withRawData(true).download(url);
							byteArray = result.rawData;
							FileUtil.rawWrite().file(file, byteArray);
							logger.info("  save {} {}", byteArray.length, file.getPath());
						}
					}

					chartFundData = JAXB.unmarshal(new ByteArrayInputStream(byteArray), yokwe.stock.jp.sony.xml.ChartFundData.class);

				} catch(DataBindingException e) {
					logger.warn("DataBindingException");
					String exceptionName = e.getClass().getSimpleName();
					logger.error("{} {}", exceptionName, e);

					logger.warn("  delete file {}", file.getPath());
					// delete file for rerun
					file.delete();
					continue;
				}
			}
			
			List<Price> priceList = new ArrayList<>();
			{
//				logger.info("chartFundData {}", StringUtil.toString(chartFundData));
				for(yokwe.stock.jp.sony.xml.ChartFundData.Fund.Year year: chartFundData.fund.yearList) {
					for(yokwe.stock.jp.sony.xml.ChartFundData.Fund.Year.Month month: year.monthList) {
						for(yokwe.stock.jp.sony.xml.ChartFundData.Fund.Year.Month.Day day: month.dayList) {
							if (day.price.isEmpty())  continue;
							if (day.volume.isEmpty()) continue;

							// 	public Price(LocalDate date, String isinCode, BigDecimal price, long volume) {
							LocalDate  date   = LocalDate.parse(String.format("%s-%s-%s", day.year, day.month, day.value));
							BigDecimal value  = new BigDecimal(day.price);
							long       volume = Long.parseLong(day.volume);
							Price price = new Price(date, isinCode, fundInfo.currency, value, volume);
							priceList.add(price);
						}
					}
				}
			}
			logger.info("  save {} {}", priceList.size(), Price.getPath(isinCode));
			Price.save(isinCode, priceList);
		}

		logger.info("STOP");
	}

}

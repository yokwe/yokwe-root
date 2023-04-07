package yokwe.stock.jp.sony;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.DataBindingException;
import jakarta.xml.bind.JAXB;
import yokwe.stock.jp.Storage;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdatePrice {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static String URL_BASE_FUNDDATA = "https://apl.wealthadvisor.jp/xml/chart/funddata";
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
			// Build chartFundData
			try {
				String url = getURL(msFundCode);
				HttpUtil.Result result = HttpUtil.getInstance().withRawData(true).download(url);
				byte[] byteArray = result.rawData;

				chartFundData = JAXB.unmarshal(new ByteArrayInputStream(byteArray), yokwe.stock.jp.sony.xml.ChartFundData.class);
			} catch(DataBindingException e) {
				logger.error("DataBindingException");
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException("DataBindingException");
			}
			
			List<Price> priceList = new ArrayList<>();
			// Build priceList from chartFundData
			for(yokwe.stock.jp.sony.xml.ChartFundData.Fund.Year year: chartFundData.fund.yearList) {
				for(yokwe.stock.jp.sony.xml.ChartFundData.Fund.Year.Month month: year.monthList) {
					for(yokwe.stock.jp.sony.xml.ChartFundData.Fund.Year.Month.Day day: month.dayList) {
						if (day.price.isEmpty())  continue;
						if (day.volume.isEmpty()) continue;

						// 	public Price(LocalDate date, String isinCode, BigDecimal price, long volume) {
						LocalDate  date  = LocalDate.parse(String.format("%s-%s-%s", day.year, day.month, day.value));
						BigDecimal price = new BigDecimal(day.price);                       // 基準価額
						BigDecimal uam   = new BigDecimal(day.volume).scaleByPowerOfTen(6); // 純資産総額
						BigDecimal unit  = uam.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : uam.divide(price, 0, RoundingMode.HALF_UP);
						
						Price fundPrice = new Price(date, isinCode, fundInfo.currency, price, uam, unit);
						priceList.add(fundPrice);
					}
				}
			}
			logger.info("  save {} {}", priceList.size(), Price.getPath(isinCode));
			Price.save(isinCode, priceList);
		}

		logger.info("STOP");
	}

}

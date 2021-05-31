package yokwe.stock.jp.toushin;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.stock.jp.toushin.Fund.Offer;
import yokwe.util.CSVUtil;
import yokwe.util.DoubleUtil;
import yokwe.util.JapanHoliday;

public class UpdateReport {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateReport.class);

	private static final DateTimeFormatter FILENAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
	
	private static void updateReport(List<Report> reportList, Fund fund, LocalDate firstDate, LocalDate lastDate, int countDays) {
		Report report = new Report();
		report.isinCode = fund.isinCode;
		report.name     = fund.name;
		
		report.issueDate      = fund.issueDate;
		report.redemptionDate = fund.redemptionDate;

		report.cat1 = fund.cat1;
		report.cat2 = fund.cat2;
		report.cat3 = fund.cat3;
		report.cat4 = fund.cat4;
		
		report.initialFeeMin  = fund.initialFeeMin;
		report.initialFeeMax  = fund.initialFeeMax;
		report.cancelationFee = fund.cancelationFee;
		report.redemptionFee  = fund.redemptionFee;
		
		report.priceC      = fund.countPrice;
		report.settlementC = fund.settlementFrequency;
		report.divC        = fund.countDividend;
		
		List<Price>    priceList = Price.load(fund.isinCode).stream().filter(o -> (o.date.equals(firstDate) || o.date.isAfter(firstDate))).collect(Collectors.toList());
		List<Dividend> divList   = Dividend.load(fund.isinCode).stream().filter(o -> (o.date.isAfter(firstDate))).collect(Collectors.toList());
		
		if (priceList.isEmpty()) {
			logger.warn("priceList is empty   {}  {}  {}", fund.isinCode, fund.issuer, fund.name);
			return;
		}
		
		report.priceC = priceList.size() - 1;
		Price lastPrice = priceList.get(report.priceC);
		
		report.price = lastPrice.basePrice.doubleValue();
		report.nav   = lastPrice.netAssetValue.doubleValue();
		report.units = lastPrice.totalUnits.doubleValue();
		
		report.minPrice = priceList.stream().mapToDouble(o -> o.basePrice.doubleValue()).min().getAsDouble();
		report.maxPrice = priceList.stream().mapToDouble(o -> o.basePrice.doubleValue()).max().getAsDouble();
		if (DoubleUtil.isAlmostZero(report.price)) {
			logger.warn("price is almost zero {}  {}  {}", fund.isinCode, fund.issuer, fund.name);
		} else {
			report.minPricePCT = DoubleUtil.round((report.price - report.minPrice) / report.price, 5);
			report.maxPricePCT = DoubleUtil.round((report.maxPrice - report.price) / report.price, 5);
		}

		report.minNav = priceList.stream().mapToDouble(o -> o.netAssetValue.doubleValue()).min().getAsDouble();
		report.maxNav = priceList.stream().mapToDouble(o -> o.netAssetValue.doubleValue()).max().getAsDouble();
		if (DoubleUtil.isAlmostZero(report.nav)) {
			logger.warn("nav is almost zero   {}  {}  {}", fund.isinCode, fund.issuer, fund.name);
			return;
		} else {
			report.minNavPCT = DoubleUtil.round((report.nav - report.minNav) / report.nav, 5);
			report.maxNavPCT = DoubleUtil.round((report.maxNav - report.nav) / report.nav, 5);
		}

		report.minUnits = priceList.stream().mapToDouble(o -> o.totalUnits.doubleValue()).min().getAsDouble();
		report.maxUnits = priceList.stream().mapToDouble(o -> o.totalUnits.doubleValue()).max().getAsDouble();
		if (DoubleUtil.isAlmostZero(report.units)) {
			logger.warn("units is almost zero {}  {}  {}", fund.isinCode, fund.issuer, fund.name);
			return;
		} else {
			report.minUnitsPCT = DoubleUtil.round((report.units - report.minUnits) / report.units, 5);
			report.maxUnitsPCT = DoubleUtil.round((report.maxUnits - report.units) / report.units, 5);
		}
		
		report.divC   = divList.size();
		report.div    = divList.stream().mapToDouble(o -> o.dividend.doubleValue()).sum();
		report.yield  = DoubleUtil.round(report.div / report.price, 5);

		reportList.add(report);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		File file;
		{
			String name = String.format("report-%s.csv", FILENAME_DATE_TIME_FORMATTER.format(LocalDateTime.now()));
			String path = Report.getPath(name);
			file = new File(path);
		}
		logger.info("file {}", file.getPath());
		
		LocalDate lastDate  = JapanHoliday.getLastTradingDate();		
		LocalDate firstDate = lastDate.minusYears(1);
		for(;;) {
			if (JapanHoliday.isClosed(firstDate)) {
				firstDate = firstDate.plusDays(1);
				continue;
			}
			break;
		}
		
		int countDays = 1; // 1 for lastDate
		for(LocalDate date = firstDate; date.isBefore(lastDate); date = date.plusDays(1)) {
			if (!JapanHoliday.isClosed(date)) countDays++;
		}
		logger.info("period {} - {} - {} days", firstDate, lastDate, countDays);
		
		
		List<Fund> list = Fund.load();
		logger.info("list {}", list.size());

		{
			List<Fund> list2 = list.stream().filter(o -> 0 < o.countPrice).collect(Collectors.toList());
			list = list2;
			logger.info("filter 0 < o.countPrice");
			logger.info("list {}", list.size());
		}
		{
			List<Fund> list2 = list.stream().filter(o -> o.offer == Offer.PUBLIC).collect(Collectors.toList());
			list = list2;
			logger.info("filter o.offer == Offer.PUBLIC");
			logger.info("list {}", list.size());
		}
		
		List<Report> reportList = new ArrayList<>();
		
		for(var e: list) {
			updateReport(reportList, e, firstDate, lastDate, countDays);
		}
		
		CSVUtil.write(Report.class).file(file, reportList);
		logger.info("save {} {}", reportList.size(), file.getPath());
		
		logger.info("START");
	}
}

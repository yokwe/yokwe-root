package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.util.DoubleUtil;
import yokwe.util.MarketHoliday;

public class UpdateReport {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final DateTimeFormatter FILENAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
	
	private static void updateReport(List<Report> reportList, Fund fund, LocalDate firstDate, LocalDate lastDate, int countDays) {
		Report report = new Report();
		report.isinCode = fund.isinCode;
		report.name     = fund.name;
		
		report.issueDate      = fund.inceptionDate;
		report.redemptionDate = fund.redemptionDate;

		report.cat1 = fund.fundType;
		report.cat2 = fund.investingAsset;
		report.cat3 = fund.investingArea;
		report.cat4 = fund.indexFundType;
		
		report.initialFeeMin  = BigDecimal.ZERO;
		report.initialFeeMax  = fund.buyFeeMax;
		report.cancelationFee = ""; // FIXME
		report.redemptionFee  = ""; // FIXME
		
		List<Price>    priceList = Price.getList(fund.isinCode).stream().filter(o -> (o.date.equals(firstDate) || o.date.isAfter(firstDate))).collect(Collectors.toList());
		List<Dividend> divList   = Dividend.getList(fund.isinCode).stream().filter(o -> (o.date.isAfter(firstDate))).collect(Collectors.toList());
		
		report.priceC      = priceList.size();
		report.settlementC = fund.divFreq;
		report.divC        = divList.size();
		
		if (priceList.isEmpty()) {
			logger.warn("priceList is empty   {}  {}  {}", fund.isinCode, fund.inceptionDate, fund.name);
			return;
		}
		
		report.priceC = priceList.size() - 1;
		Price lastPrice = priceList.get(report.priceC);
		
		report.price = lastPrice.price.doubleValue();
		report.nav   = lastPrice.nav.doubleValue();
		report.units = lastPrice.units.doubleValue();
		
		report.minPrice = priceList.stream().mapToDouble(o -> o.price.doubleValue()).min().getAsDouble();
		report.maxPrice = priceList.stream().mapToDouble(o -> o.price.doubleValue()).max().getAsDouble();
		if (DoubleUtil.isAlmostZero(report.price)) {
			logger.warn("price is almost zero {}  {}  {}", fund.isinCode, fund.inceptionDate, fund.name);
		} else {
			report.minPricePCT = DoubleUtil.round((report.price - report.minPrice) / report.price, 5);
			report.maxPricePCT = DoubleUtil.round((report.maxPrice - report.price) / report.price, 5);
		}

		report.minNav = priceList.stream().mapToDouble(o -> o.nav.doubleValue()).min().getAsDouble();
		report.maxNav = priceList.stream().mapToDouble(o -> o.nav.doubleValue()).max().getAsDouble();
		if (DoubleUtil.isAlmostZero(report.nav)) {
			logger.warn("nav is almost zero   {}  {}  {}", fund.isinCode, fund.inceptionDate, fund.name);
			return;
		} else {
			report.minNavPCT = DoubleUtil.round((report.nav - report.minNav) / report.nav, 5);
			report.maxNavPCT = DoubleUtil.round((report.maxNav - report.nav) / report.nav, 5);
		}

		report.minUnits = priceList.stream().mapToDouble(o -> o.units.doubleValue()).min().getAsDouble();
		report.maxUnits = priceList.stream().mapToDouble(o -> o.units.doubleValue()).max().getAsDouble();
		if (DoubleUtil.isAlmostZero(report.units)) {
			logger.warn("units is almost zero {}  {}  {}", fund.isinCode, fund.inceptionDate, fund.name);
			return;
		} else {
			report.minUnitsPCT = DoubleUtil.round((report.units - report.minUnits) / report.units, 5);
			report.maxUnitsPCT = DoubleUtil.round((report.maxUnits - report.units) / report.units, 5);
		}
		
		report.divC   = divList.size();
		report.div    = divList.stream().mapToDouble(o -> o.amount.doubleValue()).sum();
		report.yield  = DoubleUtil.round(report.div / report.price, 5);

		// update reportList
		reportList.add(report);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		LocalDate lastDate  = MarketHoliday.JP.getLastTradingDate();		
		LocalDate firstDate = lastDate.minusYears(1);
		for(;;) {
			if (MarketHoliday.JP.isClosed(firstDate)) {
				firstDate = firstDate.plusDays(1);
				continue;
			}
			break;
		}
		
		int countDays = 1; // 1 for lastDate
		for(LocalDate date = firstDate; date.isBefore(lastDate); date = date.plusDays(1)) {
			if (!MarketHoliday.JP.isClosed(date)) countDays++;
		}
		logger.info("period {} - {} - {} days", firstDate, lastDate, countDays);
		
		
		List<Fund> list = Fund.load();
		logger.info("list {}", list.size());

		List<Report> reportList = new ArrayList<>();
		// build reportList
		for(var e: list) {
			updateReport(reportList, e, firstDate, lastDate, countDays);
		}
		
		// save
		{
			String name = String.format("report-%s.csv", FILENAME_DATE_TIME_FORMATTER.format(LocalDateTime.now()));
			logger.info("save {} {}", reportList.size(), Report.getPath(name));
			Report.save(reportList, name);
		}
		
		logger.info("START");
	}
}

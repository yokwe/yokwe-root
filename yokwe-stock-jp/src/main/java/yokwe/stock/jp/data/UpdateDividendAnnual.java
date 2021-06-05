package yokwe.stock.jp.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.jp.tdnet.Category;
import yokwe.stock.jp.tdnet.Consolidate;
import yokwe.stock.jp.tdnet.Detail;
import yokwe.stock.jp.tdnet.Period;
import yokwe.stock.jp.tdnet.SummaryFilename;
import yokwe.stock.jp.xbrl.tdnet.report.REITReport;
import yokwe.stock.jp.xbrl.tdnet.report.StockReport;
import yokwe.util.DoubleUtil;
import yokwe.util.UnexpectedException;

public class UpdateDividendAnnual {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateDividendAnnual.class);
	
	private static LocalDate getFirstDate(LocalDate lastDate) {
		LocalDate firstDate = lastDate.minusYears(1).plusMonths(1);
		return LocalDate.of(firstDate.getYear(), firstDate.getMonthValue(), 1);
	}

	public static void main(String[] args) {
		logger.info("START");
		
		Map<String, DividendAnnual> stockCodeMap = new TreeMap<>();
		// key is stockCode
		
		// From StcokReport
		{
			Map<String, List<StockReport>> map = new TreeMap<>();
			// key is stockCode
			// build map
			{
				Map<String, StockReport> reportMap = StockReport.getMap();
				logger.info("StockReport reportMap {}", reportMap.size());
				
				int size  = reportMap.size();
				int count = 0;
				for(Map.Entry<String, StockReport> entry: reportMap.entrySet()) {
					String      key   = entry.getKey();
					StockReport value = entry.getValue();
					if ((count % 1000) == 0) {
						logger.info("{} {}", String.format("%5d / %5d", count, size), key);
					}
					count++;
					
					// Skip no dividend record
					if (value.dividendPayableDateAsPlanned.isEmpty()) continue;
					
					final String stockCode = value.stockCode;
					List<StockReport> list;
					if (map.containsKey(stockCode)) {
						list = map.get(stockCode);
					} else {
						list = new ArrayList<>();
						map.put(stockCode, list);
					}
					list.add(value);
				}
			}
			// build stockCodeMap from map
			{
				for(Map.Entry<String, List<StockReport>> entry: map.entrySet()) {
					String            stockCode = entry.getKey();
					List<StockReport> list      = entry.getValue();
					
					{
						Map<String, StockReport> keyMap = new TreeMap<>();
						Collections.sort(list);
						for(StockReport e: list) {
							String key = String.format("%s %d", e.yearEnd, e.quarterlyPeriod);
							keyMap.put(key, e);
						}
						list = new ArrayList<>(keyMap.values());
						Collections.sort(list);
					}
					
					StockReport last = list.get(list.size() - 1);
					LocalDate lastDate  = LocalDate.parse(last.dividendPayableDateAsPlanned).plusDays(1);
					LocalDate firstDate = getFirstDate(lastDate).minusDays(1);
					
					double dividend = last.annualDividendPerShare.doubleValue();
					int    divCount = 0;
					String yearEnd  = last.yearEnd;
					int    quarter  = last.quarterlyPeriod;
					String filename = last.filename;
					
					for(StockReport e: list) {
						LocalDate date = LocalDate.parse(e.dividendPayableDateAsPlanned);
						
						if (date.isAfter(firstDate) && date.isBefore(lastDate)) {
							divCount++;
						}
					}
					
					DividendAnnual dividendAnnual = new DividendAnnual(stockCode, dividend, divCount, yearEnd, quarter, filename);
					if (stockCodeMap.containsKey(stockCode)) {
						logger.error("Duplicate key {}", stockCode);
						logger.error("  old {}", map.get(stockCode));
						logger.error("  new {}", dividendAnnual);
						throw new UnexpectedException("Duplicate key");
					} else {
						stockCodeMap.put(stockCode, dividendAnnual);
					}
				}
			}
		}
		
		// From StcokREIT
		{
			Map<String, List<REITReport>> map = new TreeMap<>();
			// key is stockCode
			// build map
			{
				Map<String, REITReport> reportMap = REITReport.getMap();
				logger.info("REITReport  reportMap {}", reportMap.size());
				
				int size  = reportMap.size();
				int count = 0;
				for(Map.Entry<String, REITReport> entry: reportMap.entrySet()) {
					String     key   = entry.getKey();
					REITReport value = entry.getValue();
					if ((count % 1000) == 0) {
						logger.info("{} {}", String.format("%5d / %5d", count, size), key);
					}
					count++;
					
					// Skip no distribution record
					if (value.distributionsDate.isEmpty()) continue;
					
					final String stockCode = value.stockCode;
					List<REITReport> list;
					if (map.containsKey(stockCode)) {
						list = map.get(stockCode);
					} else {
						list = new ArrayList<>();
						map.put(stockCode, list);
					}
					list.add(value);
				}
			}
			// build stockCodeMap from map
			{
				for(Map.Entry<String, List<REITReport>> entry: map.entrySet()) {
					String           stockCode = entry.getKey();
					List<REITReport> list      = entry.getValue();
					
					REITReport last = list.get(list.size() - 1);
					LocalDate lastDate  = LocalDate.parse(last.distributionsDate).plusDays(1);
					LocalDate firstDate = getFirstDate(lastDate).minusDays(1);
					
					double dividend = 0;
					int    divCount = 0;
					String yearEnd  = last.yearEnd;
					int    quarter  = 4;
					String filename = last.filename;
					
					for(REITReport e: list) {
						LocalDate date = LocalDate.parse(e.distributionsDate);
						
						if (date.isAfter(firstDate) && date.isBefore(lastDate)) {
							divCount++;
							dividend += e.distributionsPerUnit.doubleValue() + e.distributionsInExcessOfProfitPerUnit.doubleValue();
						}
					}
					
					DividendAnnual dividendAnnual = new DividendAnnual(stockCode, dividend, divCount, yearEnd, quarter, filename);
					if (stockCodeMap.containsKey(stockCode)) {
						logger.error("Duplicate key {}", stockCode);
						logger.error("  old {}", map.get(stockCode));
						logger.error("  new {}", dividendAnnual);
						throw new UnexpectedException("Duplicate key");
					} else {
						stockCodeMap.put(stockCode, dividendAnnual);
					}
				}
			}
		}
		
		// From DividendETF
		{
			Map<String, List<DividendETF>> map = DividendETF.getMap();
			// build stockCodeMap from map
			{
				for(Map.Entry<String, List<DividendETF>> entry: map.entrySet()) {
					String            stockCode = entry.getKey();
					List<DividendETF> list      = entry.getValue();
					
					DividendETF last = list.get(list.size() - 1);
					LocalDate lastDate  = LocalDate.parse(last.date).plusDays(1);
					LocalDate firstDate = getFirstDate(lastDate).minusDays(1);
					
					double          dividend = 0;
					int             divCount = 0;
					String          yearEnd  = last.date;
					int             quarter  = 0;
					
					//   tse-qcedjpsm-71770-20170725371770-ixbrl.htm

					String tdnetCode = String.format("%s0", stockCode);
					String id = String.format("%s399999", last.record);
					final String filename = new SummaryFilename(Period.ANNUAL.value, Consolidate.NOT_CONSOLIDATE.value, Category.EFJP.value, Detail.SUMMARY.value, tdnetCode, id).toString();
					
					for(DividendETF e: list) {
						LocalDate date = LocalDate.parse(e.date);
						
						if (date.isAfter(firstDate) && (date.isBefore(lastDate) || date.isEqual(lastDate))) {
							dividend += (e.dividend / e.unit);
							divCount++;
						}
					}
					dividend = DoubleUtil.round(dividend, 2);

					DividendAnnual dividendAnnual = new DividendAnnual(stockCode, dividend, divCount, yearEnd, quarter, filename);
					if (stockCodeMap.containsKey(stockCode)) {
						logger.error("Duplicate key {}", stockCode);
						logger.error("  old {}", map.get(stockCode));
						logger.error("  new {}", dividendAnnual);
						throw new UnexpectedException("Duplicate key");
					} else {
						stockCodeMap.put(stockCode, dividendAnnual);
					}
				}
			}
		}

		logger.info("stockCodeMap {}", stockCodeMap.size());
		{
			List<DividendAnnual> dividendList = new ArrayList<>(stockCodeMap.values());
			logger.info("save {}  {}", DividendAnnual.PATH_FILE, dividendList.size());
			DividendAnnual.save(dividendList);
			logger.info("dividendList {}", dividendList.size());
		}

		logger.info("STOP");
	}
}

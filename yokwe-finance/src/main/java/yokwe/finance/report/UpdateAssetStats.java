package yokwe.finance.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.account.Asset;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.UpdateAssetAll;
import yokwe.finance.fx.StorageFX;
import yokwe.finance.report.AssetStats.DailyCompanyGeneralReport;
import yokwe.finance.report.AssetStats.DailyCompanyProductReport;
import yokwe.finance.report.AssetStats.DailyTotalCompanyReport;
import yokwe.finance.type.FXRate;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateAssetStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_TEMPLATE  = StringUtil.toURLString("data/form/ASSET_STATS.ods");
	
	private static final int PERIOD_YEAR = 1;
	
	private static final String COMPANY_GRAND_TOTAL = "＊総合計＊";
	
	private static void buildDailyCompanyProductReportList(
			Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap,
			List<DailyCompanyProductReport> valueList, List<DailyCompanyProductReport> percentList) {
		valueList.clear();
		percentList.clear();
		
		var dateList = fxRateMap.keySet().stream().collect(Collectors.toList());
		Collections.sort(dateList);
		for(var date: dateList) {
			var assetList = assetMap.get(date);
			var fxRate    = fxRateMap.get(date);
			if (assetList == null) continue;
			if (fxRate == null) continue;
			var usdRate = fxRate.usd.doubleValue();
			
			var map = new TreeMap<Company, DailyCompanyProductReport>();
			for(var e: Company.values()) {
				var report = new DailyCompanyProductReport();
				report.date    = date.toString();
				report.company = e.description;
				map.put(e, report);
			}
			var grandTotal = new DailyCompanyProductReport();
			grandTotal.date    = date.toString();
			grandTotal.company = COMPANY_GRAND_TOTAL;
			
			for(var asset: assetList) {
				var value    = asset.value.doubleValue();
				var currency = asset.currency;
				var valueJPY = value * fxRate.rate(currency).doubleValue();
				var company  = asset.company;
				var product  = asset.product;
				
				var report = map.get(company);
				
				report.total += valueJPY;
				
				switch(currency) {
				case JPY:
					report.totalJPY += value;
					
					switch(product) {
					case DEPOSIT:
						report.depositJPY += value;
						break;
					case TERM_DEPOSIT:
						report.termJPY += value;
						break;
					case STOCK:
						report.stockJPY += value;
						break;
					case FUND:
						report.fundJPY += value;
						break;
					case BOND:
						report.bondJPY += value;
						break;
					default:
						logger.error("Unexpected product");
						logger.error("  asset  {}", asset);
						throw new UnexpectedException("Unexpected product");
					}
					break;
				case USD:
					report.totalUSD += value;
					
					switch(product) {
					case DEPOSIT:
						report.depositUSD += value;
						break;
					case TERM_DEPOSIT:
						report.termUSD += value;
						break;
					case STOCK:
						report.stockUSD += value;
						break;
					case FUND:
						report.fundUSD += value;
						break;
					case BOND:
						report.bondUSD += value;
						break;
					default:
						logger.error("Unexpected product");
						logger.error("  asset  {}", asset);
						throw new UnexpectedException("Unexpected product");
					}
					break;
				default:
					logger.error("Unexpected currency");
					logger.error("  asset  {}", asset);
					throw new UnexpectedException("Unexpected currency");
				}
			}
			for(var company: Company.values()) {
				var report = map.get(company);
				valueList.add(report);
				grandTotal.add(report);
			}
			valueList.add(grandTotal);
			
			for(var company: Company.values()) {
				percentList.add(map.get(company).percent(grandTotal.total, usdRate));
			}
			percentList.add(grandTotal.percent(grandTotal.total, usdRate));
		}
	}
	private static void buildDailyCompanyGeneralReportList (
		Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap,
		List<DailyCompanyGeneralReport> valueList, List<DailyCompanyGeneralReport> percentList) {
		valueList.clear();
		percentList.clear();
		
		var dateList = fxRateMap.keySet().stream().collect(Collectors.toList());
		Collections.sort(dateList);
		for(var date: dateList) {
			var assetList = assetMap.get(date);
			var fxRate    = fxRateMap.get(date);
			if (assetList == null) continue;
			if (fxRate == null) continue;
			
			var map = new TreeMap<Company, DailyCompanyGeneralReport>();
			for(var e: Company.values()) {
				var report = new DailyCompanyGeneralReport();
				report.date    = date.toString();
				report.company = e.description;
				map.put(e, report);
			}
			var grandTotal = new DailyCompanyGeneralReport();
			grandTotal.date    = date.toString();
			grandTotal.company = COMPANY_GRAND_TOTAL;
			
			for(var asset: assetList) {
				var value    = asset.value.doubleValue();
				var currency = asset.currency;
				var valueJPY = value * fxRate.rate(currency).doubleValue();
				var company  = asset.company;
				var product  = asset.product;
				var risk     = asset.risk;
				
				var report = map.get(company);
				
				report.total += valueJPY;
				
				switch(currency) {
				case JPY:
					report.jpy += value;
					break;
				case USD:
					report.usdJPY += valueJPY;
					report.usd    += value;
					break;
				default:
					logger.error("Unexpected currency");
					logger.error("  asset  {}", asset);
					throw new UnexpectedException("Unexpected currency");
				}
				
				switch(risk) {
				case SAFE:
					report.safe += valueJPY;
					break;
				case UNSAFE:
					report.unsafe += valueJPY;
					break;
				default:
					logger.error("Unexpected risk");
					logger.error("  asset  {}", asset);
					throw new UnexpectedException("Unexpected risk");
				}
				
				switch(product) {
				case DEPOSIT:
					report.deposit += valueJPY;
					break;
				case TERM_DEPOSIT:
					report.term += valueJPY;
					break;
				case STOCK:
					report.stock += valueJPY;
					break;
				case FUND:
					report.fund += valueJPY;
					break;
				case BOND:
					report.bond += valueJPY;
					break;
				default:
					logger.error("Unexpected product");
					logger.error("  asset  {}", asset);
					throw new UnexpectedException("Unexpected product");
				}
			}
			for(var company: Company.values()) {
				var report = map.get(company);
				grandTotal.add(report);
				valueList.add(report);
			}
			valueList.add(grandTotal);
			
			for(var company: Company.values()) {
				percentList.add(map.get(company).percent(grandTotal.total));
			}
			percentList.add(grandTotal.percent(grandTotal.total));
		}
	}
	private static void buildDailyTotalCompanyReportList  (
		Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap,
		List<DailyTotalCompanyReport> valueList, List<DailyTotalCompanyReport> percentList) {
		valueList.clear();
		percentList.clear();
		
		var dateList = fxRateMap.keySet().stream().collect(Collectors.toList());
		Collections.sort(dateList);
		for(var date: dateList) {
			var assetList = assetMap.get(date);
			var fxRate    = fxRateMap.get(date);
			if (assetList == null) continue;
			if (fxRate == null) continue;
			
			double total   = 0;
			double sony    = 0;
			double smbc    = 0;
			double prestia = 0;
			double smtb    = 0;
			double rakuten = 0;
			double nikko   = 0;
			double sbi     = 0;
			
			for(var asset: assetList) {
				var value    = asset.value.doubleValue();
				var currency = asset.currency;
				var company  = asset.company;
				
				switch(currency) {
				case JPY:
				case USD:
					break;
				default:
					logger.error("Unexpected currency");
					logger.error("  asset  {}", asset);
					throw new UnexpectedException("Unexpected currency");
				}
				
				var valueJPY = value * fxRate.rate(currency).doubleValue();
				
				total += valueJPY;
				
				switch(company) {
				case SONY:
					sony += valueJPY;
					break;
				case SMBC:
					smbc += valueJPY;
					break;
				case PRESTIA:
					prestia += valueJPY;
					break;
				case SMTB:
					smtb += valueJPY;
					break;
				case RAKUTEN:
					rakuten += valueJPY;
					break;
				case NIKKO:
					nikko += valueJPY;
					break;
				case SBI:
					sbi += valueJPY;
					break;
				default:
					logger.error("Unexpected company");
					logger.error("  asset  {}", asset);
					throw new UnexpectedException("Unexpected company");
				}
			}
			
			var company = new DailyTotalCompanyReport(
				date.toString(),
				total, sony, smbc, prestia, smtb, rakuten, nikko, sbi
			);
			
			valueList.add(company);
		}
		for(var e: valueList) {
			percentList.add(e.percent(e.total));
		}
	}
	
	
	
	
	
	private static void update() {
		var today = LocalDate.now();
		
		Map<LocalDate, FXRate> fxRateMap = StorageFX.FXRate.getList().stream().filter(o -> o.date.isAfter(today.minusYears(PERIOD_YEAR))).collect(Collectors.toMap(o -> o.date, Function.identity()));
		Map<LocalDate, List<Asset>> assetMap = new TreeMap<LocalDate, List<Asset>>();
		// build assetMap
		{
			int year = today.getYear();
			
			// this year
			var assetList = new ArrayList<Asset>();
			assetList.addAll(UpdateAssetAll.getList(year - 1));
			assetList.addAll(UpdateAssetAll.getList(year));
			Collections.sort(assetList);
			
			for(var e: assetList) {
				var date = e.date;
				
				if (fxRateMap.containsKey(date)) {
					List<Asset> list = assetMap.get(date);
					if (list == null) {
						list = new ArrayList<>();
						assetMap.put(date, list);
					}
					list.add(e);
				}
			}
		}
		
		{
			String urlReport;
			{
				String timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
				String name       = String.format("asset-stats-%s.ods", timestamp);
				String pathReport = StorageReport.storage.getPath("asset-stats", name);
				
				urlReport  = StringUtil.toURLString(pathReport);
			}

			logger.info("urlReport {}", urlReport);
			logger.info("docLoad   {}", URL_TEMPLATE);
			try {
				// start LibreOffice process
				LibreOffice.initialize();
				
				SpreadSheet docLoad = new SpreadSheet(URL_TEMPLATE, true);
				SpreadSheet docSave = new SpreadSheet();
								
				// DAILY COMPANY PRODUCT VALUE
				{
					var priceList   = new ArrayList<DailyCompanyProductReport>();
					var percentList = new ArrayList<DailyCompanyProductReport>();
					buildDailyCompanyProductReportList(fxRateMap, assetMap, priceList, percentList);
					{
						var sheetNameOld = DailyCompanyProductReport.SHEET_NAME_VALUE;
						var sheetNameNew = "日付　会社　商品　金額";
						var list         = priceList;

						logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
						docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
						Sheet.fillSheet(docSave, list, sheetNameOld);
						docSave.renameSheet(sheetNameOld, sheetNameNew);
					}
					{
						var sheetNameOld = DailyCompanyProductReport.SHEET_NAME_PERCENT;
						var sheetNameNew = "日付　会社　商品　割合";
						var list         = percentList;

						logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
						docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
						Sheet.fillSheet(docSave, list, sheetNameOld);
						docSave.renameSheet(sheetNameOld, sheetNameNew);
					}
				}
				// DAILY COMPANY GENERAL VALUE
				{
					var priceList   = new ArrayList<DailyCompanyGeneralReport>();
					var percentList = new ArrayList<DailyCompanyGeneralReport>();
					buildDailyCompanyGeneralReportList(fxRateMap, assetMap, priceList, percentList);
					{
						var sheetNameOld = DailyCompanyGeneralReport.SHEET_NAME_VALUE;
						var sheetNameNew = "日付　会社　概要　金額";
						var list         = priceList;

						logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
						docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
						Sheet.fillSheet(docSave, list, sheetNameOld);
						docSave.renameSheet(sheetNameOld, sheetNameNew);
					}
					{
						var sheetNameOld = DailyCompanyGeneralReport.SHEET_NAME_PERCENT;
						var sheetNameNew = "日付　会社　概要　割合";
						var list         = percentList;

						logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
						docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
						Sheet.fillSheet(docSave, list, sheetNameOld);
						docSave.renameSheet(sheetNameOld, sheetNameNew);
					}
				}
				// buildDailyTotalCompanyReportList
				// DAILY COMPANY GENERAL VALUE
				{
					var priceList   = new ArrayList<DailyTotalCompanyReport>();
					var percentList = new ArrayList<DailyTotalCompanyReport>();
					buildDailyTotalCompanyReportList(fxRateMap, assetMap, priceList, percentList);
					{
						var sheetNameOld = DailyTotalCompanyReport.SHEET_NAME_VALUE;
						var sheetNameNew = "日付　合計　会社　金額";
						var list         = priceList;

						logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
						docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
						Sheet.fillSheet(docSave, list, sheetNameOld);
						docSave.renameSheet(sheetNameOld, sheetNameNew);
					}
					{
						var sheetNameOld = DailyTotalCompanyReport.SHEET_NAME_PERCENT;
						var sheetNameNew = "日付　合計　会社　割合";
						var list         = percentList;

						logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
						docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
						Sheet.fillSheet(docSave, list, sheetNameOld);
						docSave.renameSheet(sheetNameOld, sheetNameNew);
					}
				}
				
				// remove first sheet
				docSave.removeSheet(docSave.getSheetName(0));

				docSave.store(urlReport);
				logger.info("output    {}", urlReport);
				
				docLoad.close();
				logger.info("close     docLoad");
				docSave.close();
				logger.info("close     docSave");
			} finally {
				// stop LibreOffice process
				LibreOffice.terminate();
			}
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}

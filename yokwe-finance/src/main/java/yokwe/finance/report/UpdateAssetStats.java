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
import yokwe.finance.report.AssetStats.CompanyGeneralReport;
import yokwe.finance.report.AssetStats.CompanyProductReport;
import yokwe.finance.report.AssetStats.DailyCompanyReport;
import yokwe.finance.report.AssetStats.DailyProductReport;
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
	
	private static List<CompanyGeneralReport> getCompanyGeneralReportList(Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap) {
		var list   = new ArrayList<CompanyGeneralReport>();
		
		var date      = fxRateMap.keySet().stream().max(LocalDate::compareTo).get();
		var fxRate    = fxRateMap.get(date);
		var assetList = assetMap.get(date);
		
		for(var company: Company.values()) {
			double total   = 0;
			double jpy     = 0;
			double usdJPY  = 0;
			double usd     = 0;
			double safe    = 0;
			double unsafe  = 0;
			double deposit = 0;
			double term    = 0;
			double fund    = 0;
			double stock   = 0;
			double bond    = 0;
			
			for(var asset: assetList) {
				if (asset.company != company) continue;
				
				var value    = asset.value.doubleValue();
				var currency = asset.currency;
				var valueJPY = value * fxRate.rate(currency).doubleValue();
				var risk     = asset.risk;
				var product  = asset.product;
				
				total += valueJPY;
				
				switch(currency) {
				case JPY:
					jpy += value;
					break;
				case USD:
					usdJPY += valueJPY;
					usd    += value;
					break;
				default:
					logger.error("Unexpected currency");
					logger.error("  asset  {}", asset);
					throw new UnexpectedException("Unexpected currency");
				}
				
				switch(risk) {
				case SAFE:
					safe += valueJPY;
					break;
				case UNSAFE:
					unsafe += valueJPY;
					break;
				default:
					logger.error("Unexpected risk");
					logger.error("  asset  {}", asset);
					throw new UnexpectedException("Unexpected risk");
				}
				
				switch(product) {
				case DEPOSIT:
					deposit += valueJPY;
					break;
				case TERM_DEPOSIT:
					term += valueJPY;
					break;
				case STOCK:
					stock += valueJPY;
					break;
				case FUND:
					fund += valueJPY;
					break;
				case BOND:
					bond += valueJPY;
					break;
				default:
					logger.error("Unexpected product");
					logger.error("  asset  {}", asset);
					throw new UnexpectedException("Unexpected product");
				}
			}
			
			var report = new CompanyGeneralReport(
				company.description, total,
				jpy, usdJPY, usd, safe, unsafe,
				deposit, term, fund, stock, bond
			);
			list.add(report);
		}
		{
			double total   = 0;
			double jpy     = 0;
			double usdJPY  = 0;
			double usd     = 0;
			double safe    = 0;
			double unsafe  = 0;
			double deposit = 0;
			double term    = 0;
			double fund    = 0;
			double stock   = 0;
			double bond    = 0;
			
			for(var e: list) {
				total   += e.total;
				jpy     += e.jpy;
				usdJPY  += e.usdJPY;
				usd     += e.usd;
				safe    += e.safe;
				unsafe  += e.unsafe;
				deposit += e.deposit;
				term    += e.term;
				fund    += e.fund;
				stock   += e.stock;
				bond    += e.bond;
			}
			var report = new CompanyGeneralReport(
				date.toString(), total,
				jpy, usdJPY, usd, safe, unsafe,
				deposit, term, fund, stock, bond
			);
			list.add(report);
		}
		
		return list;
	}
	
	private static List<DailyCompanyReport> getDailyCompanyReportList(Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap) {
		var list = new ArrayList<DailyCompanyReport>();
		
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
			
			var company = new DailyCompanyReport(
				date.toString(),
				total, sony, smbc, prestia, smtb, rakuten, nikko, sbi
			);
			
			list.add(company);
		}
		
		return list;
	}
	
	private static List<CompanyProductReport> getCompanyProductReportList(Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap) {
		var list = new ArrayList<CompanyProductReport>();
		
		var date      = fxRateMap.keySet().stream().max(LocalDate::compareTo).get();
		var fxRate    = fxRateMap.get(date);
		var assetList = assetMap.get(date);
		
		for(var company: Company.values()) {
			double total       = 0;
			
			double totalJPY    = 0;
			double depositJPY  = 0;
			double termJPY     = 0;
			double fundJPY     = 0;
			double stockJPY    = 0;
			double bondJPY     = 0;
			
			double totalUSD    = 0;
			double depositUSD  = 0;
			double termUSD     = 0;
			double fundUSD     = 0;
			double stockUSD    = 0;
			double bondUSD     = 0;
			
			for(var asset: assetList) {
				if (asset.company != company) continue;
				
				var value    = asset.value.doubleValue();
				var currency = asset.currency;
				var valueJPY = value * fxRate.rate(currency).doubleValue();
				var product  = asset.product;
				
				total += valueJPY;
				
				switch(currency) {
				case JPY:
					totalJPY += value;
					
					switch(product) {
					case DEPOSIT:
						depositJPY += value;
						break;
					case TERM_DEPOSIT:
						termJPY += value;
						break;
					case STOCK:
						stockJPY += value;
						break;
					case FUND:
						fundJPY += value;
						break;
					case BOND:
						bondJPY += value;
						break;
					default:
						logger.error("Unexpected product");
						logger.error("  asset  {}", asset);
						throw new UnexpectedException("Unexpected product");
					}
					break;
				case USD:
					totalUSD += value;
					
					switch(product) {
					case DEPOSIT:
						depositUSD += value;
						break;
					case TERM_DEPOSIT:
						termUSD += value;
						break;
					case STOCK:
						stockUSD += value;
						break;
					case FUND:
						fundUSD += value;
						break;
					case BOND:
						bondUSD += value;
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
			
			var companyProduct = new CompanyProductReport(
				company.description, total,
				totalJPY, depositJPY, termJPY, fundJPY, stockJPY, bondJPY,
				totalUSD, depositUSD, termUSD, fundUSD, stockUSD, bondUSD
			);
			list.add(companyProduct);
		}
		{
			double total       = 0;
			
			double totalJPY    = 0;
			double depositJPY  = 0;
			double termJPY     = 0;
			double fundJPY     = 0;
			double stockJPY    = 0;
			double bondJPY     = 0;
			
			double totalUSD    = 0;
			double depositUSD  = 0;
			double termUSD     = 0;
			double fundUSD     = 0;
			double stockUSD    = 0;
			double bondUSD     = 0;
			
			for(var e: list) {
				total += e.total;
				
				totalJPY    += e.totalJPY;
				depositJPY  += e.depositJPY ;
				termJPY     += e.termJPY;
				fundJPY     += e.fundJPY;
				stockJPY    += e.stockJPY;
				bondJPY     += e.bondJPY;
				
				totalUSD    += e.totalUSD;
				depositUSD  += e.depositUSD;
				termUSD     += e.termUSD;
				fundUSD     += e.fundUSD;
				stockUSD    += e.stockUSD;
				bondUSD     += e.bondUSD;
			}
			
			var companyProduct = new CompanyProductReport(
				date.toString(), total,
				totalJPY, depositJPY, termJPY, fundJPY, stockJPY, bondJPY,
				totalUSD, depositUSD, termUSD, fundUSD, stockUSD, bondUSD
			);
			list.add(companyProduct);
		}
		return list;
	}
	
	private static List<DailyProductReport> getDailyProductReportList(Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap) {
		var list = new ArrayList<DailyProductReport>();
		
		var dateList = fxRateMap.keySet().stream().collect(Collectors.toList());
		Collections.sort(dateList);
		for(var date: dateList) {
			var assetList = assetMap.get(date);
			var fxRate    = fxRateMap.get(date);
			if (assetList == null) continue;
			if (fxRate == null) continue;
			
			double total       = 0;
			
			double totalJPY    = 0;
			double depositJPY  = 0;
			double termJPY     = 0;
			double fundJPY     = 0;
			double stockJPY    = 0;
			double bondJPY     = 0;
			
			double totalUSD    = 0;
			double depositUSD  = 0;
			double termUSD     = 0;
			double fundUSD     = 0;
			double stockUSD    = 0;
			double bondUSD     = 0;
			
			for(var asset: assetList) {
				var value    = asset.value.doubleValue();
				var currency = asset.currency;
				var valueJPY = value * fxRate.rate(currency).doubleValue();
				var product  = asset.product;
				
				total += valueJPY;
				
				switch(currency) {
				case JPY:
					totalJPY += value;
					
					switch(product) {
					case DEPOSIT:
						depositJPY += value;
						break;
					case TERM_DEPOSIT:
						termJPY += value;
						break;
					case STOCK:
						stockJPY += value;
						break;
					case FUND:
						fundJPY += value;
						break;
					case BOND:
						bondJPY += value;
						break;
					default:
						logger.error("Unexpected product");
						logger.error("  asset  {}", asset);
						throw new UnexpectedException("Unexpected product");
					}
					break;
				case USD:
					totalUSD += value;
					
					switch(product) {
					case DEPOSIT:
						depositUSD += value;
						break;
					case TERM_DEPOSIT:
						termUSD += value;
						break;
					case STOCK:
						stockUSD += value;
						break;
					case FUND:
						fundUSD += value;
						break;
					case BOND:
						bondUSD += value;
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
			var companyProduct = new DailyProductReport(
				date.toString(), total,
				totalJPY, depositJPY, termJPY, fundJPY, stockJPY, bondJPY,
				totalUSD, depositUSD, termUSD, fundUSD, stockUSD, bondUSD
			);
			list.add(companyProduct);
		}
		
		return list;
	}
	
	private static Map<LocalDate, List<Asset>> filter(Map<LocalDate, List<Asset>> assetMap, Company company) {
		var result = new TreeMap<LocalDate, List<Asset>>();
		
		for(var e: assetMap.entrySet()) {
			var key   = e.getKey();
			var value = e.getValue();
			
			var list = new ArrayList<Asset>(value);
			list.removeIf(o -> o.company != company);
			
			result.put(key, list);
		}
		
		return result;
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
			var companyGeneralList = getCompanyGeneralReportList(fxRateMap, assetMap);
			var dailyCompnaylList  = getDailyCompanyReportList(fxRateMap, assetMap);
			var companyProductList = getCompanyProductReportList(fxRateMap, assetMap);
			var dailyProductList   = getDailyProductReportList(fxRateMap, assetMap);

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
				
				// COMPANY GENERAL vALUE
				{
					var sheetNameOld = CompanyGeneralReport.SHEET_NAME_VALUE;
					var sheetNameNew = "会社　概要　金額";
					var list         = companyGeneralList;

					logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
					docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
					Sheet.fillSheet(docSave, list, sheetNameOld);
					docSave.renameSheet(sheetNameOld, sheetNameNew);
				}
				// COMPANY GENERAL PERCENT
				{
					var sheetNameOld = CompanyGeneralReport.SHEET_NAME_PERCENT;
					var sheetNameNew = "会社　概要　割合";
					var list         = new ArrayList<CompanyGeneralReport>();
					{
						var grandTotal = companyGeneralList.get(companyGeneralList.size() - 1).total;
						for(var e: companyGeneralList) {
							double total   = e.total   / grandTotal;
							double jpy     = e.jpy     / grandTotal;
							double usdJPY  = e.usdJPY  / grandTotal;
							double usd     = e.usd;
							double safe    = e.safe    / grandTotal;
							double unsafe  = e.unsafe  / grandTotal;
							double deposit = e.deposit / grandTotal;
							double term    = e.term    / grandTotal;
							double fund    = e.fund    / grandTotal;
							double stock   = e.stock   / grandTotal;
							double bond    = e.bond    / grandTotal;
							
							var report = new CompanyGeneralReport(
								e.company, total,
								jpy, usdJPY, usd, safe, unsafe,
								deposit, term, fund, stock, bond
							);
							list.add(report);
						}
					}

					logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
					docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
					Sheet.fillSheet(docSave, list, sheetNameOld);
					docSave.renameSheet(sheetNameOld, sheetNameNew);
				}
				// DAILY COMPANY VALUE
				{
					var sheetNameOld = DailyCompanyReport.SHEET_NAME_VALUE;
					var sheetNameNew = "日付　会社　金額";
					var list         = dailyCompnaylList;

					logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
					docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
					Sheet.fillSheet(docSave, list, sheetNameOld);
					docSave.renameSheet(sheetNameOld, sheetNameNew);
				}
				// DAILY COMPANY PERCENT
				{
					var sheetNameOld = DailyCompanyReport.SHEET_NAME_PERCENT;
					var sheetNameNew = "日付　概要　割合";
					var list         = new ArrayList<DailyCompanyReport>();
					{
						for(var e: dailyCompnaylList) {
							double total   = e.total   / e.total;
							double sony    = e.sony    / e.total;
							double smbc    = e.smbc    / e.total;
							double prestia = e.prestia / e.total;
							double smtb    = e.smtb    / e.total;
							double rakuten = e.rakuten / e.total;
							double nikko   = e.nikko   / e.total;
							double sbi     = e.sbi     / e.total;
							
							var company = new DailyCompanyReport(
								e.date,
								total, sony, smbc, prestia, smtb, rakuten, nikko, sbi
							);
							list.add(company);
						}
					}

					logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
					docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
					Sheet.fillSheet(docSave, list, sheetNameOld);
					docSave.renameSheet(sheetNameOld, sheetNameNew);
				}
				
				
				// COMPANY PRODUCT VALUE
				{
					var sheetNameOld = CompanyProductReport.SHEET_NAME_VALUE;
					var sheetNameNew = "会社　商品　金額";
					var list         = companyProductList;

					logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
					docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
					Sheet.fillSheet(docSave, list, sheetNameOld);
					docSave.renameSheet(sheetNameOld, sheetNameNew);
				}
				// COMPANY PRODUCT PERCENT
				{
					var sheetNameOld = CompanyProductReport.SHEET_NAME_PERCENT;
					var sheetNameNew = "会社　商品　割合";
					var list         = new ArrayList<CompanyProductReport>();
					{
						var last = companyProductList.get(companyProductList.size() - 1);
						var date = LocalDate.parse(last.company);
						var usdRate = fxRateMap.get(date).usd.doubleValue();
						
						var grandTotal = companyProductList.get(companyProductList.size() - 1).total;
						for(var e: companyProductList) {
							double total       = e.total / grandTotal;
							
							double totalJPY    = e.totalJPY   / grandTotal;
							double depositJPY  = e.depositJPY / grandTotal;
							double termJPY     = e.termJPY    / grandTotal;
							double fundJPY     = e.fundJPY    / grandTotal;
							double stockJPY    = e.stockJPY   / grandTotal;
							double bondJPY     = e.bondJPY    / grandTotal;
							
							double totalUSD    = e.totalUSD   * usdRate / grandTotal;
							double depositUSD  = e.depositUSD * usdRate / grandTotal;
							double termUSD     = e.termUSD    * usdRate / grandTotal;
							double fundUSD     = e.fundUSD    * usdRate / grandTotal;
							double stockUSD    = e.stockUSD   * usdRate / grandTotal;
							double bondUSD     = e.bondUSD    * usdRate / grandTotal;
							
							var report = new CompanyProductReport(
								e.company, total,
								totalJPY, depositJPY, termJPY, fundJPY, stockJPY, bondJPY,
								totalUSD, depositUSD, termUSD, fundUSD, stockUSD, bondUSD
							);
							list.add(report);
						}
					}

					logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
					docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
					Sheet.fillSheet(docSave, list, sheetNameOld);
					docSave.renameSheet(sheetNameOld, sheetNameNew);
				}
				
				// DAILY PRODUCT VALUE
				{
					var sheetNameOld = DailyProductReport.SHEET_NAME_VALUE;
					var sheetNameNew = "日付　商品　金額";
					var list         = dailyProductList;

					logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
					docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
					Sheet.fillSheet(docSave, list, sheetNameOld);
					docSave.renameSheet(sheetNameOld, sheetNameNew);
				}
				// DAILY PRODUCT PERCENT
				{
					var sheetNameOld = DailyProductReport.SHEET_NAME_PERCENT;
					var sheetNameNew = "日付　商品　割合";
					var list         = new ArrayList<DailyProductReport>();
					{
						for(var e: dailyProductList) {
							var date       = LocalDate.parse(e.date);
							var usdRate    = fxRateMap.get(date).usd.doubleValue();
							var grandTotal = e.total;
							
							double total       = e.total / grandTotal;
							
							double totalJPY    = e.totalJPY   / grandTotal;
							double depositJPY  = e.depositJPY / grandTotal;
							double termJPY     = e.termJPY    / grandTotal;
							double fundJPY     = e.fundJPY    / grandTotal;
							double stockJPY    = e.stockJPY   / grandTotal;
							double bondJPY     = e.bondJPY    / grandTotal;
							
							double totalUSD    = e.totalUSD   * usdRate / grandTotal;
							double depositUSD  = e.depositUSD * usdRate / grandTotal;
							double termUSD     = e.termUSD    * usdRate / grandTotal;
							double fundUSD     = e.fundUSD    * usdRate / grandTotal;
							double stockUSD    = e.stockUSD   * usdRate / grandTotal;
							double bondUSD     = e.bondUSD    * usdRate / grandTotal;
							
							var report = new DailyProductReport(
								date.toString(), total,
								totalJPY, depositJPY, termJPY, fundJPY, stockJPY, bondJPY,
								totalUSD, depositUSD, termUSD, fundUSD, stockUSD, bondUSD
							);
							list.add(report);
						}
					}

					logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
					docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
					Sheet.fillSheet(docSave, list, sheetNameOld);
					docSave.renameSheet(sheetNameOld, sheetNameNew);
				}
				// DAILY PRODUCT VALUE COMPANY
				{
					for(var company: Company.values()) {
						var sheetNameOld = DailyProductReport.SHEET_NAME_VALUE;
//						var sheetNameNew = "日付　商品　金額　" + company.description;
						var sheetNameNew = company.description;
						var list         = getDailyProductReportList(fxRateMap, filter(assetMap, company));

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

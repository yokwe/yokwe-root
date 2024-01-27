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
import yokwe.finance.report.AssetStats.GeneralReport;
import yokwe.finance.report.AssetStats.CompanyReport;
import yokwe.finance.report.AssetStats.ProductReport;
import yokwe.finance.type.Currency;
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
	
	private static List<CompanyReport> getCompanyReportList(Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap) {
		var list = new ArrayList<CompanyReport>();
		
		for(var e: fxRateMap.entrySet()) {
			var date      = e.getKey();			
			var assetList = assetMap.get(date);
			if (assetList == null) continue;
			
			double totalJPY   = 0;
			double sonyJPY    = 0;
			double smbcJPY    = 0;
			double prestiaJPY = 0;
			double smtbJPY    = 0;
			double rakutenJPY = 0;
			double nikkoJPY   = 0;
			double sbiJPY     = 0;
			
			double totalUSD   = 0;
			double sonyUSD    = 0;
			double smbcUSD    = 0;
			double prestiaUSD = 0;
			double smtbUSD    = 0;
			double rakutenUSD = 0;
			double nikkoUSD   = 0;
			double sbiUSD     = 0;
			
			for(var asset: assetList) {
				var value    = asset.value.doubleValue();
				var currency = asset.currency;
				
				var company  = asset.company;
				
				switch(currency) {
				case JPY:
				{
					totalJPY += value;
					
					switch(company) {
					case SONY:
						sonyJPY += value;
						break;
					case SMBC:
						smbcJPY += value;
						break;
					case PRESTIA:
						prestiaJPY += value;
						break;
					case SMTB:
						smtbJPY += value;
						break;
					case RAKUTEN:
						rakutenJPY += value;
						break;
					case NIKKO:
						nikkoJPY += value;
						break;
					case SBI:
						sbiJPY += value;
						break;
					default:
						logger.error("Unexpected company");
						logger.error("  asset  {}", asset);
						throw new UnexpectedException("Unexpected company");
					}
				}
					break;
				case USD:
				{
					totalUSD += value;
					
					switch(company) {
					case SONY:
						sonyUSD += value;
						break;
					case SMBC:
						smbcUSD += value;
						break;
					case PRESTIA:
						prestiaUSD += value;
						break;
					case SMTB:
						smtbUSD += value;
						break;
					case RAKUTEN:
						rakutenUSD += value;
						break;
					case NIKKO:
						nikkoUSD += value;
						break;
					case SBI:
						sbiUSD += value;
						break;
					default:
						logger.error("Unexpected company");
						logger.error("  asset  {}", asset);
						throw new UnexpectedException("Unexpected company");
					}
				}
					break;
				default:
					logger.error("Unexpected currency");
					logger.error("  asset  {}", asset);
					throw new UnexpectedException("Unexpected currency");
				}
			}
			
			var company = new CompanyReport(
				date,
				totalJPY, sonyJPY, smbcJPY, prestiaJPY, smtbJPY, rakutenJPY, nikkoJPY, sbiJPY,
				totalUSD, sonyUSD, smbcUSD, prestiaUSD, smtbUSD, rakutenUSD, nikkoUSD, sbiUSD
			);
			
			list.add(company);
		}
		
		Collections.sort(list);
		return list;
	}
	private static List<ProductReport> getProductReportList(Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap) {
		var list = new ArrayList<ProductReport>();
		
		for(var e: fxRateMap.entrySet()) {
			var date      = e.getKey();
			var assetList = assetMap.get(date);
			if (assetList == null) continue;
			
			double totalJPY    = 0;
			double depositJPY  = 0;
			double timeJPY     = 0;
			double fundJPY     = 0;
			double stockJPY    = 0;
			
			double totalUSD    = 0;
			double depositUSD  = 0;
			double timeUSD     = 0;
			double mmfUSD      = 0;
			double fundUSD     = 0;
			double stockUSD    = 0;
			double bondUSD     = 0;

			for(var asset: assetList) {
				var value    = asset.value.doubleValue();
				var currency = asset.currency;
				
				var product  = asset.type;
				
				switch(currency) {
				case JPY:
				{
					totalJPY += value;
					switch(product) {
					case DEPOSIT:
					case MRF:
						depositJPY += value;
						break;
					case DEPOSIT_TIME:
						timeJPY += value;
						break;
					case STOCK:
						stockJPY += value;
						break;
					case FUND:
						fundJPY += value;
						break;
					default:
						logger.error("Unexpected product");
						logger.error("  asset  {}", asset);
						throw new UnexpectedException("Unexpected product");
					}
				}
					break;
				case USD:
				{
					totalUSD += value;
					switch(product) {
					case DEPOSIT:
					case MRF:
						depositUSD += value;
						break;
					case DEPOSIT_TIME:
						timeUSD += value;
						break;
					case MMF:
						mmfUSD += value;
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
				}
					break;
				default:
					logger.error("Unexpected currency");
					logger.error("  asset  {}", asset);
					throw new UnexpectedException("Unexpected currency");
				}
			}
			
			var product = new ProductReport(
				date,
				totalJPY, depositJPY, timeJPY, fundJPY, stockJPY,
				totalUSD, depositUSD, timeUSD, mmfUSD, fundUSD, stockUSD, bondUSD
			);
			list.add(product);
		}
		
		Collections.sort(list);
		return list;
	}
	private static List<GeneralReport> getGeneralReportList(Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap) {
		var list = new ArrayList<GeneralReport>();
		
		for(var e: fxRateMap.entrySet()) {
			var date   = e.getKey();
			var fxRate = e.getValue();
			
			var assetList = assetMap.get(date);
			if (assetList == null) continue;
			
			double total   = 0;
			double jpy     = 0;
			double usd     = 0;
			double usdRate = fxRate.rate(Currency.USD).doubleValue();
			double usdJPY  = 0;
			double safe    = 0;
			double unsafe  = 0;
			
			for(var asset: assetList) {
				var value    = asset.value.doubleValue();
				var currency = asset.currency;
				var valueJPY = value * fxRate.rate(currency).doubleValue();
				var risk     = asset.risk;
				
				total += valueJPY;
				
				switch(currency) {
				case JPY:
					jpy += value;
					break;
				case USD:
					usd    += value;
					usdJPY += valueJPY;
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
			}
			
			var product = new GeneralReport(
				date, total,
				jpy, usd, usdRate, usdJPY, safe, unsafe
			);
			list.add(product);
		}
		
		Collections.sort(list);
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
			var companyList  = getCompanyReportList(fxRateMap, assetMap);
			var productList  = getProductReportList(fxRateMap, assetMap);
			var generalList = getGeneralReportList(fxRateMap, assetMap);
			
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
				
				// COMPANY
				{
					var sheetNameOld = Sheet.getSheetName(CompanyReport.class);
					var sheetNameNew = "会社";
					var list         = companyList;

					logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
					docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
					Sheet.fillSheet(docSave, list);
					docSave.renameSheet(sheetNameOld, sheetNameNew);
				}
				// GENERAL - whole
				{
					var sheetNameOld = Sheet.getSheetName(GeneralReport.class);
					var sheetNameNew = "概要";
					var list         = generalList;

					logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
					docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
					Sheet.fillSheet(docSave, list);
					docSave.renameSheet(sheetNameOld, sheetNameNew);
				}
				// PRODUCT - whole
				{
					var sheetNameOld = Sheet.getSheetName(ProductReport.class);
					var sheetNameNew = "商品";
					var list         = productList;

					logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
					docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
					Sheet.fillSheet(docSave, list);
					docSave.renameSheet(sheetNameOld, sheetNameNew);
				}
				// GENERAL - company
				{
					for(var company: Company.values()) {
						{
							var sheetNameOld = Sheet.getSheetName(GeneralReport.class);
							var sheetNameNew = company + "-概要";
							var list         = getGeneralReportList(fxRateMap, filter(assetMap, company));
							
							logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
							docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
							Sheet.fillSheet(docSave, list);
							docSave.renameSheet(sheetNameOld, sheetNameNew);
						}
						{
							var sheetNameOld = Sheet.getSheetName(ProductReport.class);
							var sheetNameNew = company + "-商品";
							var list         = getProductReportList(fxRateMap, filter(assetMap, company));
							
							logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, list.size());
							docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
							Sheet.fillSheet(docSave, list);
							docSave.renameSheet(sheetNameOld, sheetNameNew);
						}
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

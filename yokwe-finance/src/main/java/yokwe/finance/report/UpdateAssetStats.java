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
import yokwe.finance.account.UpdateAssetAll;
import yokwe.finance.fx.StorageFX;
import yokwe.finance.report.AssetStats.SummaryCategory;
import yokwe.finance.report.AssetStats.SummaryCompany;
import yokwe.finance.report.AssetStats.SummaryProduct;
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
	
	private static List<SummaryCompany> getSummaryCompanyList(Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap) {
		var list = new ArrayList<SummaryCompany>();
		
		for(var e: fxRateMap.entrySet()) {
			var date   = e.getKey();
			var fxRate = e.getValue();
			
			var assetList = assetMap.get(date);
			if (assetList == null) continue;
			
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
				var valueJPY = value * fxRate.rate(currency).doubleValue();
				
				var company  = asset.company;
				
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
			
			var company = new SummaryCompany(
				date, total,
				sony, smbc, prestia, smtb, rakuten, nikko, sbi
			);
			
			list.add(company);
		}
		
		Collections.sort(list);
		return list;
	}
	private static List<SummaryProduct> getSummaryProductList(Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap) {
		var list = new ArrayList<SummaryProduct>();
		
		for(var e: fxRateMap.entrySet()) {
			var date   = e.getKey();
			var fxRate = e.getValue();
			
			var assetList = assetMap.get(date);
			if (assetList == null) continue;
			
			double total   = 0;
			
			double totalJPY    = 0;
			double depositJPY  = 0;
			double timeJPY     = 0;
			double fundJPY     = 0;
			double stockJPY    = 0;
			
			double totalUSD    = 0;
			double rateUSD     = fxRate.rate(Currency.USD).doubleValue();
			double totalUSDJPY = 0;
			double depositUSD  = 0;
			double timeUSD     = 0;
			double mmfUSD      = 0;
			double fundUSD     = 0;
			double stockUSD    = 0;
			double bondUSD     = 0;

			for(var asset: assetList) {
				var value    = asset.value.doubleValue();
				var currency = asset.currency;
				var valueJPY = value * fxRate.rate(currency).doubleValue();
				
				var product  = asset.type;
				
				total += valueJPY;
				
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
					totalUSD    += value;
					totalUSDJPY += valueJPY;
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
			
			var product = new SummaryProduct(
				date, total,
				totalJPY, depositJPY, timeJPY, fundJPY, stockJPY,
				totalUSD, rateUSD, totalUSDJPY, depositUSD, timeUSD, mmfUSD, fundUSD, stockUSD, bondUSD
			);
			list.add(product);
		}
		
		Collections.sort(list);
		return list;
	}
	private static List<SummaryCategory> getSummayCategoryList(Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap) {
		var list = new ArrayList<SummaryCategory>();
		
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
			
			var product = new SummaryCategory(
				date, total,
				jpy, usd, usdRate, usdJPY, safe, unsafe
			);
			list.add(product);
		}
		
		Collections.sort(list);
		return list;
	}

	private static void update() {
		var today = LocalDate.now();

		Map<LocalDate, FXRate> fxRateMap = StorageFX.FXRate.getList().stream().filter(o -> o.date.isAfter(today.minusYears(1))).collect(Collectors.toMap(o -> o.date, Function.identity()));
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
			var companyList  = getSummaryCompanyList(fxRateMap, assetMap);
			var productList  = getSummaryProductList(fxRateMap, assetMap);
			var categoryList = getSummayCategoryList(fxRateMap, assetMap);
			
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
				
				{
					String sheetName = Sheet.getSheetName(SummaryCompany.class);
					logger.info("sheet     {}", sheetName);
					docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
					logger.info("size      {}", companyList.size());
					Sheet.fillSheet(docSave, companyList);
				}
				{
					String sheetName = Sheet.getSheetName(SummaryProduct.class);
					logger.info("sheet     {}", sheetName);
					docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
					logger.info("size      {}", productList.size());
					Sheet.fillSheet(docSave, productList);
				}
				{
					String sheetName = Sheet.getSheetName(SummaryCategory.class);
					logger.info("sheet     {}", sheetName);
					docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
					logger.info("size      {}", categoryList.size());
					Sheet.fillSheet(docSave, categoryList);
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

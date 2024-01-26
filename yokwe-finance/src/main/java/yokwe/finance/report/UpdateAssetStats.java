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
import yokwe.finance.report.AssetStats.Company;
import yokwe.finance.report.AssetStats.CompanyJPY;
import yokwe.finance.report.AssetStats.Product;
import yokwe.finance.report.AssetStats.ProductJPY;
import yokwe.finance.type.FXRate;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateAssetStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_TEMPLATE  = StringUtil.toURLString("data/form/ASSET_STATS.ods");
	
	private static List<Company> getCompanyList(Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap) {
		var list = new ArrayList<Company>();
		
		for(var e: fxRateMap.entrySet()) {
			var date   = e.getKey();
			var fxRate = e.getValue();
			
			var assetList = assetMap.get(date);
			if (assetList == null) continue;
			
			double total      = 0;
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
				var currency = asset.currency;
				var value    = asset.value.doubleValue();
				var company  = asset.company;
				
				switch(currency) {
				case JPY:
				{
					total    += value;
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
					var rate = fxRate.rate(currency).doubleValue();					
					total    += value * rate;
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
			
			var company = new Company(
				date, total,
				totalJPY, sonyJPY, smbcJPY, prestiaJPY, smtbJPY, rakutenJPY, nikkoJPY, sbiJPY,
				totalUSD, sonyUSD, smbcUSD, prestiaUSD, smtbUSD, rakutenUSD, nikkoUSD, sbiUSD
			);
			
			list.add(company);
		}
		
		Collections.sort(list);
		return list;
	}
	private static List<CompanyJPY> getCompanyJPYList(Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap) {
		var list = new ArrayList<CompanyJPY>();
		
		for(var e: fxRateMap.entrySet()) {
			var date   = e.getKey();
			var fxRate = e.getValue();
			
			var assetList = assetMap.get(date);
			if (assetList == null) continue;
			
			double total      = 0;
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
				var currency = asset.currency;
				var value    = asset.value.doubleValue();
				var company  = asset.company;
				
				switch(currency) {
				case JPY:
				{
					total    += value;
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
					var rate = fxRate.rate(currency).doubleValue();
					value *= rate;
					total    += value;
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
			
			var company = new CompanyJPY(
				date, total,
				totalJPY, sonyJPY, smbcJPY, prestiaJPY, smtbJPY, rakutenJPY, nikkoJPY, sbiJPY,
				totalUSD, sonyUSD, smbcUSD, prestiaUSD, smtbUSD, rakutenUSD, nikkoUSD, sbiUSD
			);
			
			list.add(company);
		}
		
		Collections.sort(list);
		return list;
	}
	private static List<Product> getProductList(Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap) {
		var list = new ArrayList<Product>();
		
		for(var e: fxRateMap.entrySet()) {
			var date   = e.getKey();
			var fxRate = e.getValue();
			
			var assetList = assetMap.get(date);
			if (assetList == null) continue;
			
			double safe = 0;
			double risk = 0;
			double total = 0;
			
			double totalJPY   = 0;
			double depositJPY = 0;
			double timeJPY = 0;
			double fundJPY    = 0;
			double stockJPY   = 0;
			
			double totalUSD   = 0;
			double depositUSD = 0;
			double timeUSD    = 0;
			double mmfUSD     = 0;
			double fundUSD    = 0;
			double stockUSD   = 0;
			double bondUSD    = 0;
			
			for(var asset: assetList) {
				var currency = asset.currency;
				var value    = asset.value.doubleValue();
				var product  = asset.type;
								
				switch(currency) {
				case JPY:
				{
					total    += value;
					totalJPY += value;
					
					switch(asset.risk) {
					case SAFE:
						safe += value;
						break;
					case UNSAFE:
						risk += value;
						break;
					default:
						logger.error("Unexpected risk");
						logger.error("  asset  {}", asset);
						throw new UnexpectedException("Unexpected risk");
					}
					
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
					var rate = fxRate.rate(currency).doubleValue();					
					total    += value * rate;
					totalUSD += value;
					
					switch(asset.risk) {
					case SAFE:
						safe += value * rate;
						break;
					case UNSAFE:
						risk += value * rate;
						break;
					default:
						logger.error("Unexpected risk");
						logger.error("  asset  {}", asset);
						throw new UnexpectedException("Unexpected risk");
					}
					
					switch(product) {
					case DEPOSIT:
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
			
			var product = new Product(
				date, safe, risk, total,
				totalJPY, depositJPY, timeJPY, fundJPY, stockJPY,
				totalUSD, depositUSD, timeUSD, mmfUSD, fundUSD, stockUSD, bondUSD
			);
			list.add(product);
		}
		
		Collections.sort(list);
		return list;
	}
	private static List<ProductJPY> getProductJPYList(Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap) {
		var list = new ArrayList<ProductJPY>();
		
		for(var e: fxRateMap.entrySet()) {
			var date   = e.getKey();
			var fxRate = e.getValue();
			
			var assetList = assetMap.get(date);
			if (assetList == null) continue;
			
			double safe = 0;
			double risk = 0;
			double total = 0;
			
			double totalJPY   = 0;
			double depositJPY = 0;
			double timeJPY    = 0;
			double fundJPY    = 0;
			double stockJPY   = 0;
			
			double totalUSD   = 0;
			double depositUSD = 0;
			double timeUSD    = 0;
			double mmfUSD     = 0;
			double fundUSD    = 0;
			double stockUSD   = 0;
			double bondUSD    = 0;
			
			for(var asset: assetList) {
				var currency = asset.currency;
				var value    = asset.value.doubleValue();
				var product  = asset.type;
								
				switch(currency) {
				case JPY:
				{
					total    += value;
					totalJPY += value;
					
					switch(asset.risk) {
					case SAFE:
						safe += value;
						break;
					case UNSAFE:
						risk += value;
						break;
					default:
						logger.error("Unexpected risk");
						logger.error("  asset  {}", asset);
						throw new UnexpectedException("Unexpected risk");
					}
					
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
					value    *= fxRate.rate(currency).doubleValue();
					total    += value;
					totalUSD += value;
					
					switch(asset.risk) {
					case SAFE:
						safe += value;
						break;
					case UNSAFE:
						risk += value;
						break;
					default:
						logger.error("Unexpected risk");
						logger.error("  asset  {}", asset);
						throw new UnexpectedException("Unexpected risk");
					}
					
					switch(product) {
					case DEPOSIT:
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
			
			var product = new ProductJPY(
				date, safe, risk, total,
				totalJPY, depositJPY, timeJPY, fundJPY, stockJPY,
				totalUSD, depositUSD, timeUSD, mmfUSD, fundUSD, stockUSD, bondUSD
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
			var companyList = getCompanyList(fxRateMap, assetMap);
			var companyJPYList = getCompanyJPYList(fxRateMap, assetMap);
			var productList = getProductList(fxRateMap, assetMap);
			var productJPYList = getProductJPYList(fxRateMap, assetMap);
			
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
					String sheetName = Sheet.getSheetName(Company.class);
					logger.info("sheet     {}", sheetName);
					docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
					logger.info("size      {}", companyList.size());
					Sheet.fillSheet(docSave, companyList);
				}
				{
					String sheetName = Sheet.getSheetName(CompanyJPY.class);
					logger.info("sheet     {}", sheetName);
					docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
					logger.info("size      {}", companyJPYList.size());
					Sheet.fillSheet(docSave, companyJPYList);
				}
				{
					String sheetName = Sheet.getSheetName(Product.class);
					logger.info("sheet     {}", sheetName);
					docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
					logger.info("size      {}", productList.size());
					Sheet.fillSheet(docSave, productList);
				}
				{
					String sheetName = Sheet.getSheetName(ProductJPY.class);
					logger.info("sheet     {}", sheetName);
					docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
					logger.info("size      {}", productJPYList.size());
					Sheet.fillSheet(docSave, productJPYList);
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

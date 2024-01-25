package yokwe.finance.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import yokwe.finance.account.Asset;
import yokwe.finance.account.UpdateAssetAll;
import yokwe.finance.fx.StorageFX;
import yokwe.finance.report.AssetStats.SummaryCompany;
import yokwe.finance.report.AssetStats.SummaryProduct;
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
		var list = new ArrayList<AssetStats.SummaryCompany>();
		
		for(var e: fxRateMap.entrySet()) {
			var date   = e.getKey();
			var fxRate = e.getValue();
			
			var assetList = assetMap.get(date);
			if (assetList == null) continue;
			
			double jpy     = 0;
			double usd     = 0;
			double total   = 0;
			double sony    = 0;
			double smbc    = 0;
			double prestia = 0;
			double smtb    = 0;
			double rakuten = 0;
			double nikko   = 0;
			double sbi     = 0;
			
			for(var asset: assetMap.get(date)) {
				var currency = asset.currency;
				var value    = asset.value.doubleValue() * fxRate.rate(asset.currency).doubleValue();
				
				switch(currency) {
				case JPY:
					jpy += value;
					break;
				case USD:
					usd += value;
					break;
				default:
					logger.error("Unexpected currency");
					logger.error("  asset  {}", asset);
					throw new UnexpectedException("Unexpected currency");
				}
				total += value;
				
				var company  = asset.company;
				switch(company) {
				case SONY:
					sony += value;
					break;
				case SMBC:
					smbc += value;
					break;
				case PRESTIA:
					prestia += value;
					break;
				case SMTB:
					smtb += value;
					break;
				case RAKUTEN:
					rakuten += value;
					break;
				case NIKKO:
					nikko += value;
					break;
				case SBI:
					sbi += value;
					break;
				default:
					logger.error("Unexpected company");
					logger.error("  asset  {}", asset);
					throw new UnexpectedException("Unexpected company");
				}
			}
			
			SummaryCompany summaryCompany = new SummaryCompany(
				date, jpy, usd, total, sony, smbc, prestia, smtb, rakuten, nikko, sbi);
			list.add(summaryCompany);
		}
		
		Collections.sort(list);
		return list;
	}
	private static List<AssetStats.SummaryProduct> getSummaryProductList(Map<LocalDate, FXRate> fxRateMap, Map<LocalDate, List<Asset>> assetMap) {
		var list = new ArrayList<AssetStats.SummaryProduct>();
		
		for(var e: fxRateMap.entrySet()) {
			var date   = e.getKey();
			var fxRate = e.getValue();
			
			var assetList = assetMap.get(date);
			if (assetList == null) continue;
			
			double jpy     = 0;
			double usd     = 0;
			double total   = 0;
			double safe    = 0;
			double risk    = 0;
			double deposit = 0;
			double time    = 0;
			double mmf     = 0;
			double fund    = 0;
			double bond    = 0;
			double stock   = 0;
			
			for(var asset: assetMap.get(date)) {
				var currency = asset.currency;
				var value    = asset.value.doubleValue() * fxRate.rate(asset.currency).doubleValue();
				
				switch(currency) {
				case JPY:
					jpy += value;
					break;
				case USD:
					usd += value;
					break;
				default:
					logger.error("Unexpected currency");
					logger.error("  asset  {}", asset);
					throw new UnexpectedException("Unexpected currency");
				}
				total += value;
				
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
					throw new UnexpectedException("Unexpected currency");
				}
				
				switch(asset.type) {
				case DEPOSIT:
				case MRF:
					deposit += value;
					break;
				case DEPOSIT_TIME:
					time += value;
					break;
				case MMF:
					mmf += value;
					break;
				case STOCK:
					stock += value;
					break;
				case FUND:
					fund += value;
					break;
				case BOND:
					bond += value;
					break;
				default:
					logger.error("Unexpected type");
					logger.error("  asset  {}", asset);
					throw new UnexpectedException("Unexpected type");
				}
			}
			
			SummaryProduct summaryProduct = new SummaryProduct(
					date, jpy, usd, total, safe, risk, deposit, time, mmf, fund, bond, stock);
			list.add(summaryProduct);
		}
		
		Collections.sort(list);
		return list;
	}
	
	private static void update() {
		var today = LocalDate.now();

		Map<LocalDate, FXRate> fxRateMap;
		{
			var oldestDate = today.minusYears(1).plusDays(1);
			
			fxRateMap = StorageFX.FXRate.getList().stream().filter(o -> !o.date.isBefore(oldestDate)).collect(Collectors.toMap(o -> o.date, o -> o));
		}
		Map<LocalDate, List<Asset>> assetMap = new TreeMap<LocalDate, List<Asset>>();
		{
			int year = today.getYear();
			
			// this year
			var assetList = new ArrayList<Asset>();
			assetList.addAll(UpdateAssetAll.getList(year - 1));
			assetList.addAll(UpdateAssetAll.getList(year));
			
			for(var e: assetList) {
				var date = e.date;
				
				if (!fxRateMap.containsKey(date)) continue;
				
				List<Asset> list;
				if (assetMap.containsKey(date)) {
					list = assetMap.get(date);
				} else {
					list = new ArrayList<>();
					assetMap.put(date, list);
				}
				list.add(e);
			}
		}		
		
		{
			var summaryCompanyList = getSummaryCompanyList(fxRateMap, assetMap);
			var summaryProductList = getSummaryProductList(fxRateMap, assetMap);
			
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
					String sheetName = Sheet.getSheetName(AssetStats.SummaryCompany.class);
					logger.info("sheet     {}", sheetName);
					docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
					logger.info("size      {}", summaryCompanyList.size());
					Sheet.fillSheet(docSave, summaryCompanyList);
				}
				{
					String sheetName = Sheet.getSheetName(AssetStats.SummaryProduct.class);
					logger.info("sheet     {}", sheetName);
					docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
					logger.info("size      {}", summaryProductList.size());
					Sheet.fillSheet(docSave, summaryProductList);
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

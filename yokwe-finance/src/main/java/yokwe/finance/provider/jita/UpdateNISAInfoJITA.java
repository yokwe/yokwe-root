package yokwe.finance.provider.jita;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.NISAInfoType;
import yokwe.finance.type.StockCodeJP;
import yokwe.util.FileUtil;
import yokwe.util.HashCode;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateNISAInfoJITA {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void downloadListed() {
		final var url      = "https://www.toushin.or.jp/files/static/486/listed_fund_for_investor.xlsx";
		final var fileXLSX = StorageJITA.storage.getFile("listed_fund_for_investor.xlsx");
		final var fileCSV  = new File(StorageJITA.ListedFundForInvestor.getPath());
		
		final boolean needsUpdateXLSX;
		final boolean needsUpdateCSV;
		
		logger.info("download {}", url);
		HttpUtil.Result result = HttpUtil.getInstance().withRawData(true).download(url);
		if (result != null && result.rawData != null) {
			// update needsUpdateXLSX
			if (fileXLSX.exists()) {
				var oldHashCode = fileXLSX.canRead() ? HashCode.getHashCode(fileXLSX) : new byte[] {0};
				var newHashCode = HashCode.getHashCode(result.rawData);
				if (Arrays.equals(oldHashCode, newHashCode)) {
					// same contents
					logger.info("same contents");
					needsUpdateXLSX = false;
				} else {
					// different contents
					logger.info("different contents");
					needsUpdateXLSX = true;
				}
			} else {
				logger.info("no xlsx file");
				needsUpdateXLSX = true;
			}
			// update needsUpdateCSV
			if (fileCSV.exists()) {
				// if update xlsx file, update csv also.
				needsUpdateCSV = needsUpdateXLSX;
			} else {
				logger.info("no csv file");
				needsUpdateCSV = true;
			}
			
			if (needsUpdateXLSX) {
				logger.info("save  {}  {}", result.rawData.length, fileXLSX.getPath());
				FileUtil.rawWrite().file(fileXLSX, result.rawData);
			}
			if (needsUpdateCSV) {
				try (SpreadSheet spreadSheet = new SpreadSheet(fileXLSX.toURI().toURL().toString(), true)) {
					var dataList = Sheet.extractSheet(spreadSheet, ListedFundForInvestor.class);
					logger.info("save  {}  {}", dataList.size(), StorageJITA.ListedFundForInvestor.getPath());
					StorageJITA.ListedFundForInvestor.save(dataList);
				} catch (MalformedURLException e) {
					String exceptionName = e.getClass().getSimpleName();
					logger.error("{} {}", exceptionName, e);
					throw new UnexpectedException(exceptionName, e);
				}
			}
		} else {
			logger.error("Unexpected result");
			logger.error("  result  {}", result);
			throw new UnexpectedException("Unexpected result");
		}
	}
	public static void downloadUnlisted() {
		final var url      = "https://www.toushin.or.jp/files/static/486/unlisted_fund_for_investor.xlsx";
		final var fileXLSX = StorageJITA.storage.getFile("unlisted_fund_for_investor.xlsx");
		final var fileCSV  = new File(StorageJITA.UnlistedFundForInvestor.getPath());

		final boolean needsUpdateXLSX;
		final boolean needsUpdateCSV;
		
		logger.info("download {}", url);
		HttpUtil.Result result = HttpUtil.getInstance().withRawData(true).download(url);
		if (result != null && result.rawData != null) {
			// update needsUpdateXLSX
			if (fileXLSX.exists()) {
				var oldHashCode = fileXLSX.canRead() ? HashCode.getHashCode(fileXLSX) : new byte[] {0};
				var newHashCode = HashCode.getHashCode(result.rawData);
				if (Arrays.equals(oldHashCode, newHashCode)) {
					// same contents
					logger.info("same contents");
					needsUpdateXLSX = false;
				} else {
					// different contents
					logger.info("different contents");
					needsUpdateXLSX = true;
				}
			} else {
				logger.info("no xlsx file");
				needsUpdateXLSX = true;
			}
			// update needsUpdateCSV
			if (fileCSV.exists()) {
				// if update xlsx file, update csv also.
				needsUpdateCSV = needsUpdateXLSX;
			} else {
				logger.info("no csv file");
				needsUpdateCSV = true;
			}
			
			if (needsUpdateXLSX) {
				logger.info("save  {}  {}", result.rawData.length, fileXLSX.getPath());
				FileUtil.rawWrite().file(fileXLSX, result.rawData);
			}
			if (needsUpdateCSV) {
				try (SpreadSheet spreadSheet = new SpreadSheet(fileXLSX.toURI().toURL().toString(), true)) {
					var dataList = Sheet.extractSheet(spreadSheet, UnlistedFundForInvestor.class);
					// if redemptionDate is null, set blank to redemptionDate
					dataList.stream().forEach(o -> {if (o.redemptionDate == null) o.redemptionDate = "";});
					logger.info("save  {}  {}", dataList.size(), StorageJITA.UnlistedFundForInvestor.getPath());
					StorageJITA.UnlistedFundForInvestor.save(dataList);
				} catch (MalformedURLException e) {
					String exceptionName = e.getClass().getSimpleName();
					logger.error("{} {}", exceptionName, e);
					throw new UnexpectedException(exceptionName, e);
				}
			}
		} else {
			logger.error("Unexpected result");
			logger.error("  result  {}", result);
			throw new UnexpectedException("Unexpected result");
		}
	}
	public static void download() {
		downloadListed();
		downloadUnlisted();
	}
	
	public static void update() {
		var list = new ArrayList<NISAInfoType>();
		
		// build list from listed
		{
			// stockCode to isinCode
			var stockCodeMap = StorageStock.StockInfoJP.getList().stream().collect(Collectors.toMap(o -> o.stockCode, o -> o.isinCode));

			for (var data : StorageJITA.ListedFundForInvestor.load()) {
				var stockCode = StockCodeJP.toStockCode5(data.stockCode);
				if (stockCodeMap.containsKey(stockCode)) {
					var isinCode  = stockCodeMap.get(stockCode);
					var tsumitate = data.isTsumitate();
					var nisaInfo  = new NISAInfoType(isinCode, tsumitate);
					list.add(nisaInfo);
				} else {
					logger.warn("Unexpected stockCode  {}  {}  {}", data.inceptionDate, data.stockCode, data.fundName);
				}
			}
		}
		// build list from unlisted
		{
			// stockCode to isinCode
			var fundCodeMap = StorageFund.FundInfo.getList().stream().collect(Collectors.toMap(o -> o.fundCode, o -> o.isinCode));
			
			for (var data : StorageJITA.UnlistedFundForInvestor.load()) {
				var fundCode = data.fundCode;
				if (fundCodeMap.containsKey(fundCode)) {
					var isinCode  = fundCodeMap.get(fundCode);
					var tsumitate = data.isTsumitate();
					var nisaInfo  = new NISAInfoType(isinCode, tsumitate);
					list.add(nisaInfo);
				} else {
					logger.warn("Unexpected fundCode  {}  {}  {}", data.inceptionDate, data.fundCode, data.fundName);
				}
			}
		}
		
		logger.info("save  {}  {}", list.size(), StorageJITA.NISAInfoJITA.getPath());
		StorageJITA.NISAInfoJITA.save(list);
	}
	
	public static void main(String[] args) {
		try {
			logger.info("START");

			LibreOffice.initialize();
			
			download();
			update();

			logger.info("STOP");
		} catch (Throwable e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
		} finally {
			LibreOffice.terminate();
		}
	}
}

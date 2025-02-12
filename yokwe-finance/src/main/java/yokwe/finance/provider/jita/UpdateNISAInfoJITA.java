package yokwe.finance.provider.jita;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.NISAInfoType;
import yokwe.finance.type.StockInfoJPType;
import yokwe.util.FileUtil;
import yokwe.util.HashCode;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateNISAInfoJITA {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	@Sheet.SheetName("対象商品一覧")
	@Sheet.HeaderRow(1)
	@Sheet.DataRow(2)
	public static class ListedFundForInvestor extends Sheet implements Comparable<ListedFundForInvestor> {
		@Sheet.ColumnName("リスト更新日")
		@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
		public String updateDate; // date as YYYYMMDD
		@Sheet.ColumnName("追加・変更の別")
		public String changeType; // 追加 or 変更
		@Sheet.ColumnName("上場投信・上場投資法人の別")
		public String fundType; // 上場投信 or 上場投資法人
		@Sheet.ColumnName("銘柄コード")
		public String stockCode; // 4 digits stockCode
		@Sheet.ColumnName("ファンド名称")
		public String fundName;
		@Sheet.ColumnName("運用会社名")
		public String assetManagementCompanyName;
		@Sheet.ColumnName("設定日・設立日")
		@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
		public String inceptionDate; // date as YYYYMMDD
		@Sheet.ColumnName("成長投資枠取扱可能日")
		@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
		public String growthStartDate; // date as YYYYMMDD
		@Sheet.ColumnName("決算回数")
		public String numberOfSettlements; // 年１回 or 年2回 or 四半期 or 隔月
		@Sheet.ColumnName("つみたて投資枠の対象・非対象")
		public String tsumitate; // 対象 or 非対象

		@Override
		public int compareTo(ListedFundForInvestor that) {
			return this.stockCode.compareTo(that.stockCode);
		}

		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
		
		public boolean isTsumitate() {
			return tsumitate.equals("対象");
		}
	}

	@Sheet.SheetName("対象商品一覧")
	@Sheet.HeaderRow(1)
	@Sheet.DataRow(2)
	public static class UnlistedFundForInvestor extends Sheet implements Comparable<UnlistedFundForInvestor> {
		@Sheet.ColumnName("リスト更新日")
		@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
		public String updateDate; // date as YYYYMMDD
		@Sheet.ColumnName("追加・変更の別")
		public String changeType; // 追加 or 変更
		@Sheet.ColumnName("投信協会ファンドコード")
		public String fundCode; // 4 digits stockCode
		@Sheet.ColumnName("ファンド名称")
		public String fundName;
		@Sheet.ColumnName("運用会社名")
		public String assetManagementCompanyName;
		@Sheet.ColumnName("設定日")
		@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
		public String inceptionDate; // date as YYYYMMDD
		@Sheet.ColumnName("償還日")
		public String redemptionDate; // date as YYYYMMDD
		@Sheet.ColumnName("成長投資枠取扱可能日")
		@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
		public String growthStartDate; // date as YYYYMMDD
		@Sheet.ColumnName("決算回数")
		public String numberOfSettlements; // 年１回 or 年2回 or 四半期 or 隔月
		@Sheet.ColumnName("つみたて投資枠の対象・非対象")
		public String tsumitate; // 対象 or 非対象

		@Override
		public int compareTo(UnlistedFundForInvestor that) {
			return this.fundCode.compareTo(that.fundCode);
		}

		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
		
		public boolean isTsumitate() {
			return tsumitate.equals("対象");
		}
	}
	
	public static boolean downloadListed() {
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
		return needsUpdateCSV;
	}
	public static boolean downloadUnlisted() {
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
		return needsUpdateCSV;
	}
	public static boolean download() {
		var needsUpdateListed   = downloadListed();
		var needsUpdateUnlisted = downloadUnlisted();
		
		return needsUpdateListed || needsUpdateUnlisted;
	}
	
	public static void update() {
		var list = new ArrayList<NISAInfoType>();
		
		// build list from listed
		{
			// stockCode to isinCode
			var stockCodeMap = StorageStock.StockInfoJP.getList().stream().collect(Collectors.toMap(o -> o.stockCode, o -> o.isinCode));

			for (var data : StorageJITA.ListedFundForInvestor.load()) {
				var stockCode = StockInfoJPType.toStockCode5(data.stockCode);
				if (stockCodeMap.containsKey(stockCode)) {
					var isinCode  = stockCodeMap.get(stockCode);
					var tsumitate = data.isTsumitate();
					var nisaInfo  = new NISAInfoType(isinCode, tsumitate);
					list.add(nisaInfo);
				} else {
					logger.warn("Unexpected stockCode");
//					logger.warn("  {}", data.toString());
					logger.warn("  {}  {}  {}", data.inceptionDate, data.stockCode, data.fundName);
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
					logger.warn("Unexpected fundCode");
//					logger.warn("  {}", data.toString());
					logger.warn("  {}  {}  {}", data.inceptionDate, data.fundCode, data.fundName);
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
			
			var needsUpdate = download();
			if (needsUpdate) update();

			logger.info("STOP");
		} catch (Throwable e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
		} finally {
			LibreOffice.terminate();
		}

	}

}

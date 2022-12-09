package yokwe.stock.jp.toushin2;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import yokwe.stock.jp.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;


public class UpdateFundData {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static String getPagePath(int fileNo) {
		return Storage.Toushin2.getPath("page", Integer.toString(fileNo));
	}
	
	private static File getPage(int pageNo, int startNo) {
		File file = new File(getPagePath(pageNo));
		if (!file.exists()) {
			String url = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST999900/fundDataSearch";
			
			final String body;
			{
				Map<String, String> bodyMap = new LinkedHashMap<>();
				
				bodyMap.put("t_keyword",                "");
				bodyMap.put("t_kensakuKbn",             "1");
				bodyMap.put("t_fundCategory",           "");
				bodyMap.put("s_keyword",                "");
				bodyMap.put("s_kensakuKbn",             "1");
				bodyMap.put("s_supplementKindCd",       "1");
				bodyMap.put("s_standardPriceCond1",     "0");
				bodyMap.put("s_standardPriceCond2",     "0");
				bodyMap.put("s_riskCond1",              "0");
				bodyMap.put("s_riskCond2",              "0");
				bodyMap.put("s_sharpCond1",             "0");
				bodyMap.put("s_sharpCond2",             "0");
				bodyMap.put("s_buyFee",                 "1");
				bodyMap.put("s_trustReward",            "1");
				bodyMap.put("s_monthlyCancelCreateVal", "1");
				bodyMap.put("s_instCd",                 "");
				bodyMap.put("salesInstDiv",             "");
				bodyMap.put("s_fdsInstCd",              "");
				bodyMap.put("startNo",                  Integer.toString(startNo));
				bodyMap.put("draw",                     "0");
				bodyMap.put("searchBtnClickFlg",        "false");

				StringJoiner sj = new StringJoiner("&");		
				for(var entry: bodyMap.entrySet()) {
					sj.add(entry.getKey() + "=" + entry.getValue());
				}
				body = sj.toString();
			}

			String contentType = "application/x-www-form-urlencoded;charset=UTF-8";

			HttpUtil.Result result = HttpUtil.getInstance().withPost(body, contentType).download(url);
			if (result.result == null) {
				logger.error("Dowload failed");
				logger.error("  url    {}", url);
				logger.error("  result {} {}", result.code, result.reasonPhrase);
				throw new UnexpectedException("Dowload failed");
			}
			logger.info("getPage {}", pageNo);
			FileUtil.write().file(file, result.result);
		}
		return file;
	}
	
	private static List<File> getPageAll() {
		List<File> list = new ArrayList<>();
		
		int pageNo  = 0;
		int startNo = 0;

		final int allPageNo;
		final int pageSize;
		{
			File file = getPage(pageNo, startNo);
			list.add(file);
			
			String jsonString = FileUtil.read().file(file);
			var page = JSON.unmarshal(FundDataSearch.class, jsonString);
			allPageNo = page.allPageNo;
			pageSize  = page.pageSize;
		}

		for(;;) {
			pageNo  += 1;
			startNo += pageSize;
			if (allPageNo <= pageNo) break;
			
			if ((pageNo % 20) == 0) logger.info("getPage {}", String.format("%4d / %4d", pageNo, allPageNo));

			File file = getPage(pageNo, startNo);
			list.add(file);
		}
		
		return list;
	}
	
	public static class DividendPrice {
		// 年月日	        基準価額(円)	純資産総額（百万円）	分配金	決算期
		// 2022年04月25日	19239	        1178200	                0       4
		// 2022年04月26日	19167	        1174580   
		@CSVUtil.ColumnName("年月日")
		public String date;
		@CSVUtil.ColumnName("基準価額(円)")
		public String price;
		@CSVUtil.ColumnName("純資産総額（百万円）")
		public String nav;
		@CSVUtil.ColumnName("分配金")
		public String dividend;
		@CSVUtil.ColumnName("決算期")
		public String period;
	}
//	private static void updateDividendPrice(FundData fundData) {
//		// https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/csv-file-download?isinCd=JP90C000DJ15&associFundCd=AE313167
//		String url = String.format("https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/csv-file-download?isinCd=%s&associFundCd=%s", fundData.isinCode, fundData.assocFundCode);
//		
//		var result = HttpUtil.getInstance().withCharset("SHIFT_JIS").download(url);
//		if (result.result == null) {
//			logger.error("Dowload failed");
//			logger.error("  url    {}", url);
//			logger.error("  result {} {}", result.code, result.reasonPhrase);
//			throw new UnexpectedException("Dowload failed");
//		}
//		var list = CSVUtil.read(DividendPrice.class).file(result.result);
//		if (list == null) {
//			logger.error("CSVUtil.read failed");
//			logger.error("  url    {}", url);
//			throw new UnexpectedException("CSVUtil.read failed");
//		}
//		logger.info("updateDividendPrice {} {}", fundData.isinCode, list.size());
//		
//		var divList   = new ArrayList<Dividend>();
//		var priceList = new ArrayList<Price>();
//		for(var e: list) {
//			
//		}
//	}
	
	private static File getDividendPrice(FundData fundData) {
		String path = Storage.Toushin2.getPath("div-price", fundData.isinCode);
		File file = new File(path);
		if (!file.exists()) {
			String url = String.format("https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/csv-file-download?isinCd=%s&associFundCd=%s", fundData.isinCode, fundData.fundCode);

			var result = HttpUtil.getInstance().withCharset("SHIFT_JIS").download(url);
			if (result.result == null) {
				logger.error("Dowload failed");
				logger.error("  url    {}", url);
				logger.error("  result {} {}", result.code, result.reasonPhrase);
				throw new UnexpectedException("Dowload failed");
			}
//			logger.info("getDividendPrice {} {}", fundData.isinCode, result.result.length());
			FileUtil.write().file(file, result.result);
		}
		return file;
	}
	
	private static List<File> getDividendPriceAll(ArrayList<FundData> fundDataList) {
		List<File> fileList = new ArrayList<>();
		int count = 0;
		for(var fundData: fundDataList) {
			if ((count++ % 100) == 0) logger.info("downloadPage {}", String.format("%4d / %4d", count, fundDataList.size()));
			File file = getDividendPrice(fundData);
			fileList.add(file);
		}
		return fileList;
	}
	
	private static void update() {
		// build resultInfoList
		var resultInfoList = new ArrayList<FundDataSearch.ResultInfo>();
		{
			List<File> fileList = getPageAll();
			for(var file: fileList) {
				String jsonString = FileUtil.read().file(file);
				var fundDataSearch = JSON.unmarshal(FundDataSearch.class, jsonString);
				for(var resultInfo: fundDataSearch.resultInfoArray) {
					resultInfoList.add(resultInfo);
				}
			}
		}
		logger.info("resultInfoList {}", resultInfoList.size());
		
		// build fundDataList
		var fundDataList = new ArrayList<FundData>();
		{
			for(var resultInfo: resultInfoList) {
				String    isinCode       = resultInfo.isinCd;
				String    fundCode       = resultInfo.associFundCd;
				
				LocalDate listingDate    = LocalDate.parse(resultInfo.establishedDate.subSequence(0,  10));
				{
					if (resultInfo.establishedDate.length() == 19) {
						listingDate = LocalDate.parse(resultInfo.establishedDate.subSequence(0,  10));
					} else {
						logger.info("Unexpected establishedDate");
						logger.info("  isinCode        {}!", isinCode);
						logger.info("  establishedDate {}!", resultInfo.establishedDate);
						throw new UnexpectedException("Unexpected establishedDate");
					}
				}
				
				LocalDate redemptionDate;
				{
					if (resultInfo.redemptionDate.length() == 8) {
						if (resultInfo.redemptionDate.equals("99999999")) {
							redemptionDate = FundData.NO_REDEMPTION_DATE;
						} else {
							String yyyy = resultInfo.redemptionDate.substring(0, 4);
							String mm   = resultInfo.redemptionDate.substring(4, 6);
							String dd   = resultInfo.redemptionDate.substring(6, 8);
							redemptionDate = LocalDate.parse(yyyy + "-" + mm + "-" + dd);
						}
					} else {
						logger.info("Unexpected redemptionDate");
						logger.info("  isinCode       {}!", isinCode);
						logger.info("  redemptionDate {}!", resultInfo.redemptionDate);
						throw new UnexpectedException("Unexpected redemptionDate");
					}
				}
				
				int       divFreq = resultInfo.setlFqcy.equals("-") ? 0 : Integer.parseInt(resultInfo.setlFqcy);
				String    name    = resultInfo.fundNm;
				
				BigDecimal expenseRatio           = resultInfo.trustReward;
				BigDecimal expenseRatioManagement = resultInfo.entrustTrustReward;
				BigDecimal expenseRatioSales      = resultInfo.bondTrustReward;
				BigDecimal expenseRatioTrustBank  = resultInfo.custodyTrustReward;
				
				BigDecimal buyFreeMax           = (resultInfo.buyFee != null) ? resultInfo.buyFee : BigDecimal.ZERO;
				String     cancelLationFeeCode  = resultInfo.cancelLationFeeCd;
				String     retentionMoneyCode   = resultInfo.retentionMoneyCd;
				
				FundData fundData = new FundData(
						isinCode, fundCode, listingDate, redemptionDate, divFreq, name,
						expenseRatio, expenseRatioManagement, expenseRatioSales, expenseRatioTrustBank,
						buyFreeMax, cancelLationFeeCode, retentionMoneyCode
						);
				fundDataList.add(fundData);
			}
			logger.info("fundDataList {} {}", fundDataList.size(), FundData.getPath());
			FundData.save(fundDataList);
		}
		
		// update dividend and price
		{
			getDividendPriceAll(fundDataList);
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}

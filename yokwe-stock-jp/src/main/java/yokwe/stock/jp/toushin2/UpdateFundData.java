package yokwe.stock.jp.toushin2;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	private static File downloadDividendPrice(FundData fundData) {
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
			FileUtil.write().file(file, result.result);
		}
		return file;
	}
	
	private static List<File> downloadDividendPriceAll(List<FundData> fundDataList) {
		List<File> fileList = new ArrayList<>();
		int count = 0;
		for(var fundData: fundDataList) {
			if ((count++ % 100) == 0) logger.info("downloadDividendPriceAll {}", String.format("%4d / %4d", count, fundDataList.size()));
			File file = downloadDividendPrice(fundData);
			fileList.add(file);
		}
		return fileList;
	}
	
	private static void updateResultInfoList(List<FundDataSearch.ResultInfo> resultInfoList) {
		List<File> fileList = getPageAll();
		for(var file: fileList) {
			String jsonString = FileUtil.read().file(file);
			var fundDataSearch = JSON.unmarshal(FundDataSearch.class, jsonString);
			for(var resultInfo: fundDataSearch.resultInfoArray) {
				resultInfoList.add(resultInfo);
			}
		}
	}
	
	private static void updateFundDataList(List<FundDataSearch.ResultInfo> resultInfoList, List<FundData> fundDataList) {
		Pattern pat = Pattern.compile("(?<yyyy>[12][09][0-9][0-9])-(?<mm>[01]?[0-9])-(?<dd>[0123]?[0-9]) 00:00:00");

		for(var resultInfo: resultInfoList) {
			String    isinCode       = resultInfo.isinCd;
			String    fundCode       = resultInfo.associFundCd;
			
			LocalDate listingDate;
			{
				// 2016-07-29 00:00:00
				Matcher m = pat.matcher(resultInfo.establishedDate);
				if (m.find()) {
					int yyyy = Integer.parseInt(m.group("yyyy"));
					int mm   = Integer.parseInt(m.group("mm"));
					int dd   = Integer.parseInt(m.group("dd"));
					listingDate = LocalDate.of(yyyy, mm, dd);
				} else {
					logger.error("Unexpected establishedDate");
					logger.error("  isinCode        {}", isinCode);
					logger.error("  establishedDate {}", resultInfo.establishedDate);
					throw new UnexpectedException("Unexpected establishedDate");
				}
			}
			
			LocalDate redemptionDate;
			{
				if (resultInfo.redemptionDate.length() == 8) {
					if (resultInfo.redemptionDate.equals("99999999")) {
						redemptionDate = FundData.NO_REDEMPTION_DATE;
					} else {
						// 20221209
						// 012345678
						int yyyy = Integer.parseInt(resultInfo.redemptionDate.substring(0, 4));
						int mm   = Integer.parseInt(resultInfo.redemptionDate.substring(4, 6));
						int dd   = Integer.parseInt(resultInfo.redemptionDate.substring(6, 8));
						redemptionDate = LocalDate.of(yyyy, mm, dd);
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
	}
	
	private static void updateDividendPrice(List<FundData>fundDataList) {
		Pattern pat = Pattern.compile("(?<yyyy>[12][09][0-9][0-9])年(?<mm>[01]?[0-9])月(?<dd>[0123]?[0-9])日");
		
		var fileList = downloadDividendPriceAll(fundDataList);
		int count = 0;
		for(var file: fileList) {
			var isinCode = file.getName();
			var priceList = new ArrayList<Price>();
			var divList   = new ArrayList<Dividend>();
			
			if ((count++ % 100) == 0) logger.info("updateDividendPrice {}", String.format("%4d / %4d", count, fundDataList.size()));

			var diviedendPriceList = CSVUtil.read(DividendPrice.class).file(file);
			for(var e: diviedendPriceList) {
				LocalDate  date;
				{
					Matcher m = pat.matcher(e.date);
					if (m.find()) {
						int yyyy = Integer.parseInt(m.group("yyyy"));
						int mm   = Integer.parseInt(m.group("mm"));
						int dd   = Integer.parseInt(m.group("dd"));
						date = LocalDate.of(yyyy, mm, dd);
					} else {
						logger.error("Unexpected date");
						logger.error("  isinCode      {}", isinCode);
						logger.error("  dividendPrice {}", e);
						throw new UnexpectedException("Unexpected date");
					}
				}
				BigDecimal nav   = new BigDecimal(e.nav);
				BigDecimal price = new BigDecimal(e.price);
				
				priceList.add(new Price(date, nav, price));
				
				if (!e.dividend.isEmpty()) {
					BigDecimal div = new BigDecimal(e.dividend);
					divList.add(new Dividend(date, div));
				}
			}
			// logger.info("save {}  div {}  price {}", isinCode, divList.size(), priceList.size());
			Dividend.save(isinCode, divList);
			Price.save(isinCode, priceList);
		}
	}
	
	private static void updateSeller(List<FundDataSearch.ResultInfo> resultInfoList) {
		int count = 0;
		for(var resultInfo: resultInfoList) {
			if ((count++ % 100) == 0) logger.info("updateSeller {}", String.format("%4d / %4d", count, resultInfoList.size()));
			if (resultInfo.institutionInfo == null) continue;
			String isinCode = resultInfo.isinCd;
			var sellerList = new ArrayList<Seller>();
			for(var inst: resultInfo.institutionInfo) {				
				String     name     = inst.instName;
				BigDecimal salesFee = inst.salesFee;
				
				if (salesFee == null) continue;
				
				sellerList.add(new Seller(name, salesFee));
			}
			if (!sellerList.isEmpty()) {
				Seller.save(isinCode, sellerList);
			}
		}
	}
	
	private static String getDetailPath(String isinCode) {
		return Storage.Toushin2.getPath("detail", isinCode);
	}
	private static File downloadDetailPage(String isinCode) {
		File file = new File(getDetailPath(isinCode));

		String url = String.format("https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000?isinCd=%s", isinCode);
		var result = HttpUtil.getInstance().download(url);
		
		if (result.result == null) {
			logger.error("Download failed");
			logger.error("  url    {}", url);
			logger.error("  result {} {}", result.code, result.reasonPhrase);
			throw new UnexpectedException("Dowload failed");
		}
		FileUtil.write().file(file, result.result);

		return file;
	}
	private static List<File> downloadDetailAll(List<FundData>fundDataList) {
		List<File> fileList = new ArrayList<>();
		int count = 0;
		for(var fundData: fundDataList) {
			String isinCode = fundData.isinCode;
			
			if ((count++ % 100) == 0) logger.info("downloadDetail {}", String.format("%4d / %4d", count, fundDataList.size()));

			File file = new File(getDetailPath(isinCode));
			if (!file.exists()) {
				file = downloadDetailPage(isinCode);
			}
			fileList.add(file);
		}
		return fileList;
	}
	private static void updateDetail(List<FundData>fundDataList) {
		List<Detail> detailList = new ArrayList<>();
		{
			List<File> fileList = downloadDetailAll(fundDataList);
			
			int count = 0;
			for(var file: fileList) {
				if ((count++ % 100) == 0) logger.info("updateDividend {}", String.format("%4d / %4d", count, fundDataList.size()));
				
				logger.info("file {}", file.getName());
				String page = FileUtil.read().file(file);
				var detail = Detail.getInstance(page);
				logger.info("detail {}", detail);
				
				if (detail != null) detailList.add(detail);
			}
		}
		logger.info("save {} {}", detailList.size(), Detail.getPath());
		Detail.save(detailList);
	}
	
	private static void update() {
		// build resultInfoList
		var resultInfoList = new ArrayList<FundDataSearch.ResultInfo>();
		updateResultInfoList(resultInfoList);
		logger.info("resultInfoList {}", resultInfoList.size());
		
		// build fundDataList
		var fundDataList = new ArrayList<FundData>();
		updateFundDataList(resultInfoList, fundDataList);
		logger.info("fundDataList {}", fundDataList.size());
		FundData.save(fundDataList);
		
		/*
		// update dividend and price
		updateDividendPrice(fundDataList);
		
		// update seller
		updateSeller(resultInfoList);
		*/
		
		// update detail
		updateDetail(fundDataList);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}

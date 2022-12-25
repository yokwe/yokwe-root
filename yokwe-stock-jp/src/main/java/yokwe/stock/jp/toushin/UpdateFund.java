package yokwe.stock.jp.toushin;

import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.hc.core5.http2.HttpVersionPolicy;

import yokwe.util.FileUtil;
import yokwe.util.ListUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.HttpUtil;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;
import yokwe.util.http.Task;
import yokwe.util.json.JSON;

public class UpdateFund {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	
	//
	// Fund Seller
	//
	private static final Pattern PAT_ESTABLISHED_DATE = Pattern.compile("(?<yyyy>[12][09][0-9][0-9])-(?<mm>[01]?[0-9])-(?<dd>[0123]?[0-9]) 00:00:00");
	private static final Pattern PAT_REDEMPTION_DATE = Pattern.compile("(?<yyyy>[12][09][0-9][0-9])(?<mm>[01]?[0-9])(?<dd>[0123]?[0-9])");
	private static Fund toFund(FundDataSearch.ResultInfo resultInfo) {
		String    isinCode       = resultInfo.isinCd;
		String    fundCode       = resultInfo.associFundCd;
		
		LocalDate listingDate;
		{
			// 2016-07-29 00:00:00
			Matcher m = PAT_ESTABLISHED_DATE.matcher(resultInfo.establishedDate);
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
			if (resultInfo.redemptionDate.equals("99999999")) {
				redemptionDate = Fund.NO_REDEMPTION_DATE;
			} else {
				Matcher m = PAT_REDEMPTION_DATE.matcher(resultInfo.redemptionDate);
				if (m.find()) {
					int yyyy = Integer.parseInt(m.group("yyyy"));
					int mm   = Integer.parseInt(m.group("mm"));
					int dd   = Integer.parseInt(m.group("dd"));
					redemptionDate = LocalDate.of(yyyy, mm, dd);
				} else {
					logger.error("Unexpected redemptionDate");
					logger.error("  isinCode       {}", isinCode);
					logger.error("  redemptionDate {}", resultInfo.redemptionDate);
					throw new UnexpectedException("Unexpected redemptionDate");
				}
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
		
		String fundType = FundDataSearch.FundType.getInstance(resultInfo.unitOpenDiv).getName();

		String investingArea;
		{
			List<String> areaList = new ArrayList<>();
			if (resultInfo.investArea10kindCd1.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("1").getName());
			if (resultInfo.investArea10kindCd2.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("2").getName());
			if (resultInfo.investArea10kindCd3.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("3").getName());
			if (resultInfo.investArea10kindCd4.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("4").getName());
			if (resultInfo.investArea10kindCd5.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("5").getName());
			if (resultInfo.investArea10kindCd6.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("6").getName());
			if (resultInfo.investArea10kindCd7.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("7").getName());
			if (resultInfo.investArea10kindCd8.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("8").getName());
			if (resultInfo.investArea10kindCd8.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("9").getName());
			if (resultInfo.investArea10kindCd10.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("10").getName());
			
			investingArea = String.join(", ", areaList);
		}
		
		String investingAsset = FundDataSearch.InvestingAsset.getInstance(resultInfo.investAssetKindCd).getName();
		String indexFundType  = FundDataSearch.IndexFundType.getInstance(resultInfo.supplementKindCd).getName();
		String settlementDate = resultInfo.setlDate;
		
		Fund fund = new Fund(
				isinCode, fundCode, listingDate, redemptionDate, divFreq, name,
				expenseRatio, expenseRatioManagement, expenseRatioSales, expenseRatioTrustBank,
				buyFreeMax, cancelLationFeeCode, retentionMoneyCode,
				fundType, investingArea, investingAsset, indexFundType, settlementDate
				);
		return fund;
	}

	private static class FundDataSearchConumer implements Consumer<String> {
		private static void saveSeller(FundDataSearch.ResultInfo resultInfo) {
			var sellerList = new ArrayList<Seller>();
			
			if (resultInfo.institutionInfo != null) {;
				for(var inst: resultInfo.institutionInfo) {				
					String     name     = inst.instName;
					BigDecimal salesFee = inst.salesFee;
					
					if (salesFee == null) continue;
					
					sellerList.add(new Seller(name, salesFee));
				}
			}
			if (!sellerList.isEmpty()) {
				Seller.save(resultInfo.isinCd, sellerList);
			}
		}

		public List<Fund> fundList  = new ArrayList<>();
		public int        allPageNo = -1;
		public int        pageSize  = -1;
		
		public FundDataSearchConumer() {
		}
		@Override
		public void accept(String string) {
			var data = JSON.unmarshal(FundDataSearch.class, string);
			if (data == null) {
				logger.error("JSON unmarshll failed");
				logger.error("  string {}", string);
				throw new UnexpectedException("JSON unmarshll failed");
			} else {
				allPageNo = data.allPageNo;
				pageSize  = data.pageSize;
				
				for(var e: data.resultInfoArray) {
					fundList.add(toFund(e));
					saveSeller(e);
				}
			}
		}
	}

	private static String getBody(int startNo) {
		return String.format(
			"t_keyword=&t_kensakuKbn=&t_fundCategory=&s_keyword=&s_kensakuKbn=1&" +
			"s_supplementKindCd=1&s_standardPriceCond1=0&s_standardPriceCond2=0&" +
			"s_riskCond1=0&s_riskCond2=0&s_sharpCond1=0&s_sharpCond2=0&s_buyFee=1&" +
			"s_trustReward=1&s_monthlyCancelCreateVal=1&s_instCd=&salesInstDiv=&" +
			"s_fdsInstCd=&startNo=%d&draw=0&searchBtnClickFlg=false", startNo);
	}

	private static void updateFundSeller(Download download) {
		String url         = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST999900/fundDataSearch";
		String contentType = "application/x-www-form-urlencoded;charset=UTF-8";
		
		var consumer = new FundDataSearchConumer();

		// startNo = 0
		HttpUtil.Result result = HttpUtil.getInstance().withPost(getBody(0), contentType).download(url);
		if (result.result == null) {
			logger.error("Dowload failed");
			logger.error("  url    {}", url);
			logger.error("  result {} {}", result.code, result.reasonPhrase);
			throw new UnexpectedException("Dowload failed");
		}
		consumer.accept(result.result);

		int pageNo  = 0;
		int startNo = 0;
		for(;;) {
			pageNo  += 1;
			startNo += consumer.pageSize;
			if (consumer.allPageNo <= pageNo) break;
			
			String content = getBody(startNo);
			
			Task task = StringTask.post(url, consumer, content, contentType);
			download.addTask(task);
		}
		
		int progressInterval = 30;
		logger.info("progressInterval  {}", progressInterval);
		download.setProgressInterval(progressInterval);

		logger.info("BEFORE RUN");
		download.startAndWait();
		logger.info("AFTER  RUN");
		
		logger.info("fundList {}  {}", consumer.fundList.size(), Fund.getPath());
		Fund.save(consumer.fundList);
	}

	
	//
	// Dividend Price
	//
	private static class DivPriceConsumer implements Consumer<String> {
		private static final Pattern PAT_DATE = Pattern.compile("(?<yyyy>[12][09][0-9][0-9])年(?<mm>[01]?[0-9])月(?<dd>[0123]?[0-9])日");

		public final String isinCode;
		
		public DivPriceConsumer(String isinCode) {
			this.isinCode = isinCode;
		}
		
		@Override
		public void accept(String string) {
			var divPriceList = ListUtil.load(DivPrice.class, new StringReader(string));
			if (divPriceList == null) {
				logger.error("Unexpected null");
				logger.error("string {}", string);
				throw new UnexpectedException("Unexpected null");
			}
			
			// build divList and priceList
			var divList   = new ArrayList<Dividend>();
			var priceList = new ArrayList<Price>();
			for(var divPrice: divPriceList) {
				LocalDate  date;
				{
					Matcher m = PAT_DATE.matcher(divPrice.date);
					if (m.find()) {
						int yyyy = Integer.parseInt(m.group("yyyy"));
						int mm   = Integer.parseInt(m.group("mm"));
						int dd   = Integer.parseInt(m.group("dd"));
						date = LocalDate.of(yyyy, mm, dd);
					} else {
						logger.error("Unexpected date");
						logger.error("  isinCode {}", isinCode);
						logger.error("  divPrice {}", divPrice);
						throw new UnexpectedException("Unexpected date");
					}
				}
				BigDecimal nav   = new BigDecimal(divPrice.nav);
				BigDecimal price = new BigDecimal(divPrice.price);
				
				priceList.add(new Price(date, nav, price));
				
				if (!divPrice.dividend.isEmpty()) {
					BigDecimal div = new BigDecimal(divPrice.dividend);
					divList.add(new Dividend(date, div));
				}
			}

			// logger.info("save {}  div {}  price {}", isinCode, divList.size(), priceList.size());
			if (!divList.isEmpty())   Dividend.save(isinCode, divList);
			if (!priceList.isEmpty()) Price.save(isinCode, priceList);
		}
	}
	
	private static void moveUnknownFile() {
		Set<String> validNameSet = Fund.getList().stream().map(o -> o.isinCode + ".csv").collect(Collectors.toSet());
		
		// price
		FileUtil.moveUnknownFile(validNameSet, Price.getPath(), Price.getPathDelist());
		// div
		FileUtil.moveUnknownFile(validNameSet, Dividend.getPath(), Dividend.getPathDelist());
	}
	
	private static void updateDivPrice(Download download) {
		Charset charset = Charset.forName("SHIFT_JIS");
		var fundList = Fund.getList();
		Collections.shuffle(fundList); // shuffle fundList
		for(var fund: fundList) {
			String isinCode = fund.isinCode;
			String fundCode = fund.fundCode;
			String url = String.format("https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/csv-file-download?isinCd=%s&associFundCd=%s", isinCode, fundCode);

			var consumer = new DivPriceConsumer(isinCode);
			var task = StringTask.get(url, consumer, charset);
			download.addTask(task);
		}
		
		int progressInterval = 500;
		logger.info("progressInterval  {}", progressInterval);
		download.setProgressInterval(progressInterval);
		
		logger.info("BEFORE RUN");
		download.startAndWait();
		logger.info("AFTER  RUN");
	}
	
	private static void initialize(Download download) {
		int threadCount       = 10;
		int maxPerRoute       = 50;
		int maxTotal          = 100;
		int soTimeout         = 30;
		int connectionTimeout = 30;
		int progressInterval  = 100;
		logger.info("threadCount       {}", threadCount);
		logger.info("maxPerRoute       {}", maxPerRoute);
		logger.info("maxTotal          {}", maxTotal);
		logger.info("soTimeout         {}", soTimeout);
		logger.info("connectionTimeout {}", connectionTimeout);
		logger.info("progressInterval  {}", progressInterval);
		
		RequesterBuilder requesterBuilder = RequesterBuilder.custom()
				.setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
				.setSoTimeout(soTimeout)
				.setMaxTotal(maxTotal)
				.setDefaultMaxPerRoute(maxPerRoute);

		download.setRequesterBuilder(requesterBuilder);
		
		// Configure custom header
		download.setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit");
		
		// Configure thread count
		download.setThreadCount(threadCount);
		
		// connection timeout in second
		download.setConnectionTimeout(connectionTimeout);
		
		// progress interval
		download.setProgressInterval(progressInterval);
	}
	public static void main(String[] args) {
		logger.info("START");
		
		Download download = new DownloadSync();
		initialize(download);
		
		updateFundSeller(download);
		updateDivPrice(download);
		moveUnknownFile();
		
		logger.info("STOP");
	}
}

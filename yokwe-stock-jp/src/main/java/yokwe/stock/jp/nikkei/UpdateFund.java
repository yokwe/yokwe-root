package yokwe.stock.jp.nikkei;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class UpdateFund {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static void testFund() {
		var detail = DownloadFile.FUND_DETAIL;
		var list = detail.getCodeList();
		int count = 0;
		for(var code: list) {
			String path = detail.getPath(code);
			String page = FileUtil.read().file(path);

			count++;
			if ((count % 1000) == 1) logger.info("{}", String.format("%4d / %4d", count, list.size()));
			//logger.info("{} {}", path, String.format("%4d / %4d", count, list.size()));
			
			boolean hasError = false;
			
			var qpCommon01 = CommonCode.getInstance(page);
			if (qpCommon01 == null) {
				logger.error("{}  qpCommon01 is null", path);
				hasError = true;
			}
			var qpCommon02 = CommonName.getInstance(page);
			if (qpCommon02 == null) {
				logger.error("{}  qpCommon02 is null", path);
				hasError = true;
			}
			var qpCommon03 = CommonPrice.getInstance(page);
			if (qpCommon03 == null) {
				logger.error("{}  qpCommon03 is null", path);
				hasError = true;
			}
			var qpBasic = FundValues.getInstance(page);
			if (qpBasic == null) {
				logger.error("{}  qpBasic is null", path);
				hasError = true;
			}
			var qpFund = FundInfo.getInstance(page);
			if (qpFund == null) {
				logger.error("{}  qpFund is null", path);
				hasError = true;
			}
			var qpPolicy = FundPolicy.getInstance(page);
			if (qpPolicy == null) {
				logger.error("{}  qpPolicy is null", path);
				hasError = true;
			}
			
			if (hasError) break;

			if (count <= 5) {
				logger.info("{}", code);
				logger.info("  {}", qpCommon01.toString());
				logger.info("  {}", qpCommon02.toString());
				logger.info("  {}", qpCommon03.toString());
				logger.info("  {}", qpBasic.toString());
				logger.info("  {}", qpFund.toString());
				logger.info("  {}", qpPolicy.toString());
			} else {
//				break;
			}
		}
	}
	public static void testPerf() {
		var detail = DownloadFile.PERF_DETAIL;
		var list = detail.getCodeList();
		int count = 0;
		for(var code: list) {
			String path = detail.getPath(code);
			String page = FileUtil.read().file(path);

			count++;
			if ((count % 1000) == 1) logger.info("{}", String.format("%4d / %4d", count, list.size()));
			//logger.info("{} {}", path, String.format("%4d / %4d", count, list.size()));
			
			boolean hasError = false;
			
			var qpCommon01 = CommonCode.getInstance(page);
			if (qpCommon01 == null) {
				logger.error("{}  qpCommon01 is null", path);
				hasError = true;
			}
			var qpCommon02 = CommonName.getInstance(page);
			if (qpCommon02 == null) {
				logger.error("{}  qpCommon02 is null", path);
				hasError = true;
			}
			var qpCommon03 = CommonPrice.getInstance(page);
			if (qpCommon03 == null) {
				logger.error("{}  qpCommon03 is null", path);
				hasError = true;
			}
			var qpRiskReturn = PerfValues.getInstance(page);
			if (qpRiskReturn == null) {
				logger.error("{}  qpRiskReturn is null", path);
				hasError = true;
			}
			var qpScore = PerfScore.getInstance(page);
			if (qpScore == null) {
				logger.error("{}  qpScore is null", path);
				hasError = true;
			}

			if (hasError) break;

			if (count <= 5) {
				logger.info("{}", code);
				logger.info("  {}", qpCommon01.toString());
				logger.info("  {}", qpCommon02.toString());
				logger.info("  {}", qpCommon03.toString());
				logger.info("  {}", qpRiskReturn.toString());
				logger.info("  {}", qpScore.toString());
			} else {
//				break;
			}
		}
	}
	public static void testDiv() {
		var detail = DownloadFile.DIV_DETAIL;
		var list = detail.getCodeList();
		int count = 0;
		for(var code: list) {
			String path = detail.getPath(code);
			File   file = new File(path);
			if (!file.exists()) continue;
			
			String page = FileUtil.read().file(path);

			count++;
			if ((count % 1000) == 1) logger.info("{}", String.format("%4d / %4d", count, list.size()));
			//logger.info("{} {}", path, String.format("%4d / %4d", count, list.size()));
			
			boolean hasError = false;
			
			var qpCommon01 = CommonCode.getInstance(page);
			if (qpCommon01 == null) {
				logger.error("{}  qpCommon01 is null", path);
				hasError = true;
			}
			var qpCommon02 = CommonName.getInstance(page);
			if (qpCommon02 == null) {
				logger.error("{}  qpCommon02 is null", path);
				hasError = true;
			}
			var qpCommon03 = CommonPrice.getInstance(page);
			if (qpCommon03 == null) {
				logger.error("{}  qpCommon03 is null", path);
				hasError = true;
			}
			var qpDiv = DivHistory.getInstance(page);
			if (qpDiv == null) {
				logger.error("{}  qpDiv is null", path);
				hasError = true;
			}
			var qpDivHealth = DivScore.getInstance(page);
			if (qpDivHealth == null) {
				logger.error("{}  qpDivHealth is null", path);
				hasError = true;
			}
			var qpYield = DivLast.getInstance(page);
			if (qpYield == null) {
				logger.error("{}  qpYield is null", path);
				hasError = true;
			}

			if (hasError) break;

			if (count <= 5) {
				logger.info("{}", code);
				logger.info("  {}", qpCommon01.toString());
				logger.info("  {}", qpCommon02.toString());
				logger.info("  {}", qpCommon03.toString());
				logger.info("  {}", qpDiv.toString());
				logger.info("  {}", qpDivHealth.toString());
				logger.info("  {}", qpYield.toString());
			} else {
//				break;
			}
		}
	}
	
	private static final BigDecimal MINUS_ONE = BigDecimal.ONE.negate();
	
	private static String fromPercentString(String string) {
		String numericString = string.trim().replace("%", "");
		return numericString.compareTo("--") == 0 ? "" : new BigDecimal(numericString).movePointLeft(2).toPlainString();
	}
	
	private static String fromCurrencyString(String string) {
		String currencyString = string.trim().replace(",", "").replace("円", "");
		return currencyString.compareTo("--") == 0 ? "" : new BigDecimal(currencyString).movePointLeft(2).toPlainString();
	}
	
	private static Pattern pat_DATE  = Pattern.compile("(19[6-9][0-9]|20[0-9]{2})年([012]?[0-9])月([123]?[0-9])日");
	private static LocalDate fromDateString(String string) {
		if (string.compareTo("無期限") == 0) return Fund.UNLIMITED_DATE;
		if (string.compareTo("--") == 0) return Fund.UNLIMITED_DATE;
		
		Matcher m = pat_DATE.matcher(string);
		if (m.matches()) {
			String yearString  = m.group(1);
			String monthString = m.group(2);
			String dayString   = m.group(3);
			int year  = Integer.parseInt(yearString);
			int month = Integer.parseInt(monthString);
			int day   = Integer.parseInt(dayString);
			return LocalDate.of(year, month, day);
		} else {
			logger.error("Unexpecetd");
			logger.error("  {}!", string);
			throw new UnexpectedException("Unxpected");
		}
	}

 	private static void set(Fund fund, CommonName commonName) {
		fund.name = commonName.name;
	}
	private static void set(Fund fund, FundInfo fundInfo) {
		fund.category1 = fundInfo.category1;
		fund.category2 = fundInfo.category2;
		fund.category3 = fundInfo.category3;
		fund.settlementFrequency = fundInfo.settlementFrequency;
		fund.initiationDate = fromDateString(fundInfo.establishmentDate);
		fund.redemptionDate = fromDateString(fundInfo.redemptionDate);
		fund.salesType = fundInfo.salesType;
		fund.fundType = fundInfo.fundType;
		fund.initialFee = fromPercentString(fundInfo.initialFee);
		fund.trustFee = fromPercentString(fundInfo.trustFee);
		
		if (fund.category2 == null) fund.category2 = "";
		if (fund.category3 == null) fund.category3 = "";
	}
	private static void set(Fund fund, FundPolicy fundPolicy) {
		fund.policy = fundPolicy.policy;
	}
	private static void set(Fund fund, PerfScore perfScore) {
		fund.scoreAsOf = perfScore.asOf;
		fund.scoreOverAll = perfScore.scoreOverAll;
		fund.scoreRisk = perfScore.scoreRisk;
		fund.scoreReturn = perfScore.scoreReturn;
		fund.scoreDownsideResistance = perfScore.scoreDownsideResistance;
		fund.scoreCost = perfScore.scoreCost;
		fund.scoreDivHealth = perfScore.scoreDivHealth;
	}
	private static void set(Fund fund, PerfValues perfValues) {
		fund.valueAsOf = perfValues.asOf;
		fund.return6m = fromPercentString(perfValues.return6m);
		fund.return1y = fromPercentString(perfValues.return1y);
		fund.return3y = fromPercentString(perfValues.return3y);
		fund.return5y = fromPercentString(perfValues.return5y);
		fund.return10y = fromPercentString(perfValues.return10y);
		fund.risk6m = fromPercentString(perfValues.risk6m);
		fund.risk1y = fromPercentString(perfValues.risk1y);
		fund.risk3y = fromPercentString(perfValues.risk3y);
		fund.risk5y = fromPercentString(perfValues.risk5y);
		fund.risk10y = fromPercentString(perfValues.risk10y);
		fund.sharpRatio6m = perfValues.sharpRatio6m;
		fund.sharpRatio1y = perfValues.sharpRatio1y;
		fund.sharpRatio3y = perfValues.sharpRatio3y;
		fund.sharpRatio5y = perfValues.sharpRatio5y;
		fund.sharpRatio10y = perfValues.sharpRatio10y;
	}
	private static void set(Fund fund, DivScore divScore) {
		fund.divScore1Y  = fromPercentString(divScore.score1Y);
		fund.divScore3Y  = fromPercentString(divScore.score3Y);
		fund.divScore5Y  = fromPercentString(divScore.score5Y);
		fund.divScore10Y = fromPercentString(divScore.score10Y);
	}
	private static void set(Fund fund, DivLast divLast) {
		fund.divLastDate = fromDateString(divLast.date);
		fund.divLastAmount = fromCurrencyString(divLast.amount);
		fund.divLastRate = fromPercentString(divLast.rate);
		fund.divLastPrice = fromCurrencyString(divLast.price);
	}

	
	public static void main(String[] args) {
		logger.info("START");
		
		Map<String, String> codeMap = yokwe.stock.jp.toushin.Fund.getList().stream().collect(Collectors.toMap(o -> o.fundCode, o -> o.isinCode));
		//  fundCode isinCode		
		
		List<Fund> fundList = new ArrayList<>();
		
		int errorCount = 0;
		var fundCodeList = DownloadFile.FUND_DETAIL.getCodeList();
		int count = 0;
		for(var fundCode: fundCodeList) {
			count++;
			if ((count % 100) == 1) logger.info("{}", String.format("%4d / %4d", count, fundCodeList.size()));
			
			if (!codeMap.containsKey(fundCode)) {
				logger.warn("no such code");
				logger.warn("  fundCode {}", fundCode);
				continue;
			}
			
			Fund fund = new Fund();
			fund.isinCode = codeMap.get(fundCode);
			fund.fundCode = fundCode;
			
			{
				String path = DownloadFile.FUND_DETAIL.getPath(fundCode);
				String page = FileUtil.read().file(path);
				
				var commonName = CommonName.getInstance(page);
				if (commonName == null) {
					logger.warn("{} commonName is null", path);
					errorCount++;
				} else {
					set(fund, commonName);
				}
				
				var fundInfo = FundInfo.getInstance(page);
				if (fundInfo == null) {
					logger.warn("{} fundInfo is null", path);
					errorCount++;
				} else {
					set(fund, fundInfo);
				}
				
				var fundPolicy = FundPolicy.getInstance(page);
				if (fundPolicy == null) {
					logger.warn("{} fundPolicy is null", path);
					errorCount++;
				} else {
					set(fund, fundPolicy);
				}
			}
			
			{
				String path = DownloadFile.PERF_DETAIL.getPath(fundCode);
				String page = FileUtil.read().file(path);

				var perfScore = PerfScore.getInstance(page);
				if (perfScore == null) {
					logger.warn("{} perfScore is null", path);
					errorCount++;
				} else {
					set(fund, perfScore);
				}
				
				var perfValues = PerfValues.getInstance(page);
				if (perfValues == null) {
					logger.warn("{} perfValues is null", path);
					errorCount++;
				} else {
					set(fund, perfValues);
				}
			}
			{
				String path = DownloadFile.DIV_DETAIL.getPath(fundCode);
				String page = FileUtil.read().file(path);

				var divScore = DivScore.getInstance(page);
				if (divScore == null) {
					logger.warn("{} divScore is null", path);
					errorCount++;
				} else {
					set(fund, divScore);
				}
				var divLast = DivLast.getInstance(page);
				if (divLast == null) {
					logger.warn("{} divLast is null", path);
					errorCount++;
				} else {
					set(fund, divLast);
				}
			}

			if (!fund.salesType.equals("--")) continue;
			if (fund.valueAsOf.equals("--")) continue;
			
			fundList.add(fund);
		}
		
		if (errorCount == 0) {
			logger.info("fundList  {}  {}", fundList.size(), Fund.getPath());
			Fund.save(fundList);
		} else {
			logger.info("errorCount {}", errorCount);
		}
		
		logger.info("STOP");
	}
}

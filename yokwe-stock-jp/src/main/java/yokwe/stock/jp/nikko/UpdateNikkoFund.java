package yokwe.stock.jp.nikko;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.toushin.Fund;
import yokwe.stock.jp.toushin.UpdateStats;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public final class UpdateNikkoFund {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = false;
		
	// https://www.smbcnikko.co.jp/products/inv/direct_fee/csv/coursedata.csv
	// 0905|インデックスファンドＴＳＰ|日興アセットマネジメント|国内株式|ノーロード|02311862|1
	// nikkoCode, name, company, type, fee, fundCode, flag
	
	public static final String URL = "https://www.smbcnikko.co.jp/products/inv/direct_fee/csv/coursedata.csv";
	public static String getURL() {
		return URL;
	}
	
	public static class CourseData {
		public String nikkoCode;
		public String name;
		public String company;
		public String type;
		public String salesFee;
		public String fundCode; // Do not use this fundCode. Instead get fundCode from nikkoCode using NikkoFundInfo
		public String flag;
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	private static String downloadCourseData() {
		String path = Storage.Nikko.getPath("coursedata.csv");
		File   file = new File(path);
		
		if (DEBUG_USE_FILE) {
			if (file.exists()) return FileUtil.read().file(file);
		}

		String url = getURL();
		HttpUtil.Result result = HttpUtil.getInstance().download(url);
		if (result == null) {
			logger.error("result == null");
			logger.error("  url {}!", url);
			throw new UnexpectedException("result == null");
		}
		if (result.result == null) {
			logger.error("result.result == null");
			logger.error("  url       {}!", url);
			logger.error("  response  {}  {}", result.code, result.reasonPhrase);
			throw new UnexpectedException("result.result == null");
		}
		
		// for debug
		logger.info("save  {}  {}", result.result.length(), path);
		FileUtil.write().file(file, result.result);
		
		return result.result;
	}
		
	public static void main(String[] args) {
		logger.info("START");
		
		Map<String, Fund> fundMap = Fund.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
		//  isinCode
		Map<String, NikkoFundInfo> fundInfoMap = NikkoFundInfo.getList().stream().collect(Collectors.toMap(o -> o.nikkoCode, Function.identity()));
		//  nikkoCode
		
		List<NikkoFund> fundList = new ArrayList<>();
		
		List<CourseData> list;
		{
			String string = downloadCourseData();
			Reader reader  = new StringReader(string);
			list = CSVUtil.read(CourseData.class).withHeader(false).withSeparator('|').file(reader);
			logger.info("list  {}", list.size());
		}
		
		Pattern pat = Pattern.compile("([0-9\\.]+)％");
		
		for(var e: list) {
			if (e.salesFee.contains("投信つみたてプラン専用銘柄")) continue;
			
			BigDecimal salesFee;
			if (e.salesFee.equals("ノーロード")) {
				salesFee = BigDecimal.ZERO;
			} else {
				Matcher m = pat.matcher(e.salesFee);
				if (m.find()) {
					salesFee = new BigDecimal(m.group(1)).movePointLeft(2).stripTrailingZeros();
				} else {
					logger.error("Unexpected fee");
					logger.error("  fund  {}", e);
					throw new UnexpectedException("Unexpected fee");
				}
			}
//			logger.debug("fee  {}  -  {}", fee, e.fee);

			if (fundInfoMap.containsKey(e.nikkoCode)) {
				NikkoFundInfo fundInfo = fundInfoMap.get(e.nikkoCode);
				
				String isinCode = fundInfo.isinCode;
				String fundCode = fundInfo.fundCode;
				
				// sanity check
				if (!fundMap.containsKey(isinCode)) {
					logger.warn("Bogus isinCode  {}  {}  {}  {}", isinCode, fundCode, e.nikkoCode, e.name);
					continue;
				}
				BigDecimal expenseRatio;
				{
					Fund fund = fundMap.get(isinCode);
					expenseRatio = fund.expenseRatio.multiply(UpdateStats.CONSUMPTION_TAX_RATE);
				}
				
				NikkoFund  fund = new NikkoFund(isinCode, fundCode, e.nikkoCode, salesFee, expenseRatio, e.name);
				fundList.add(fund);
			} else {
				logger.warn("Bogus nikkoCode  {}  {}  {}", e.nikkoCode, e.fundCode, e.name);
			}
		}

		logger.info("save  {}  {}", fundList.size(), NikkoFundInfo.getPath());
		NikkoFund.save(fundList);
		
		logger.info("STOP");		
	}
	
}

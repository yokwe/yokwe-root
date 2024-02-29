package yokwe.finance.provider.nikko;

import java.io.File;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.type.TradingFundType;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateTradingFundNikko {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = false;
	
	private static final String URL     = "https://www.smbcnikko.co.jp/products/inv/direct_fee/csv/coursedata.csv";
	private static final String CHARSET = "UTF-8";
	
	private static final String NO_LOAD = "ノーロード";
	
	private static String download(String url, String charset, String filePath, boolean useFile) {
		final String page;
		{
			File file = new File(filePath);
			if (useFile && file.exists()) {
				page = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().withCharset(charset).download(url);
				if (result == null || result.result == null) {
					logger.error("Unexpected");
					logger.error("  result  {}", result);
					throw new UnexpectedException("Unexpected");
				}
				page = result.result;
				// debug
				if (DEBUG_USE_FILE) logger.info("save  {}  {}", page.length(), file.getPath());
				FileUtil.write().file(file, page);
			}
		}
		return page;
	}
	
	
	public static class CourseData {
		public String nikkoCode;
		public String name;
		public String company;
		public String type;
		public String salesFee;
		public String fundCode; // Do not use this fundCode.
		public String flag;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	
	private static void update() {
		var fundCodeMap = StorageFund.FundInfo.getList().stream().collect(Collectors.toMap(o -> o.fundCode, o -> o.isinCode));
		logger.info("fundCode  {}", fundCodeMap.size());

		String page;
		{
			String filePath = StorageNikko.storage.getPath("coursedata.csv");
			page = download(URL, CHARSET, filePath, DEBUG_USE_FILE);
		}
		var fundList = CSVUtil.read(CourseData.class).withHeader(false).withSeparator('|').file(new StringReader(page));
		logger.info("fundList  {}", fundList.size());
		
		var list = new ArrayList<TradingFundType>();
		{
			Pattern pat     = Pattern.compile("(?<percent>[0-9]+\\.[0-9]+)％");
			Pattern patFund = Pattern.compile("銘柄コード：(?<nikkoCode>.{4})　投信協会コード：(?<fundCode>.{8})");
			
			int countA = 0;
			int countB = 0;
			int countC = 0;
			int countD = 0;
			int countE = 0;
			int countF = 0;
			int countG = 0;
			int countH = 0;
			
			for(var data: fundList) {
				String     isinCode;
				BigDecimal salesFee;
				{
					{
						var fundCode = data.fundCode;
						if (fundCodeMap.containsKey(data.fundCode)) {
							isinCode = fundCodeMap.get(fundCode);
							countA++;
						} else {
							// find correct fundCode from fund page
							String url = "https://fund2.smbcnikko.co.jp/smbc_nikko_fund/qsearch.exe?F=detail_kokunai1&KEY1=" + fundCode;
							HttpUtil.Result result = HttpUtil.getInstance().withCharset(CHARSET).download(url);
							if (result != null && result.result != null) {
								Matcher m = patFund.matcher(result.result);
								if (m.find()) {
									String newFundCode  = m.group("fundCode");
									
									if (fundCodeMap.containsKey(newFundCode)) {
										isinCode = fundCodeMap.get(newFundCode);
										countB++;
									} else {
										logger.info("bogus new fundCode  {}  {}  {}  {}", newFundCode, data.fundCode, data.nikkoCode, data.name);
										countC++;
										continue;
									}
								} else {
									logger.info("no fund page {}  {}  {}", data.fundCode, data.nikkoCode, data.name);
									countD++;
									continue;
								}
							} else {
								logger.info("faild to download fund page {}  {}  {}", data.fundCode, data.nikkoCode, data.name);
								countE++;
								continue;
							}
						}
					}
					
					if (data.salesFee.startsWith(NO_LOAD)) {
						salesFee = BigDecimal.ZERO;
						countF++;
					} else {
						Matcher m = pat.matcher(data.salesFee);
						if (m.find()) {
							String string = m.group("percent");
							salesFee = new BigDecimal(string).movePointLeft(2); // change to percent
							countG++;
						} else {
							salesFee = TradingFundType.SALES_FEE_UNKNOWN;
							logger.info("bogus salesFee  {}  {}  {}  !{}!", data.fundCode, data.nikkoCode, data.salesFee);
							countH++;
						}
					}
				}
				
				list.add(new TradingFundType(isinCode, salesFee));
			}
			
			logger.info("countA  {}", countA);
			logger.info("countB  {}", countB);
			logger.info("countC  {}", countC);
			logger.info("countD  {}", countD);
			logger.info("countE  {}", countE);
			logger.info("countF  {}", countF);
			logger.info("countG  {}", countG);
			logger.info("countH  {}", countH);
		}
		
		{
			logger.info("list  {}", list.size());
			var fundCodeSet = StorageNikko.FundInfoNikko.getList().stream().filter(o -> o.isDirect() && o.hasProspectus()).map(o -> o.isinCode).collect(Collectors.toSet());
			list.removeIf(o -> !fundCodeSet.contains(o.isinCode));
			logger.info("list  {}", list.size());
		}
		
		logger.info("save  {}  {}", list.size(), StorageNikko.TradingFundNikko.getPath());
		StorageNikko.TradingFundNikko.save(list);
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}

package yokwe.finance.account.prestia;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class FundPrestia implements Comparable<FundPrestia> {	
	public String fundCode;
	public String fundName;
	
	public FundPrestia(String fundCode, String fundName) {
		this.fundCode = fundCode;
		this.fundName = fundName;
	}
	
	@Override
	public String toString() {
		return String.format("{%s  %s}", fundCode, fundName);
	}
	@Override
	public int compareTo(FundPrestia that) {
		return this.fundCode.compareTo(that.fundCode);
	}
	
	
	public static class FundPrestiaInfo {
		private static final Pattern PAT = Pattern.compile("<tr><td class=\"align-l\">(?<fundName>.+?)</td><td>(?<fundCode>.+?)</td></tr>");
		
		public static List<FundPrestiaInfo> getInstance(String page) {
			return ScrapeUtil.getList(FundPrestiaInfo.class, PAT, page);
		}
		
		public final String fundCode;
		public final String fundName;
		
		public FundPrestiaInfo(String fundCode, String fundName) {
			this.fundCode    = fundCode;
			this.fundName    = fundName;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s}", fundCode, fundName);
		}
	}
	
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	public static void main(String[] args) {
		logger.info("START");
		
		String url = "https://www.smbctb.co.jp/ib_help/investments_and_reports/mutual_funds_fundscode_list.html";
		
		var result = HttpUtil.getInstance().download(url);
		if (result == null ) {
			logger.error("result is null");
			throw new UnexpectedException("result is null");
		}
		if (result.result == null) {
			logger.error("result.result is null");
			logger.error("  result  {}", result.toString());
			throw new UnexpectedException("result.result is null");
		}
		
		var list = new ArrayList<FundPrestia>();
		{
			var fundPrestiaInfoList = FundPrestiaInfo.getInstance(result.result);
			for(var e: fundPrestiaInfoList) {
				list.add(new FundPrestia(e.fundCode, e.fundName));
			}
		}
		logger.info("save  {}  {}", list.size(), StoragePrestia.FundPrestia.getPath());
		StoragePrestia.FundPrestia.save(list);
		
		logger.info("STOP");
	}
}

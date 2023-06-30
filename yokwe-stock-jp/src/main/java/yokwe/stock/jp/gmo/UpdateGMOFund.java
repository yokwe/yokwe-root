package yokwe.stock.jp.gmo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.stock.jp.toushin.Fund;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateGMOFund {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static class FundData {
		// <input id="JP90C000HR52" value="三菱ＵＦＪ国際投信株式会社" type="hidden">
		private static final Pattern PAT = Pattern.compile(
			"<input id=\"(?<isinCode>[A-Z0-9]+)\" value=.+? type=\"hidden\">"
		);
		public static List<FundData> getInstance(String page) {
			return ScrapeUtil.getList(FundData.class, PAT, page);
		}
		
		public String isinCode;		
		
		public FundData(String isinCode) {
			this.isinCode = isinCode;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}

	public static void main(String[] args) {
		logger.info("START");
		
		List<GMOFund> fundList = new ArrayList<>();
		
		{
			String url = "https://www.click-sec.com/corp/guide/fund/search/";
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
			
			Map<String, Fund> fundMap = Fund.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
			String page = result.result;
			List<FundData> list = FundData.getInstance(page);
			logger.info("list  {}", list.size());
			for(var e: list) {
				String isinCode = e.isinCode;
				if (fundMap.containsKey(isinCode)) {
					Fund   fund     = fundMap.get(isinCode);
					String fundCode = fund.fundCode;
					String name     = fund.name;
					
					fundList.add(new GMOFund(isinCode, fundCode, name));
				} else {
					logger.warn("Unpexpected isinCode");
					logger.warn("  isinCode  {}", isinCode);
				}
			}
		}

		logger.info("save  {}  {}", fundList.size(), GMOFund.getPath());
		GMOFund.save(fundList);
		
		logger.info("STOP");
	}

}

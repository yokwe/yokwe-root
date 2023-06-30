package yokwe.stock.jp.nomura;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateNomuraFund {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = false;

	private static final int     MAX_DISP  = 50;
	private static final Charset UTF_8     = StandardCharsets.UTF_8;
	
	private static final Map<String, String> fundMap = yokwe.stock.jp.toushin.Fund.getList().stream().collect(Collectors.toMap(o -> o.fundCode, o -> o.isinCode));
	//                       fundCode isinCode
	
	public static class HitCount {
		// showHit("1196");
		public static final Pattern PAT = Pattern.compile(
			"showHit\\(\"(?<value>[0-9]+)\"\\);"
		);
		
		public static HitCount getInstance(String page) {
			return ScrapeUtil.get(HitCount.class, PAT, page);
		}

		public final int value;
		
		public HitCount(int value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("{%d}", value);
		}
	}
	
	
	public static class FundData {
		private static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<th><p .+?><a href=\"./qsearch.exe\\?F=users/nomura/detail2&KEY1=(?<fundCode>[0-9A-Z]+)\" .+?>(?<name>.+?)</a>.+?</p></th>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"</tr>",
			Pattern.DOTALL
		);
		public static List<FundData> getInstance(String page) {
			return ScrapeUtil.getList(FundData.class, PAT, page);
		}
		
		public String fundCode;		
		public String name;
		
		public FundData(String fundCode, String name) {
			this.fundCode = fundCode;
			int index = name.indexOf(" 愛称：");
			this.name = (index == -1) ? name : name.substring(0, index);
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}



	// F=users/nomura/list2&MAXDISP=50&GO_BEFORE=&BEFORE=100
	public static String getURL(int before) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("F", "users/nomura/list2");
		map.put("GO_BEFORE", "");
		map.put("BEFORE",   String.valueOf(before));
		map.put("MAXDISP",  String.valueOf(MAX_DISP));
		
		String string = map.entrySet().stream().map(o -> o.getKey() + "=" + URLEncoder.encode(o.getValue(), UTF_8)).collect(Collectors.joining("&"));
		
		return String.format("https://advance.quote.nomura.co.jp/meigara/nomura2/qsearch.exe?%s", string);
	}
	
	public static String getPage(int before) {
		final File file;
		{
			String name = String.format("%d.html", before);
			String path = Storage.Nomura.getPath("page", name);
			file = new File(path);
		}
		
		if (DEBUG_USE_FILE) {
			if (file.exists()) return FileUtil.read().file(file);
		}

		String url = getURL(before);
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
		
		String page = result.result.replace("\r", "\n");
		
		// for debug
		FileUtil.write().file(file, page);

		return page;
	}
	
	private static void addFund(List<NomuraFund> fundList, List<FundData> list) {
		for(var e: list) {
			NomuraFund fund = new NomuraFund();
			
			String isinCode;
			if (fundMap.containsKey(e.fundCode)) {
				isinCode = fundMap.get(e.fundCode);
			} else {
				logger.warn("Unexpecetd fundCode");
				logger.warn("  fund  {}", e);
				continue;
			}
			
			fund.isinCode = isinCode;
			fund.fundCode = e.fundCode;
			fund.name     = e.name;
			
			fundList.add(fund);
		}
		
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<NomuraFund> fundList = new ArrayList<>();
		
		HitCount hitCount;
		{
			String page = getPage(0);
			logger.info("page 0  {}", page.length());
			
			hitCount = HitCount.getInstance(page);
			logger.info("hitCount  {}", hitCount.value);
			
			List<FundData> list = FundData.getInstance(page);
			
			addFund(fundList, list);
		}
		
		for(int i = 0; i < hitCount.value; i += MAX_DISP) {
			if (i == 0) continue;
			
			String page = getPage(i);
			var    list = FundData.getInstance(page);
			logger.info("list      {}  {}", list.size(), i);
			if (list.size() != MAX_DISP && (i + list.size() != hitCount.value)) {
				logger.error("Unexpected list size");
				logger.error("  list      {}", list.size());
				logger.error("  i         {}", i);
				logger.error("  hitCount  {}", hitCount.value);
				throw new UnexpectedException("Unexpected fundLis size");
			}
			
			addFund(fundList, list);
		}
		logger.info("fundList  {}", fundList.size());
		if (fundList.size() != hitCount.value) {
			logger.error("Unexpected fundLis size");
			logger.error("  hitCount  {}", hitCount.value);
			logger.error("  fundList  {}", fundList.size());
			throw new UnexpectedException("Unexpected fundLis size");
		}

		logger.info("save  {}  {}", fundList.size(), NomuraFund.getPath());
		NomuraFund.save(fundList);
		
		logger.info("STOP");
	}
}

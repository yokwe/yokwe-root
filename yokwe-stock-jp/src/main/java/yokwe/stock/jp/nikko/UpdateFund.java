package yokwe.stock.jp.nikko;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.Charset;
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

public class UpdateFund {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	// https://fund2.smbcnikko.co.jp/smbc_nikko_fund/qsearch.exe?F=list_kokunai&
	//   KEY1=&KEY2=&KEY3=&KEY4=&KEY5=&KEY6=&KEY7=&KEY8=&KEY9=&KEY10=&
	//   KEY11=&KEY12=&KEY13=&KEY14=&KEY15=&KEY16=&KEY17=&KEY18=&KEY19=&KEY20=&
	//   KEY21=&KEY22=&KEY24=&
	//   KEY107=&KEY111=&KEY112=&KEY113=&KEY116=&KEY117=&
	//   KEY118=-AF%8F%83%8E%91%8E%59%91%8D%8A%7A&
	//   KEY120=&
	//   TARGET12=&TARGET13=&TARGET16=&TARGET22=&
	//   TARGET23=&GO_BEFORE=&BEFORE=0&
	//   MAXDISP=50&KEY119=50&
	//   ptype=search&REFINDEX=-AF%8F%83%8E%91%8E%59%91%8D%8A%7A

	// F=list_kokunai 国内投信
	// F=list_gaikoku 外国投信
	// F=gaikammf     日興外貨MMF
	// F=mrf          日興MRF
	// F=kousha       公社債投信

	
	private static final int     MAX_DISP  = 50;
	private static final Charset SHIFT_JIS = Charset.forName("SHIFT_JIS");
	
	public static class FundInfo {
		private static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td .+?><p>(?:<!--)?<a href=\"qsearch\\.exe\\?F=detail_kokunai1&KEY1=(?<fundCode>[0-9A-Z]+)\">(?:-->)?(?:<!--)?<a .+?>(?:-->)?(?<name>.+?)</a></p></td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>\\s+<p>(?<tradeSougou>.*?)</p>\\s+<p>(?<tradeDirect>.*?)</p>\\s+</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"</tr>",
			Pattern.DOTALL
		);
		public static List<FundInfo> getInstance(String page) {
			return ScrapeUtil.getList(FundInfo.class, PAT, page);
		}
		
		public String fundCode;
		
		public String tradeDirect;
		public String tradeSougou;
		
		public String name;
		
		public FundInfo(String fundCode, String tradeDirect, String tradeSougou, String name) {
			this.fundCode    = fundCode;
			this.tradeDirect = tradeDirect;
			this.tradeSougou = tradeSougou;
			this.name        = name;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}

	public static class HitCount {
		// <input type="hidden" name="HIT_COUNT" id="HIT_COUNT" value="1085" />
		public static final Pattern PAT = Pattern.compile(
			"<input type=\"hidden\" name=\"HIT_COUNT\" id=\"HIT_COUNT\" value=\"(?<value>[0-9]+)\" />"
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
	

	
	public static String getURL(int before) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("F", "list_kokunai");
		map.put("KEY1", "");
		map.put("KEY2", "");
		map.put("KEY3", "");
		map.put("KEY4", "");
		map.put("KEY5", "");
		map.put("KEY6", "");
		map.put("KEY7", "");
		map.put("KEY8", "");
		map.put("KEY9", "");
		//
		map.put("KEY10", "");
		map.put("KEY11", "");
		map.put("KEY12", "");
		map.put("KEY13", "");
		map.put("KEY14", "");
		map.put("KEY15", "");
		map.put("KEY16", "");
		map.put("KEY17", "");
		map.put("KEY18", "");
		map.put("KEY19", "");
		//
		map.put("KEY20", "");
		map.put("KEY21", "");
		map.put("KEY22", "");
		map.put("KEY24", "");
		//
		map.put("KEY107", "");
		//
		map.put("KEY111", "");
		map.put("KEY112", "");
		map.put("KEY113", "");
		//
		map.put("KEY116", "");
		map.put("KEY117", "");
		map.put("KEY118", "-AF純資産総額");
		//
		map.put("KEY120", "");
		//
		map.put("TARGET12", "");
		map.put("TARGET13", "");
		map.put("TARGET16", "");
		map.put("TARGET22", "");
		map.put("TARGET23", "");
		//
		map.put("GO_BEFORE", "");
		map.put("BEFORE",   String.valueOf(before));
		map.put("MAXDISP",  String.valueOf(MAX_DISP));
		map.put("KEY119",   String.valueOf(MAX_DISP));
		map.put("ptype",    "search");
		map.put("REFINDEX", "-AF純資産総額");
		
		String string = map.entrySet().stream().map(o -> o.getKey() + "=" + URLEncoder.encode(o.getValue(), SHIFT_JIS)).collect(Collectors.joining("&"));
		
		return String.format("https://fund2.smbcnikko.co.jp/smbc_nikko_fund/qsearch.exe?%s", string);
	}
	
	public static void addFund(Map<String, String> fundMap, List<Fund> fundList, List<FundInfo> list) {
		for(var e: list) {
			if (fundMap.containsKey(e.fundCode)) {
				Fund fund = new Fund(fundMap.get(e.fundCode), e.fundCode, e.tradeDirect.isEmpty() ? "0" : "1", e.tradeSougou.isEmpty() ? "0" : "1", e.name);
				fundList.add(fund);
			} else {
				logger.error("Unexpected fundCode");
				logger.error("  fundInfo  {}", e);
				throw new UnexpectedException("Unexpected fundCode");
			}
		}
	}
	
	public static String getPage(int before) {
		final File file;
		{
			String name = String.format("%d.html", before);
			String path = Storage.Nikko.getPath("page", name);
			file = new File(path);
		}
		
		// for debug
		if (file.exists()) {
			return FileUtil.read().file(file);
		}
		
		String url = getURL(before);
		HttpUtil.Result result = HttpUtil.getInstance().withCharset(SHIFT_JIS.name()).download(url);
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
		String page = result.result.replace("\r", "\n").replace("shift_jis", "utf-8");
		
		// for debug
		FileUtil.write().file(file, page);
		
		return page;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		Map<String, String> fundMap = yokwe.stock.jp.toushin.Fund.getList().stream().collect(Collectors.toMap(o -> o.fundCode, o -> o.isinCode));
		// fundCode isinCode
		
		List<Fund> fundList = new ArrayList<>();
		
		final HitCount hitCount;
		{
			String page = getPage(0);
			
			hitCount = HitCount.getInstance(page);
			logger.info("hitCount  {}", hitCount.value);
			
			var list = FundInfo.getInstance(page);
			logger.info("list      {}  {}", list.size(), 0);
			if (list.size() != MAX_DISP) {
				logger.error("Unexpected list size");
				logger.error("  list      {}", list.size());
				logger.error("  hitCount  {}", hitCount.value);
				throw new UnexpectedException("Unexpected fundLis size");
			}

			addFund(fundMap, fundList, list);
		}
		
		for(int i = 0; i < hitCount.value; i += MAX_DISP) {
			if (i == 0) continue;
			
			String page = getPage(i);
			var    list = FundInfo.getInstance(page);
			logger.info("list      {}  {}", list.size(), i);
			if (list.size() != MAX_DISP && (i + list.size() != hitCount.value)) {
				logger.error("Unexpected list size");
				logger.error("  list      {}", list.size());
				logger.error("  i         {}", i);
				logger.error("  hitCount  {}", hitCount.value);
				throw new UnexpectedException("Unexpected fundLis size");
			}
			
			addFund(fundMap, fundList, list);
		}
		logger.info("fundList  {}", fundList.size());
		if (fundList.size() != hitCount.value) {
			logger.error("Unexpected fundLis size");
			logger.error("  hitCount  {}", hitCount.value);
			logger.error("  fundList  {}", fundList.size());
			throw new UnexpectedException("Unexpected fundLis size");
		}

		logger.info("save  {}  {}", fundList.size(), Fund.getPath());
		Fund.save(fundList);
		
		logger.info("STOP");		
	}
	
}

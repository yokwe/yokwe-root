package yokwe.stock.jp.release;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.util.StringUtil;
import yokwe.util.http.HttpUtil;

public class Page {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(Release.class);

	// <div id="kaiji-date-1">2020年01月27日</div>
	private static final Pattern KAIJI_DATE_PAT = Pattern.compile("<div id=\"kaiji-date-1\">(.+?)</div>");

	// 2020年02月27日
	private static final Pattern DATE_PAT = Pattern.compile("(20[0-9][0-9])年([01][0-9])月([0-3][0-9])日");
	private static StringUtil.MatcherFunction<LocalDate> DATE_OP = (m -> LocalDate.parse(String.format("%s-%s-%s", m.group(1), m.group(2), m.group(3))));
	
	// <div id="kaiji-text-1">に開示された情報はありません。</div>
	private static final Pattern KAIJI_TEXT_PAT = Pattern.compile("<div id=\"kaiji-text-1\">(.+?)</div>");
	private static final String KAIJI_TEXT_NO_DATA = "に開示された情報はありません。";
	
	// <div class="pager-R" onClick="pager('I_list_002_20200127.html')"><span>次へ</span><img src="./gif/icn_next.png" alt="次へ"></img></div>
	// <div class="pager-R" onClick="pager('')"><span>次へ</span><img src="./gif/icn_next.png" alt="次へ"></img></div>
	private static final Pattern NEXT_PAT = Pattern.compile("<div class=\"pager-R\" onClick=\"pager\\('(.*?)'\\)\">");
	
	// <div class="kaijiSum">101～173件&nbsp;/&nbsp;全173件</div>
	// <div class="kaijiSum">1～100件&nbsp;/&nbsp;全173件</div>
	private static final Pattern KAIJI_SUM_PAT = Pattern.compile("<div class=\"kaijiSum\">.+?全(.+?)件</div>");

	private static final Pattern RELEASE_PAT = Pattern.compile(
			"<tr.*?>\\s+" +
			"<td class=\"(?:even|odd)new-L kjTime\".+?>(.+?)</td>\\s+" +
			"<td class=\"(?:even|odd)new-M kjCode\".+?>(.+?)</td>\\s+" +
			"<td class=\"(?:even|odd)new-M kjName\".+?>(.+?)\\s*</td>\\s+" +
			"<td class=\"(?:even|odd)new-M kjTitle\".+?><a href=\"(.+?)\" .+?>(.+?)</a>(?:<BR>.+?)?</td>\\s+" +
			"<td class=\"(?:even|odd)new-M kjXbrl\".+?>(?:<div class=\"xbrl-mask\">.+href=\"(.+?)\".+?)? </td>\\s+" +
			"<td class=\"(?:even|odd)new-M kjPlace\".+?>(.+?)\\s*</td>\\s+" +
			"<td class=\"(?:even|odd)new-R kjHistroy\".+?>(.*?)(?:[　]+?)?</td>\\s+" +
			"</tr>"
			);
	private static StringUtil.MatcherFunction<Release> RELEASE_OP = (m -> new Release(m.group(1), m.group(2), m.group(3), m.group(4), m.group(5), m.group(6), m.group(7), m.group(8)));

	public LocalDate   date;
	public int         count;
	public List<Release> entryList;
	
	private Page(LocalDate date) {
		this.date      = date;
		this.count     = 0;
		this.entryList = new ArrayList<>();
	}
	@Override
	public String toString() {
		return String.format("{%s %4d}", date, entryList.size());
	}
	
	public static Page getInstance(LocalDate date) {
		Page ret = new Page(date);
		
		for(int page = 1;; page++) {
			String url = Release.getPageURL(date, page);
//			logger.info("url {}", url);
			
			HttpUtil.Result result = HttpUtil.getInstance().download(url);
			if (result == null) {
				// Can be HttpStatus.SC_NOT_FOUND 404 or HttpStatus.SC_BAD_REQUEST 400
				if (page == 1) return null;
				else break;
			}
			
			final String string = result.result;
			
			{
				String kaijiDate = StringUtil.getGroupOne(KAIJI_DATE_PAT, string);
//					logger.info("kaijiDate {}", kaijiDate);
				
				List<LocalDate> list = StringUtil.find(kaijiDate, DATE_PAT, DATE_OP).collect(Collectors.toList());
				// Sanity check
				if (!date.equals(list.get(0))) {
					logger.error("date mismatch");
					logger.error("  date {}", date);
					logger.error("  list {}", list.get(0));
					throw new UnexpectedException("date mismatch");
				}
			}
			
			{
				String kaijiText = StringUtil.getGroupOne(KAIJI_TEXT_PAT, string);
//					logger.info("kaijiText {}", kaijiText);
				if (kaijiText.equals(KAIJI_TEXT_NO_DATA)) break;
			}
			
			{
				String kaijiSum = StringUtil.getGroupOne(KAIJI_SUM_PAT, string);
				try {
					int count = Integer.parseInt(kaijiSum);
					if (ret.count == 0) {
						ret.count = count;
					} else {
						// Sanity check
						if (ret.count != count) {
							logger.error("count mismatch");
							logger.error("  count {}", count);
							logger.error("  ret   {}", ret.count);
							throw new UnexpectedException("count mismatch");
						}
					}
				} catch (NumberFormatException e) {
					String exceptionName = e.getClass().getSimpleName();
					logger.error("{} {}", exceptionName, e);
					throw new UnexpectedException(exceptionName, e);
				}
			}
			
			{
				List<Release> list = StringUtil.find(string, RELEASE_PAT, RELEASE_OP).collect(Collectors.toList());
				// Update dateTime field of Release with date
				list.stream().forEach(o -> o.dateTime = LocalDateTime.of(date, o.dateTime.toLocalTime()));
//				logger.info("list {}", list.size());
				ret.entryList.addAll(list);
			}
			
			{
				String next = StringUtil.getGroupOne(NEXT_PAT, string);
				if (next.isEmpty()) break;
			}
		}
		
		// Sanity check
		{
			if (ret.count != ret.entryList.size()) {
				logger.warn("count mismatch");
				logger.warn("  count {}", ret.count);
				logger.warn("  entry {}", ret.entryList.size());
			}
		}
		return ret;
	}
}
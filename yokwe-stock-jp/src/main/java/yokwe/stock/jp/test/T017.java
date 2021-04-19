package yokwe.security.japan.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import yokwe.util.FileUtil;
import yokwe.util.StringUtil;

public class T017 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T017.class);
	
	public static String[] getGroup(Pattern pat, String string) {
		Matcher m = pat.matcher(string);
		if (m.find()) {
			int count = m.groupCount();
			String[] ret = new String[count];
			for(int i = 0; i < count; i++) {
				ret[i] = m.group(i + 1);
			}
			return ret;
		} else {
			return new String[0];
		}
	}
	public static List<String[]> getGroupList(Pattern pat, String string) {
		List<String[]> list = new ArrayList<>();
		
		Matcher m = pat.matcher(string);
		while (m.find()) {
			int count = m.groupCount();
			String[] ret = new String[count];
			for(int i = 0; i < count; i++) {
				ret[i] = m.group(i + 1);
			}
			list.add(ret);
		}
		
		return list;
	}
	public static String getGroup1(Pattern pat, String string) {
		String[] result = getGroup(pat, string);
		if (result.length == 1) {
			return result[0];
		} else {
			return null;
		}
	}

	public static class Entry {
		public String time;
		public String code;
		public String name;
		public String pdf;
		public String title;
		public String xbrl;
		public String place;
		public String history;
		
		public Entry(String time, String code, String name, String pdf, String title, String xbrl, String place, String history) {
			this.time    = time;
			this.code    = code;
			this.name    = name;
			this.pdf     = pdf;
			this.title   = title;
			this.xbrl    = xbrl;
			this.place   = place;
			this.history = history;
		}
		@Override
		public String toString() {
			return String.format("{%s %s %s %s %s %s %s %s}", time, code, name, pdf, title, xbrl, place, history);
		}
	}
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		String string = FileUtil.read().file("tmp/I_list_001_20200127.html");
//		logger.info("string {}", string);
		{
			// <div id="kaiji-date-1">2020年01月27日</div>
			Pattern PAT_KANJI_DATE = Pattern.compile("<div id=\"kaiji-date-1\">(.+?)</div>");
			String result = getGroup1(PAT_KANJI_DATE, string);
			logger.info("  {}", result);
		}
		{
			// <div id="kaiji-text-1">2020年01月27日</div>
			Pattern PAT_KAIJI_DATE = Pattern.compile("<div id=\"kaiji-text-1\">(.+?)</div>");
			String[] result = getGroup(PAT_KAIJI_DATE, string);
			logger.info("  {}", Arrays.asList(result));
		}
		{
			// <td class="oddnew-M kjXbrl" noWrap align="center"> </td>
			// <td class="oddnew-M kjXbrl" noWrap align="center"><div class="xbrl-mask"><div class="xbrl-button"><a class="style002" href="091220450962.zi200127p">XBRL</a></div></div> </td>

			// <td class="oddnew-M kjTitle" align="left"><a href="140120200124450468.pdf" target="_blank">2020年３月期　第３四半期決算短信〔日本基準〕（連結）</a></td>
			// <td class="oddnew-M kjTitle" align="left"><a href="140120200124450041.pdf" target="_blank">過年度の有価証券報告書の訂正に関するお知らせ</a><BR><s>過年度の有価証券報告書等の訂正に関するお知らせ</s></a></td>

			Pattern PAT_ENTRY = Pattern.compile(
					"<tr.*?>\\s+" +
					"<td class=\"(?:even|odd)new-L kjTime\".+?>(.+?)</td>\\s+" +
					"<td class=\"(?:even|odd)new-M kjCode\".+?>(.+?)</td>\\s+" +
					"<td class=\"(?:even|odd)new-M kjName\".+?>(.+?)\\s*</td>\\s+" +
					"<td class=\"(?:even|odd)new-M kjTitle\".+?><a href=\"(.+?)\" target=\"_blank\">(.+?)</a>(?:<BR>.+?)?</td>\\s+" +
					"<td class=\"(?:even|odd)new-M kjXbrl\".+?>(?:<div class=\"xbrl-mask\">.+href=\"(.+?)\".+?)? </td>\\s+" +
					"<td class=\"(?:even|odd)new-M kjPlace\".+?>(.+?)\\s*</td>\\s+" +
					"<td class=\"(?:even|odd)new-R kjHistroy\".+?>(.*?)(?:[　]+?)?</td>\\s+" +
					"</tr>"
					);
			List<String[]> result = getGroupList(PAT_ENTRY, string);
			int size = result.size();
			for(int i = 0; i < size; i++) {
				String[] strings = result.get(i);
				logger.info("  {} / {}  {}", i, size, Arrays.asList(strings));
			}
		}
		{
			// <td class="oddnew-M kjXbrl" noWrap align="center"> </td>
			// <td class="oddnew-M kjXbrl" noWrap align="center"><div class="xbrl-mask"><div class="xbrl-button"><a class="style002" href="091220450962.zi200127p">XBRL</a></div></div> </td>

			// <td class="oddnew-M kjTitle" align="left"><a href="140120200124450468.pdf" target="_blank">2020年３月期　第３四半期決算短信〔日本基準〕（連結）</a></td>
			// <td class="oddnew-M kjTitle" align="left"><a href="140120200124450041.pdf" target="_blank">過年度の有価証券報告書の訂正に関するお知らせ</a><BR><s>過年度の有価証券報告書等の訂正に関するお知らせ</s></a></td>

			Pattern PAT_ENTRY = Pattern.compile(
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
			StringUtil.MatcherFunction<Entry> OP = (m -> new Entry(m.group(1), m.group(2), m.group(3), m.group(4), m.group(5), m.group(6), m.group(7), m.group(8)));

			List<Entry> result = StringUtil.find(string, PAT_ENTRY, OP).collect(Collectors.toList());
			int size = result.size();
			for(int i = 0; i < size; i++) {
				Entry entry = result.get(i);
				logger.info("  {} / {}  {}", i, size, entry);
			}
		}
		
		logger.info("STOP");
	}
}

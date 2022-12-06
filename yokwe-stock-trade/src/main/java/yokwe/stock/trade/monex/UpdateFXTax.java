package yokwe.stock.trade.monex;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yokwe.stock.trade.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.http.HttpUtil;

public final class UpdateFXTax {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final String SOURCE_URL       = "https://mst.monex.co.jp/mst/servlet/ITS/ucu/UsEvaluationRateGST";
	public static final String SOURCE_ENCODING  = "SHIFT_JIS";
	
	public static String getName(int year) {
		return String.format("monex-fx-tax-%d.csv", year);
	}
	public static String getPath(int year) {
		return Storage.Monex.getPath(getName(year));
	}

	public static final int    THIS_YEAR      = LocalDate.now().getYear();
	public static final String NAME_THIS_YEAR = getName(THIS_YEAR);
	
	public static final String PATH_MONEX_FX_TAX = Storage.Monex.getPath("monex-fx-tax.csv");

//    <tr>
//      <td class="al-c table-sub-th">2018/01/04</td>
//      <td class="al-r">113.75</td>
//      <td class="al-r">111.75</td>
//    </tr>


	private static final String  PATTERN_STRING = "<tr>\\s+<td class=\"al-c table-sub-th\">([0-9/]+)</td>\\s+<td class=\"al-r\">([0-9\\.]+)</td>\\s+<td class=\"al-r\">([0-9\\.]+)</td>\\s+</tr>";
	private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING, (Pattern.MULTILINE | Pattern.DOTALL));
	
	public static List<FXTax> load() {
		return CSVUtil.read(FXTax.class).file(PATH_MONEX_FX_TAX);
	}
	
	private static void updateThisYear() {
		logger.info("updateThisYear {}", THIS_YEAR);
		String path     = getPath(THIS_YEAR);
		
		String contents = HttpUtil.getInstance().download(SOURCE_URL).result;

		Matcher matcher = PATTERN.matcher(contents);
		
		List<FXTax> monexStockFXList = new ArrayList<>();
		
		for(;;) {
			if (!matcher.find()) break;
			
			String date = matcher.group(1);
			String tts = matcher.group(2);
			String ttb = matcher.group(3);
			
			FXTax monexStockFX = new FXTax(date.replaceAll("/", "-"), Double.valueOf(tts), Double.valueOf(ttb));
			monexStockFXList.add(monexStockFX);
			
			logger.info("{}", monexStockFX);
		}
//		logger.info("URL  = {}", SOURCE_URL);
//		logger.info("PATH = {}", path);
		
		CSVUtil.write(FXTax.class).file(path, monexStockFXList);
	}

	public static void main(String[] args) {
		logger.info("START");
		
		List<FXTax> monexStockFXList = new ArrayList<>();

		updateThisYear();
		for(int i = THIS_YEAR; 2000 < i; i--) {
			String path = getPath(i);
			File file = new File(path);
			if (!file.canRead()) break;
			
			List<FXTax> list = CSVUtil.read(FXTax.class).file(path);
			logger.info("read {} {}", path, list.size());
			monexStockFXList.addAll(list);
		}
		Collections.sort(monexStockFXList);
		
		logger.info("DATA = {}", monexStockFXList.size());
		logger.info("PATH = {}", PATH_MONEX_FX_TAX);
		CSVUtil.write(FXTax.class).file(PATH_MONEX_FX_TAX, monexStockFXList);
		
		logger.info("STOP");		
	}
}

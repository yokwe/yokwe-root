package yokwe.finance.trade.rakuten;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.trade.AccountHistory;
import yokwe.finance.trade.AccountHistory.Currency;
import yokwe.util.FileUtil;
import yokwe.util.HashCode;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class UpdateAccountHistory {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final LocalDate TODAY         = LocalDate.now();
	public static final File      DIR_DOWNLOAD  = StorageRakuten.storage.getFile("download");
	public static final File      USER_DOWNLOAD = new File(System.getProperty("user.home"), "Downloads");
	public static final Charset   SHIFT_JIS     = Charset.forName("SHIFT_JIS");
	
	public static final FilenameFilter FILTER_ADJUSTHISTORY_JP    = (d, n) -> n.startsWith("adjusthistory(JP)_") && n.endsWith(".csv");
	public static final FilenameFilter FILTER_ADJUSTHISTORY_US    = (d, n) -> n.startsWith("adjusthistory(US)_") && n.endsWith(".csv");
	public static final FilenameFilter FILTER_TRADEHISTORY_JP     = (d, n) -> n.startsWith("tradehistory(JP)_") && n.endsWith(".csv");
	public static final FilenameFilter FILTER_TRADEHISTORY_US     = (d, n) -> n.startsWith("tradehistory(US)_") && n.endsWith(".csv");
	public static final FilenameFilter FILTER_TRADEHISTORY_FB     = (d, n) -> n.startsWith("tradehistory(FB)_") && n.endsWith(".csv");
	public static final FilenameFilter FILTER_TRADEHISTORY_INVST  = (d, n) -> n.startsWith("tradehistory(INVST)_") && n.endsWith(".csv");

	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
	
	private static void update() {
		copyFiles();
		
		UpdateAccountHistoryJP.update();
		UpdateAccountHistoryUS.update();
	}
	
	public static void copyFiles() {
		copyNewFiles(FILTER_ADJUSTHISTORY_JP);		
		copyNewFiles(FILTER_ADJUSTHISTORY_US);		
		copyNewFiles(FILTER_TRADEHISTORY_JP);
		copyNewFiles(FILTER_TRADEHISTORY_US);
		copyNewFiles(FILTER_TRADEHISTORY_FB);		
		copyNewFiles(FILTER_TRADEHISTORY_INVST);		
	}
	

	
	private static final Pattern PAT_DATE = Pattern.compile("(20[0-9][0-9])/([1-9]|1[012])/([1-9]|[12][0-9]|3[01])");
	public static LocalDate toLocalDate(String string) {
		var matcher = PAT_DATE.matcher(string);
		if (matcher.matches()) {
			var y = matcher.group(1);
			var m = matcher.group(2);
			var d = matcher.group(3);
			
			return LocalDate.of(Integer.valueOf(y), Integer.valueOf(m), Integer.valueOf(d));
		} else {
			logger.error("Unexpeced string");
			logger.error("  string  {}!", string);
			throw new UnexpectedException("Unexpeced string");
		}
	}
	
	private static List<AccountHistory> merge(List<AccountHistory> oldList, List<AccountHistory> newList) {
		var list = new ArrayList<AccountHistory>(oldList);
		
		for(var e: newList) {
			if (!list.contains(e)) list.add(e);
		}
		
		return list;
	}
	public static List<AccountHistory> mergeMixed(List<AccountHistory> oldList, List<AccountHistory> newList) {
		var oldListJPY = oldList.stream().filter(o -> o.currency == Currency.JPY).collect(Collectors.toList());
		var newListJPY = newList.stream().filter(o -> o.currency == Currency.JPY).collect(Collectors.toList());
		var listJPY    = merge(oldListJPY, newListJPY);
		
		var oldListUSD = oldList.stream().filter(o -> o.currency == Currency.USD).collect(Collectors.toList());
		var newListUSD = newList.stream().filter(o -> o.currency == Currency.USD).collect(Collectors.toList());
		var listUSD    = merge(oldListUSD, newListUSD);
		
		var ret = new ArrayList<AccountHistory>(listJPY.size() + listUSD.size());
		ret.addAll(listJPY);
		ret.addAll(listUSD);
		Collections.sort(ret);
		
		return ret;
	}
	
	public static void copyNewFiles(FilenameFilter filenameFilter) {
		var newFileList = Arrays.asList(USER_DOWNLOAD.listFiles(filenameFilter));
		var oldFileList = Arrays.asList(DIR_DOWNLOAD.listFiles(filenameFilter));
		var oldHashSet  = oldFileList.stream().map(o -> StringUtil.toHexString(HashCode.getHashCode(o))).collect(Collectors.toSet());
		
		for(var newFile: newFileList) {
			var newHash = StringUtil.toHexString(HashCode.getHashCode(newFile));
			if (oldHashSet.contains(newHash)) {
				// exist
			} else {
				// not exist
				var file = new File(DIR_DOWNLOAD, newFile.getName());
				var string  = FileUtil.read().withCharset(SHIFT_JIS).file(newFile);
				FileUtil.write().file(file, string);
				logger.info("copy    {}", file.getName());
			}
			// delete new file
			FileUtil.delete(newFile);
			logger.info("delete  {}", newFile.getName());
		}
	}
}

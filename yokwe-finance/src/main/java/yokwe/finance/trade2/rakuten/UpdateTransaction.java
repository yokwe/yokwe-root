package yokwe.finance.trade2.rakuten;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.trade2.Transaction;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.HashCode;
import yokwe.util.StringUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;

public class UpdateTransaction {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	static final File    DIR_DOWNLOAD  = StorageRakuten.storage.getFile("download");
	static final File    USER_DOWNLOAD = new File(System.getProperty("user.home"), "Downloads");
	static final Charset SHIFT_JIS     = Charset.forName("SHIFT_JIS");

	private static File getDownloadFile(String name) {
		var file = new File(DIR_DOWNLOAD, name);
		if (!file.exists()) {
			logger.error("Unexpeced file", file.getPath());
			logger.error("  file  {}", file.getPath());
			throw new UnexpectedException("Unexpeced file");
		}
		return file;
	}
	private static File getDownloadFile(String prefix, String type, String date) {
		return getDownloadFile(prefix + "(" + type + ")_" + date + ".csv");
	}
	private static File getAdjustHistory(String type, String date) {
		return getDownloadFile("adjusthistory", type, date);
	}
	private static File getTradeHistory(String type, String date) {
		return getDownloadFile("tradehistory", type, date);
	}

	static File getAdjustHistoryJP(String date) {
		return getAdjustHistory("JP", date);
	}
	static File getAdjustHistoryUS(String date) {
		return getAdjustHistory("US", date);
	}
	static File getTradeHistoryFB(String date) {
		return getTradeHistory("FB", date);
	}
	static File getTradeHistoryINVST(String date) {
		return getTradeHistory("INVST", date);
	}
	static File getTradeHistoryJP(String date) {
		return getTradeHistory("JP", date);
	}
	static File getTradeHistoryUS(String string) {
		return getTradeHistory("US", string);
	}
	
	static final FilenameFilter FILTER_ADJUSTHISTORY_JP    = (d, n) -> n.startsWith("adjusthistory(JP)_") && n.endsWith(".csv");
	static final FilenameFilter FILTER_ADJUSTHISTORY_US    = (d, n) -> n.startsWith("adjusthistory(US)_") && n.endsWith(".csv");
	static final FilenameFilter FILTER_TRADEHISTORY_FB     = (d, n) -> n.startsWith("tradehistory(FB)_") && n.endsWith(".csv");
	static final FilenameFilter FILTER_TRADEHISTORY_INVST  = (d, n) -> n.startsWith("tradehistory(INVST)_") && n.endsWith(".csv");
	static final FilenameFilter FILTER_TRADEHISTORY_JP     = (d, n) -> n.startsWith("tradehistory(JP)_") && n.endsWith(".csv");
	static final FilenameFilter FILTER_TRADEHISTORY_US     = (d, n) -> n.startsWith("tradehistory(US)_") && n.endsWith(".csv");
	
	private static void copyFiles(FilenameFilter filenameFilter) {
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
//			logger.info("delete  {}", newFile.getName());
		}
	}
	static void copyCSVFiles() {
		copyFiles(FILTER_ADJUSTHISTORY_JP);		
		copyFiles(FILTER_ADJUSTHISTORY_US);		
		copyFiles(FILTER_TRADEHISTORY_JP);
		copyFiles(FILTER_TRADEHISTORY_US);
		copyFiles(FILTER_TRADEHISTORY_FB);		
		copyFiles(FILTER_TRADEHISTORY_INVST);		
	}

	
	static String getFileDate() {
		var files = DIR_DOWNLOAD.listFiles(FILTER_ADJUSTHISTORY_JP);
		if (files.length == 0) return null;
		
		Arrays.sort(files);
		for(var e: files) {
			logger.info("file  {}", e.getName());
		}
		var name = files[files.length - 1].getName();
		var date = name.substring(name.indexOf('_') + 1, name.indexOf('.'));
		logger.info("date  {}", date);
		return date;
	}
	
	static class CSVFiles {
		final File adjustHistoryJP;
		final File adjustHistoryUS;
		final File tradeHistoryFB;
		final File tradeHistoryINVST;
		final File tradeHistoryJP;
		final File tradeHistoryUS;
		
		CSVFiles(String date) {
			adjustHistoryJP   = getAdjustHistoryJP(date);
			adjustHistoryUS   = getAdjustHistoryUS(date);
			tradeHistoryFB    = getTradeHistoryFB(date);
			tradeHistoryINVST = getTradeHistoryINVST(date);
			tradeHistoryJP    = getTradeHistoryJP(date);
			tradeHistoryUS    = getTradeHistoryUS(date);
		}
		CSVFiles() {
			this(getFileDate());
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	
	private static final Pattern PAT_DATE = Pattern.compile("(20[0-9][0-9])/([1-9]|1[012])/([1-9]|[12][0-9]|3[01])");
	static LocalDate toLocalDate(String string) {
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
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
	
	static void update() {
		copyCSVFiles();
		
		var list = new ArrayList<Transaction>();
		
		var csvFiles = new CSVFiles();
		list.addAll(TradeHistoryJP.toTransaction(CSVUtil.read(TradeHistoryJP.class).file(csvFiles.tradeHistoryJP)));
		list.addAll(TradeHistoryUS.toTransaction(CSVUtil.read(TradeHistoryUS.class).file(csvFiles.tradeHistoryUS)));
		list.addAll(TradeHistoryFB.toTransaction(CSVUtil.read(TradeHistoryFB.class).file(csvFiles.tradeHistoryFB)));
		list.addAll(TradeHistoryINVST.toTransaction(CSVUtil.read(TradeHistoryINVST.class).file(csvFiles.tradeHistoryINVST)));
		//
		list.addAll(AdjustHistoryJP.toTransaction(CSVUtil.read(AdjustHistoryJP.class).file(csvFiles.adjustHistoryJP)));
		list.addAll(AdjustHistoryUS.toTransaction(CSVUtil.read(AdjustHistoryUS.class).file(csvFiles.adjustHistoryUS)));
		
		Collections.sort(list);
		logger.info("save {}  {}", list.size(), StorageRakuten.TransactionList.getPath());
		StorageRakuten.TransactionList.save(list);
	}
}

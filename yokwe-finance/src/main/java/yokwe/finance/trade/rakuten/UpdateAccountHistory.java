package yokwe.finance.trade.rakuten;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
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

	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
	
	private static void update() {
		UpdateAccountHistoryJP.copyFiles();
		UpdateAccountHistoryUS.copyFiles();
		
		UpdateAccountHistoryJP.update();
		UpdateAccountHistoryUS.update();
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
	
	private static boolean equals(List<AccountHistory> listA, List<AccountHistory> listB) {
		if (listA.size() == listB.size()) {
			int size = listA.size();
			for(int i = 0; i < size; i++) {
				var a = listA.get(i);
				var b = listB.get(i);
				if (a.equals(b)) continue;
				
				logger.info("XX  a  {}", a);
				logger.info("XX  b  {}", b);
				
				logger.info("   {}  {}  {}  {}", a.settlementDate.equals(b.settlementDate), a.settlementDate, b.settlementDate, "settlementDate");
				logger.info("   {}  {}  {}  {}", a.tradeDate.equals(b.tradeDate), a.tradeDate, b.tradeDate, "tradeDate");
				logger.info("   {}  {}  {}  {}", a.currency.equals(b.currency), a.currency, b.currency, "currency");
				logger.info("   {}  {}  {}  {}", a.asset.equals(b.asset), a.asset, b.asset, "asset");
				logger.info("   {}  {}  {}  {}", a.transaction.equals(b.transaction), a.transaction, b.transaction, "transaction");
				logger.info("   {}  {}  {}  {}", a.units.equals(b.units), a.units, b.units, "units");
				logger.info("   {}  {}  {}  {}", a.unitPrice.equals(b.unitPrice), a.unitPrice, b.unitPrice, "unitPrice");
				logger.info("   {}  {}  {}  {}", a.amount.equals(b.amount), a.amount, b.amount, "amount");
				logger.info("   {}  {}  {}  {}", a.code.equals(b.code), a.code, b.code, "code");
				logger.info("   {}  {}  {}  {}", a.comment.equals(b.comment), a.comment, b.comment, "comment");
				
				return false;
			}
			return true;
		} else {
			return false;
		}
	}
	
	private static List<AccountHistory> merge(List<AccountHistory> oldList, List<AccountHistory> newList) {
		var list = new ArrayList<AccountHistory>();
		
		var oldMap = oldList.stream().collect(Collectors.groupingBy(o -> o.settlementDate, TreeMap::new, Collectors.toCollection(ArrayList::new)));
		var newMap = newList.stream().collect(Collectors.groupingBy(o -> o.settlementDate, TreeMap::new, Collectors.toCollection(ArrayList::new)));
		
		// copy oldMap not appeared in newMap to list
		for(var entry: oldMap.entrySet()) {
			var date  = entry.getKey();
			var oList = entry.getValue();
			
			if (!newMap.containsKey(date)) {
				list.addAll(oList);
			}
		}
		
		for(var entry: newMap.entrySet()) {
			var date  = entry.getKey();
			var nList = entry.getValue();
			Collections.sort(nList);
			if (oldMap.containsKey(date)) {
				var oList = oldMap.get(date);
				Collections.sort(oList);
				if (equals(nList,  oList)) {
					list.addAll(oList);
				} else {
					logger.info("replace {}  {}", oList.getFirst().currency, date);
					list.addAll(nList);
				}
			} else {
				logger.info("add     {}  {}", nList.getFirst().currency, date);
				list.addAll(nList);
			}
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

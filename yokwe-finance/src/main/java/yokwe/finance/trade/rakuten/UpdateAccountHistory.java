package yokwe.finance.trade.rakuten;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.trade.AccountHistory;
import yokwe.finance.trade.AccountHistory.Currency;
import yokwe.util.UnexpectedException;

public class UpdateAccountHistory {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final LocalDate TODAY        = LocalDate.now();
	public static final File      DIR_DOWNLOAD = StorageRakuten.storage.getFile("download");

	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
	
	private static void update() {
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
		
	public static List<AccountHistory> merge(List<AccountHistory> oldList, List<AccountHistory> newList) {
		var oldListJPY = oldList.stream().filter(o -> o.currency == Currency.JPY).collect(Collectors.toList());
		var oldListUSD = oldList.stream().filter(o -> o.currency == Currency.USD).collect(Collectors.toList());
		var newListJPY = newList.stream().filter(o -> o.currency == Currency.JPY).collect(Collectors.toList());
		var newListUSD = newList.stream().filter(o -> o.currency == Currency.USD).collect(Collectors.toList());
		
		// JPY
		{
			var settlementDateSet = oldListJPY.stream().map(o -> o.settlementDate).collect(Collectors.toSet());
			for(var e: newListJPY) {
				if (settlementDateSet.contains(e.settlementDate)) continue;
				oldListJPY.add(e);
			}
		}
		// USD
		{
			var settlementDateSet = oldListUSD.stream().map(o -> o.settlementDate).collect(Collectors.toSet());
			for(var e: newListUSD) {
				if (settlementDateSet.contains(e.settlementDate)) continue;
				oldListUSD.add(e);
			}
		}
		
		var ret = new ArrayList<AccountHistory>(oldListJPY.size() + oldListUSD.size());
		ret.addAll(oldListJPY);
		ret.addAll(oldListUSD);
		Collections.sort(ret);
		
		return ret;
	}
}

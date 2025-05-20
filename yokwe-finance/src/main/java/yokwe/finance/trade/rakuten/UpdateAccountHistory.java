package yokwe.finance.trade.rakuten;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.stock.StorageStock;
import yokwe.finance.trade.AccountHistory;
import yokwe.finance.trade.AccountHistory.Currency;
import yokwe.finance.type.StockCodeJP;
import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;

public class UpdateAccountHistory {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final LocalDate TODAY = LocalDate.now();
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
	
	private static void update() {
		var dir = StorageRakuten.storage.getFile("download");

		// build stockNameMapJP
		{
			var files = dir.listFiles(o -> o.getName().startsWith("tradehistory(JP)_") && o.getName().endsWith(".csv"));
			for(var file : files) {
				logger.info("tradeHistoryJP  {}", file.getName());
				var list = CSVUtil.read(TradeHistoryJP.class).file(file);
				logger.info("read  {}  {}", list.size(), file.getName());
				buildStockNameMapJP(list);
				logger.info("stockNameMapJP  {}", stockNameMapJP.size());
			}
		}
		
		var oldList = StorageRakuten.AccountHistory.getList();
		// update oldList
		{
			logger.info("dir  {}", dir.getPath());
			
			var files = dir.listFiles(o -> o.getName().endsWith(".csv"));
			for(var file: files) {
				var name = file.getName();
				if (!name.startsWith("adjusthistory")) continue;
				
				logger.info("file  {}", file.getName());
				
				if (name.startsWith("adjusthistory(JP)")) {
					var array = CSVUtil.read(AdjustHistoryJP.class).file(file).toArray(AdjustHistoryJP[]::new);
					logger.info("read  {}  {}", array.length, name);
					var newList = toAccountHistory(array);
					oldList = merge(oldList, newList);
					continue;
				}
				if (name.startsWith("adjusthistory(US)")) {
					var array = CSVUtil.read(AdjustHistoryUS.class).file(file).toArray(AdjustHistoryUS[]::new);
					logger.info("read  {}  {}", array.length, name);
					var newList = toAccountHistory(array);
					oldList = merge(oldList, newList);
					continue;
				}
				logger.warn("ignore file  {}", file.getName());
			}
		}
				
		logger.info("save  {}  {}", oldList.size(), StorageRakuten.AccountHistory.getPath());
		StorageRakuten.AccountHistory.save(oldList);
	}
	
	
	private static final Pattern PAT_DATE = Pattern.compile("(20[0-9][0-9])/([1-9]|1[012])/([1-9]|[12][0-9]|3[01])");
	private static LocalDate toLocalDate(String string) {
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
	
	private static Map<String, String> stockCodeMapJP = StorageStock.StockInfoJP.getList().stream().collect(Collectors.toMap(o -> o.stockCode, o -> o.name));
	//                 code    name
	private static String toStockNameJP(String code) {
		var ret = stockCodeMapJP.get(code);
		if (ret != null) {
			return ret;
		} else {
			logger.error("Unexpected code");
			logger.error("  code  {}!", code);
			throw new UnexpectedException("Unexpected code");
		}
	}
	private static Map<String, String> stockNameMapJP = new TreeMap<>();
	private static void buildStockNameMapJP(List<TradeHistoryJP> list) {
		for(var e: list) {
			var code = e.code;
			var name = e.name;
			
			stockNameMapJP.put(name, StockCodeJP.toStockCode5(code));
		}
	}
	private static String toStockCodeJP(String stockName) {
		// find stock code from stockNameMap
		{
			var code = stockNameMapJP.get(stockName);
			if (code != null) return code;
		}
		
		// find stock code from stockNameMap
		for(var entry: stockNameMapJP.entrySet()) {
			var name = entry.getKey();
			var code = entry.getValue();
			
			if (stockName.replace("　", "").equals(name)) {
				// save for later use
				stockNameMapJP.put(stockName, code);
				return code;
			}
		}
		
		// find stock code from stockCodeMap
		for(var entry: stockCodeMapJP.entrySet()) {
			var code = entry.getKey();
			var name = entry.getValue();
			
			if (name.startsWith(stockName)) {
				// save for later use
				stockNameMapJP.put(stockName, code);
				return code;
			}
		}
		
		logger.error("Unexpected name");
		logger.error("  name  {}!", stockName);
		throw new UnexpectedException("Unexpected name");
	}
	
	private static Map<AdjustHistoryJP.TradeType, Function<AdjustHistoryJP, AccountHistory>> convertMapJP = new TreeMap<>();
	static {
		convertMapJP.put(AdjustHistoryJP.TradeType.DIVIDEND_STOCK,    new ConvertAdjustHistoryJP.DIVIDEND_STOCK());
		convertMapJP.put(AdjustHistoryJP.TradeType.DEPOSIT_CAMPAIGN,  new ConvertAdjustHistoryJP.DEPOSIT_CAMPAIGN());
		convertMapJP.put(AdjustHistoryJP.TradeType.DEOSIT_TRANSFER,   new ConvertAdjustHistoryJP.DEOSIT_TRANSFER());
		convertMapJP.put(AdjustHistoryJP.TradeType.WITHDRAW_TRANSFER, new ConvertAdjustHistoryJP.WITHDRAW_TRANSFER());
		convertMapJP.put(AdjustHistoryJP.TradeType.SELL_STOCK,        new ConvertAdjustHistoryJP.SELL_STOCK());
		convertMapJP.put(AdjustHistoryJP.TradeType.SELL_FUND,         new ConvertAdjustHistoryJP.SELL_FUND());
		convertMapJP.put(AdjustHistoryJP.TradeType.BUY_FUND,          new ConvertAdjustHistoryJP.BUY_FUND());
		convertMapJP.put(AdjustHistoryJP.TradeType.BUY_STOCK,         new ConvertAdjustHistoryJP.BUY_STOCK());
		convertMapJP.put(AdjustHistoryJP.TradeType.TAX_INCOME,        new ConvertAdjustHistoryJP.TAX_INCOME());
		convertMapJP.put(AdjustHistoryJP.TradeType.TAX_LOCAL,         new ConvertAdjustHistoryJP.TAX_LOCAL());
		convertMapJP.put(AdjustHistoryJP.TradeType.TAX_REFUND_INCOME, new ConvertAdjustHistoryJP.TAX_REFUND_INCOME());
		convertMapJP.put(AdjustHistoryJP.TradeType.TAX_REFUND_LOCAL,  new ConvertAdjustHistoryJP.TAX_REFUND_LOCAL());
		convertMapJP.put(AdjustHistoryJP.TradeType.DEPOSIT_REALTIME,  new ConvertAdjustHistoryJP.DEPOSIT_REALTIME());
		convertMapJP.put(AdjustHistoryJP.TradeType.DEPOSIT_POINT,     new ConvertAdjustHistoryJP.DEPOSIT_POINT());
		convertMapJP.put(AdjustHistoryJP.TradeType.WITHDRAW,          new ConvertAdjustHistoryJP.WITHDRAW());
	}
	private static class ConvertAdjustHistoryJP {
		private static class DIVIDEND_STOCK implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.CASH;
				ret.currency       = AccountHistory.Currency.JPY;
				ret.transaction    = AccountHistory.Transaction.DIVIDEND;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = toStockCodeJP(e.name);
				ret.comment        = toStockNameJP(ret.code);
				
				return ret;
			}
		}
		private static class DEPOSIT_CAMPAIGN implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.CASH;
				ret.currency       = AccountHistory.Currency.JPY;
				ret.transaction    = AccountHistory.Transaction.DEPOSIT;
				ret.units          = BigDecimal.ZERO;
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.tradeType.toString();
				
				return ret;
			}
		}
		private static class DEOSIT_TRANSFER implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.CASH;
				ret.currency       = AccountHistory.Currency.JPY;
				ret.transaction    = AccountHistory.Transaction.DEPOSIT;
				ret.units          = BigDecimal.ZERO;
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.tradeType.toString();
				
				return ret;
			}
		}
		private static class WITHDRAW_TRANSFER implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.CASH;
				ret.currency       = AccountHistory.Currency.JPY;
				ret.transaction    = AccountHistory.Transaction.WITHDRAW;
				ret.units          = BigDecimal.ZERO;
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountWithdraw.replace(",", "")).negate();
				ret.code           = "";
				ret.comment        = e.tradeType.toString();
				
				return ret;
			}
		}
		private static class SELL_STOCK implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.STOCK;
				ret.currency       = AccountHistory.Currency.JPY;
				ret.transaction    = AccountHistory.Transaction.SELL;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = new BigDecimal(e.unitPrice.replace(",", ""));
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = toStockCodeJP(e.name);
				ret.comment        = toStockNameJP(ret.code);
				
				return ret;
			}
		}
		private static class SELL_FUND implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.FUND;
				ret.currency       = AccountHistory.Currency.JPY;
				ret.transaction    = AccountHistory.Transaction.SELL;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = new BigDecimal(e.unitPrice.replace(",", ""));
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", "")).negate();
				ret.code           = "";
				ret.comment        = e.name;
				
				return ret;
			}
		}
		private static class BUY_FUND implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.FUND;
				ret.currency       = AccountHistory.Currency.JPY;
				ret.transaction    = AccountHistory.Transaction.BUY;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = new BigDecimal(e.unitPrice.replace(",", ""));
				ret.amount         = new BigDecimal(e.amountWithdraw.replace(",", "")).negate();
				ret.code           = "";
				ret.comment        = e.name;
				
				return ret;
			}
		}
		private static class BUY_STOCK implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.STOCK;
				ret.currency       = AccountHistory.Currency.JPY;
				ret.transaction    = AccountHistory.Transaction.BUY;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = new BigDecimal(e.unitPrice.replace(",", ""));
				ret.amount         = new BigDecimal(e.amountWithdraw.replace(",", ""));
				ret.code           = toStockCodeJP(e.name);
				ret.comment        = toStockNameJP(ret.code);
				
				return ret;
			}
		}
		private static class TAX_INCOME implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.CASH;
				ret.currency       = AccountHistory.Currency.JPY;
				ret.transaction    = AccountHistory.Transaction.TAX;
				ret.units          = BigDecimal.ZERO;
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountWithdraw.replace(",", "")).negate();
				ret.code           = "";
				ret.comment        = e.tradeType.toString();
				
				return ret;
			}
		}
		private static class TAX_LOCAL implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.CASH;
				ret.currency       = AccountHistory.Currency.JPY;
				ret.transaction    = AccountHistory.Transaction.TAX;
				ret.units          = BigDecimal.ZERO;
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountWithdraw.replace(",", "")).negate();
				ret.code           = "";
				ret.comment        = e.tradeType.toString();
				
				return ret;
			}
		}
		private static class TAX_REFUND_INCOME implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.CASH;
				ret.currency       = AccountHistory.Currency.JPY;
				ret.transaction    = AccountHistory.Transaction.TAX;
				ret.units          = BigDecimal.ZERO;
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.tradeType.toString();
				
				return ret;
			}
		}
		private static class TAX_REFUND_LOCAL implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.CASH;
				ret.currency       = AccountHistory.Currency.JPY;
				ret.transaction    = AccountHistory.Transaction.TAX;
				ret.units          = BigDecimal.ZERO;
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.tradeType.toString();
				
				return ret;
			}
		}
		private static class DEPOSIT_REALTIME implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.CASH;
				ret.currency       = AccountHistory.Currency.JPY;
				ret.transaction    = AccountHistory.Transaction.DEPOSIT;
				ret.units          = BigDecimal.ZERO;
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.tradeType.toString();
				
				return ret;
			}
		}
		private static class DEPOSIT_POINT implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.CASH;
				ret.currency       = AccountHistory.Currency.JPY;
				ret.transaction    = AccountHistory.Transaction.DEPOSIT;
				ret.units          = BigDecimal.ZERO;
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.tradeType.toString();
				
				return ret;
			}
		}
		private static class WITHDRAW implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.CASH;
				ret.currency       = AccountHistory.Currency.JPY;
				ret.transaction    = AccountHistory.Transaction.WITHDRAW;
				ret.units          = BigDecimal.ZERO;
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountWithdraw.replace(",", "")).negate();
				ret.code           = "";
				ret.comment        = e.tradeType.toString();
				
				return ret;
			}
		}
	}
	
	static int countSkip = 0;
	private static List<AccountHistory> toAccountHistory(AdjustHistoryJP[] array) {
		var ret = new ArrayList<AccountHistory>();
		int countA = 0;
		int countB = 0;
		int countC = 0;
		for(var e: array) {
			countA++;
			var accountHistory = convertMapJP.get(e.tradeType).apply(e);
			if (accountHistory.settlementDate.isBefore(TODAY)) {
				countB++;
				ret.add(accountHistory);
			} else {
				countC++;
				logger.info("too early  {}", accountHistory.settlementDate);
			}
		}
		
		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		
		return ret;
	}
	
	private static List<AccountHistory> merge(List<AccountHistory> oldList, List<AccountHistory> newList) {
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
	
	
	private static List<AccountHistory> toAccountHistory(AdjustHistoryUS[] array) {
		var ret = new ArrayList<AccountHistory>();
		// FIXME
		return ret;
	}


}

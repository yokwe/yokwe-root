package yokwe.finance.trade.rakuten;

import static yokwe.finance.trade.rakuten.UpdateAccountHistory.DIR_DOWNLOAD;
import static yokwe.finance.trade.rakuten.UpdateAccountHistory.FILTER_ADJUSTHISTORY_US;
import static yokwe.finance.trade.rakuten.UpdateAccountHistory.FILTER_TRADEHISTORY_US;
import static yokwe.finance.trade.rakuten.UpdateAccountHistory.TODAY;
import static yokwe.finance.trade.rakuten.UpdateAccountHistory.mergeMixed;
import static yokwe.finance.trade.rakuten.UpdateAccountHistory.toLocalDate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.stock.StorageStock;
import yokwe.finance.trade.AccountHistory;
import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;

public class UpdateAccountHistoryUS {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void update() {
		// build stockNameMap
		{
			var files = DIR_DOWNLOAD.listFiles(FILTER_TRADEHISTORY_US);
			Arrays.sort(files);
			for(var file : files) {
				var list = CSVUtil.read(TradeHistoryUS.class).file(file);
				logger.info("read  {}  {}", list.size(), file.getName());
				buildStockNameMap(list);
				logger.info("stockNameMap  {}", stockNameMap.size());
			}
		}

		var oldList = StorageRakuten.AccountHistory.getList();
		logger.info("read  {}  {}", oldList.size(), StorageRakuten.AccountHistory.getFile().getName());
		
		// update oldList
		{
			var files = DIR_DOWNLOAD.listFiles(FILTER_ADJUSTHISTORY_US);
			Arrays.sort(files);
			for(var file: files) {
				var name = file.getName();
				logger.info("file  {}", file.getName());
				
				var array = CSVUtil.read(AdjustHistoryUS.class).file(file).toArray(AdjustHistoryUS[]::new);
				logger.info("read  {}  {}", array.length, name);
				var newList = toAccountHistory(array);
				oldList = mergeMixed(oldList, newList);
			}
		}
				
		logger.info("save  {}  {}", oldList.size(), StorageRakuten.AccountHistory.getPath());
		StorageRakuten.AccountHistory.save(oldList);
	}
	
	private static Map<String, String> stockCodeMap = StorageStock.StockInfoUSAll.getList().stream().collect(Collectors.toMap(o -> o.stockCode, o -> o.name));
	//                 code    name
	private static String toStockName(String code) {
		var ret = stockCodeMap.get(code);
		if (ret != null) {
			return ret;
		} else {
			logger.error("Unexpected code");
			logger.error("  code  {}!", code);
			throw new UnexpectedException("Unexpected code");
		}
	}
	private static Map<String, String> stockNameMap = new TreeMap<>();
	private static void buildStockNameMap(List<TradeHistoryUS> list) {
		for(var e: list) {
			var code = e.code;
			var name = e.name;
			
			stockNameMap.put(name, code);
		}
	}
	private static String toStockCode(String stockName) {
		// find stock code from stockNameMap
		{
			var code = stockNameMap.get(stockName);
			if (code != null) return code;
		}
				
		// find stock code from stockCodeMap
		for(var entry: stockCodeMap.entrySet()) {
			var code = entry.getKey();
			var name = entry.getValue();
			
			if (name.equals(stockName)) {
				// save for later use
				stockNameMap.put(stockName, code);
				return code;
			}
		}
		
		logger.error("Unexpected name");
		logger.error("  name  {}!", stockName);
		throw new UnexpectedException("Unexpected name");
	}
	
	private static Map<AdjustHistoryUS.TradeType, Function<AdjustHistoryUS, AccountHistory>> functionMap = Map.ofEntries(
		Map.entry(AdjustHistoryUS.TradeType.DIVIDEND_BOND,     new TradeTypeFunction.DIVIDEND_BOND()),
		Map.entry(AdjustHistoryUS.TradeType.SELL_BOND,         new TradeTypeFunction.SELL_BOND()),
		Map.entry(AdjustHistoryUS.TradeType.BUY_BOND,          new TradeTypeFunction.BUY_BOND()),
		Map.entry(AdjustHistoryUS.TradeType.DIVIDEND_STOCK,    new TradeTypeFunction.DIVIDEND_STOCK()),
		Map.entry(AdjustHistoryUS.TradeType.SELL_STOCK,        new TradeTypeFunction.SELL_STOCK()),
		Map.entry(AdjustHistoryUS.TradeType.BUY_STOCK,         new TradeTypeFunction.BUY_STOCK()),
		Map.entry(AdjustHistoryUS.TradeType.SELL_MMF,          new TradeTypeFunction.SELL_MMF()),
		Map.entry(AdjustHistoryUS.TradeType.BUY_MMF,           new TradeTypeFunction.BUY_MMF()),
		Map.entry(AdjustHistoryUS.TradeType.DEOSIT_TRANSFER,   new TradeTypeFunction.DEOSIT_TRANSFER()),
		Map.entry(AdjustHistoryUS.TradeType.WITHDRAW_TRANSFER, new TradeTypeFunction.WITHDRAW_TRANSFER())
	);
	
	private static class TradeTypeFunction {
		private static class DIVIDEND_BOND implements Function<AdjustHistoryUS, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryUS e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.CASH;
				ret.currency       = AccountHistory.Currency.USD;
				ret.operation      = AccountHistory.Operation.DIVIDEND;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.name;
				
				return ret;
			}
		}
		private static class SELL_BOND implements Function<AdjustHistoryUS, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryUS e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.BOND;
				ret.currency       = AccountHistory.Currency.USD;
				ret.operation      = AccountHistory.Operation.SELL;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.name;
				
				return ret;
			}
		}
		private static class BUY_BOND implements Function<AdjustHistoryUS, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryUS e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.BOND;
				ret.currency       = AccountHistory.Currency.USD;
				ret.operation      = AccountHistory.Operation.BUY;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountWithdraw.replace(",", "")).negate();
				ret.code           = "";
				ret.comment        = e.name;
				
				return ret;
			}
		}
		private static class DIVIDEND_STOCK implements Function<AdjustHistoryUS, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryUS e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.CASH;
				ret.currency       = AccountHistory.Currency.USD;
				ret.operation      = AccountHistory.Operation.DIVIDEND;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = toStockCode(e.name);
				ret.comment        = toStockName(ret.code);
				
				return ret;
			}
		}
		private static class SELL_STOCK implements Function<AdjustHistoryUS, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryUS e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.STOCK;
				ret.currency       = AccountHistory.Currency.USD;
				ret.operation      = AccountHistory.Operation.SELL;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = new BigDecimal(e.unitPrice.replace(",", ""));
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = toStockCode(e.name);
				ret.comment        = toStockName(ret.code);
				
				return ret;
			}
		}
		private static class BUY_STOCK implements Function<AdjustHistoryUS, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryUS e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.STOCK;
				ret.currency       = AccountHistory.Currency.USD;
				ret.operation      = AccountHistory.Operation.BUY;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = new BigDecimal(e.unitPrice.replace(",", ""));
				ret.amount         = new BigDecimal(e.amountWithdraw.replace(",", "")).negate();
				ret.code           = toStockCode(e.name);
				ret.comment        = toStockName(ret.code);
				
				return ret;
			}
		}
		private static class SELL_MMF implements Function<AdjustHistoryUS, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryUS e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.FUND;
				ret.currency       = AccountHistory.Currency.USD;
				ret.operation      = AccountHistory.Operation.SELL;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = new BigDecimal(e.unitPrice.replace(",", ""));
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.name;
				
				return ret;
			}
		}
		private static class BUY_MMF implements Function<AdjustHistoryUS, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryUS e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.FUND;
				ret.currency       = AccountHistory.Currency.USD;
				ret.operation      = AccountHistory.Operation.BUY;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = new BigDecimal(e.unitPrice.replace(",", ""));
				ret.amount         = new BigDecimal(e.amountWithdraw.replace(",", "")).negate();
				ret.code           = "";
				ret.comment        = e.name;
				
				return ret;
			}
		}
		private static class DEOSIT_TRANSFER implements Function<AdjustHistoryUS, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryUS e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.CASH;
				ret.currency       = AccountHistory.Currency.USD;
				ret.operation      = AccountHistory.Operation.DEPOSIT;
				ret.units          = BigDecimal.ZERO;
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.tradeType.toString() + " " + e.amountDepositJPY + "円";
				
				return ret;
			}
		}
		private static class WITHDRAW_TRANSFER implements Function<AdjustHistoryUS, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryUS e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.asset          = AccountHistory.Asset.CASH;
				ret.currency       = AccountHistory.Currency.USD;
				ret.operation      = AccountHistory.Operation.WITHDRAW;
				ret.units          = BigDecimal.ZERO;
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountWithdraw.replace(",", "")).negate();
				ret.code           = "";
				ret.comment        = e.tradeType.toString() + " " + e.amountWithdrawJPY + "円";
				
				return ret;
			}
		}
	}
	
	private static List<AccountHistory> toAccountHistory(AdjustHistoryUS[] array) {
		var ret = new ArrayList<AccountHistory>();
		for(var e: array) {
			var accountHistory = functionMap.get(e.tradeType).apply(e);
			if (accountHistory.settlementDate.isBefore(TODAY)) {
				ret.add(accountHistory);
			} else {
				ret.add(accountHistory);
				logger.warn("settlementDate is future date  {}", accountHistory);
			}
		}
		
		return ret;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		UpdateAccountHistory.copyFiles();
		update();
		
		logger.info("STOP");
	}
}

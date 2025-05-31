package yokwe.finance.trade.rakuten;

import static yokwe.finance.trade.rakuten.UpdateAccountHistory.DIR_DOWNLOAD;
import static yokwe.finance.trade.rakuten.UpdateAccountHistory.FILTER_ADJUSTHISTORY_JP;
import static yokwe.finance.trade.rakuten.UpdateAccountHistory.FILTER_TRADEHISTORY_JP;
import static yokwe.finance.trade.rakuten.UpdateAccountHistory.TODAY;
import static yokwe.finance.trade.rakuten.UpdateAccountHistory.mergeMixed;
import static yokwe.finance.trade.rakuten.UpdateAccountHistory.toLocalDate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.trade.AccountHistory;
import yokwe.finance.trade.AccountHistory.Asset;
import yokwe.finance.trade.AccountHistory.Currency;
import yokwe.finance.trade.AccountHistory.Operation;
import yokwe.finance.type.StockCodeJP;
import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;

public class UpdateAccountHistoryJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
	public static void update() {
		List<AccountHistory> depositList = new ArrayList<AccountHistory>();

		// build stockNameMap and historyList
		{
			var files = DIR_DOWNLOAD.listFiles(FILTER_TRADEHISTORY_JP);
			Arrays.sort(files);
			for(var file : files) {
				var array = CSVUtil.read(TradeHistoryJP.class).file(file).toArray(TradeHistoryJP[]::new);
				logger.info("read  {}  {}", array.length, file.getName());
				buildStockNameMap(array);
				logger.info("stockNameMap  {}", stockNameMap.size());
				
				depositList = toDepositList(array);
				logger.info("depositList   {}", depositList.size());
			}
		}
		
		// update oldList
		var oldList = StorageRakuten.AccountHistory.getList();
		{
			var files = DIR_DOWNLOAD.listFiles(FILTER_ADJUSTHISTORY_JP);
			Arrays.sort(files);
			for(var file: files) {
				var array = CSVUtil.read(AdjustHistoryJP.class).file(file).toArray(AdjustHistoryJP[]::new);
				logger.info("read  {}  {}", array.length, file.getName());
				var newList = toAccountHistory(array);
				logger.info("newList       {}", newList.size());
				
				// merge depositList if necessary
				{
					Collections.sort(newList);
					var dateFirst = newList.get(0).settlementDate;
					var dateLast  = newList.get(newList.size() - 1).settlementDate;
					for(var e: depositList) {
						if (e.settlementDate.isBefore(dateFirst)) continue;
						if (e.settlementDate.isAfter(dateLast))   continue;
						newList.add(e);
					}
					logger.info("newList       {}", newList.size());
				}
				
				oldList = mergeMixed(oldList, newList);
			}
		}
		logger.info("save  {}  {}", oldList.size(), StorageRakuten.AccountHistory.getPath());
		StorageRakuten.AccountHistory.save(oldList);
	}
	
	
	private static Map<String, String> stockCodeMap = StorageStock.StockInfoJP.getList().stream().collect(Collectors.toMap(o -> o.stockCode, o -> o.name));
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
	private static void buildStockNameMap(TradeHistoryJP[] array) {
		for(var e: array) {
			var code = e.code;
			var name = e.name;
			
			stockNameMap.put(name, StockCodeJP.toStockCode5(code));
		}
	}
	private static String toStockCode(String stockName) {
		// find stock code from stockNameMap
		{
			var code = stockNameMap.get(stockName);
			if (code != null) return code;
		}
		
		// find stock code from stockNameMap
		for(var entry: stockNameMap.entrySet()) {
			var name = entry.getKey();
			var code = entry.getValue();
			
			if (stockName.replace("　", "").equals(name)) {
				// save for later use
				stockNameMap.put(stockName, code);
				return code;
			}
		}
		
		// find stock code from stockCodeMap
		for(var entry: stockCodeMap.entrySet()) {
			var code = entry.getKey();
			var name = entry.getValue();
			
			if (name.startsWith(stockName)) {
				// save for later use
				stockNameMap.put(stockName, code);
				return code;
			}
		}
		
		logger.error("Unexpected name");
		logger.error("  name  {}!", stockName);
		throw new UnexpectedException("Unexpected name");
	}
	
	private static Map<AdjustHistoryJP.TradeType, Function<AdjustHistoryJP, AccountHistory>> functionMap = Map.ofEntries(
		Map.entry(AdjustHistoryJP.TradeType.DIVIDEND_STOCK,    new TradeTypeFunction.DIVIDEND_STOCK()),
		Map.entry(AdjustHistoryJP.TradeType.DEPOSIT_CAMPAIGN,  new TradeTypeFunction.DEPOSIT_CAMPAIGN()),
		Map.entry(AdjustHistoryJP.TradeType.DEOSIT_TRANSFER,   new TradeTypeFunction.DEOSIT_TRANSFER()),
		Map.entry(AdjustHistoryJP.TradeType.WITHDRAW_TRANSFER, new TradeTypeFunction.WITHDRAW_TRANSFER()),
		Map.entry(AdjustHistoryJP.TradeType.SELL_STOCK,        new TradeTypeFunction.SELL_STOCK()),
		Map.entry(AdjustHistoryJP.TradeType.SELL_FUND,         new TradeTypeFunction.SELL_FUND()),
		Map.entry(AdjustHistoryJP.TradeType.BUY_FUND,          new TradeTypeFunction.BUY_FUND()),
		Map.entry(AdjustHistoryJP.TradeType.BUY_STOCK,         new TradeTypeFunction.BUY_STOCK()),
		Map.entry(AdjustHistoryJP.TradeType.TAX_INCOME,        new TradeTypeFunction.TAX_INCOME()),
		Map.entry(AdjustHistoryJP.TradeType.TAX_LOCAL,         new TradeTypeFunction.TAX_LOCAL()),
		Map.entry(AdjustHistoryJP.TradeType.TAX_REFUND_INCOME, new TradeTypeFunction.TAX_REFUND_INCOME()),
		Map.entry(AdjustHistoryJP.TradeType.TAX_REFUND_LOCAL,  new TradeTypeFunction.TAX_REFUND_LOCAL()),
		Map.entry(AdjustHistoryJP.TradeType.DEPOSIT_REALTIME,  new TradeTypeFunction.DEPOSIT_REALTIME()),
		Map.entry(AdjustHistoryJP.TradeType.DEPOSIT_POINT,     new TradeTypeFunction.DEPOSIT_POINT()),
		Map.entry(AdjustHistoryJP.TradeType.WITHDRAW,          new TradeTypeFunction.WITHDRAW())
	);

	private static class TradeTypeFunction {
		private static class DIVIDEND_STOCK implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = AccountHistory.Currency.JPY;
				ret.operation      = AccountHistory.Operation.DIVIDEND;
				ret.asset          = AccountHistory.Asset.CASH;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = toStockCode(e.name);
				ret.comment        = toStockName(ret.code);
				
				return ret;
			}
		}
		private static class DEPOSIT_CAMPAIGN implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = AccountHistory.Currency.JPY;
				ret.operation      = AccountHistory.Operation.DEPOSIT;
				ret.asset          = AccountHistory.Asset.CASH;
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
				ret.currency       = AccountHistory.Currency.JPY;
				ret.operation      = AccountHistory.Operation.DEPOSIT;
				ret.asset          = AccountHistory.Asset.CASH;
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
				ret.currency       = AccountHistory.Currency.JPY;
				ret.operation      = AccountHistory.Operation.WITHDRAW;
				ret.asset          = AccountHistory.Asset.CASH;
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
				ret.currency       = AccountHistory.Currency.JPY;
				ret.operation      = AccountHistory.Operation.SELL;
				ret.asset          = AccountHistory.Asset.STOCK;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = new BigDecimal(e.unitPrice.replace(",", ""));
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = toStockCode(e.name);
				ret.comment        = toStockName(ret.code);
				
				return ret;
			}
		}
		
		
		private static Map<String, String> fundNameMap = new TreeMap<>();
		//                 name    isinCode
		private static Map<String, String> fundCodeMap = new TreeMap<>();
		//                 isinCode name
		static {
			for(var e: yokwe.finance.provider.rakuten.StorageRakuten.FundCodeNameRakuten.getList()) {
				var name = e.name;
				var code = e.isinCode;
				fundNameMap.put(name, code);
			}
			for(var e: StorageFund.FundInfo.getList()) {
				var name = e.name;
				var code = e.isinCode;
				fundNameMap.put(name, code);
				fundCodeMap.put(code, name);
			}
		}
		private static String getCode(String name) {
			if (name.contains("/")) {
				name = name.substring(0, name.indexOf("/"));
			}
			{
				var code = fundNameMap.get(name);
				if (code != null) return code;
			}
			{
				if (name.contains("(")) {
					var code = fundNameMap.get(name.substring(0, name.indexOf("(")));
					if (code != null) return code;
				}
			}
			{
				if (name.contains("SMT")) {
					var code = fundNameMap.get(name.replace("SMT", "ＳＭＴ"));
					if (code != null) return code;
				}
			}
			{
				var code = fundNameMap.get(name.replace(" ", ""));
				if (code != null) return code;
			}
			{
				var code = fundNameMap.get(name.replace("(", "（").replace(")", "）"));
				if (code != null) return code;
			}
			logger.error("Unexpected name");
			logger.error("  name  {}!", name);
			throw new UnexpectedException("Unexpected name");
		}
		//                 name    isinCode
		private static class SELL_FUND implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var code = getCode(e.name);
				var name = fundCodeMap.get(code);
				
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = AccountHistory.Currency.JPY;
				ret.operation      = AccountHistory.Operation.SELL;
				ret.asset          = AccountHistory.Asset.FUND;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = new BigDecimal(e.unitPrice.replace(",", ""));
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", ""));
				ret.code           = code;
				ret.comment        = name;
				
				return ret;
			}
		}
		private static class BUY_FUND implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var code = getCode(e.name);
				var name = fundCodeMap.get(code);
				
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = AccountHistory.Currency.JPY;
				ret.operation      = AccountHistory.Operation.BUY;
				ret.asset          = AccountHistory.Asset.FUND;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = new BigDecimal(e.unitPrice.replace(",", ""));
				ret.amount         = new BigDecimal(e.amountWithdraw.replace(",", "")).negate();
				ret.code           = code;
				ret.comment        = name;
				
				return ret;
			}
		}
		private static class BUY_STOCK implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = AccountHistory.Currency.JPY;
				ret.operation      = AccountHistory.Operation.BUY;
				ret.asset          = AccountHistory.Asset.STOCK;
				ret.units          = new BigDecimal(e.units.replace(",", ""));
				ret.unitPrice      = new BigDecimal(e.unitPrice.replace(",", ""));
				ret.amount         = new BigDecimal(e.amountWithdraw.replace(",", "")).negate();
				ret.code           = toStockCode(e.name);
				ret.comment        = toStockName(ret.code);
				
				return ret;
			}
		}
		private static class TAX_INCOME implements Function<AdjustHistoryJP, AccountHistory> {
			@Override
			public AccountHistory apply(AdjustHistoryJP e) {
				var ret = new AccountHistory();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = AccountHistory.Currency.JPY;
				ret.operation      = AccountHistory.Operation.TAX;
				ret.asset          = AccountHistory.Asset.CASH;
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
				ret.currency       = AccountHistory.Currency.JPY;
				ret.operation      = AccountHistory.Operation.TAX;
				ret.asset          = AccountHistory.Asset.CASH;
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
				ret.currency       = AccountHistory.Currency.JPY;
				ret.operation      = AccountHistory.Operation.TAX;
				ret.asset          = AccountHistory.Asset.CASH;
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
				ret.currency       = AccountHistory.Currency.JPY;
				ret.operation      = AccountHistory.Operation.TAX;
				ret.asset          = AccountHistory.Asset.CASH;
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
				ret.currency       = AccountHistory.Currency.JPY;
				ret.operation      = AccountHistory.Operation.DEPOSIT;
				ret.asset          = AccountHistory.Asset.CASH;
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
				ret.currency       = AccountHistory.Currency.JPY;
				ret.operation      = AccountHistory.Operation.DEPOSIT;
				ret.asset          = AccountHistory.Asset.CASH;
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
				ret.currency       = AccountHistory.Currency.JPY;
				ret.operation      = AccountHistory.Operation.WITHDRAW;
				ret.asset          = AccountHistory.Asset.CASH;
				ret.units          = BigDecimal.ZERO;
				ret.unitPrice      = BigDecimal.ZERO;
				ret.amount         = new BigDecimal(e.amountWithdraw.replace(",", "")).negate();
				ret.code           = "";
				ret.comment        = e.tradeType.toString();
				
				return ret;
			}
		}
	}
	
	private static List<AccountHistory> toAccountHistory(AdjustHistoryJP[] array) {
		var ret = new ArrayList<AccountHistory>();
		for(var e: array) {
			var accountHistory = functionMap.get(e.tradeType).apply(e);
			if (accountHistory.settlementDate.isBefore(TODAY)) {
				ret.add(accountHistory);
			} else {
				ret.add(accountHistory);
				logger.warn("settlementDate has future date  {}  {}  {} {}",
					accountHistory.settlementDate, accountHistory.currency, accountHistory.operation, accountHistory.comment);
			}
		}
		
		return ret;
	}
	
	private static List<AccountHistory> toDepositList(TradeHistoryJP[] array) {
		var ret = new ArrayList<AccountHistory>();
		for(var e: array) {
			switch(e.tradeType) {
			case BUY:
			case SELL:
				break;
			case DEPOSIT: {
				var accountHistory = new AccountHistory();
				accountHistory.settlementDate = toLocalDate(e.settlementDate); // 受渡日
				accountHistory.tradeDate      = toLocalDate(e.tradeDate);      // 約定日
				accountHistory.currency       = Currency.JPY;
				accountHistory.operation      = Operation.DEPOSIT;
				accountHistory.asset          = Asset.STOCK;
				accountHistory.units          = new BigDecimal(e.units.replace(",", ""));
				accountHistory.unitPrice      = new BigDecimal(e.unitPrice.replace(",", ""));
				accountHistory.amount         = accountHistory.units.multiply(accountHistory.unitPrice);
				accountHistory.code           = StockCodeJP.toStockCode5(e.code);
				accountHistory.comment        = toStockName(accountHistory.code);
				
				ret.add(accountHistory);
			}
				break;
			default:
				logger.error("Unexpeced tradeType");
				logger.error("  {}", e.toString());
				throw new UnexpectedException("Unexpeced tradeType");
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

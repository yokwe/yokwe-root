package yokwe.finance.trade.rakuten;

import static yokwe.finance.trade.rakuten.UpdateAccountReport.URL_TEMPLATE;
import static yokwe.finance.trade.rakuten.UpdateAccountReport.toCentValue;
import static yokwe.finance.trade.rakuten.UpdateAccountReport.toDollarValue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import yokwe.finance.trade.AccountHistory;
import yokwe.finance.trade.AccountHistory.Asset;
import yokwe.finance.trade.AccountHistory.Currency;
import yokwe.finance.trade.AccountHistory.Operation;
import yokwe.finance.trade.AccountReportJPY;
import yokwe.finance.trade.AccountReportUSD;
import yokwe.finance.trade.Portfolio;
import yokwe.util.StringUtil;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateAccountReportUSD {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
	
	public static void update() {
		var list = getAccountReportList();
		logger.info("list  {}", list.size());		
		// remove entry older than 1 year
		list.removeIf(o -> o.date.isBefore(LocalDate.now().minusYears(1)));
		
		generateReport(list);
	}
	
	private static void generateReport(List<AccountReportUSD> list) {
		String urlReport;
		{
			String timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
			String name       = String.format("account-report-usd-%s.ods", timestamp);
			String pathReport = StorageRakuten.storage.getPath("report", name);
			
			urlReport  = StringUtil.toURLString(pathReport);
		}

		logger.info("urlReport {}", urlReport);
		logger.info("docLoad   {}", URL_TEMPLATE);
		try {
			// start LibreOffice process
			LibreOffice.initialize();
			
			SpreadSheet docLoad = new SpreadSheet(URL_TEMPLATE, true);
			SpreadSheet docSave = new SpreadSheet();
			
			String sheetName = Sheet.getSheetName(AccountReportJPY.class);
			logger.info("sheet     {}", sheetName);
			docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
			Sheet.fillSheet(docSave, list);
			
			// remove first sheet
			docSave.removeSheet(docSave.getSheetName(0));

			docSave.store(urlReport);
			logger.info("output    {}", urlReport);
			
			docLoad.close();
			logger.info("close     docLoad");
			docSave.close();
			logger.info("close     docSave");
		} finally {
			// stop LibreOffice process
			LibreOffice.terminate();
		}
	}
	
	private static class Context {
		Portfolio  portfolio    = new Portfolio();
		int        fundTotal    = 0;
		int        cashTotal    = 0;
		int        stockCost    = 0;
		int        realizedGain = 0;
	}
	
	private static List<AccountReportUSD> getAccountReportList() {
		var ret = new ArrayList<AccountReportUSD>();
		
		var context = new Context();
		
		var list = StorageRakuten.AccountHistory.getList().stream().filter(o -> o.currency == Currency.USD).collect(Collectors.toList());
		// map needs to use TreeMap for entrySet ordering
		var map  = list.stream().collect(Collectors.groupingBy(o -> o.settlementDate, TreeMap::new, Collectors.toCollection(ArrayList::new)));
		
		// convert AccountHistory to AccountReportJPY
		LocalDate lastDate = null;
		for(var entry: map.entrySet()) {
			var date    = entry.getKey();
			var mapList = entry.getValue();
			
			for(;;) {
				if (lastDate == null) break;
				lastDate = lastDate.plusDays(1);
				if (!lastDate.isBefore(date)) break;
				ret.add(reportAsOf(context, lastDate));
			}
			lastDate = date;
			
			for(var e: mapList) {
				var key = new TransactionAsset(e);
				var function = functionMap.get(key);
				if (function == null) {
					logger.info("no function  {}  {}", key);
					continue;
				}
				ret.add(function.apply(context, e));
			}
		}
		
		// add line of today
		{
			var today = LocalDate.now();
			for(;;) {
				if (lastDate == null) break;
				lastDate = lastDate.plusDays(1);
				if (lastDate.isAfter(today)) break;
				ret.add(reportAsOf(context, lastDate));
			}
		}
		
		// sanity check
		{
			var stockCost = 0;
			for(var e: context.portfolio.getHoldingMap().values()) {
				stockCost += e.totalCost();
			}
			if (context.stockCost != stockCost) {
				logger.error("Unexpected stockCost value");
				logger.error("fundTotal     {}", context.fundTotal);
				logger.error("cashTotal     {}", context.cashTotal);
				logger.error("stockCost     {}", context.stockCost);
				logger.error("realizedGain  {}", context.realizedGain);
				for(var holding: context.portfolio.getHoldingMap().values()) {
					if (holding.totalUnits() == 0) continue;
					logger.error("holding       {}  {}  {}", holding.code, holding.totalUnits(), holding.totalCost());
				}
//				throw new UnexpectedException("Unexpected stockCost value");
			}
		}
		
		return ret;
	}
	
	private static AccountReportUSD reportAsOf(Context context, LocalDate date) {
		// update context
		// fundTotal = cashTotal + stockCost
		
		// build report
		var ret = new AccountReportUSD();
		
		ret.date           = date;
		ret.deposit        = BigDecimal.ZERO;
		ret.withdraw       = BigDecimal.ZERO;
		ret.fundTotal      = toDollarValue(context.fundTotal);
		ret.cashTotal      = toDollarValue(context.cashTotal);
		ret.stockValue     = toDollarValue(context.portfolio.valueAsOf(date));
		ret.stockCost      = toDollarValue(context.stockCost);
		ret.unrealizedGain = (ret.stockValue.signum() <= 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
		ret.realizedGain   = toDollarValue(context.realizedGain);
		ret.dividend       = BigDecimal.ZERO;
		ret.buy            = BigDecimal.ZERO;
		ret.sell           = BigDecimal.ZERO;
		ret.sellCost       = BigDecimal.ZERO;
		ret.sellGain       = BigDecimal.ZERO;
		ret.code           = "";
		ret.comment        = "";
		
		return ret;
	}
	
	private static record TransactionAsset (Operation transaction, Asset asset) {
		public TransactionAsset(AccountHistory history) {
			this(history.operation, history.asset);
		}
	}
	
	private static Map<TransactionAsset, BiFunction<Context, AccountHistory, AccountReportUSD>> functionMap = Map.ofEntries(
			Map.entry(new TransactionAsset(Operation.DEPOSIT,  Asset.CASH),  new ConvertFunction.DEPOSIT_CASH()),
			Map.entry(new TransactionAsset(Operation.WITHDRAW, Asset.CASH),  new ConvertFunction.WITHDRAW_CASH()),
			Map.entry(new TransactionAsset(Operation.DIVIDEND, Asset.CASH),  new ConvertFunction.DIVIDEND_CASH()),
			Map.entry(new TransactionAsset(Operation.BUY,      Asset.STOCK), new ConvertFunction.BUY_STOCK()),
			Map.entry(new TransactionAsset(Operation.SELL,     Asset.STOCK), new ConvertFunction.SELL_STOCK()),
			Map.entry(new TransactionAsset(Operation.BUY,      Asset.BOND),  new ConvertFunction.BUY_BOND()),
			Map.entry(new TransactionAsset(Operation.SELL,     Asset.BOND),  new ConvertFunction.SELL_BOND()),
			Map.entry(new TransactionAsset(Operation.BUY,      Asset.FUND),  new ConvertFunction.BUY_FUND()),
			Map.entry(new TransactionAsset(Operation.SELL,     Asset.FUND),  new ConvertFunction.SELL_FUND())
	);
	
	private static class ConvertFunction {
		private static class DEPOSIT_CASH implements BiFunction<Context, AccountHistory, AccountReportUSD> {
			@Override
			public AccountReportUSD apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = toCentValue(accountHistory.amount);
				var comment = accountHistory.comment;

				// update context
				// fundTotal = cashTotal + stockCost
				context.fundTotal += amount;
				context.cashTotal += amount;
				
				// build report
				var ret = new AccountReportUSD();
				
				ret.date           = date;
				ret.deposit        = toDollarValue(amount);
				ret.withdraw       = BigDecimal.ZERO;
				ret.fundTotal      = toDollarValue(context.fundTotal);
				ret.cashTotal      = toDollarValue(context.cashTotal);
				ret.stockValue     = toDollarValue(context.portfolio.valueAsOf(date));
				ret.stockCost      = toDollarValue(context.stockCost);
				ret.unrealizedGain = (ret.stockValue.signum() <= 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
				ret.realizedGain   = toDollarValue(context.realizedGain);
				ret.dividend       = BigDecimal.ZERO;
				ret.buy            = BigDecimal.ZERO;
				ret.sell           = BigDecimal.ZERO;
				ret.sellCost       = BigDecimal.ZERO;
				ret.sellGain       = BigDecimal.ZERO;
				ret.code           = "";
				ret.comment        = comment;
				
				return ret;
			}
		}
		private static class WITHDRAW_CASH implements BiFunction<Context, AccountHistory, AccountReportUSD> {
			@Override
			public AccountReportUSD apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = toCentValue(accountHistory.amount.negate());
				var comment = accountHistory.comment;
				
				// update context
				// fundTotal = cashTotal + stockCost
				context.fundTotal -= amount;
				context.cashTotal -= amount;

				// build report
				var ret = new AccountReportUSD();
				
				ret.date           = date;
				ret.deposit        = BigDecimal.ZERO;
				ret.withdraw       = toDollarValue(amount);
				ret.fundTotal      = toDollarValue(context.fundTotal);
				ret.cashTotal      = toDollarValue(context.cashTotal);
				ret.stockValue     = toDollarValue(context.portfolio.valueAsOf(date));
				ret.stockCost      = toDollarValue(context.stockCost);
				ret.unrealizedGain = (ret.stockValue.signum() <= 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
				ret.realizedGain   = toDollarValue(context.realizedGain);
				ret.dividend       = BigDecimal.ZERO;
				ret.buy            = BigDecimal.ZERO;
				ret.sell           = BigDecimal.ZERO;
				ret.sellCost       = BigDecimal.ZERO;
				ret.sellGain       = BigDecimal.ZERO;
				ret.code           = "";
				ret.comment        = comment;
				
				return ret;
			}
		}
		private static class DIVIDEND_CASH implements BiFunction<Context, AccountHistory, AccountReportUSD> {
			@Override
			public AccountReportUSD apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = toCentValue(accountHistory.amount);
				var code    = accountHistory.code;
				var name    = accountHistory.comment;

				// update context
				// fundTotal = cashTotal + stockCost
				context.fundTotal    += amount;
				context.cashTotal    += amount;
				context.realizedGain += amount;
				
				// build report
				var ret = new AccountReportUSD();
				
				ret.date           = date;
				ret.deposit        = BigDecimal.ZERO;
				ret.withdraw       = BigDecimal.ZERO;
				ret.fundTotal      = toDollarValue(context.fundTotal);
				ret.cashTotal      = toDollarValue(context.cashTotal);
				ret.stockValue     = toDollarValue(context.portfolio.valueAsOf(date));
				ret.stockCost      = toDollarValue(context.stockCost);
				ret.unrealizedGain = (ret.stockValue.signum() <= 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
				ret.realizedGain   = toDollarValue(context.realizedGain);
				ret.dividend       = toDollarValue(amount);
				ret.buy            = BigDecimal.ZERO;
				ret.sell           = BigDecimal.ZERO;
				ret.sellCost       = BigDecimal.ZERO;
				ret.sellGain       = BigDecimal.ZERO;
				ret.code           = code;
				ret.comment        = name;
				
				return ret;
			}
		}
		private static class BUY_STOCK implements BiFunction<Context, AccountHistory, AccountReportUSD> {
			@Override
			public AccountReportUSD apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = toCentValue(accountHistory.amount.negate());
				var code    = accountHistory.code;
				var name    = accountHistory.comment;
				var units   = accountHistory.units.intValue();
				
				// update portfolio
				context.portfolio.getHolding(code, name).buy(date, units, amount);
				
				// update context
				// fundTotal = cashTotal + stockCost
				context.cashTotal -= amount;
				context.stockCost += amount;

				// build report
				var ret = new AccountReportUSD();
				
				ret.date           = date;
				ret.deposit        = BigDecimal.ZERO;
				ret.withdraw       = BigDecimal.ZERO;
				ret.fundTotal      = toDollarValue(context.fundTotal);
				ret.cashTotal      = toDollarValue(context.cashTotal);
				ret.stockValue     = toDollarValue(context.portfolio.valueAsOf(date));
				ret.stockCost      = toDollarValue(context.stockCost);
				ret.unrealizedGain = (ret.stockValue.signum() <= 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
				ret.realizedGain   = toDollarValue(context.realizedGain);
				ret.dividend       = BigDecimal.ZERO;
				ret.buy            = toDollarValue(amount);
				ret.sell           = BigDecimal.ZERO;
				ret.sellCost       = BigDecimal.ZERO;
				ret.sellGain       = BigDecimal.ZERO;
				ret.code           = code;
				ret.comment        = name;
				
				return ret;
			}
		}
		private static class SELL_STOCK implements BiFunction<Context, AccountHistory, AccountReportUSD> {
			@Override
			public AccountReportUSD apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = toCentValue(accountHistory.amount);
				var code    = accountHistory.code;
				var name    = accountHistory.comment;
				var units   = accountHistory.units.intValue();
				
				// update portfolio
				var sellCost = context.portfolio.getHolding(code, name).sell(date, units, amount);
				var gain     = amount - sellCost;

				// update context
				// fundTotal = cashTotal + stockCost
				context.fundTotal    += amount - sellCost;
				context.cashTotal    += amount;
				context.stockCost    -= sellCost;
				context.realizedGain += gain;
				
				// build report
				var ret = new AccountReportUSD();
				
				ret.date           = date;
				ret.deposit        = BigDecimal.ZERO;
				ret.withdraw       = BigDecimal.ZERO;
				ret.fundTotal      = toDollarValue(context.fundTotal);
				ret.cashTotal      = toDollarValue(context.cashTotal);
				ret.stockValue     = toDollarValue(context.portfolio.valueAsOf(date));
				ret.stockCost      = toDollarValue(context.stockCost);
				ret.unrealizedGain = (ret.stockValue.signum() <= 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
				ret.realizedGain   = toDollarValue(context.realizedGain);
				ret.dividend       = BigDecimal.ZERO;
				ret.buy            = BigDecimal.ZERO;
				ret.sell           = toDollarValue(amount);
				ret.sellCost       = toDollarValue(sellCost);
				ret.sellGain       = toDollarValue(gain);
				ret.code           = code;
				ret.comment        = name;
				
				return ret;
			}
		}
		private static class BUY_BOND implements BiFunction<Context, AccountHistory, AccountReportUSD> {
			@Override
			public AccountReportUSD apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = toCentValue(accountHistory.amount.negate());
				var code    = accountHistory.code;
				var name    = accountHistory.comment;
				var units   = accountHistory.units.intValue();
				
				// update portfolio
				context.portfolio.getHolding(code, name).buy(date, units, amount);
				
				// update context
				// fundTotal = cashTotal + stockCost
				context.cashTotal -= amount;
				context.stockCost += amount;

				// build report
				var ret = new AccountReportUSD();
				
				ret.date           = date;
				ret.deposit        = BigDecimal.ZERO;
				ret.withdraw       = BigDecimal.ZERO;
				ret.fundTotal      = toDollarValue(context.fundTotal);
				ret.cashTotal      = toDollarValue(context.cashTotal);
				ret.stockValue     = toDollarValue(context.portfolio.valueAsOf(date));
				ret.stockCost      = toDollarValue(context.stockCost);
				ret.unrealizedGain = (ret.stockValue.signum() <= 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
				ret.realizedGain   = toDollarValue(context.realizedGain);
				ret.dividend       = BigDecimal.ZERO;
				ret.buy            = toDollarValue(amount);
				ret.sell           = BigDecimal.ZERO;
				ret.sellCost       = BigDecimal.ZERO;
				ret.sellGain       = BigDecimal.ZERO;
				ret.code           = code;
				ret.comment        = name;
				
				return ret;
			}
		}
		private static class SELL_BOND implements BiFunction<Context, AccountHistory, AccountReportUSD> {
			@Override
			public AccountReportUSD apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = toCentValue(accountHistory.amount);
				var code    = accountHistory.code;
				var name    = accountHistory.comment;
				var units   = accountHistory.units.intValue();
				
				// update portfolio
				var sellCost = context.portfolio.getHolding(code, name).sell(date, units, amount);
				var gain     = amount - sellCost;

				// update context
				// fundTotal = cashTotal + stockCost
				context.fundTotal    += amount - sellCost;
				context.cashTotal    += amount;
				context.stockCost    -= sellCost;
				context.realizedGain += gain;
				
				// build report
				var ret = new AccountReportUSD();
				
				ret.date           = date;
				ret.deposit        = BigDecimal.ZERO;
				ret.withdraw       = BigDecimal.ZERO;
				ret.fundTotal      = toDollarValue(context.fundTotal);
				ret.cashTotal      = toDollarValue(context.cashTotal);
				ret.stockValue     = toDollarValue(context.portfolio.valueAsOf(date));
				ret.stockCost      = toDollarValue(context.stockCost);
				ret.unrealizedGain = (ret.stockValue.signum() <= 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
				ret.realizedGain   = toDollarValue(context.realizedGain);
				ret.dividend       = BigDecimal.ZERO;
				ret.buy            = BigDecimal.ZERO;
				ret.sell           = toDollarValue(amount);
				ret.sellCost       = toDollarValue(sellCost);
				ret.sellGain       = toDollarValue(gain);
				ret.code           = code;
				ret.comment        = name;
				
				return ret;
			}
		}
		private static class BUY_FUND implements BiFunction<Context, AccountHistory, AccountReportUSD> {
			@Override
			public AccountReportUSD apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = toCentValue(accountHistory.amount.negate());
				var code    = accountHistory.code;
				var name    = accountHistory.comment;
				var units   = accountHistory.units.intValue();
				
				// update portfolio
				context.portfolio.getHolding(code, name).buy(date, units, amount);
				
				// update context
				// fundTotal = cashTotal + stockCost
				context.cashTotal -= amount;
				context.stockCost += amount;

				// build report
				var ret = new AccountReportUSD();
				
				ret.date           = date;
				ret.deposit        = BigDecimal.ZERO;
				ret.withdraw       = BigDecimal.ZERO;
				ret.fundTotal      = toDollarValue(context.fundTotal);
				ret.cashTotal      = toDollarValue(context.cashTotal);
				ret.stockValue     = toDollarValue(context.portfolio.valueAsOf(date));
				ret.stockCost      = toDollarValue(context.stockCost);
				ret.unrealizedGain = (ret.stockValue.signum() <= 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
				ret.realizedGain   = toDollarValue(context.realizedGain);
				ret.dividend       = BigDecimal.ZERO;
				ret.buy            = toDollarValue(amount);
				ret.sell           = BigDecimal.ZERO;
				ret.sellCost       = BigDecimal.ZERO;
				ret.sellGain       = BigDecimal.ZERO;
				ret.code           = code;
				ret.comment        = name;
				
				return ret;
			}
		}
		private static class SELL_FUND implements BiFunction<Context, AccountHistory, AccountReportUSD> {
			@Override
			public AccountReportUSD apply(Context context, AccountHistory accountHistory) {
				// FIXME when selling GS米ドルファンド, amount includes interest. And JPY is used for tax of profit.
				// FIXME so totalUnits has different value of summation of amount of buy and sell units.
				var date    = accountHistory.settlementDate;
				var amount  = toCentValue(accountHistory.amount);
				var code    = accountHistory.code;
				var name    = accountHistory.comment;
				var units   = accountHistory.units.intValue();
				
				// update portfolio
				var sellCost = context.portfolio.getHolding(code, name).sell(date, units, amount);
				var gain     = amount - sellCost;

				// update context
				// fundTotal = cashTotal + stockCost
				context.fundTotal    += amount - sellCost;
				context.cashTotal    += amount;
				context.stockCost    -= sellCost;
				context.realizedGain += gain;
				
				// build report
				var ret = new AccountReportUSD();
				
				ret.date           = date;
				ret.deposit        = BigDecimal.ZERO;
				ret.withdraw       = BigDecimal.ZERO;
				ret.fundTotal      = toDollarValue(context.fundTotal);
				ret.cashTotal      = toDollarValue(context.cashTotal);
				ret.stockValue     = toDollarValue(context.portfolio.valueAsOf(date));
				ret.stockCost      = toDollarValue(context.stockCost);
				ret.unrealizedGain = (ret.stockValue.signum() <= 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
				ret.realizedGain   = toDollarValue(context.realizedGain);
				ret.dividend       = BigDecimal.ZERO;
				ret.buy            = BigDecimal.ZERO;
				ret.sell           = toDollarValue(amount);
				ret.sellCost       = toDollarValue(sellCost);
				ret.sellGain       = toDollarValue(gain);
				ret.code           = code;
				ret.comment        = name;
				
				return ret;
			}
		}
	}
}

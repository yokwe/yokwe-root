package yokwe.finance.trade.rakuten;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import yokwe.finance.trade.AccountHistory;
import yokwe.finance.trade.AccountHistory.Asset;
import yokwe.finance.trade.AccountHistory.Currency;
import yokwe.finance.trade.AccountHistory.Operation;
import yokwe.finance.trade.AccountReportJPY;
import yokwe.finance.trade.Portfolio;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateAccountReportJPY {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_TEMPLATE  = StringUtil.toURLString("data/form/ACCOUNT.ods");
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
	
	public static void update() {
		var list = getAccountReportList();
		logger.info("list  {}", list.size());
		generateReport(list);
	}
	
	private static void generateReport(List<AccountReportJPY> list) {
		String urlReport;
		{
			String timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
			String name       = String.format("account-report-jpy-%s.ods", timestamp);
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
	
	private static record TransactionAsset (Operation transaction, Asset asset) {
		public TransactionAsset(AccountHistory history) {
			this(history.operation, history.asset);
		}
	}
	
	private static Map<TransactionAsset, BiFunction<Context, AccountHistory, AccountReportJPY>> functionMap = Map.ofEntries(
		Map.entry(new TransactionAsset(Operation.DEPOSIT,  Asset.CASH),  new ConvertFunction.DEPOSIT_CASH()),
		Map.entry(new TransactionAsset(Operation.WITHDRAW, Asset.CASH),  new ConvertFunction.WITHDRAW_CASH()),
		Map.entry(new TransactionAsset(Operation.DIVIDEND, Asset.CASH),  new ConvertFunction.DIVIDEND_CASH()),
		Map.entry(new TransactionAsset(Operation.TAX,      Asset.CASH),  new ConvertFunction.TAX_CASH()),
		Map.entry(new TransactionAsset(Operation.BUY,      Asset.STOCK), new ConvertFunction.BUY_STOCK()),
		Map.entry(new TransactionAsset(Operation.SELL,     Asset.STOCK), new ConvertFunction.SELL_STOCK()),
		Map.entry(new TransactionAsset(Operation.DEPOSIT,  Asset.STOCK), new ConvertFunction.DEPOSIT_STOCK()),
		Map.entry(new TransactionAsset(Operation.BUY,      Asset.FUND),  new ConvertFunction.BUY_FUND()),
		Map.entry(new TransactionAsset(Operation.SELL,     Asset.FUND),  new ConvertFunction.SELL_FUND())
	);
	
	private static class ConvertFunction {
		private static class DEPOSIT_CASH implements BiFunction<Context, AccountHistory, AccountReportJPY> {
			@Override
			public AccountReportJPY apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = accountHistory.amount.intValue();
				var comment = accountHistory.comment;

				// update context
				// fundTotal = cashTotal + stockCost
				context.fundTotal += amount;
				context.cashTotal += amount;
				
				// build report
				var ret = new AccountReportJPY();
				
				ret.date           = date;
				ret.deposit        = amount;
				ret.withdraw       = 0;
				ret.fundTotal      = context.fundTotal;
				ret.cashTotal      = context.cashTotal;
				ret.stockValue     = context.portfolio.valueAsOf(date);
				ret.stockCost      = context.stockCost;
				ret.unrealizedGain = (ret.stockValue <= 0) ? 0 : (ret.stockValue - ret.stockCost);
				ret.realizedGain   = context.realizedGain;
				ret.dividend       = 0;
				ret.buy            = 0;
				ret.sell           = 0;
				ret.sellCost       = 0;
				ret.sellGain       = 0;
				ret.code           = "";
				ret.comment        = comment;
				
				return ret;
			}
		}
		private static class WITHDRAW_CASH implements BiFunction<Context, AccountHistory, AccountReportJPY> {
			@Override
			public AccountReportJPY apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = accountHistory.amount.negate().intValue();
				var comment = accountHistory.comment;
				
				// update context
				// fundTotal = cashTotal + stockCost
				context.fundTotal -= amount;
				context.cashTotal -= amount;

				// build report
				var ret = new AccountReportJPY();
				
				ret.date           = date;
				ret.deposit        = 0;
				ret.withdraw       = amount;
				ret.fundTotal      = context.fundTotal;
				ret.cashTotal      = context.cashTotal;
				ret.stockValue     = context.portfolio.valueAsOf(date);
				ret.stockCost      = context.stockCost;
				ret.unrealizedGain = (ret.stockValue <= 0) ? 0 : (ret.stockValue - ret.stockCost);
				ret.realizedGain   = context.realizedGain;
				ret.dividend       = 0;
				ret.buy            = 0;
				ret.sell           = 0;
				ret.sellCost       = 0;
				ret.sellGain       = 0;
				ret.code           = "";
				ret.comment        = comment;
				
				return ret;
			}
		}
		private static class DIVIDEND_CASH implements BiFunction<Context, AccountHistory, AccountReportJPY> {
			@Override
			public AccountReportJPY apply(Context context, AccountHistory accountHistory) {
				var date   = accountHistory.settlementDate;
				var amount = accountHistory.amount.intValue();
				var code   = accountHistory.code;
				var name   = accountHistory.comment;
				
				// update context
				// fundTotal = cashTotal + stockCost
				context.fundTotal    += amount;
				context.cashTotal    += amount;
				context.realizedGain += amount;
				
				// build report
				var ret = new AccountReportJPY();
				
				ret.date           = date;
				ret.deposit        = 0;
				ret.withdraw       = 0;
				ret.fundTotal      = context.fundTotal;
				ret.cashTotal      = context.cashTotal;
				ret.stockValue     = context.portfolio.valueAsOf(date);
				ret.stockCost      = context.stockCost;
				ret.unrealizedGain = (ret.stockValue <= 0) ? 0 : (ret.stockValue - ret.stockCost);
				ret.realizedGain   = context.realizedGain;
				ret.dividend       = amount;
				ret.buy            = 0;
				ret.sell           = 0;
				ret.sellCost       = 0;
				ret.sellGain       = 0;
				ret.code           = code;
				ret.comment        = name;
				
				return ret;
			}
		}
		private static class TAX_CASH implements BiFunction<Context, AccountHistory, AccountReportJPY> {
			@Override
			public AccountReportJPY apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = accountHistory.amount.intValue();  // negative for tax, positive for tax refund
				var comment = accountHistory.comment;
				
				// update context
				// fundTotal = cashTotal + stockCost
				context.fundTotal    += amount;
				context.cashTotal    += amount;
				context.realizedGain += amount;

				// build report
				var ret = new AccountReportJPY();
				
				ret.date           = date;
				ret.deposit        = amount < 0 ? 0 : amount;
				ret.withdraw       = amount < 0 ? -amount : 0;
				ret.fundTotal      = context.fundTotal;
				ret.cashTotal      = context.cashTotal;
				ret.stockValue     = context.portfolio.valueAsOf(date);
				ret.stockCost      = context.stockCost;
				ret.unrealizedGain = (ret.stockValue <= 0) ? 0 : (ret.stockValue - ret.stockCost);
				ret.realizedGain   = context.realizedGain;
				ret.dividend       = 0;
				ret.buy            = 0;
				ret.sell           = 0;
				ret.sellCost       = 0;
				ret.sellGain       = 0;
				ret.code           = "";
				ret.comment        = comment;
				
				return ret;
			}
		}
		private static class BUY_STOCK implements BiFunction<Context, AccountHistory, AccountReportJPY> {
			@Override
			public AccountReportJPY apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = accountHistory.amount.negate().intValue(); // make positive
				var code    = accountHistory.code;
				var name    = accountHistory.comment;
				var units   = accountHistory.units.intValue();
				var comment = accountHistory.comment;
				
				// update portfolio
				context.portfolio.getHolding(code, name).buy(date, units, amount);
				
				// update context
				// fundTotal = cashTotal + stockCost
				context.cashTotal -= amount;
				context.stockCost += amount;

				// build report
				var ret = new AccountReportJPY();
				
				ret.date           = date;
				ret.deposit        = 0;
				ret.withdraw       = 0;
				ret.fundTotal      = context.fundTotal;
				ret.cashTotal      = context.cashTotal;
				ret.stockValue     = context.portfolio.valueAsOf(date);
				ret.stockCost      = context.stockCost;
				ret.unrealizedGain = (ret.stockValue <= 0) ? 0 : (ret.stockValue - ret.stockCost);
				ret.realizedGain   = context.realizedGain;
				ret.dividend       = 0;
				ret.buy            = amount;
				ret.sell           = 0;
				ret.sellCost       = 0;
				ret.sellGain       = 0;
				ret.code           = code;
				ret.comment        = comment;
				
				return ret;
			}
		}
		private static class SELL_STOCK implements BiFunction<Context, AccountHistory, AccountReportJPY> {
			@Override
			public AccountReportJPY apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = accountHistory.amount.intValue();
				var code    = accountHistory.code;
				var name    = accountHistory.comment;
				var units   = accountHistory.units.intValue();
				var comment = accountHistory.comment;
				
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
				var ret = new AccountReportJPY();
				
				ret.date           = date;
				ret.deposit        = 0;
				ret.withdraw       = 0;
				ret.fundTotal      = context.fundTotal;
				ret.cashTotal      = context.cashTotal;
				ret.stockValue     = context.portfolio.valueAsOf(date);
				ret.stockCost      = context.stockCost;
				ret.unrealizedGain = (ret.stockValue <= 0) ? 0 : (ret.stockValue - ret.stockCost);
				ret.realizedGain   = context.realizedGain;
				ret.dividend       = 0;
				ret.buy            = 0;
				ret.sell           = amount;
				ret.sellCost       = sellCost;
				ret.sellGain       = gain;
				ret.code           = code;
				ret.comment        = comment;
				
				return ret;
			}
		}
		private static class DEPOSIT_STOCK implements BiFunction<Context, AccountHistory, AccountReportJPY> {
			@Override
			public AccountReportJPY apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = accountHistory.amount.intValue();
				var code    = accountHistory.code;
				var name    = accountHistory.comment;
				var units   = accountHistory.units.intValue();
				var comment = accountHistory.comment;

				// update portfolio
				context.portfolio.getHolding(code, name).buy(date, units, 0);
				
				// update context
				// fundTotal = cashTotal + stockCost

				// build report
				var ret = new AccountReportJPY();
				
				ret.date           = date;
				ret.deposit        = 0;
				ret.withdraw       = 0;
				ret.fundTotal      = context.fundTotal;
				ret.cashTotal      = context.cashTotal;
				ret.stockValue     = context.portfolio.valueAsOf(date);
				ret.stockCost      = context.stockCost;
				ret.unrealizedGain = (ret.stockValue <= 0) ? 0 : (ret.stockValue - ret.stockCost);
				ret.realizedGain   = context.realizedGain;
				ret.dividend       = 0;
				ret.buy            = amount;
				ret.sell           = 0;
				ret.sellCost       = 0;
				ret.sellGain       = 0;
				ret.code           = code;
				ret.comment        = "入庫：" + comment;
				
				return ret;
			}
		}
		private static class BUY_FUND implements BiFunction<Context, AccountHistory, AccountReportJPY> {
			@Override
			public AccountReportJPY apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = accountHistory.amount.negate().intValue();
				var code    = accountHistory.code;
				var name    = accountHistory.comment;
				var units   = accountHistory.units.intValue();
				var comment = accountHistory.comment;
				
				var priceFactor = accountHistory.unitPrice.multiply(BigDecimal.valueOf(units)).divideToIntegralValue(BigDecimal.valueOf(amount)).round(new MathContext(3, RoundingMode.UP));
//				logger.info("priceFactor  {}  {}", code, priceFactor.toPlainString());
				
				
				// update portfolio
				context.portfolio.getHolding(code, name, priceFactor).buy(date, units, amount);
				
				// update context
				// fundTotal = cashTotal + stockCost
				context.cashTotal -= amount;
				context.stockCost += amount;

				// build report
				var ret = new AccountReportJPY();
				
				ret.date           = date;
				ret.deposit        = 0;
				ret.withdraw       = 0;
				ret.fundTotal      = context.fundTotal;
				ret.cashTotal      = context.cashTotal;
				ret.stockValue     = context.portfolio.valueAsOf(date);
				ret.stockCost      = context.stockCost;
				ret.unrealizedGain = (ret.stockValue <= 0) ? 0 : (ret.stockValue - ret.stockCost);
				ret.realizedGain   = context.realizedGain;
				ret.dividend       = 0;
				ret.buy            = amount;
				ret.sell           = 0;
				ret.sellCost       = 0;
				ret.sellGain       = 0;
				ret.code           = code;
				ret.comment        = comment;
				
				return ret;
			}
		}
		private static class SELL_FUND implements BiFunction<Context, AccountHistory, AccountReportJPY> {
			@Override
			public AccountReportJPY apply(Context context, AccountHistory accountHistory) {
				var date    = accountHistory.settlementDate;
				var amount  = accountHistory.amount.intValue();
				var code    = accountHistory.code;
				var name    = accountHistory.comment;
				var units   = accountHistory.units.intValue();
				var comment = accountHistory.comment;
				
				var priceFactor = accountHistory.unitPrice.multiply(BigDecimal.valueOf(units)).divideToIntegralValue(BigDecimal.valueOf(amount)).round(new MathContext(3, RoundingMode.UP));
//				logger.info("priceFactor  {}  {}", code, priceFactor.toPlainString());
				
				// update portfolio
				var sellCost = context.portfolio.getHolding(code, name, priceFactor).sell(date, units, amount);
				var gain     = amount - sellCost;

				// update context
				// fundTotal = cashTotal + stockCost
				context.fundTotal    += amount - sellCost;
				context.cashTotal    += amount;
				context.stockCost    -= sellCost;
				context.realizedGain += gain;
				
				// build report
				var ret = new AccountReportJPY();
				
				ret.date           = date;
				ret.deposit        = 0;
				ret.withdraw       = 0;
				ret.fundTotal      = context.fundTotal;
				ret.cashTotal      = context.cashTotal;
				ret.stockValue     = context.portfolio.valueAsOf(date);
				ret.stockCost      = context.stockCost;
				ret.unrealizedGain = (ret.stockValue <= 0) ? 0 : (ret.stockValue - ret.stockCost);
				ret.realizedGain   = context.realizedGain;
				ret.dividend       = 0;
				ret.buy            = 0;
				ret.sell           = amount;
				ret.sellCost       = sellCost;
				ret.sellGain       = gain;
				ret.code           = code;
				ret.comment        = comment;
				
				return ret;
			}
		}

	}
	
	private static AccountReportJPY reportAsOf(Context context, LocalDate date) {
		// update context
		// fundTotal = cashTotal + stockCost
		
		// build report
		var ret = new AccountReportJPY();
		
		ret.date           = date;
		ret.deposit        = 0;
		ret.withdraw       = 0;
		ret.fundTotal      = context.fundTotal;
		ret.cashTotal      = context.cashTotal;
		ret.stockValue     = context.portfolio.valueAsOf(date);
		ret.stockCost      = context.stockCost;
		ret.unrealizedGain = (ret.stockValue <= 0) ? 0 : (ret.stockValue - ret.stockCost);
		ret.realizedGain   = context.realizedGain;
		ret.dividend       = 0;
		ret.buy            = 0;
		ret.sell           = 0;
		ret.sellCost       = 0;
		ret.sellGain       = 0;
		ret.code           = "";
		ret.comment        = "";
		
		return ret;
	}

	private static List<AccountReportJPY> getAccountReportList() {
		var ret = new ArrayList<AccountReportJPY>();
		
		var context = new Context();

		var list = StorageRakuten.AccountHistory.getList().stream().filter(o -> o.currency == Currency.JPY).collect(Collectors.toList());
//		var map  = list.stream().collect(Collectors.groupingBy(o -> o.settlementDate, TreeMap::new, Collectors.toCollection(ArrayList::new)));
		
		// convert AccountHistory to AccountReportJPY
		for(var e: list) {
			var key = new TransactionAsset(e);
			var function = functionMap.get(key);
			if (function == null) {
				logger.info("no function  {}  {}", key);
				continue;
			}
			
			ret.add(function.apply(context, e));
		}
		
		// add line of today
		ret.add(reportAsOf(context, LocalDate.now()));
		
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
				throw new UnexpectedException("Unexpected stockCost value");
			}
		}
		
		return ret;
	}
	
}

package yokwe.finance.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import yokwe.finance.trade2.Portfolio;
import yokwe.finance.trade2.Transaction;
import yokwe.finance.trade2.Transaction.Currency;
import yokwe.finance.trade2.rakuten.StorageRakuten;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateAccountRakuten {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_TEMPLATE  = StringUtil.toURLString("data/form/ACCOUNT.ods");

	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
	
	private static void update() {
		var transactionList = StorageRakuten.TransactionList.getList();
		
		var accountJPY = toAccount(transactionList.stream().filter(o -> o.currency == Currency.JPY).toList());
		logger.info("accountJPY  {}", accountJPY.size());
		var accountUSD = toAccount(transactionList.stream().filter(o -> o.currency == Currency.USD).toList());
		logger.info("accountUSD  {}", accountUSD.size());
		
		// remove entry older than 1 year
		accountJPY.removeIf(o -> o.date.isBefore(LocalDate.now().minusYears(1)));
		accountUSD.removeIf(o -> o.date.isBefore(LocalDate.now().minusYears(1)));
		
		generateReport(accountJPY, AccountUSD.toAccountUSD(accountUSD));
	}
	private static void generateReport(List<Account> listJPY, List<AccountUSD> listUSD) {
		String urlReport;
		{
			String timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
			String name       = String.format("account-rakuten-%s.ods", timestamp);
			String pathReport = StorageReport.storage.getPath("account-rakuten", name);
			
			urlReport  = StringUtil.toURLString(pathReport);
		}

		logger.info("urlReport {}", urlReport);
		logger.info("docLoad   {}", URL_TEMPLATE);
		try {
			// start LibreOffice process
			LibreOffice.initialize();
			
			SpreadSheet docLoad = new SpreadSheet(URL_TEMPLATE, true);
			SpreadSheet docSave = new SpreadSheet();
			
			{
				String sheetNameOld = Sheet.getSheetName(Account.class);
				String sheetNameNew = "account-jpy";
				logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, listJPY.size());
				docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
				Sheet.fillSheet(docSave, listJPY, sheetNameOld);
				docSave.renameSheet(sheetNameOld, sheetNameNew);
			}
			{
				String sheetNameOld = Sheet.getSheetName(AccountUSD.class);
				String sheetNameNew = "account-usd";
				logger.info("sheet     {}  {}  {}", sheetNameOld, sheetNameNew, listUSD.size());
				docSave.importSheet(docLoad, sheetNameOld, docSave.getSheetCount());
				Sheet.fillSheet(docSave, listUSD, sheetNameOld);
				docSave.renameSheet(sheetNameOld, sheetNameNew);
			}
			
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
	
	private static List<Account> toAccount(List<Transaction> transactionList) {
		var context = new Context();
		
		// map needs to use TreeMap for entrySet ordering
		var map = transactionList.stream().collect(Collectors.groupingBy(o -> o.settlementDate, TreeMap::new, Collectors.toCollection(ArrayList::new)));
		
		var ret = new ArrayList<Account>();
		
		LocalDate lastDate = null;
		for(var entry: map.entrySet()) {
			var date    = entry.getKey();
			var mapList = entry.getValue();
			
			// fill gap
			for(;;) {
				if (lastDate == null) break;
				lastDate = lastDate.plusDays(1);
				if (!lastDate.isBefore(date)) break;
				ret.add(reportAsOf(context, lastDate));
			}
			lastDate = date;
			
			for(var e: mapList) {
				var accountReport = toAccount(context, e);
				if (accountReport != null) ret.add(accountReport);
			}
		}
		
		// fill gap to today
		{
			var today = LocalDate.now();
			for(;;) {
				if (lastDate == null) break;
				lastDate = lastDate.plusDays(1);
				if (lastDate.isAfter(today)) break;
				ret.add(reportAsOf(context, lastDate));
			}
		}
		
		return ret;
	}
	
	private static Account getAccount(Context context, LocalDate date, String code, String comment) {
		var ret = new Account();
		
		ret.date           = date;
		ret.fundTotal      = context.fundTotal;
		ret.cashTotal      = context.cashTotal;
		ret.stockValue     = context.portfolio.valueAsOf(date);
		ret.stockCost      = context.stockCost;
		ret.unrealizedGain = (ret.stockValue <= 0) ? 0 : (ret.stockValue - ret.stockCost);
		ret.realizedGain   = context.realizedGain;
		ret.code           = code;
		ret.comment        = comment;

		return ret;
	}
	private static Account getAccount(Context context, Transaction transaction) {
		return getAccount(context, transaction.settlementDate, transaction.code, transaction.comment);
	}
	
	private static Account reportAsOf(Context context, LocalDate date) {
		return getAccount(context, date, "", "");
	}
	
	
	private static final Map<Transaction.Type, Map<Transaction.Asset, BiFunction<Context, Transaction, Account>>> typeMap = Map.ofEntries(
		Map.entry(Transaction.Type.DEPOSIT, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH,     new DEPOSIT()),
			Map.entry(Transaction.Asset.STOCK_JP, new DEPOSTI_STOCK()),
			Map.entry(Transaction.Asset.STOCK_US, new DEPOSTI_STOCK())
		)),
		Map.entry(Transaction.Type.WITHDRAW, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH, new WITHDRAW())
		)),
		Map.entry(Transaction.Type.DEPOSIT_TRANSFER, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH, new DEPOSIT())
		)),
		Map.entry(Transaction.Type.WITHDRAW_TRANSFER, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH, new WITHDRAW())
		)),
		Map.entry(Transaction.Type.DIVIDEND, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH,   new DIVIDEND_CASH()),
			Map.entry(Transaction.Asset.MMF_US, new DIVIDEND_MMF_US())
		)),
		Map.entry(Transaction.Type.TAX, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH, new TAX())
		)),
		Map.entry(Transaction.Type.TAX_REFUND, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH, new TAX_REFUND())
		)),
		Map.entry(Transaction.Type.BUY, Map.ofEntries(
			Map.entry(Transaction.Asset.STOCK_JP, new BUY()),
			Map.entry(Transaction.Asset.FUND_JP,  new BUY()),
			Map.entry(Transaction.Asset.STOCK_US, new BUY()),
			Map.entry(Transaction.Asset.BOND_US,  new BUY()),
			Map.entry(Transaction.Asset.MMF_US,   new BUY())
		)),
		Map.entry(Transaction.Type.SELL, Map.ofEntries(
			Map.entry(Transaction.Asset.STOCK_JP, new SELL()),
			Map.entry(Transaction.Asset.FUND_JP,  new SELL()),
			Map.entry(Transaction.Asset.STOCK_US, new SELL()),
			Map.entry(Transaction.Asset.BOND_US,  new SELL()),
			Map.entry(Transaction.Asset.MMF_US,   new SELL())
		)),
		Map.entry(Transaction.Type.BALANCE, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH, new BALANCE())
		))
	);
	private static Account toAccount(Context context, Transaction transaction) {
		var assetMap = typeMap.get(transaction.type);
		if (assetMap == null) logger.warn("unexpected  type  {}  asset  {}", transaction.type, transaction.asset);
		var func = assetMap.get(transaction.asset);
		if (func == null) logger.warn("unexpected  type  {}  asset  {}", transaction.type, transaction.asset);
		return func.apply(context, transaction);
	}
	//
	private static class DEPOSIT implements BiFunction<Context, Transaction, Account> {
		@Override
		public Account apply(Context context, Transaction transaction) {
			var amount  = transaction.amount;

			// update context
			// fundTotal = cashTotal + stockCost
			context.fundTotal += amount;
			context.cashTotal += amount;
			
			var ret = getAccount(context, transaction);
			
			ret.deposit = amount;
			
			return ret;
		}
	}
	private static class WITHDRAW implements BiFunction<Context, Transaction, Account> {
		@Override
		public Account apply(Context context, Transaction transaction) {
			var amount  = transaction.amount;
			
			// update context
			// fundTotal = cashTotal + stockCost
			context.fundTotal -= amount;
			context.cashTotal -= amount;
			
			var ret = getAccount(context, transaction);
			
			ret.withdraw = amount;
			
			return ret;
		}
	}
	private static class DEPOSTI_STOCK implements BiFunction<Context, Transaction, Account> {
		@Override
		public Account apply(Context context, Transaction transaction) {
			var amount  = transaction.amount;
			
			// update portfolio
			transaction.amount = 0;
			context.portfolio.buy(transaction);
			transaction.amount = amount;
			
			// update context
			// fundTotal = cashTotal + stockCost
			
			var ret = getAccount(context, transaction);
			
			ret.buy  = amount;
			
			return ret;
		}
	}
	private static class DIVIDEND_CASH implements BiFunction<Context, Transaction, Account> {
		@Override
		public Account apply(Context context, Transaction transaction) {
			var amount = transaction.amount;
			
			// update context
			// fundTotal = cashTotal + stockCost
			context.fundTotal    += amount;
			context.cashTotal    += amount;
			context.realizedGain += amount;
			
			var ret = getAccount(context, transaction);
			
			ret.dividend = amount;
			
			return ret;
		}
	}
	private static class DIVIDEND_MMF_US implements BiFunction<Context, Transaction, Account> {
		@Override
		public Account apply(Context context, Transaction transaction) {
			var amount = transaction.amount;
			
			// update portfolio
			{
				transaction.amount = 0;
				context.portfolio.buy(transaction);
				transaction.amount = amount;
			}
			
			// update context
			// fundTotal = cashTotal + stockCost
			context.realizedGain += amount;
			
			var ret = getAccount(context, transaction);
			
			ret.dividend = amount;
			
			return ret;
		}
	}
	private static class TAX implements BiFunction<Context, Transaction, Account> {
		@Override
		public Account apply(Context context, Transaction transaction) {
			var amount  = transaction.amount;
			
			// update context
			// fundTotal = cashTotal + stockCost
			context.fundTotal    -= amount;
			context.cashTotal    -= amount;
			context.realizedGain -= amount;
			
			var ret = getAccount(context, transaction);
			
			ret.withdraw = amount;
			
			return ret;
		}
	}
	private static class TAX_REFUND implements BiFunction<Context, Transaction, Account> {
		@Override
		public Account apply(Context context, Transaction transaction) {
			var amount  = transaction.amount;
			
			// update context
			// fundTotal = cashTotal + stockCost
			context.fundTotal    += amount;
			context.cashTotal    += amount;
			context.realizedGain += amount;
			
			var ret = getAccount(context, transaction);
			
			ret.deposit = transaction.amount;
			
			return ret;
		}
	}
	private static class BUY implements BiFunction<Context, Transaction, Account> {
		@Override
		public Account apply(Context context, Transaction transaction) {			
			// update portfolio
			context.portfolio.buy(transaction);
			
			// update context
			// fundTotal = cashTotal + stockCost
			var amount  = transaction.amount;
			context.cashTotal -= amount;
			context.stockCost += amount;
			
			var ret = getAccount(context, transaction);
			
			ret.buy = amount;
			
			return ret;
		}
	}
	//
	private static class SELL implements BiFunction<Context, Transaction, Account> {
		@Override
		public Account apply(Context context, Transaction transaction) {
			var amount  = transaction.amount;
			
			// update portfolio
			var sellCost = context.portfolio.sell(transaction);
			var gain     = amount - sellCost;

			// update context
			// fundTotal = cashTotal + stockCost
			context.fundTotal    += amount - sellCost;
			context.cashTotal    += amount;
			context.stockCost    -= sellCost;
			context.realizedGain += gain;
			
			var ret = getAccount(context, transaction);
			
			ret.sell     = amount;
			ret.sellCost = sellCost;
			ret.sellGain = gain;
			
			return ret;
		}
	}
	//
	private static class BALANCE implements BiFunction<Context, Transaction, Account> {
		@Override
		public Account apply(Context context, Transaction transaction) {
			// sanity check
			var balance  = transaction.amount;
			if (balance != context.cashTotal) {
				logger.error("balance not match");
				logger.error("  {}  {}  {}  --  {}", transaction.settlementDate, transaction.currency, context.cashTotal, balance);
				throw new UnexpectedException("balance not match");
			}
			return null;
		}
	}

}

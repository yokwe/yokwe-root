package yokwe.finance.trade2.rakuten;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import yokwe.finance.trade2.AccountReportJPY;
import yokwe.finance.trade2.Transaction;
import yokwe.finance.trade2.Transaction.Currency;
import yokwe.finance.trade2.rakuten.UpdateAccountReport.Context;

public class UpdateAccountReportJPY {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static List<AccountReportJPY> toAccountReportJPY(List<Transaction> transactionList) {
		var context = new Context();
		
		// map needs to use TreeMap for entrySet ordering
		var map = transactionList.stream().filter(o -> o.currency == Currency.JPY).
			collect(Collectors.groupingBy(o -> o.settlementDate, TreeMap::new, Collectors.toCollection(ArrayList::new)));
		
		var ret = new ArrayList<AccountReportJPY>();
		
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
				ret.add(toAccountReportJPY(context, e));
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
	
	public static AccountReportJPY reportAsOf(Context context, LocalDate date) {
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
	
	
	private static final Map<Transaction.Type, Map<Transaction.Asset, BiFunction<Context, Transaction, AccountReportJPY>>> typeMap = Map.ofEntries(
		Map.entry(Transaction.Type.DEPOSIT, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH,     new DEPOSTI_CASH()),
			Map.entry(Transaction.Asset.STOCK_JP, new DEPOSTI_STOCK_JP())
		)),
		Map.entry(Transaction.Type.WITHDRAW, Map.ofEntries(
				Map.entry(Transaction.Asset.CASH, new WITHDRAW_CASH())
		)),
		Map.entry(Transaction.Type.DEPOSIT_TRANSFER, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH, new DEPOSTI_CASH())
		)),
		Map.entry(Transaction.Type.WITHDRAW_TRANSFER, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH, new WITHDRAW_CASH())
		)),
		Map.entry(Transaction.Type.DIVIDEND, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH, new DIVIDEND_CASH())
		)),
		Map.entry(Transaction.Type.TAX, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH, new TAX_CASH())
		)),
		Map.entry(Transaction.Type.TAX_REFUND, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH, new TAX_REFUND_CASH())
		)),
		Map.entry(Transaction.Type.BUY, Map.ofEntries(
			Map.entry(Transaction.Asset.STOCK_JP, new BUY()),
			Map.entry(Transaction.Asset.FUND_JP,  new BUY())
		)),
		Map.entry(Transaction.Type.SELL, Map.ofEntries(
			Map.entry(Transaction.Asset.STOCK_JP, new SELL()),
			Map.entry(Transaction.Asset.FUND_JP,  new SELL())
		))
	);
	private static AccountReportJPY toAccountReportJPY(Context context, Transaction transaction) {
		var assetMap = typeMap.get(transaction.type);
		if (assetMap == null) logger.warn("unexpected  type  {}  asset  {}", transaction.type, transaction.asset);
		var func = assetMap.get(transaction.asset);
		if (func == null) logger.warn("unexpected  type  {}  asset  {}", transaction.type, transaction.asset);
		return func.apply(context, transaction);
	}
	//
	private static AccountReportJPY deposit(Context context, Transaction transaction) {
		var date    = transaction.settlementDate;
		var amount  = transaction.amount;
		var comment = transaction.comment;

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
	private static AccountReportJPY withdraw(Context context, Transaction transaction) {
		var date    = transaction.settlementDate;
		var amount  = transaction.amount;
		var comment = transaction.comment;

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
	private static AccountReportJPY buy(Context context, Transaction transaction) {
		var date   = transaction.settlementDate;
		var amount = transaction.amount;
		var code   = transaction.code;
		var name   = transaction.comment;
		
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
		ret.comment        = name;
		
		return ret;
	}
	private static AccountReportJPY sell(Context context, Transaction transaction, int sellCost) {
		var date   = transaction.settlementDate;
		var amount = transaction.amount;
		var code   = transaction.code;
		var name   = transaction.comment;
		
		var gain     = amount - sellCost;

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
		ret.comment        = name;
		
		return ret;
	}
	//
	private static class DEPOSTI_CASH implements BiFunction<Context, Transaction, AccountReportJPY> {
		@Override
		public AccountReportJPY apply(Context context, Transaction transaction) {
			var amount  = transaction.amount;

			// update context
			// fundTotal = cashTotal + stockCost
			context.fundTotal += amount;
			context.cashTotal += amount;
			
			return deposit(context, transaction);
		}
	}
	private static class WITHDRAW_CASH implements BiFunction<Context, Transaction, AccountReportJPY> {
		@Override
		public AccountReportJPY apply(Context context, Transaction transaction) {
			var amount  = transaction.amount;
			
			// update context
			// fundTotal = cashTotal + stockCost
			context.fundTotal -= amount;
			context.cashTotal -= amount;
			
			return withdraw(context, transaction);
		}
	}
	private static class DEPOSTI_STOCK_JP implements BiFunction<Context, Transaction, AccountReportJPY> {
		@Override
		public AccountReportJPY apply(Context context, Transaction transaction) {
			var amount  = transaction.amount;
			
			// update portfolio
			transaction.amount = 0;
			context.portfolio.buy(transaction);
			transaction.amount = amount;
			
			// update context
			// fundTotal = cashTotal + stockCost
			//context.cashTotal -= amount;
			//context.stockCost += amount;
			
			return buy(context, transaction);
		}
	}
	private static class DIVIDEND_CASH implements BiFunction<Context, Transaction, AccountReportJPY> {
		@Override
		public AccountReportJPY apply(Context context, Transaction transaction) {
			var date   = transaction.settlementDate;
			var amount = transaction.amount;
			var code   = transaction.code;
			var name   = transaction.comment;
			
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
	private static class TAX_CASH implements BiFunction<Context, Transaction, AccountReportJPY> {
		@Override
		public AccountReportJPY apply(Context context, Transaction transaction) {
			var amount  = transaction.amount;
			
			// update context
			// fundTotal = cashTotal + stockCost
			context.fundTotal    -= amount;
			context.cashTotal    -= amount;
			context.realizedGain -= amount;
			
			return withdraw(context, transaction);
		}
	}
	private static class TAX_REFUND_CASH implements BiFunction<Context, Transaction, AccountReportJPY> {
		@Override
		public AccountReportJPY apply(Context context, Transaction transaction) {
			var amount  = transaction.amount;
			
			// update context
			// fundTotal = cashTotal + stockCost
			context.fundTotal    += amount;
			context.cashTotal    += amount;
			context.realizedGain += amount;
			
			return deposit(context, transaction);
		}
	}
	private static class BUY implements BiFunction<Context, Transaction, AccountReportJPY> {
		@Override
		public AccountReportJPY apply(Context context, Transaction transaction) {
			var amount  = transaction.amount;
			
			// update portfolio
			context.portfolio.buy(transaction);
			
			// update context
			// fundTotal = cashTotal + stockCost
			context.cashTotal -= amount;
			context.stockCost += amount;
			
			return buy(context, transaction);
		}
	}
	//
	private static class SELL implements BiFunction<Context, Transaction, AccountReportJPY> {
		@Override
		public AccountReportJPY apply(Context context, Transaction transaction) {
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
			
			return sell(context, transaction, sellCost);
		}
	}
}

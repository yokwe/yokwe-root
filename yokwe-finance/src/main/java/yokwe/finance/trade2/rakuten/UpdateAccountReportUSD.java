package yokwe.finance.trade2.rakuten;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import yokwe.finance.trade2.AccountReportUSD;
import yokwe.finance.trade2.Transaction;
import yokwe.finance.trade2.Transaction.Currency;
import yokwe.finance.trade2.rakuten.UpdateAccountReport.Context;

public class UpdateAccountReportUSD {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static BigDecimal toDollar(int cent) {
		return new BigDecimal(cent).movePointLeft(2);
	}
	
	public static List<AccountReportUSD> toAccountReportUSD(List<Transaction> transactionList) {
		var context = new Context();
		
		// map needs to use TreeMap for entrySet ordering
		var map = transactionList.stream().filter(o -> o.currency == Currency.USD).
			collect(Collectors.groupingBy(o -> o.settlementDate, TreeMap::new, Collectors.toCollection(ArrayList::new)));
		
		var ret = new ArrayList<AccountReportUSD>();
		
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
				ret.add(toAccountReportUSD(context, e));
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

	public static AccountReportUSD reportAsOf(Context context, LocalDate date) {
		var stockValue = context.portfolio.valueAsOf(date);
		
		// build report
		var ret = new AccountReportUSD();
		
		ret.date           = date;
		ret.deposit        = BigDecimal.ZERO;
		ret.withdraw       = BigDecimal.ZERO;
		ret.fundTotal      = toDollar(context.fundTotal);
		ret.cashTotal      = toDollar(context.cashTotal);
		ret.stockValue     = toDollar(stockValue);
		ret.stockCost      = toDollar(context.stockCost);
		ret.unrealizedGain = (stockValue < 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
		ret.realizedGain   = toDollar(context.realizedGain);
		ret.dividend       = BigDecimal.ZERO;
		ret.buy            = BigDecimal.ZERO;
		ret.sell           = BigDecimal.ZERO;
		ret.sellCost       = BigDecimal.ZERO;
		ret.sellGain       = BigDecimal.ZERO;
		ret.code           = "";
		ret.comment        = "";
		
		return ret;
	}
	
	private static final Map<Transaction.Type, Map<Transaction.Asset, BiFunction<Context, Transaction, AccountReportUSD>>> typeMap = Map.ofEntries(
		Map.entry(Transaction.Type.DEPOSIT_TRANSFER, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH, new DEPOSTI_CASH())
		)),
		Map.entry(Transaction.Type.WITHDRAW_TRANSFER, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH, new WITHDRAW_CASH())
		)),
		Map.entry(Transaction.Type.DIVIDEND, Map.ofEntries(
			Map.entry(Transaction.Asset.CASH, new DIVIDEND_CASH()),
			Map.entry(Transaction.Asset.MMF_US, new DIVIDEND_MMF())
		)),
		Map.entry(Transaction.Type.BUY, Map.ofEntries(
			Map.entry(Transaction.Asset.BOND_US,  new BUY()),
			Map.entry(Transaction.Asset.STOCK_US, new BUY()),
			Map.entry(Transaction.Asset.MMF_US,   new BUY())
		)),
		Map.entry(Transaction.Type.SELL, Map.ofEntries(
			Map.entry(Transaction.Asset.BOND_US,  new SELL()),
			Map.entry(Transaction.Asset.STOCK_US, new SELL()),
			Map.entry(Transaction.Asset.MMF_US,   new SELL())
		))
	);
	
	private static AccountReportUSD toAccountReportUSD(Context context, Transaction transaction) {
		var assetMap = typeMap.get(transaction.type);
		if (assetMap == null) logger.warn("unexpected  type  {}  asset  {}", transaction.type, transaction.asset);
		var func = assetMap.get(transaction.asset);
		if (func == null) logger.warn("unexpected  type  {}  asset  {}", transaction.type, transaction.asset);
		return func.apply(context, transaction);
	}
	
	private static AccountReportUSD withdraw(Context context, Transaction transaction) {
		var date    = transaction.settlementDate;
		var amount  = transaction.amount;
		var comment = transaction.comment;
		
		var stockValue = context.portfolio.valueAsOf(date);
		
		var ret = new AccountReportUSD();
		
		ret.date           = date;
		ret.deposit        = BigDecimal.ZERO;
		ret.withdraw       = toDollar(amount);
		ret.fundTotal      = toDollar(context.fundTotal);
		ret.cashTotal      = toDollar(context.cashTotal);
		ret.stockValue     = toDollar(stockValue);
		ret.stockCost      = toDollar(context.stockCost);
		ret.unrealizedGain = (stockValue < 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
		ret.realizedGain   = toDollar(context.realizedGain);
		ret.dividend       = BigDecimal.ZERO;
		ret.buy            = BigDecimal.ZERO;
		ret.sell           = BigDecimal.ZERO;
		ret.sellCost       = BigDecimal.ZERO;
		ret.sellGain       = BigDecimal.ZERO;
		ret.code           = "";
		ret.comment        = comment;
		
		return ret;
	}
	private static AccountReportUSD deposit(Context context, Transaction transaction) {
		var date    = transaction.settlementDate;
		var amount  = transaction.amount;
		var comment = transaction.comment;
		
		var stockValue = context.portfolio.valueAsOf(date);

		var ret = new AccountReportUSD();
		
		ret.date           = date;
		ret.deposit        = toDollar(amount);
		ret.withdraw       = BigDecimal.ZERO;
		ret.fundTotal      = toDollar(context.fundTotal);
		ret.cashTotal      = toDollar(context.cashTotal);
		ret.stockValue     = toDollar(stockValue);
		ret.stockCost      = toDollar(context.stockCost);
		ret.unrealizedGain = (stockValue < 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
		ret.realizedGain   = toDollar(context.realizedGain);
		ret.dividend       = BigDecimal.ZERO;
		ret.buy            = BigDecimal.ZERO;
		ret.sell           = BigDecimal.ZERO;
		ret.sellCost       = BigDecimal.ZERO;
		ret.sellGain       = BigDecimal.ZERO;
		ret.code           = "";
		ret.comment        = comment;
		
		return ret;
	}
	private static class DEPOSTI_CASH implements BiFunction<Context, Transaction, AccountReportUSD> {
		@Override
		public AccountReportUSD apply(Context context, Transaction transaction) {
			var amount  = transaction.amount;
			
			// update context
			// fundTotal = cashTotal + stockCost
			context.fundTotal += amount;
			context.cashTotal += amount;
			
			return deposit(context, transaction);
		}
	}
	private static class WITHDRAW_CASH implements BiFunction<Context, Transaction, AccountReportUSD> {
		@Override
		public AccountReportUSD apply(Context context, Transaction transaction) {
			var amount  = transaction.amount;
			
			// update context
			// fundTotal = cashTotal + stockCost
			context.fundTotal -= amount;
			context.cashTotal -= amount;
			
			return withdraw(context, transaction);
		}
	}
	private static class DIVIDEND_CASH implements BiFunction<Context, Transaction, AccountReportUSD> {
		@Override
		public AccountReportUSD apply(Context context, Transaction transaction) {
			var date   = transaction.settlementDate;
			var amount = transaction.amount;
			var code   = transaction.code;
			var name   = transaction.comment;
			
			// update context
			// fundTotal = cashTotal + stockCost
			context.fundTotal    += amount;
			context.cashTotal    += amount;
			context.realizedGain += amount;
			
			var stockValue = context.portfolio.valueAsOf(date);
			
			var ret = new AccountReportUSD();
			
			ret.date           = date;
			ret.deposit        = BigDecimal.ZERO;
			ret.withdraw       = BigDecimal.ZERO;
			ret.fundTotal      = toDollar(context.fundTotal);
			ret.cashTotal      = toDollar(context.cashTotal);
			ret.stockValue     = toDollar(stockValue);
			ret.stockCost      = toDollar(context.stockCost);
			ret.unrealizedGain = (stockValue < 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
			ret.realizedGain   = toDollar(context.realizedGain);
			ret.dividend       = toDollar(amount);
			ret.buy            = BigDecimal.ZERO;
			ret.sell           = BigDecimal.ZERO;
			ret.sellCost       = BigDecimal.ZERO;
			ret.sellGain       = BigDecimal.ZERO;
			ret.code           = code;
			ret.comment        = name;
			
			return ret;
		}
	}
	private static class DIVIDEND_MMF implements BiFunction<Context, Transaction, AccountReportUSD> {
		@Override
		public AccountReportUSD apply(Context context, Transaction transaction) {
			var date   = transaction.settlementDate;
			var amount = transaction.amount;
			var code   = transaction.code;
			var name   = transaction.comment;
			
			// update portfolio
			{
				transaction.amount = 0;
				context.portfolio.buy(transaction);
				transaction.amount = amount;
			}
			
			// update context
			// fundTotal = cashTotal + stockCost
			context.fundTotal    += amount;
			context.realizedGain += amount;
			
			var stockValue = context.portfolio.valueAsOf(date);
			
			var ret = new AccountReportUSD();
			
			ret.date           = date;
			ret.deposit        = BigDecimal.ZERO;
			ret.withdraw       = BigDecimal.ZERO;
			ret.fundTotal      = toDollar(context.fundTotal);
			ret.cashTotal      = toDollar(context.cashTotal);
			ret.stockValue     = toDollar(stockValue);
			ret.stockCost      = toDollar(context.stockCost);
			ret.unrealizedGain = (stockValue < 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
			ret.realizedGain   = toDollar(context.realizedGain);
			ret.dividend       = toDollar(amount);
			ret.buy            = BigDecimal.ZERO;
			ret.sell           = BigDecimal.ZERO;
			ret.sellCost       = BigDecimal.ZERO;
			ret.sellGain       = BigDecimal.ZERO;
			ret.code           = code;
			ret.comment        = name;
			
			return ret;
		}
	}
	private static class BUY implements BiFunction<Context, Transaction, AccountReportUSD> {
		@Override
		public AccountReportUSD apply(Context context, Transaction transaction) {
			var date   = transaction.settlementDate;
			var amount = transaction.amount;
			var code   = transaction.code;
			var name   = transaction.comment;
			
			// update portfolio
			context.portfolio.buy(transaction);
			
			// update context
			// fundTotal = cashTotal + stockCost
			context.cashTotal -= amount;
			context.stockCost += amount;
			
			var stockValue = context.portfolio.valueAsOf(date);
			
			// build report
			var ret = new AccountReportUSD();
			
			ret.date           = date;
			ret.deposit        = BigDecimal.ZERO;
			ret.withdraw       = BigDecimal.ZERO;
			ret.fundTotal      = toDollar(context.fundTotal);
			ret.cashTotal      = toDollar(context.cashTotal);
			ret.stockValue     = toDollar(stockValue);
			ret.stockCost      = toDollar(context.stockCost);
			ret.unrealizedGain = (stockValue < 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
			ret.realizedGain   = toDollar(context.realizedGain);
			ret.dividend       = BigDecimal.ZERO;
			ret.buy            = toDollar(amount);
			ret.sell           = BigDecimal.ZERO;
			ret.sellCost       = BigDecimal.ZERO;
			ret.sellGain       = BigDecimal.ZERO;
			ret.code           = code;
			ret.comment        = name;
			
			return ret;
		}
	}
	private static class SELL implements BiFunction<Context, Transaction, AccountReportUSD> {
		@Override
		public AccountReportUSD apply(Context context, Transaction transaction) {
			var date   = transaction.settlementDate;
			var amount = transaction.amount;
			var code   = transaction.code;
			var name   = transaction.comment;
			
			// update portfolio
			var sellCost = context.portfolio.sell(transaction);
			var gain     = amount - sellCost;
			
			// update context
			// fundTotal = cashTotal + stockCost
			context.cashTotal    += amount;
			context.stockCost    -= amount;
			context.realizedGain += gain;
			
			var stockValue = context.portfolio.valueAsOf(date);
			
			// build report
			var ret = new AccountReportUSD();
			
			ret.date           = date;
			ret.deposit        = BigDecimal.ZERO;
			ret.withdraw       = BigDecimal.ZERO;
			ret.fundTotal      = toDollar(context.fundTotal);
			ret.cashTotal      = toDollar(context.cashTotal);
			ret.stockValue     = toDollar(stockValue);
			ret.stockCost      = toDollar(context.stockCost);
			ret.unrealizedGain = (stockValue < 0) ? BigDecimal.ZERO : ret.stockValue.subtract(ret.stockCost);
			ret.realizedGain   = toDollar(context.realizedGain);
			ret.dividend       = BigDecimal.ZERO;
			ret.buy            = BigDecimal.ZERO;
			ret.sell           = toDollar(amount);
			ret.sellCost       = toDollar(sellCost);
			ret.sellGain       = toDollar(gain);
			ret.code           = code;
			ret.comment        = name;
			
			return ret;
		}
	}
}

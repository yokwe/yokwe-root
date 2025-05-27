package yokwe.finance.trade.nikko;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.trade.AccountHistory;
import yokwe.finance.trade.rakuten.StorageRakuten;
import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;

public class UpdateAccountHistory {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final LocalDate TODAY        = LocalDate.now();

	private static final File    DIR_DOWNLOAD = StorageNikko.storage.getFile("download");

	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
	
	private static void update() {
		var oldList = StorageNikko.AccountHistory.getList();
		logger.info("read  {}  {}", oldList.size(), StorageRakuten.AccountHistory.getFile().getName());
		
		{
			var files = DIR_DOWNLOAD.listFiles((d, n) -> n.startsWith("Torireki") && n.endsWith(".csv"));
			Arrays.sort(files);
			for(var file: files) {
				var name = file.getName();
				logger.info("file  {}", file.getName());
				
				var array = CSVUtil.read(Torireki.class).file(file).toArray(Torireki[]::new);
				logger.info("read  {}  {}", array.length, name);
				var newList = toAccountHistory(array);
				oldList = merge(oldList, newList);
			}
		}
				
		logger.info("save  {}  {}", oldList.size(), StorageNikko.AccountHistory.getPath());
		StorageNikko.AccountHistory.save(oldList);

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
	
	private static List<AccountHistory> toAccountHistory(Torireki[] array) {
		var ret = new ArrayList<AccountHistory>();
		for(var e: array) {
			var accountHistory = functionMap.get(e.tradeType).apply(e);
			if (accountHistory == null) continue;
			
			if (accountHistory.settlementDate.isBefore(TODAY)) {
				ret.add(accountHistory);
			} else {
				ret.add(accountHistory);
				logger.warn("settlementDate is future date  {}", accountHistory);
			}
		}
		
		return ret;
	}
	
	private static Map<Torireki.TradeType, Map<Torireki.ProductType, Function<Torireki, AccountHistory>>> functionMap2 = new TreeMap<>();
	static {
//		functionMap2.put(Torireki.TradeType.DEPOSIT, new TreeMap<Torireki.ProductType, Function<Torireki, AccountHistory>>)
	}
	
	private static Map<Torireki.TradeType, Function<Torireki, AccountHistory>> functionMap = new TreeMap<>();
	static {
		functionMap.put(Torireki.TradeType.DEPOSIT,       new TradeTypeFunction.DEPOSIT());
		functionMap.put(Torireki.TradeType.WITHDRAW,      new TradeTypeFunction.WITHDRAW());
		functionMap.put(Torireki.TradeType.TRANSFER,      new TradeTypeFunction.TRANSFER());
		functionMap.put(Torireki.TradeType.BALANCE,       new TradeTypeFunction.BALANCE());
		// stock
		functionMap.put(Torireki.TradeType.RECEIVE_STOCK, new TradeTypeFunction.RECEIVE_STOCK());
		functionMap.put(Torireki.TradeType.BUY,           new TradeTypeFunction.BUY());
		functionMap.put(Torireki.TradeType.REINVESTMENT,  new TradeTypeFunction.REINVESTMENT());
		functionMap.put(Torireki.TradeType.SELL,          new TradeTypeFunction.SELL());
		functionMap.put(Torireki.TradeType.DIVIDEND,      new TradeTypeFunction.DIVIDEND());
		// fund
		functionMap.put(Torireki.TradeType.DIVIDNED_BOND, new TradeTypeFunction.DIVIDNED_BOND());
		functionMap.put(Torireki.TradeType.REDEMPTION,    new TradeTypeFunction.REDEMPTION());
		functionMap.put(Torireki.TradeType.WITHDRAW_MMF,  new TradeTypeFunction.WITHDRAW_MMF());
	}
	
	private static class TradeTypeFunction {
		private static class DEPOSIT implements Function<Torireki, AccountHistory> {
			private static Map<Torireki.ProductType, Function<Torireki, AccountHistory>> functionMap = new TreeMap<>();
			static {
				functionMap.put(Torireki.ProductType.WITHDRAW_DEPOSIT, new ProcuctFunction.WITHDRAW_DEPOSIT());
			}
			private static class ProcuctFunction {
				private static class WITHDRAW_DEPOSIT implements Function<Torireki, AccountHistory> {
					@Override
					public AccountHistory apply(Torireki e) {
						var ret = new AccountHistory();
						
						ret.settlementDate = toLocalDate(e.settlementDate);
						ret.tradeDate      = ret.settlementDate;
						ret.asset          = AccountHistory.Asset.CASH;
						ret.currency       = AccountHistory.Currency.JPY;
						ret.transaction    = AccountHistory.Transaction.DEPOSIT;
						ret.units          = BigDecimal.ZERO;
						ret.unitPrice      = BigDecimal.ZERO;
						ret.amount         = new BigDecimal(e.deposit.replace(",", ""));
						ret.code           = "";
						ret.comment        = e.note;
						
						return ret;
					}
				}
			}
			@Override
			public AccountHistory apply(Torireki e) {
				return functionMap.get(e.productType).apply(e);
			}
		}
		private static class WITHDRAW implements Function<Torireki, AccountHistory> {
			private static Map<Torireki.ProductType, Function<Torireki, AccountHistory>> functionMap = new TreeMap<>();
			static {
				functionMap.put(Torireki.ProductType.WITHDRAW_DEPOSIT, new ProcuctFunction.WITHDRAW_DEPOSIT());
			}
			private static class ProcuctFunction {
				private static class WITHDRAW_DEPOSIT implements Function<Torireki, AccountHistory> {
					@Override
					public AccountHistory apply(Torireki e) {
						var ret = new AccountHistory();
						
						ret.settlementDate = toLocalDate(e.settlementDate);
						ret.tradeDate      = ret.settlementDate;
						ret.asset          = AccountHistory.Asset.CASH;
						ret.currency       = AccountHistory.Currency.JPY;
						ret.transaction    = AccountHistory.Transaction.WITHDRAW;
						ret.units          = BigDecimal.ZERO;
						ret.unitPrice      = BigDecimal.ZERO;
						ret.amount         = new BigDecimal(e.withdraw.replace(",", "")).negate();
						ret.code           = "";
						ret.comment        = e.note;
						
						return ret;
					}
				}
			}
			@Override
			public AccountHistory apply(Torireki e) {
				return functionMap.get(e.productType).apply(e);
			}
		}
		private static class TRANSFER implements Function<Torireki, AccountHistory> {
			private static Map<Torireki.ProductType, Function<Torireki, AccountHistory>> functionMap = new TreeMap<>();
			static {
				functionMap.put(Torireki.ProductType.WITHDRAW_DEPOSIT, new ProcuctFunction.WITHDRAW_DEPOSIT());
			}
			private static class ProcuctFunction {
				private static class WITHDRAW_DEPOSIT implements Function<Torireki, AccountHistory> {
					@Override
					public AccountHistory apply(Torireki e) {
						var ret = new AccountHistory();
						
						ret.settlementDate = toLocalDate(e.settlementDate);
						ret.tradeDate      = ret.settlementDate;
						ret.asset          = AccountHistory.Asset.CASH;
						ret.currency       = AccountHistory.Currency.JPY;
						ret.transaction    = AccountHistory.Transaction.WITHDRAW;
						ret.units          = BigDecimal.ZERO;
						ret.unitPrice      = BigDecimal.ZERO;
						ret.amount         = new BigDecimal(e.withdraw.replace(",", "")).negate();
						ret.code           = "";
						ret.comment        = e.note;
						
						return ret;
					}
				}
			}
			@Override
			public AccountHistory apply(Torireki e) {
				return functionMap.get(e.productType).apply(e);
			}
		}
		private static class BALANCE implements Function<Torireki, AccountHistory> {
			public AccountHistory apply(Torireki e) {
				return null; // NOTHNG TO DO
			}
		}
		// stock
		private static class RECEIVE_STOCK implements Function<Torireki, AccountHistory> {
			private static Map<Torireki.ProductType, Function<Torireki, AccountHistory>> functionMap = new TreeMap<>();
			static {
				functionMap.put(Torireki.ProductType.FOREIGN_STOCK, new ProcuctFunction.FOREIGN_STOCK());
			}
			private static class ProcuctFunction {
				private static class FOREIGN_STOCK implements Function<Torireki, AccountHistory> {
					@Override
					public AccountHistory apply(Torireki e) {
						var ret = new AccountHistory();
						
						ret.settlementDate = toLocalDate(e.settlementDate);
						ret.tradeDate      = toLocalDate(e.settlementDate);
						ret.asset          = AccountHistory.Asset.STOCK;
						ret.currency       = AccountHistory.Currency.JPY;
						ret.transaction    = AccountHistory.Transaction.IMPORT;
						ret.units          = new BigDecimal(e.units.replace(",", ""));
						ret.unitPrice      = BigDecimal.ZERO;
						ret.amount         = BigDecimal.ZERO;
						ret.code           = "";
						ret.comment        = e.name;
						
						if (e.note.equals("通貨:USD")) ret.currency = AccountHistory.Currency.USD;
						
						return ret;
					}
				}
			}
			@Override
			public AccountHistory apply(Torireki e) {
				return functionMap.get(e.productType).apply(e);
			}
		}
		private static class BUY implements Function<Torireki, AccountHistory> {
			public AccountHistory apply(Torireki e) {
				return null; // FIXME
			}
		}
		private static class REINVESTMENT implements Function<Torireki, AccountHistory> {
			public AccountHistory apply(Torireki e) {
				return null; // FIXME
			}
		}
		private static class SELL implements Function<Torireki, AccountHistory> {
			public AccountHistory apply(Torireki e) {
				return null; // FIXME
			}
		}
		private static class DIVIDEND implements Function<Torireki, AccountHistory> {
			public AccountHistory apply(Torireki e) {
				return null; // FIXME
			}
		}
		// func
		private static class DIVIDNED_BOND implements Function<Torireki, AccountHistory> {
			public AccountHistory apply(Torireki e) {
				return null; // FIXME
			}
		}
		private static class REDEMPTION implements Function<Torireki, AccountHistory> {
			public AccountHistory apply(Torireki e) {
				return null; // FIXME
			}
		}
		private static class WITHDRAW_MMF implements Function<Torireki, AccountHistory> {
			public AccountHistory apply(Torireki e) {
				return null; // FIXME
			}
		}
	}
	
	private static final Pattern PAT_DATE = Pattern.compile("([0-9][0-9])/([0-9][0-9])/([0-9][0-9])");
	private static LocalDate toLocalDate(String string) {
		var matcher = PAT_DATE.matcher(string);
		if (matcher.matches()) {
			var y = matcher.group(1);
			var m = matcher.group(2);
			var d = matcher.group(3);
			
			return LocalDate.of(2000 + Integer.valueOf(y), Integer.valueOf(m), Integer.valueOf(d));
		} else {
			logger.error("Unexpeced string");
			logger.error("  string  {}!", string);
			throw new UnexpectedException("Unexpeced string");
		}
	}

}

package yokwe.stock.trade.gmo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.LoggerFactory;

import yokwe.stock.trade.Storage;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class Transaction implements Comparable<Transaction> {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Transaction.class);
	
	public static final String PATH_ACTIVITY = Storage.getPath("activity", "投資活動_gmo.ods");
	public static final String URL_ACTIVITY  = StringUtil.toURLString(PATH_ACTIVITY);
	
	public enum Type {
		JPY_IN, JPY_OUT,
		DIVIDEND,
		BUY, SELL,
		FEE, CHANGE,
	}

	public final Type   type;
	public final String date; // settlement date
	
	public final String symbol;
	public final int    quantity;
	public final int    price;
	public final int    fee;
	public final int    total;   // Actual amount subtract/add from/to account - contains fee
	
	public final int    jpy;
	
	public final String newSymbol;
	public final int    newQuantity;

	private Transaction(
		Type type, String date,
		String symbol, int quantity, int price, int fee, int total,
		int  jpy,
		String newSymbol, int newQuantity) {
		this.type     = type;
		this.date     = date.replace("/", "-");
		
		// Use finance.yahoo.com style stockCode - 4 digits + ".T"
		String stockCode = symbol.replace(".0", ".T");
		
		this.symbol   = stockCode;
		this.quantity = quantity;
		this.price    = price;
		this.fee      = fee;
		this.total    = total;
		
		this.jpy      = jpy;
		
		this.newSymbol   = newSymbol;
		this.newQuantity = newQuantity;
	}
	
	@Override
	public String toString() {
		return String.format("%s %-8s %-8s %4d %5d %5d %7d %7d",
				date, type, symbol, quantity, price, fee, total, jpy);
	}

	@Override
	public int compareTo(Transaction that) {
		int ret = this.date.compareTo(that.date);
		if (ret == 0) ret = this.type.compareTo(that.type);
		if (ret == 0) ret = this.symbol.compareTo(that.symbol);
		return ret;
	}

	private static Transaction jpyIn(String date, int jpy) {
		return new Transaction(Type.JPY_IN, date, "", 0, 0, 0, 0, jpy, "", 0);
	}
	private static Transaction jpyOut(String date, int jpy) {
		return new Transaction(Type.JPY_OUT, date, "", 0, 0, 0, 0, jpy, "", 0);
	}
	private static Transaction buy(String date, String symbol, int quantity, int price, int fee, int total) {
		return new Transaction(Type.BUY, date, symbol, quantity, price, fee, total, 0, "", 0);
	}
	private static Transaction sell(String date, String symbol, int quantity, int price, int fee, int total) {
		return new Transaction(Type.SELL, date, symbol, quantity, price, fee, total, 0, "", 0);
	}
	private static Transaction dividend(String date, String symbol, int quantity, int price, int fee, int total) {
		return new Transaction(Type.DIVIDEND, date, symbol, quantity, price, fee, total, 0, "", 0);
	}
	private static Transaction fee(String date, int jpy) {
		return new Transaction(Type.FEE, date, "", 0, 0, 0, 0, jpy, "", 0);
	}
	private static Transaction change(String date, String symbol, int quantity, String newSymbol, int newQuantity) {
		return new Transaction(Type.CHANGE, date, symbol, quantity, 0, 0, 0, 0, newSymbol, newQuantity);
	}

	public static List<Transaction> getTransactionList(SpreadSheet docActivity) {
		List<Transaction> transactionList = new ArrayList<>();
		
		List<String> sheetNameList = docActivity.getSheetNameList();
		sheetNameList.sort((a, b) -> a.compareTo(b));
		
		// Process Account
		for(String sheetName: sheetNameList) {
			if (!sheetName.matches("20[0-9][0-9]")) continue;

			logger.info("sheetName {}", sheetName);
			
			List<Activity> activityList = Sheet.extractSheet(docActivity, Activity.class, sheetName);
			logger.info("activityList {}", activityList.size());
			
			for(Activity activity: activityList) {
				switch(activity.tradeType) {
				case Activity.TRADE_CONNECT_AUTOMATIC_DEPOSIT:
					transactionList.add(Transaction.jpyIn(activity.settlementDate, activity.settlementPrice));
					break;
				case Activity.TRADE_CONNECT_AUTOMATIC_WITHDRAW:
					transactionList.add(Transaction.jpyOut(activity.settlementDate, activity.settlementPrice));
					break;
				case Activity.TRADE_DEPOSIT:
					transactionList.add(Transaction.jpyIn(activity.settlementDate, activity.settlementPrice));
					break;
				case Activity.TRADE_WITHDRAW:
					transactionList.add(Transaction.jpyOut(activity.settlementDate, activity.settlementPrice));
					break;
				case Activity.TRADE_CASH_TRANSACTION:
					switch (activity.transactionType) {
					case Activity.TRANSACTION_BUY:
						transactionList.add(Transaction.buy(activity.settlementDate, activity.stockCode, activity.tradeAmount, activity.tradeUnitPrice, activity.fee + activity.feeTax, -activity.settlementPrice));
						break;
					case Activity.TRANSACTION_SELL:
						transactionList.add(Transaction.sell(activity.settlementDate, activity.stockCode, activity.tradeAmount, activity.tradeUnitPrice, activity.fee + activity.feeTax, activity.settlementPrice));
						break;
					default:
						logger.error("Unexpected  {}", activity);
						throw new UnexpectedException("Unexpected");
					}
					break;
				case Activity.TRADE_DIVIDEND_DEPOSIT:
					// no information for quantity and price
					// FIXME Are quantity and price used in dividend calculation or reporting?
					transactionList.add(Transaction.dividend(activity.settlementDate, activity.stockCode, 0, 0, 0, activity.settlementPrice));
					break;
				case Activity.TRADE_TRANSFER_TAX_COLLECTION:
					transactionList.add(Transaction.fee(activity.settlementDate, activity.settlementPrice));
					break;
				case Activity.TRADE_TRANSFER_TAX_REFUND:
					transactionList.add(Transaction.fee(activity.settlementDate, activity.settlementPrice));
					break;
				default:
					logger.error("Unexpected  {}", activity);
					throw new UnexpectedException("Unexpected");
				}
			}
		}
		
		Collections.sort(transactionList);
		return transactionList;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		String url = URL_ACTIVITY;
		logger.info("url        {}", url);		
		try (SpreadSheet docActivity = new SpreadSheet(url, true)) {
			List<Transaction> transactionList = getTransactionList(docActivity);
			for(Transaction transaction: transactionList) {
				logger.info("{}", transaction);
			}
			logger.info("transactionList {}", transactionList.size());
		}
		logger.info("STOP");
		System.exit(0);
	}

}

package yokwe.stock.trade.monex;

import java.util.ArrayList;
import java.util.List;

import yokwe.stock.trade.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.DateMap;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.libreoffice.SpreadSheet;

//
// Foreign Exchange Profit and Loss
//

public class FXProfit implements Comparable<FXProfit> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final String PATH = Storage.Monex.getPath("fx-profit.csv");
	public static String getPath() {
		return PATH;
	}
	
	public static void save(List<FXProfit> list) {
		ListUtil.save(FXProfit.class, getPath(), list);
	}
	public static List<FXProfit> getList() {
		return ListUtil.getList(FXProfit.class, getPath());
	}
	
	public enum Kind {
		DEPOSIT,  // deposit of USD
		WITHDRAW, // withdraw of USD
		MINUS,    // decrease of USD
		PLUS,     // increase of USD
	}
	
	public enum Type {
		DEPOSIT (Kind.DEPOSIT),   // deposit of USD
		WITHDRAW(Kind.WITHDRAW),  // withdraw of USD
		
		BUY(Kind.MINUS), // Buying of Stock
		FEE(Kind.MINUS), // Fee of ADR
		
		SELL(Kind.PLUS), // Selling of Stock
		DIV(Kind.PLUS);	// Dividend of Stock
		
		public Kind kind;
		Type(Kind kind) {
			this.kind = kind;
		}
	}
	
	public String  date;
	public Type    type;
	@CSVUtil.DecimalPlaces(2)
	public double  fxRate;
	@CSVUtil.DecimalPlaces(2)
	public double  usd;
	public int     jpy;
	public String  symbol;
	
	public FXProfit(
		String  date,
		Type    type,
		double  fxRate,
		double  usd,
		int     jpy,
		String  symbol
		) {
		this.date    = date;
		this.type    = type;
		this.fxRate  = fxRate;
		this.usd     = usd;
		this.jpy     = jpy;
		this.symbol  = symbol;
	}
	public FXProfit() {
		this("", null, 0, 0, 0, "");
	}
		
	public static FXProfit deposit(String date, double fxRate, double usd, int jpy) {
		return new FXProfit(date, Type.DEPOSIT, fxRate, usd, (int)jpy, "");
	}
	public static FXProfit withdraw(String date, double fxRate, double usd, double jpy) {
		return new FXProfit(date, Type.WITHDRAW, fxRate, usd, (int)jpy, "");
	}
	public static FXProfit buy(String date, double fxRate, double usd, String symbol) {
		return new FXProfit(date, Type.BUY, fxRate, usd, 0, symbol);
	}
	public static FXProfit fee(String date, double fxRate, double usd) {
		return new FXProfit(date, Type.FEE, fxRate, usd, 0, "");
	}
	public static FXProfit sell(String date, double fxRate, double usd, String symbol) {
		return new FXProfit(date, Type.SELL, fxRate, usd, 0, symbol);
	}
	public static FXProfit div(String date, double fxRate, double usd, String symbol) {
		return new FXProfit(date, Type.DIV, fxRate, usd, 0, symbol);
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	@Override
	public int compareTo(FXProfit that) {
		int ret = 0;
		if (ret == 0) ret = this.date.compareTo(that.date);
		if (ret == 0) ret = this.type.compareTo(that.type);
		if (ret == 0) ret = this.symbol.compareTo(that.symbol);
		return ret;
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof FXProfit) {
				FXProfit that = (FXProfit)o	;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}
	@Override
	public int hashCode() {
		return this.date.hashCode() ^ this.type.hashCode() ^ this.symbol.hashCode();
	}
	
	public static void update() {
		var fxTaxMap = new DateMap<FXTax>();
		// build fxMap
		for(var e: UpdateFXTax.load()) {
			fxTaxMap.put(e.date, e);
		}

		List<Transaction> transactionList;
		// build trasactionList
		try (SpreadSheet docActivity = new SpreadSheet(Transaction.URL_ACTIVITY, true)) {
			boolean useTradeDate = true;
			transactionList = Transaction.getTransactionList(docActivity, useTradeDate);		
		}
		
		List<FXProfit> fxProfitList = new ArrayList<>();
		// build fxProfitList
		for(var e: transactionList) {
			logger.info("transaction {}", StringUtil.toString(e));
			String date = e.date;
			var fxTax = fxTaxMap.get(date);
			
			FXProfit fxProfit;
			switch(e.type) {
			case JPY_IN:
			case JPY_OUT:
				continue;
			case USD_IN:
				fxProfit = FXProfit.deposit(date, e.fxRate, e.usd, -e.jpy);
				fxProfitList.add(fxProfit);
				break;
			case USD_OUT:
				fxProfit = FXProfit.withdraw(date, e.fxRate, e.usd, -e.jpy);
				fxProfitList.add(fxProfit);
				break;
			case BUY:
				fxProfit = FXProfit.buy(date, fxTax.tts, -e.total, e.symbol);
				fxProfitList.add(fxProfit);
				break;
			case SELL:
				fxProfit = FXProfit.sell(date, fxTax.ttb, e.total, e.symbol);
				fxProfitList.add(fxProfit);
				break;
			case DIVIDEND:
				fxProfit = FXProfit.div(date, fxTax.ttb, e.total, e.symbol);
				fxProfitList.add(fxProfit);
				break;
			case FEE:
				fxProfit = FXProfit.fee(date, fxTax.tts, -e.usd);
				fxProfitList.add(fxProfit);
				break;
			case CHANGE:
				break;
			default:
				logger.error("Unexepected");
				logger.error("  data {}", StringUtil.toString(e));
				throw new UnexpectedException("Unexepected");
			}
		}
		logger.info("save {} {}", fxProfitList.size(), FXProfit.getPath());
		FXProfit.save(fxProfitList);
	}
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
		System.exit(0);
	}
}

package yokwe.stock.trade.gmo;

import java.util.Collections;
import java.util.List;

import yokwe.stock.trade.data.StockHistory;
import yokwe.stock.trade.data.StockHistoryUtil;
import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateStockHistory {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static List<StockHistory> getStockHistoryList() {
		try (SpreadSheet docActivity = new SpreadSheet(Transaction.URL_ACTIVITY, true)) {

			// Create transaction from activity
			List<Transaction> transactionList = Transaction.getTransactionList(docActivity);
			StockHistory.Builder builder = new StockHistory.Builder();
			
			for(Transaction transaction: transactionList) {
				switch(transaction.type) {
				case BUY:
					builder.buy(transaction.date, transaction.symbol, transaction.quantity, transaction.fee, transaction.total);
					break;
				case SELL:
					builder.sell(transaction.date, transaction.symbol, transaction.quantity, transaction.fee, transaction.total);
					break;
				case DIVIDEND:
					builder.dividend(transaction.date, transaction.symbol, transaction.fee, transaction.total);
					break;
				case CHANGE:
					builder.change(transaction.date, transaction.symbol, -transaction.quantity, transaction.newSymbol, transaction.newQuantity);
					break;
				case JPY_IN:
				case JPY_OUT:
				case FEE:
					break;
				default:
					logger.error("Unexpected {}", transaction);
					throw new UnexpectedException("Unexpected");
				}
			}
			
			List<StockHistory> stockHistoryList = builder.getStockList();
			
			Collections.sort(stockHistoryList);
			
			return stockHistoryList;
		}
	}

	public static void main(String[] args) {
		logger.info("START");
		
		List<StockHistory>stockHistoryList = getStockHistoryList();

		CSVUtil.write(StockHistory.class).file(StockHistoryUtil.PATH_STOCK_HISTORY_GMO, stockHistoryList);
		logger.info("stockHistoryList {}  {}", StockHistoryUtil.PATH_STOCK_HISTORY_GMO, stockHistoryList.size());
		
		logger.info("STOP");
		System.exit(0);
	}
}

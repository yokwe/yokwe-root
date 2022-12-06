package yokwe.stock.trade.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import yokwe.stock.trade.Storage;
import yokwe.stock.trade.data.StockHistory;
import yokwe.util.DoubleUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class Report {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final boolean MODE_TEST = false;
	
	public static final String TIMESTAMP   = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
	public static final String NAME_REPORT = String.format("STOCK_REPORT_%s.ods", TIMESTAMP);
	
	public static final String PATH_TEMPLATE = Storage.Report.getPath("TEMPLATE_STOCK_REPORT.ods");
	public static final String URL_TEMPLATE  = StringUtil.toURLString(PATH_TEMPLATE);

	private static void generateReportStockHistory(SpreadSheet docLoad, SpreadSheet docSave, String prefix, List<Transaction> transactionList) {
		Collection<List<StockHistory>> collectionList = UpdateStockHistory.filter(UpdateStockHistory.getStockHistoryListWithDividend(transactionList), true, true);
		
		List<StockHistory> stockHistoryList = new ArrayList<>();
		
		for(List<StockHistory> list: collectionList) {
			stockHistoryList.addAll(list);
			stockHistoryList.add(new StockHistory());
		}
		
		String sheetName = Sheet.getSheetName(StockHistory.class);
		docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
		Sheet.fillSheet(docSave, stockHistoryList);
		
		String newSheetName = String.format("%s-%s", prefix, sheetName);
		logger.info("sheet {}", newSheetName);
		docSave.renameSheet(sheetName, newSheetName);			
	}
	
	private static void generateReportTransfer(SpreadSheet docLoad, SpreadSheet docSave, String prefix, List<Transaction> transactionList) {
		Collection<List<StockHistory>> collectionList = UpdateStockHistory.filter(UpdateStockHistory.getStockHistoryListWithoutDividend(transactionList), true, false);

		List<Transfer> transferList = new ArrayList<>();
		for(List<StockHistory> stockHistoryList: collectionList) {
			for(StockHistory stockHistory: stockHistoryList) {
				final Transfer transfer;
				if (stockHistory.buyQuantity != 0 && stockHistory.sellQuantity == 0) {
					double buyPrice = DoubleUtil.roundQuantity((stockHistory.buy - stockHistory.buyFee) / stockHistory.buyQuantity);
					double averagePrice = DoubleUtil.roundQuantity(stockHistory.totalCost / stockHistory.totalQuantity);
					transfer = Transfer.buy(stockHistory.symbol, stockHistory.date,
							stockHistory.buyQuantity, buyPrice, stockHistory.buyFee, stockHistory.buy,
							stockHistory.totalQuantity, stockHistory.totalCost, averagePrice);
				} else if (stockHistory.buyQuantity == 0 && stockHistory.sellQuantity != 0) {
					double sellPrice = DoubleUtil.roundQuantity((stockHistory.sell + stockHistory.sellFee) / stockHistory.sellQuantity);
					transfer = Transfer.sell(stockHistory.symbol, stockHistory.date,
							stockHistory.sellQuantity, sellPrice, stockHistory.sellFee, stockHistory.sell, stockHistory.sellCost, stockHistory.sellProfit);
				} else if (stockHistory.buyQuantity != 0 && stockHistory.sellQuantity != 0) {
					// Buy and sell for the stock happened same day.
					double buyPrice = DoubleUtil.roundQuantity((stockHistory.buy - stockHistory.buyFee) / stockHistory.buyQuantity);
					// Calculate averagePrice before sell
					double averagePrice = DoubleUtil.roundPrice((stockHistory.totalCost + stockHistory.sell) / (stockHistory.totalQuantity + stockHistory.sellQuantity));					
					double sellPrice = DoubleUtil.roundQuantity((stockHistory.sell - stockHistory.sellFee) / stockHistory.sellQuantity);
					transfer = Transfer.buySell(stockHistory.symbol, stockHistory.date,
							stockHistory.buyQuantity, buyPrice, stockHistory.buyFee, stockHistory.buy,
							stockHistory.totalQuantity, stockHistory.totalCost, averagePrice,
							stockHistory.date, stockHistory.sellQuantity, sellPrice, stockHistory.sellFee, stockHistory.sell, stockHistory.sellCost, stockHistory.sellProfit);
				} else if (stockHistory.buyQuantity == 0 && stockHistory.sellQuantity == 0 && stockHistory.dividend == 0) {
					// Change
					double averagePrice = DoubleUtil.roundQuantity(stockHistory.totalCost / stockHistory.totalQuantity);
					transfer = Transfer.change(stockHistory.symbol, stockHistory.date,
							stockHistory.totalQuantity, stockHistory.totalCost, averagePrice);
				} else {
//					logger.error("Unexpected  {}", stockHistory);
//					throw new UnexpectedException("Unexpected");
					transfer = null;
				}
				if (transfer != null) transferList.add(transfer);
			}
			transferList.add(new Transfer());
		}
		
		String sheetName = Sheet.getSheetName(Transfer.class);
		docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
		Sheet.fillSheet(docSave, transferList);
		
		String newSheetName = String.format("%s-%s", prefix, sheetName);
		logger.info("sheet {}", newSheetName);
		docSave.renameSheet(sheetName, newSheetName);						
	}
	
	private static void generateReportAccount(SpreadSheet docLoad, SpreadSheet docSave, String prefix, List<Transaction> transactionList) {
		String DATE_FIRST = LocalDate.now().minusYears(1).toString();

		List<Account> accountList = new ArrayList<>();
		
		Portfolio portfolio = new Portfolio();
		
		int    fundJPY = 0;
		double fund    = 0;
		double cash    = 0;
		double stock   = 0;
		double gain    = 0;
		double sellCost = 0;
		double sellGain = 0;

		for(Transaction transaction: transactionList) {
			final Account account;
			
			switch(transaction.type) {
			case DEPOSIT_JPY:         // Increase cash JPY
				fundJPY += transaction.amountJPY;
				account = Account.fundJPY(transaction.date, transaction.amountJPY, null, fundJPY, fund, cash, stock, gain);
				break;
			case WITHDRAW_JPY:        // Decrease cash
				fundJPY -= transaction.amountJPY;
				account = Account.fundJPY(transaction.date, null, transaction.amountJPY, fundJPY, fund, cash, stock, gain);
				break;
			case EXCHANGE_JPY_TO_USD: // Buy USD from JPY
				fundJPY -= transaction.amountJPY;
				fund = DoubleUtil.roundPrice(fund + transaction.amountUSD);
				cash = DoubleUtil.roundPrice(cash + transaction.amountUSD);
				account = Account.fundJPYUSD(transaction.date,
						null, transaction.amountJPY, fundJPY,
						transaction.amountUSD, null, fund, cash, stock, gain);
				break;
			case EXCHANGE_USD_TO_JPY: // Buy JPY from USD
				fundJPY += transaction.amountJPY;
				fund = DoubleUtil.roundPrice(fund - transaction.amountUSD);
				cash = DoubleUtil.roundPrice(cash - transaction.amountUSD);
				account = Account.fundJPYUSD(transaction.date,
						transaction.amountJPY, null, fundJPY,
						null, transaction.amountUSD, fund, cash, stock, gain);
				break;			
			case DEPOSIT:  // Increase cash
				fund = DoubleUtil.roundPrice(fund + transaction.credit);
				cash = DoubleUtil.roundPrice(cash + transaction.credit);
				account = Account.fundUSD(transaction.date, fundJPY, transaction.credit, null, fund, cash, stock, gain);
				break;
			case WITHDRAW: // Decrease cash
				fund = DoubleUtil.roundPrice(fund - transaction.debit);
				cash = DoubleUtil.roundPrice(cash - transaction.debit);
				account = Account.fundUSD(transaction.date, fundJPY, null, transaction.debit, fund, cash, stock, gain);
				break;
			case INTEREST: // Interest of account
				cash = DoubleUtil.roundPrice(cash + transaction.credit);
				gain = DoubleUtil.roundPrice(gain + transaction.credit);
				account = Account.dividend(transaction.date, fundJPY, transaction.credit, fund, cash, stock, gain, null);
				break;
			case DIVIDEND: // Dividend of stock
				cash = DoubleUtil.roundPrice(cash + transaction.credit);
				gain = DoubleUtil.roundPrice(gain + transaction.credit);
				account = Account.dividend(transaction.date, fundJPY, transaction.credit, fund, cash, stock, gain, transaction.symbol);
				break;
			case BUY:      // Buy stock   *NOTE* Buy must  be before SELL
				portfolio.buy(transaction.symbol, transaction.quantity, transaction.debit);
				
				cash = DoubleUtil.roundPrice(cash - transaction.debit);
				stock = DoubleUtil.roundPrice(stock + transaction.debit);
				account = Account.buy(transaction.date, fundJPY, fund, cash, stock, gain, transaction.symbol, transaction.debit);
				break;
			case SELL:     // Sell stock  *NOTE* Sell must be after BUY
				sellCost = portfolio.sell(transaction.symbol, transaction.quantity);
				sellGain = DoubleUtil.roundPrice(transaction.credit - sellCost);
				
				cash = DoubleUtil.roundPrice(cash + transaction.credit);
				stock = DoubleUtil.roundPrice(stock - sellCost);
				gain = DoubleUtil.roundPrice(gain + sellGain);
				account = Account.sell(transaction.date, fundJPY, fund, cash, stock, gain, transaction.symbol, transaction.credit, sellCost, sellGain);
				break;
			case CHANGE:   // Stock split, reverse split or symbol change
				portfolio.change(transaction.symbol, transaction.quantity, transaction.newSymbol, transaction.newQuantity);
				account = Account.change(transaction.date, fundJPY, fund, cash, stock, gain, transaction.newSymbol);
				break;
			default:
				logger.error("Unexpected  {}", transaction);
				throw new UnexpectedException("Unexpected");				
			}
			
			// limit output to one year
			if (account != null && DATE_FIRST.compareTo(account.date) < 0) accountList.add(account);
//			if (account != null) accountList.add(account);
		}
		
		String sheetName = String.format("%s-%s", prefix, Sheet.getSheetName(Account.class));
		docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
		Sheet.fillSheet(docSave, accountList, sheetName);
		
		logger.info("sheet {}", sheetName);
	}
	
	private static void generateReport(SpreadSheet docLoad, SpreadSheet docSave, String prefix, List<Transaction> transactionList) {
		generateReportStockHistory(docLoad, docSave, prefix, transactionList);
		generateReportTransfer(docLoad, docSave, prefix, transactionList);
		generateReportAccount(docLoad, docSave, prefix, transactionList);
	}
	
	public static void generateReport() {
		String pathReport = Storage.Report.getPath("monex", NAME_REPORT);
		String urlReport  = StringUtil.toURLString(pathReport);

		try (SpreadSheet docLoad = new SpreadSheet(URL_TEMPLATE, true)) {
			SpreadSheet docSave = new SpreadSheet();
			
			generateReport(docLoad, docSave, "monex",     Transaction.getMonex());

			// remove first sheet
			docSave.removeSheet(docSave.getSheetName(0));

			docSave.store(urlReport);
			logger.info("output {}", urlReport);
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		generateReport();

		logger.info("STOP");
		System.exit(0);
	}
}

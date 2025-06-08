package yokwe.finance.trade2.rakuten;

import static yokwe.finance.trade.rakuten.UpdateAccountReport.URL_TEMPLATE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import yokwe.finance.trade2.AccountReportJPY;
import yokwe.finance.trade2.AccountReportUSD;
import yokwe.finance.trade2.Portfolio;
import yokwe.util.StringUtil;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateAccountReport {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
	
	public static void update() {
		var transactionList = StorageRakuten.TransactionList.getList();
		
		var reportJPY = UpdateAccountReportJPY.toAccountReportJPY(transactionList);
		logger.info("reportJPY  {}", reportJPY.size());
		var reportUSD = UpdateAccountReportUSD.toAccountReportUSD(transactionList);
		logger.info("reportUSD  {}", reportUSD.size());
		
		// remove entry older than 1 year
		reportJPY.removeIf(o -> o.date.isBefore(LocalDate.now().minusYears(1)));
		reportUSD.removeIf(o -> o.date.isBefore(LocalDate.now().minusYears(1)));
		
		generateReport(reportJPY, reportUSD);
	}
	private static void generateReport(List<AccountReportJPY> listJPY, List<AccountReportUSD> listUSD) {
		String urlReport;
		{
			String timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
			String name       = String.format("account-report-%s.ods", timestamp);
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
			
			{
				String sheetName = Sheet.getSheetName(AccountReportJPY.class);
				logger.info("sheet     {}", sheetName);
				docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
				Sheet.fillSheet(docSave, listJPY);
			}
			{
				String sheetName = Sheet.getSheetName(AccountReportUSD.class);
				logger.info("sheet     {}", sheetName);
				docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
				Sheet.fillSheet(docSave, listUSD);
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
	
	static class Context {
		Portfolio  portfolio    = new Portfolio();
		int        fundTotal    = 0;
		int        cashTotal    = 0;
		int        stockCost    = 0;
		int        realizedGain = 0;
	}
}

package yokwe.stock.us;

import java.util.ArrayList;
import java.util.List;

import yokwe.stock.us.Stock.Market;
import yokwe.stock.us.bats.BATSStock;
import yokwe.stock.us.bats.UpdateBATSStock;
import yokwe.stock.us.nasdaq.NASDAQStock;
import yokwe.stock.us.nasdaq.UpdateNASDAQStock;
import yokwe.stock.us.nyse.NYSEStock;
import yokwe.stock.us.nyse.UpdateNYSEStock;
import yokwe.util.MarketHoliday;

public class UpdateStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static void downloadFiles() {
		UpdateNYSEStock.download();
		UpdateNASDAQStock.download();
		UpdateBATSStock.download();
	}
	private static void updateFiles() {
		UpdateNYSEStock.update();
		UpdateNASDAQStock.update();
		UpdateBATSStock.update();
	}
	private static void updateStock() {
		// load nyse
		List<Stock> nyseList = NYSEStock.getList();
		logger.info("nyse    {}", nyseList.size());

		// load nasdaq
		List<Stock> nasdaqList = NASDAQStock.getList();
		logger.info("nasdaq  {}", nasdaqList.size());

		// load bats
		List<Stock> batsList = BATSStock.getList();
		logger.info("bats    {}", batsList.size());

		int countNYSE   = 0;
		int countNASDAQ = 0;
		int countBATS   = 0;
		List<Stock> list = new ArrayList<>();
		for(var e: nyseList) {
			if (e.market == Market.NYSE) {
				list.add(e);
				countNYSE++;
			}
		}
		for(var e: nasdaqList) {
			if (e.market == Market.NASDAQ) {
				list.add(e);
				countNASDAQ++;
			}
		}
		for(var e: batsList) {
			if (e.market == Market.BATS) {
				list.add(e);
				countBATS++;
			}
		}
		
		logger.info("stock   {}  {}", list.size(), Stock.getPath());
		Stock.save(list);
		
		logger.info("nyae    {}", countNYSE);
		logger.info("nasdaq  {}", countNASDAQ);
		logger.info("bats    {}", countBATS);
	}
	
	public static void update() {
		downloadFiles();
		updateFiles();
		updateStock();
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		MarketHoliday.US.getLastTradingDate();
		
		update();

		logger.info("STOP");
	}
}

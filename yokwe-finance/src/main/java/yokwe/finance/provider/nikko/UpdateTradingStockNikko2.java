package yokwe.finance.provider.nikko;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.finance.Storage;
import yokwe.finance.account.Secret;
import yokwe.finance.account.nikko.AccountNikko;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.TradingStockType;
import yokwe.finance.type.TradingStockType.FeeType;
import yokwe.finance.type.TradingStockType.TradeType;
import yokwe.finance.util.SeleniumUtil;
import yokwe.util.ScrapeUtil;

public class UpdateTradingStockNikko2 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static class StockInfo {
		// <a href="javascript:kaiuriPage('/StockOrderConfirmation/DF8C90489665/usa/kai/odr/hyoji', '22493') ">A</a>
		// <a href="javascript:kaiuriPage('/StockOrderConfirmation/DF8C90489714/usa/kai/odr/hyoji', '26914') ">ACOR</a>
		public static final Pattern PAT = Pattern.compile(
				"<a href=\"javascript:kaiuriPage\\('/StockOrderConfirmation/............/usa/kai/odr/hyoji', '(?<nikkoCode>.+?)'\\) \">(?<stockCode>[A-Z/]+)</a>" +
				""
		);
		public static List<StockInfo> getInstance(String page) {
			return ScrapeUtil.getList(StockInfo.class, PAT, page);
		}
		
		public String stockCode;
		public String nikkoCode;
		
		public StockInfo(String stockCode, String nikkoCode) {
			this.stockCode = stockCode;
			this.nikkoCode = nikkoCode;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s}", stockCode, nikkoCode);
		}
	}
	
	
	private static void update() {
		Storage.initialize();

		var set = new HashSet<String>();
		
		{
			var secret = Secret.read();
			logger.info("branch    {}", secret.nikko.branch);
			logger.info("account   {}", secret.nikko.account);
//			logger.info("password  {}", secret.nikko.password);
			
			logger.info("driver");
			var driver = SeleniumUtil.getWebDriver();
			
			logger.info("login");
			AccountNikko.login(driver, secret.nikko.branch, secret.nikko.account, secret.nikko.password);
			
			AccountNikko.firstPageUSStock(driver);
			for(int i = 1; i < 100; i++) {
				var page = driver.getPageSource();

				{
					var list = StockInfo.getInstance(page);
					logger.info("page  {}  {}", i, list.size());
					
					for(var e: list) {
						var stockCode = e.stockCode.replace("/", ".");
						set.add(stockCode);
					}
				}
				
				if (AccountNikko.nextPageUSStock(driver)) continue;
				
				break;
			}
			
			logger.info("logout");
			AccountNikko.logout(driver);
			
			logger.info("closeDriver");
			SeleniumUtil.closeDriver(driver);
		}
		
		List<TradingStockType> tradingStockList = new ArrayList<>();
		{
			var stockMap = StorageStock.StockInfoUSAll.getMap();
			
			for(var symbol: set) {
				if (stockMap.containsKey(symbol)) {
					tradingStockList.add(new TradingStockType(symbol, FeeType.PAID, TradeType.BUY_SELL));
				} else {
					logger.warn("Unexpected symbol  {}", symbol);
				}
			}
		}
		
		logger.info("save  {}  {}", tradingStockList.size(), StorageNikko.TradingStockNikko.getPath());
		StorageNikko.TradingStockNikko.save(tradingStockList);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
		System.exit(0);
	}
}

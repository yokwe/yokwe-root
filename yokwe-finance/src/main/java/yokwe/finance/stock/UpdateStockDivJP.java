package yokwe.finance.stock;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.provider.jpx.StorageJPX;
import yokwe.finance.provider.jreit.StorageJREIT;
import yokwe.finance.provider.manebu.StorageManebu;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.StockInfoJPType;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class UpdateStockDivJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	
	private static void update() {
		var stockCodeMap = StorageFund.FundInfo.getList().stream().filter(o -> !o.stockCode.isEmpty()).collect(Collectors.toMap(o -> o.stockCode, o -> o.isinCode));
		// stockCode isinCode
		var reitSet = StorageJREIT.JREITInfo.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
		
		var etfInfoMap = StorageManebu.ETFInfo.getMap();
		
		var list = StorageStock.StockInfoJP.getList();
		logger.info("list   {}", list.size());
		int countETF   = 0;
		int countREIT  = 0;
		int countStock = 0;
		int countSave  = 0;
		int countSkip  = 0;
		for(var stock: list) {
			String stockCode = stock.stockCode;
			
			List<DailyValue> divList = null;
			{
				if (stockCodeMap.containsKey(stockCode)) {
					// ETF
					String isinCode = stockCodeMap.get(stockCode);
					divList = StorageFund.FundDiv.getList(isinCode);
					
					// NOTE FundDiv and FundPrice is not always per 1 unit. It can be 1, 10, 100 or 1000 units.
					var etfInfo = etfInfoMap.get(stockCode);
					if (etfInfo == null) {
						if (stock.topix == StockInfoJPType.Topix.NEW) {
							// very new and not issued
							logger.warn("skip   {}  {}  {}  {}", stockCode, stock.type, stock.topix, stock.name);
							countSkip++;
							continue;
						}
						logger.error("no etfInfo");
						logger.error("  {}  {}  {}  {}", stockCode, stock.type, stock.topix, stock.name);
						throw new UnexpectedException("no etfInfo");
					} else {
						// adjust div using etfInfo.fundUnit to get per 1 unit.
						for(var div: divList) {
							div.value = div.value.divide(etfInfo.fundUnit);
						}
					}
					countETF++;
				} else if (reitSet.contains(stockCode)) {
					// REIT
					divList = StorageJREIT.JREITDiv.getList(stockCode);
					countREIT++;
				} else {
					// STOCK
					divList = StorageJPX.StockDivJPX.getList(stockCode);
					countStock++;
				}
			}
//			logger.info("save  {}  {}", stockCode, StockDivJP.getPath(stockCode));
			if (!divList.isEmpty()) {
				StorageStock.StockDivJP.save(stockCode, divList);
				countSave++;
			}
		}
		
		logger.info("etf    {}", countETF);
		logger.info("reit   {}", countREIT);
		logger.info("stock  {}", countStock);
		logger.info("skip   {}", countSkip);
		logger.info("save   {}", countSave);
	}
	
	private static void moveUnknownFile() {
		Set<String> validNameSet = new TreeSet<>();
		for(var e: StorageStock.StockInfoJP.getList()) {
			File file = new File(StorageStock.StockDivJP.getPath(e.stockCode));
			validNameSet.add(file.getName());
		}
		
		FileUtil.moveUnknownFile(validNameSet, StorageStock.StockDivJP.getPath(), StorageStock.StockDivJP.getPathDelist());
	}
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		moveUnknownFile();
		
		update();
		
		logger.info("STOP");
	}
}

package yokwe.finance.stock;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.finance.fund.FundDiv;
import yokwe.finance.fund.FundInfo;
import yokwe.finance.provider.jreit.REITDiv;
import yokwe.finance.provider.jreit.REITInfo;
import yokwe.finance.provider.manebu.ETFInfo;
import yokwe.finance.type.DailyValue;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class UpdateStockDivJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	
	private static void update() {
		var stockCodeMap = FundInfo.getList().stream().filter(o -> !o.stockCode.isEmpty()).collect(Collectors.toMap(o -> o.stockCode, o -> o.isinCode));
		// stockCode isinCode
		var reitSet = REITInfo.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
		
		var etfInfoMap = ETFInfo.getMap();
		
		var list = StockInfoJP.getList();
		logger.info("list   {}", list.size());
		int countETF   = 0;
		int countREIT  = 0;
		int countStock = 0;
		int countSave  = 0;
		for(var stock: list) {
			String stockCode = stock.stockCode;
			
			List<DailyValue> divList = null;
			{
				if (stockCodeMap.containsKey(stockCode)) {
					String isinCode = stockCodeMap.get(stockCode);
					divList = FundDiv.getList(isinCode);
					
					// NOTE FundDiv and FundPrice is not always per 1 unit. It can be 1, 10, 100 or 1000 units.
					var etfInfo = etfInfoMap.get(stockCode);
					if (etfInfo == null) {
						logger.error("no etfInfo");
						logger.error("  {}  {}", stockCode, stock.name);
						throw new UnexpectedException("no etfInfo");
					} else {
						// adjust div using etfInfo.fundUnit to get per 1 unit.
						for(var div: divList) {
							div.value = div.value.divide(etfInfo.fundUnit);
						}
					}
					countETF++;
				} else if (reitSet.contains(stockCode)) {
					divList = REITDiv.getList(stockCode);
					countREIT++;
				} else {
					divList = StockDivJP.getList(stockCode);
					countStock++;
				}
			}
//			logger.info("save  {}  {}", stockCode, StockDivJP.getPath(stockCode));
			if (!divList.isEmpty()) {
				StockDivJP.save(stockCode, divList);
				countSave++;
			}
		}
		
		logger.info("etf    {}", countETF);
		logger.info("reit   {}", countREIT);
		logger.info("stock  {}", countStock);
		logger.info("save   {}", countSave);
	}
	
	private static void moveUnknownFile() {
		Set<String> validNameSet = new TreeSet<>();
		for(var e: StockInfoJP.getList()) {
			File file = new File(StockDivJP.getPath(e.stockCode));
			validNameSet.add(file.getName());
		}
		
		FileUtil.moveUnknownFile(validNameSet, StockDivJP.getPath(), StockDivJP.getPathDelist());
	}
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		moveUnknownFile();
		
		update();
		
		logger.info("STOP");
	}
}

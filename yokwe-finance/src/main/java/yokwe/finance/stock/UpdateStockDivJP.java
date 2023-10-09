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
import yokwe.finance.type.DailyValue;
import yokwe.util.FileUtil;

public class UpdateStockDivJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	
	private static void update() {
		var etfMap = FundInfo.getList().stream().filter(o -> !o.stockCode.isEmpty()).collect(Collectors.toMap(o -> o.stockCode, o -> o.isinCode));
		// stockCode isinCode
		var reitSet = REITInfo.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
		
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
				if (etfMap.containsKey(stockCode)) {
					String isinCode = etfMap.get(stockCode);
					divList = FundDiv.getList(isinCode);
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

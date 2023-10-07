package yokwe.finance.stock.jp;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.finance.fund.jp.FundDiv;
import yokwe.finance.fund.jp.FundInfo;
import yokwe.finance.provider.jreit.REITDiv;
import yokwe.finance.provider.jreit.REITInfo;
import yokwe.finance.provider.yahoo.StockDivJPYahoo;
import yokwe.finance.type.DailyValue;
import yokwe.util.FileUtil;

public class UpdateStockDivJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	
	private static void update() {
		var etfMap = FundInfo.getList().stream().filter(o -> !o.stockCode.isEmpty()).collect(Collectors.toMap(o -> o.stockCode, o -> o.isinCode));
		// stockCode isinCode
		var reitSet = REITInfo.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
		
		var list = StockInfoJP.getList();
		int count = 0;
		for(var stock: list) {
			if ((++count % 100) == 1) logger.info("{}  /  {}", count, list.size());
			String stockCode = stock.stockCode;
			
			List<DailyValue> divList = null;
			{
				if (etfMap.containsKey(stockCode)) {
					String isinCode = etfMap.get(stockCode);
					divList = FundDiv.getList(isinCode);
				} else if (reitSet.contains(stockCode)) {
					divList = REITDiv.getList(stockCode);
				} else {
					divList = StockDivJPYahoo.getList(stockCode);
				}
			}
			if (divList == null) {
				logger.warn("No data  {}  {}", stockCode, stock.name);
				continue;
			}
			
			logger.info("save  {}  {}", stockCode, StockDivJP.getPath(stockCode));
			StockDivJP.save(stockCode, divList);
		}
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

package yokwe.finance.provider.jpx;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.finance.provider.jpx.UpdateStockDetail.Result;
import yokwe.finance.type.StockCodeJP;
import yokwe.finance.type.StockInfoJPType;
import yokwe.finance.type.StockInfoJPType.Type;
import yokwe.util.UnexpectedException;
import yokwe.util.json.JSON;

public class UpdateStockInfoJPX {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final List<StockListType> stockList = StorageJPX.StockList.getList();
	private static final int stockListSize = stockList.size();
	
	private static final Map<String, Type> typeMap = new HashMap<>(stockListSize);
	static {
		StorageJPX.ETF.getList().stream().forEach(      o -> typeMap.put(o.stockCode, StockInfoJPType.Type.ETF));
		StorageJPX.ETN.getList().stream().forEach(      o -> typeMap.put(o.stockCode, StockInfoJPType.Type.ETN));
		StorageJPX.InfraFund.getList().stream().forEach(o -> typeMap.put(o.stockCode, StockInfoJPType.Type.INFRA));
		StorageJPX.REIT.getList().stream().forEach(     o -> typeMap.put(o.stockCode, StockInfoJPType.Type.REIT));
		typeMap.put("83010", Type.CERTIFICATE); // 日本銀行
		typeMap.put("84210", Type.CERTIFICATE); // 信金中央金庫
		
		typeMap.put("グロース",           Type.DOMESTIC_GROWTH);
		typeMap.put("プライム",           Type.DOMESTIC_PRIME);
		typeMap.put("スタンダード",       Type.DOMESTIC_STANDARD);
		typeMap.put("外国株グロース",     Type.FOREIGN_GROWTH);
		typeMap.put("外国株プライム",     Type.FOREIGN_PRIME);
		typeMap.put("外国株スタンダード", Type.FOREIGN_STANDARD);
	}
	
	private static void updateDetail(Result result, List<StockInfoJPType> list) {
		if (result.section1.data == null) {
			logger.error("data is null");
			throw new UnexpectedException("data is null");
		}
		
		for(var data: result.section1.data.values()) {
			String     stockCode = StockCodeJP.toStockCode5(data.TTCODE2);
			String     isinCode  = data.ISIN;
			int        tradeUnit = Integer.parseInt(data.LOSH.replace(",", ""));
			String     sector33  = data.JSEC_CNV;
			BigDecimal issued    = new BigDecimal(data.SHRK.replace(",", ""));
			String     name      = data.FLLN;
			
			var typeString = data.LISS_CNV;
			Type type = typeMap.get(stockCode);
			if (type == null) type = typeMap.get(typeString);
			if (type == null) {
				logger.error("Unexpected type");
				logger.error("  stockCode   {}", stockCode);
				logger.error("  typeString  {}", typeString);
				throw new UnexpectedException("Unexpected type");
			}
				
			var stockInfoJP = new StockInfoJPType(stockCode, isinCode, tradeUnit, type, sector33, issued, name);
			list.add(stockInfoJP);
		}
	}
	
	private static void updateFile() {
		// update price using list (StockPrice)
		logger.info("updateFile");
		
		var list = new ArrayList<StockInfoJPType>(stockListSize);
		
		int count = 0;
		for(var stock: stockList) {
			if ((++count % 1000) == 1) logger.info("{}  /  {}", count, stockListSize);

			var string = StorageJPX.StockDetailJSON.load(stock.stockCode);
			var result = JSON.unmarshal(Result.class, string);
			updateDetail(result, list);
		}
		
		StorageJPX.StockInfoJPX.save(list);
		
		// fix name in stockList
		// use same name for stock-info-jpx.csv and stockList.csv
		{
			var nameMap = list.stream().collect(Collectors.toMap(o -> o.stockCode, o -> o.name));
			stockList.stream().filter(o -> nameMap.containsKey(o.stockCode)).forEach(o -> o.name = nameMap.get(o.stockCode));
			StorageJPX.StockList.save(stockList);
		}
	}
	private static void update() {
		updateFile();
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}

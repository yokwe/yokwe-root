package yokwe.finance.provider.jpx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import yokwe.finance.provider.jpx.UpdateStockDetail.Data;
import yokwe.finance.provider.jpx.UpdateStockDetail.Result;
import yokwe.finance.type.OHLCV;
import yokwe.finance.type.StockCodeJP;
import yokwe.util.MarketHoliday;
import yokwe.util.UnexpectedException;
import yokwe.util.json.JSON;

public class UpdateStockPriceJPX {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final List<StockListType> stockList = StorageJPX.StockList.getList();
	private static final int stockListSize = stockList.size();
	
	private static final LocalDate lastTradingDate = MarketHoliday.JP.getLastTradingDate();
	
	
	private static List<OHLCV> getPriceList(Data data) {
		List<OHLCV> priceList = new ArrayList<>();
		{
			var stockCode = StockCodeJP.toStockCode5(data.TTCODE2);
//			logger.info("stock  {}  {}", stockCode, data.FLLN);

			BigDecimal o = null;
			BigDecimal h = null;
			BigDecimal l = null;
			BigDecimal c = null;
			
			for(var ohlcvString: data.A_HISTDAYL.split(",?\\\\n")) {
				String[] valueString = ohlcvString.split(",");
				if (valueString.length != 6 && valueString.length != 7) {
					logger.error("Unexpected ohlcvString");
					logger.error("  stockCode    {}", stockCode);
					logger.error("  valueString  {}", valueString.length);
					logger.error("  ohlcvString  {}", ohlcvString);
					throw new UnexpectedException("Unexpected ohlcvString");
				}
				
//				logger.info("YY  {}", ToString.withFieldName(valueString));
				
				var dateString = valueString[0];
				var oString    = valueString[1];
				var hString    = valueString[2];
				var lString    = valueString[3];
				var cString    = valueString[4];
				var vString    = valueString[5];
				
				var date = LocalDate.parse(dateString.replace('/', '-'));
				var v = Long.parseLong(vString);
				if (v == 0) {
					if (o == null) continue;
					// use last value
				} else {
					o = new BigDecimal(oString);
					h = new BigDecimal(hString);
					l = new BigDecimal(lString);
					c = new BigDecimal(cString);
				}
				
				priceList.add(new OHLCV(date, o, h, l, c, v));
			}
			
			// add latest price
			{
				if (data.A_HISTDAYL.contains(data.ZXD)) {
					// already processed latest price
				} else {
					// no latest price
					var dateString = data.ZXD;
					var oString    = data.DOP;
					var hString    = data.DHP;
					var lString    = data.DLP;
					var cString    = data.DPP;
					var vString    = data.DV;
					
					var date = LocalDate.parse(dateString.replace('/', '-'));
					long v;
					if (vString.equals("-")) {
						// there is no trading
						// use last value of  o h l c
						v = 0;
					} else {
						// there is trading
						o = new BigDecimal(oString.replace(",", ""));
						h = new BigDecimal(hString.replace(",", ""));
						l = new BigDecimal(lString.replace(",", ""));
						c = new BigDecimal(cString.replace(",", ""));
						v = Long.parseLong(vString.replace(",", ""));
					}
					
					if (o != null) {
						priceList.add(new OHLCV(date, o, h, l, c, v));
					}
				}						
			}
		}
		return priceList;
	}
	
	private static void updatePrice(StockListType stock, Result result) {
		if (result.section1.data == null) {
			logger.warn("data is null  {}  {}", stock.stockCode, stock.name);
			logger.warn("  result  {}", result.toString());
			return;
		}
		
		BinaryOperator<OHLCV> mergeOHLCV = new BinaryOperator<OHLCV>() {
			@Override
			public OHLCV apply(OHLCV t, OHLCV u) {
				if (t.equals(u)) return t;
				if (t.equalsByValue(u)) return t;
				
				logger.error("Unexpected value");
				logger.error("  t  {}", t.toString());
				logger.error("  u  {}", u.toString());
				throw new UnexpectedException("Unexpected value");
			}
		};
		
		for(var data: result.section1.data.values()) {
			var stockCode = StockCodeJP.toStockCode5(data.TTCODE2);
			var oldList   = StorageJPX.StockPriceJPX.getList(stockCode);
			var oldMap    = oldList.stream().collect(Collectors.toMap(o -> o.date, o -> o, mergeOHLCV, TreeMap::new));

			var newList   = getPriceList(data);
			Collections.sort(newList);
			var newMap    = newList.stream().collect(Collectors.toMap(o -> o.date, o -> o, mergeOHLCV, TreeMap::new));
			
			boolean needsMergeList = true;
			{
				if (oldList.size() < 3) {
					// no need to merge
					needsMergeList = false;
				} else {
					LocalDate date;
					{
						var oldListLastDate = oldList.get(oldList.size() - 1).date;
						var newListLastDate = newList.get(newList.size() - 1).date;
						
						if (oldListLastDate.equals(newListLastDate)) {
							date = oldList.get(oldList.size() - 2).date;
						} else {
							date = oldListLastDate;
						}
					}
					
					if (oldMap.containsKey(date) && newMap.containsKey(date)) {
						var oldPrice = oldMap.get(date);
						var newPrice = newMap.get(date);
						
						if (oldPrice.equalsByValue(newPrice)) {
							// expected
						} else {
							// stock split
							logger.info("stock split  {}  {}  {}", stockCode, oldPrice, newPrice);
							needsMergeList = false;
						}
					} else {
						logger.error("Unexpected date");
						logger.error("  sockCode  {}", stockCode);
						logger.error("  date      {}", date);
						logger.error("  old       {}", oldList);
						logger.error("  new       {}", newList);
//						logger.error("  data      {}", data.toString());
						throw new UnexpectedException("Unexpected date");
					}
				}
			}
			
			if (needsMergeList) {
				for(var oldPrice: oldList) {
					var date = oldPrice.date;
					// keep price in newList for lastTradingDat
					if (date.equals(lastTradingDate)) continue;
					
					var newPrice = newMap.get(date);
					if (newPrice == null) {
						// add not existing data to newList
						newList.add(oldPrice);
					} else {
						// sanity check
						if (newPrice.equalsByValue(oldPrice)) {
							// expected
						} else {
							// not expected
							logger.error("Unexpected price");
							logger.error("  sockCode  {}", stockCode);
							logger.error("  oldPrice  {}", oldPrice);
							logger.error("  newPrice  {}", newPrice);
							throw new UnexpectedException("Unexpected price");
						}
					}
				}
			}
//			logger.info("save  {}  {}", newList.size(), StorageJPX.StockPrice.getPath(stockCode));
			StorageJPX.StockPriceJPX.save(stockCode, newList);
		}
	}
		
	public static void updateFile() {
		// update price using list (StockPrice)
		logger.info("updateFile");
		
		int count = 0;
		for(var stock: stockList) {
			if ((++count % 1000) == 1) logger.info("{}  /  {}", count, stockListSize);
			
			var string = StorageJPX.StockDetailJSON.load(stock.stockCode);
			var result = JSON.unmarshal(Result.class, string);
			updatePrice(stock, result);
		}
	}
	
	private static void delistUnknownFile() {
		var validNameSet = stockList.stream().map(o -> o.stockCode).collect(Collectors.toSet());
		StorageJPX.StockPriceJPX.delistUnknownFile(validNameSet);
	}
	
	private static void update() {
		delistUnknownFile();
		
		updateFile();
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}

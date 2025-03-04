package yokwe.finance.provider.jpx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.finance.type.OHLCV;
import yokwe.finance.type.StockCodeJP;
import yokwe.util.MarketHoliday;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.json.JSON;
import yokwe.util.json.JSON.Ignore;

public class UpdateStockPriceJPX {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final List<StockListType> stockList = StorageJPX.StockList.getList();
	private static final int stockListSize = stockList.size();
	
	private static final LocalDate lastTradingDate = MarketHoliday.JP.getLastTradingDate();
	
	
	public static class Result {
		public String   cputime;
		public Section1 section1;
		public int      status;
		public String   ver;
		@Ignore
		public Object   urlparam;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static class Section1 {
		public Map<String, Data> data;
		public int               hitcount;
		public int               status;
		public String            type;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static class Data {
		@Ignore
		public String A_CALC027; // "-"
		@Ignore
        public String A_CALC028; // "-"
		@Ignore
        public String A_CALC029; // "-"
        public String A_HISTDAYL; // "2023/09/06,3865.0,3920.0,3865.0,3915.0,20900,\n"
        public String DHP;  // 高値 "4,120"
        public String DHPT; // 高値 時刻 "09:00"
		@Ignore
        public String DJ;   // 売買代金 "39,077,500"
        public String DLP;  // 低値 "4,100"
        public String DLPT; // 低値 時刻 "09:01"
        public String DOP;  // 始値 "4,120"
        public String DOPT; // 始値 時刻 "09:00"
        public String DPP;  // 現在値 "4,105"
        public String DPPT; // 現在値 時刻 "10:41"
        public String DV;   // 売買高 "9,500"
		@Ignore
        public String DYRP; // 前日比 パーセント "-0.72"
		@Ignore
        public String DYWP; // 前日比 "-30"
		@Ignore
        public String EXCC; // "T"
		@Ignore
        public String EXDV; // "0000"
        public String FLLN; // 名前 "極洋"
		@Ignore
        public String FLLNE; // "KYOKUYO CO., LTD."
		@Ignore
        public String FTRTS; // ""
        public String ISIN; // "JP3257200000"
		@Ignore
        public String JDMKCP; // "-"
		@Ignore
        public String JDSHRK; // "-"
		@Ignore
        public String JSEC; // sector33Code"0050"
        public String LISS; // "ﾌﾟﾗｲﾑ"
		@Ignore
        public String LISSE; // ""
        public String LOSH; // 売買単位 "100"
        public String MKCP; // 時価総額 "49,943,700,205.0"
		@Ignore
        public String NAME; // "極　洋"
		@Ignore
        public String NAMEE; // "KYOKUYO"
		@Ignore
        public String PRP; // 前日終値 "4,135"
		@Ignore
        public String PRSS; // "C"
		@Ignore
        public String PSTS; // ""
        public String QAP;  // 売気配 "4,115"
        public String QAPT; // 売気配 時刻"10:43"
        public String QBP;  // 買気配 "4,105"
        public String QBPT; // 買気配 時刻 "10:43"
        public String SHRK; // 発行済株式数 "12,078,283"
		@Ignore
        public String TTCODE;  // "1301/T"
        public String TTCODE2; // "1301"
        public String YHPD; // 年初来高値 日付 "2024/09/24"
        public String YHPR; // 年初来高値 "4,600"
        public String YLPD; // 年初来安値 日付 "2024/08/05"
        public String YLPR; // 年初来安値 "3,400"
        public String ZXD;  // 売買日付 "2025/02/28"
		@Ignore
        public int    DYWP_FLG; // -1
		@Ignore
        public int    DYRP_FLG; // -1
		@Ignore
        public int    A_CALC029_FLG; // 0
		@Ignore
        public String EXDV_CNV; // ""
		@Ignore
        public String EXDVE_CNV; // ""
		@Ignore
        public Index[] INDEX_INFOMATION; // 0
        public String JSEC_CNV;  // sector33 日本顔 "水産・農林業"
		@Ignore
        public String JSECE_CNV; // sector33 英語 "Fishery, Agriculture & Forestry"
		@Ignore
        public String PSTSE; // ""
        public String LISS_CNV; // "プライム"
		@Ignore
        public String LISSE_CNV; // "Prime"
        
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static class Index {
        public String YEAR;     // "2024/09"
        public String FY;       // "中間"
        public String FYE;      // "Company / Interim"
        public String EPS;      // "-"
        public String CPTLPSTK; // "-"
        public String ROE;      // "-"
        public String PER;      // "-"
        public String PBR;      // "-"
        public String DIVD;     // "0.00"
        public String KABU;     // "-"
	}

	private static List<OHLCV> getPriceList(Data data) {
		List<OHLCV> priceList = new ArrayList<>();
		{
			var stockCode = StockCodeJP.toStockCode5(data.TTCODE2);
//			logger.info("stock  {}  {}", stockCode, data.FLLN);

			BigDecimal o = null;
			BigDecimal h = null;
			BigDecimal l = null;
			BigDecimal c = null;
			
			for(var ohlcvString: data.A_HISTDAYL.split("\\n")) {
				String[] valueString = ohlcvString.split(",");
				if (valueString.length < 6) {
					logger.error("Unexpected ohlcvString");
					logger.error("  stockCode    {}", stockCode);
					logger.error("  valueString  {}", valueString.length);
					logger.error("  ohlcvString  {}", ohlcvString);
					throw new UnexpectedException("Unexpected ohlcvString");
				}
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
	
	private static void updatePrice(Result result) {
		if (result.section1.data == null) {
			logger.error("data is null");
			throw new UnexpectedException("data is null");
		}
		
		for(var data: result.section1.data.values()) {
			var stockCode = StockCodeJP.toStockCode5(data.TTCODE2);
			var oldList   = StorageJPX.StockPriceJPX.getList(stockCode);
			var oldMap    = oldList.stream().collect(Collectors.toMap(o -> o.date, o -> o));

			var newList   = getPriceList(data);
			Collections.sort(newList);
			var newMap    = newList.stream().collect(Collectors.toMap(o -> o.date, o -> o));
			
			boolean needsMergeList = true;
			{
				var date = MarketHoliday.JP.getPreviousTradingDate(lastTradingDate);
				
				if (oldMap.containsKey(date) && newMap.containsKey(date)) {
					var oldPrice = oldMap.get(date);
					var newPrice = newMap.get(date);
					
					if (oldPrice.equals(newPrice)) {
						// expected
					} else {
						// not expected
						logger.info("price changed  {}  {}  {}", stockCode, oldPrice, newPrice);
						needsMergeList = false;
						break;
					}
				}
			}
			
			if (needsMergeList) {
				for(var oldPrice: oldList) {
					var date = oldPrice.date;
					if (newMap.containsKey(date)) {
						if (date.equals(lastTradingDate)) {
							// keep price in newList for lastTradingDate
						} else {
							// exist in newList
							var newPrice = newMap.get(date);
							if (newPrice.equals(oldPrice)) {
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
					} else {
						// not exist in newList
						newList.add(oldPrice);
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
			updatePrice(result);
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

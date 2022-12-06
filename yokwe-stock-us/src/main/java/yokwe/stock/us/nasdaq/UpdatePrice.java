package yokwe.stock.us.nasdaq;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.us.Symbol;
import yokwe.stock.us.nasdaq.api.API;
import yokwe.stock.us.nasdaq.api.AssetClass;
import yokwe.stock.us.nasdaq.api.Historical;
import yokwe.util.MarketHoliday;
import yokwe.util.StringUtil;

public class UpdatePrice {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static class Request {
		public String     symbol;     // normalized symbol like TRNT-A and RDS.A not like TRTN^A and RDS/A
		public AssetClass assetClass; // STOCKS or ETF
		public LocalDate  fromDate;
		public LocalDate  toDate;
		
		public Request(String symbol, AssetClass assetClass, LocalDate fromDate, LocalDate toDate) {
			this.symbol     = symbol;
			this.assetClass = assetClass;
			this.fromDate   = fromDate;
			this.toDate     = toDate;
		}

		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static void update(Map<String, StockPrice> stockPriceMap, Request request) {
		final String symbol = request.symbol;
		Historical historical = Historical.getInstance(symbol, request.assetClass, request.fromDate, request.toDate);
		
		if (historical == null) {
			logger.warn("no historical {}", symbol);
			return;
		}
		if (historical.data == null) {
			logger.warn("no data {}", symbol);
			return;
		}
		if (historical.data.tradesTable == null) {
			logger.warn("no tradesTable {}", symbol);
			return;
		}
		if (historical.data.tradesTable.rows == null) {
			logger.warn("no rows {}", symbol);
			return;
		}
		
		List<Price> list = new ArrayList<>();
		{
			String  toDateStrng = request.toDate.toString();
			boolean containsToDate = false;
			
			for(var e: historical.data.tradesTable.rows) {
				// close: "194.39", date: "11/26/2021", high: "196.82", low: "194.19", open: "196.82", volume: "11,113"
				// close: "$17.86", date: "11/26/2021", high: "$18.155", low: "$17.765", open: "$18.03", volume: "1,645,865
				String date  = API.convertDate(e.date);
				String open  = e.open.replace("$", "");
				String high  = e.high.replace("$", "");
				String low   = e.low.replace("$", "");
				String close = e.close.replace("$", "");
				String value = e.volume.replace(",", "").replace("N/A", "0");
				
				if (date.equals(toDateStrng)) containsToDate = true;
				
				Price price = new Price(
					symbol,
					date,
					Double.parseDouble(open),
					Double.parseDouble(high),
					Double.parseDouble(low),
					Double.parseDouble(close),
					Long.parseLong(value));
				
				list.add(price);
			}
			if (!containsToDate) {
				logger.warn("no toDate data {}", symbol);
				return;
			}
		}
		
		// read existing data
		Map<String, Price> map = Price.getMap(symbol);
		
		for(var price: list) {
			String date = price.date;
			
			if (map.containsKey(date)) {
				Price old = map.get(date);
				if (price.equals(old)) {
					// OK
				} else {
					logger.warn("Overwrite");
					logger.warn("  old {}", old);
					logger.warn("  new {}", price);
					map.put(date, price);
				}
			} else {
				map.put(date, price);
			}
		}

		// save
		Price.save(map.values());
		
		// update stockPriceMap
		{
			List<Price> priceList = map.values().stream().collect(Collectors.toList());
			Collections.sort(priceList);
			updateStockPriceMap(stockPriceMap, symbol, priceList);
			
			// save stockPriceMap
			StockPrice.save(stockPriceMap.values());
		}
	}
	
	private static void updateStockPriceMap(Map<String, StockPrice> stockPriceMap, String symbol, List<Price> priceList) {
		final StockPrice stockPrice;
		if (stockPriceMap.containsKey(symbol)) {
			stockPrice = stockPriceMap.get(symbol);
		} else {
			stockPrice = new StockPrice();
			stockPrice.symbol = symbol;
			stockPriceMap.put(symbol, stockPrice);
		}
		
		if (priceList.isEmpty()) {
			stockPrice.dateFirst = StockPrice.DEFAULT_DATE;
			stockPrice.dateLast  = StockPrice.DEFAULT_DATE;
			stockPrice.closeLast = 0;
		} else {
			Price firstPrice = priceList.get(0);
			Price lastPrice  = priceList.get(priceList.size() - 1);
			
			stockPrice.dateFirst = firstPrice.date.toString();
			stockPrice.dateLast  = lastPrice.date.toString();
			stockPrice.closeLast = lastPrice.close;
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		// build toDate and fromDate
		final LocalDate lastTradingDate         = MarketHoliday.US.getLastTradingDate();
		final LocalDate lastTradingDatePrevious = MarketHoliday.US.getPreviousTradingDate(lastTradingDate);
		final LocalDate lastTradingDateMinus1Year;
		{
			LocalDate date = lastTradingDate.minusYears(1);
			lastTradingDateMinus1Year = MarketHoliday.US.isClosed(date) ? MarketHoliday.US.getPreviousTradingDate(date) : date;
		}
		logger.info("date range {} - {}", lastTradingDateMinus1Year, lastTradingDate);
		
		// read existing StockPrice
		Map<String, StockPrice> stockPriceMap = StockPrice.getMap();
		//  symbol
		
		// assetMap
		Map<String, AssetClass> assetMap = NASDAQSymbol.getList().stream().collect(Collectors.toMap(o -> o.symbol, o -> o.type.toAssetClass()));
		//  symbol
		
		// FIXME handle delisting of symbol properly
		
		// build requestList
		List<Request> requestList = new ArrayList<>();
		{
			List<Symbol> symbolList = Symbol.getList();
			logger.info("symbol    {}", symbolList.size());

			int countCaseA = 0;
			int countCaseB = 0;
			int countCaseC = 0;
			int countCaseD = 0;
			int countCaseE = 0;
			for(var e: symbolList) {
				String     symbol     = e.symbol;
				
				AssetClass assetClass = assetMap.get(symbol);
				if (assetClass == null) {
					logger.warn("Unknown symbol {}", symbol);
					continue;
//					logger.error("Unknown symbol");
//					logger.error("   symbol {}", symbol);
//					throw new UnexpectedException("Unknown symbol");
				}
				
				// read existing price
				List<Price> priceList = Price.getList(symbol);
				
				// update stockPriceMap
				updateStockPriceMap(stockPriceMap, symbol, priceList);
				StockPrice stockPrice = stockPriceMap.get(symbol);
				
				final LocalDate myFromDate;
				final LocalDate myToDate;

				if (stockPrice.isEmpty()) {
					// whole
					myFromDate = lastTradingDateMinus1Year;
					myToDate   = lastTradingDate;
					logger.info("A {} {} {}", myFromDate, myToDate, symbol);
					countCaseA++;
				} else {
					// date of existing data
					LocalDate dateFirst = LocalDate.parse(stockPrice.dateFirst);
					LocalDate dateLast  = LocalDate.parse(stockPrice.dateLast);
					
					if (dateLast.isEqual(lastTradingDate)) {
						// already processed at lastTradingDate
						countCaseB++;
//						logger.info("B {}-{}  {}-{}  {}", dateFirst, dateLast, myFromDate, myToDate, symbol);
						continue;
					} else if (dateLast.isEqual(lastTradingDatePrevious)) {
						// already processed at lastTradingDatePrevious
						myFromDate = lastTradingDatePrevious;
						myToDate   = lastTradingDate;
						countCaseC++;
//						logger.info("C {}-{}  {}-{}  {}", dateFirst, dateLast, myFromDate, myToDate, symbol);						
					} else if (dateFirst.isBefore(lastTradingDateMinus1Year) || dateFirst.isEqual(lastTradingDateMinus1Year)) {
						myFromDate = MarketHoliday.US.getNextTradingDate(dateLast);
						myToDate   = lastTradingDate;
						countCaseD++;
//						logger.info("D {}-{}  {}-{}  {}", dateFirst, dateLast, myFromDate, myToDate, symbol);
					} else {
						// whole
						myFromDate = lastTradingDateMinus1Year;
						myToDate   = lastTradingDate;
						countCaseE++;
//						logger.info("E {}-{}  {}-{}  {}", dateFirst, dateLast, myFromDate, myToDate, symbol);
					}
				}
				requestList.add(new Request(symbol, assetClass, myFromDate, myToDate));
			}
			
			logger.info("caseA     {}", countCaseA);
			logger.info("caseB     {}", countCaseB);
			logger.info("caseC     {}", countCaseC);
			logger.info("caseD     {}", countCaseD);
			logger.info("caseE     {}", countCaseE);
			logger.info("request   {}", requestList.size());
			
			// save stockPriceMap
			StockPrice.save(stockPriceMap.values());
		}

		{
			int count = 0;
			int toatlCount = requestList.size();
			
			Collections.shuffle(requestList);
			for(var e: requestList) {
				if ((count % 50) == 0) {
					logger.info("{}", String.format("%5d / %5d %s", count, toatlCount, e.symbol));
				}
				count++;
				
				update(stockPriceMap, e);
			}
		}
		logger.info("save {} {}", stockPriceMap.size(), StockPrice.getPath());
		
		logger.info("STOP");
	}
}

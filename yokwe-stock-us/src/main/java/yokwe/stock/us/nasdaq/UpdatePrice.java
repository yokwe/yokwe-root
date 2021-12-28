package yokwe.stock.us.nasdaq;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.us.nasdaq.api.API;
import yokwe.stock.us.nasdaq.api.AssetClass;
import yokwe.stock.us.nasdaq.api.Historical;
import yokwe.util.Market;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class UpdatePrice {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdatePrice.class);
	
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
		// add one day to todate
		Historical historical = Historical.getInstance(request.symbol, request.assetClass, request.fromDate, request.toDate.plusDays(1));
		
		if (historical.data == null) {
			logger.warn("no data {}", request.symbol);
			return; // no data
		}
		if (historical.data.tradesTable == null) {
			logger.warn("no tradesTable {}", request.symbol);
			return; // no data
		}
		if (historical.data.tradesTable.rows == null) {
			logger.warn("no rows {}", request.symbol);
			return; // no data
		}
		
		// read existing data
		Map<String, Price> map = Price.getMap(request.symbol);
		
		for(var e: historical.data.tradesTable.rows) {
			// close: "194.39", date: "11/26/2021", high: "196.82", low: "194.19", open: "196.82", volume: "11,113"
			// close: "$17.86", date: "11/26/2021", high: "$18.155", low: "$17.765", open: "$18.03", volume: "1,645,865
			String date  = API.convertDate(e.date);
			String open  = e.open.replace("$", "");
			String high  = e.high.replace("$", "");
			String low   = e.low.replace("$", "");
			String close = e.close.replace("$", "");
			String value = e.volume.replace(",", "").replace("N/A", "0");
			
			Price price = new Price(
				request.symbol,
				date,
				Double.parseDouble(open),
				Double.parseDouble(high),
				Double.parseDouble(low),
				Double.parseDouble(close),
				Long.parseLong(value));
			
			if (map.containsKey(date)) {
				Price old = map.get(date);
				if (price.equals(old)) {
					// OK
				} else {
					logger.error("Unpexpected");
					logger.error("  old {}", old);
					logger.error("  new {}", price);
					throw new UnexpectedException("Unpexpected");
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
			updateStockPriceMap(stockPriceMap, request.symbol, priceList);
			
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
			stockPrice.close     = 0;
		} else {
			Price firstPrice = priceList.get(0);
			Price lastPrice  = priceList.get(priceList.size() - 1);
			
			stockPrice.dateFirst = firstPrice.date.toString();
			stockPrice.dateLast  = lastPrice.date.toString();
			stockPrice.close     = lastPrice.close;
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		// build toDate and fromDate
		final LocalDate toDate   = Market.getLastTradingDate();
		final LocalDate fromDate;
		{
			LocalDate date = toDate.minusYears(1);
			fromDate = Market.isClosed(date) ? Market.getPreviousTradeDate(date) : date;
		}
		logger.info("date range {} - {}", fromDate, toDate);
		
		// read existing StockPrice
		Map<String, StockPrice> stockPriceMap = StockPrice.getMap();
		//  symbol
		
		// build requestList
		List<Request> requestList = new ArrayList<>();
		{
			List<Symbol> symbolList = Symbol.getList();
			logger.info("symbol    {}", symbolList.size());

			int countCaseA = 0;
			int countCaseB = 0;
			int countCaseC = 0;
			int countCaseD = 0;
			for(Symbol e: symbolList) {
				String symbol = e.symbol;
				
				// read existing price
				List<Price> priceList = Price.getList(symbol);
				
				// update stockPriceMap
				updateStockPriceMap(stockPriceMap, symbol, priceList);
				StockPrice stockPrice = stockPriceMap.get(symbol);
				
				final LocalDate myFromDate;
				final LocalDate myToDate;

				if (stockPrice.isEmpty()) {
					// whole
					myFromDate = fromDate;
					myToDate   = toDate;
					logger.info("A {} {} {}", myFromDate, myToDate, symbol);
					countCaseA++;
				} else {
					// date of existing data
					LocalDate dateFirst = LocalDate.parse(stockPrice.dateFirst);
					LocalDate dateLast  = LocalDate.parse(stockPrice.dateLast);
					
					if (dateLast.isEqual(toDate)) {
						// already processed
						countCaseB++;
//						logger.info("B {}-{}  {}", dateFirst, dateLast, symbol);
						continue;
					} else if (dateFirst.isBefore(fromDate) || dateFirst.isEqual(fromDate)) {
						myFromDate = Market.getNextTradeDate(dateLast);
						myToDate   = toDate;
						countCaseC++;
//						logger.info("C {}-{}  {}-{}  {}", dateFirst, dateLast, myFromDate, myToDate, symbol);
					} else {
						myFromDate = fromDate;
						myToDate   = toDate;
						countCaseD++;
//						logger.info("D {}-{}  {}-{}  {}", dateFirst, dateLast, myFromDate, myToDate, symbol);
					}
				}
				requestList.add(new Request(e.symbol, e.assetClass, myFromDate, myToDate));
			}
			
			logger.info("caseA     {}", countCaseA);
			logger.info("caseB     {}", countCaseB);
			logger.info("caseC     {}", countCaseC);
			logger.info("caseD     {}", countCaseD);
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

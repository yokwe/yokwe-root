package yokwe.stock.us.nasdaq;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import yokwe.stock.us.Storage;
import yokwe.stock.us.nasdaq.api.Quote;
import yokwe.util.CSVUtil;
import yokwe.util.Market;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class UpdatePrice {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdatePrice.class);
	
	public static class Request {
		public String symbol;     // normalized symbol like TRNT-A and RDS.A not like TRTN^A and RDS/A
		public String assetClass; // STOCKS or ETF
		
		public Request(String symbol, String assetClass) {
			this.symbol     = symbol;
			this.assetClass = assetClass;
		}

		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static class Duration {
		public static enum Unit {
			YEAR, MONTH, WEEK, DAY
		}
		
		public static Duration Year(int value) {
			return new Duration(Unit.YEAR, value);
		}
		public static Duration Month(int value) {
			return new Duration(Unit.MONTH, value);
		}
		public static Duration Week(int value) {
			return new Duration(Unit.WEEK, value);
		}
		public static Duration Day(int value) {
			return new Duration(Unit.DAY, value);
		}
		
		public Unit unit;
		public int  value;
		
		public Duration(Unit unit, int value) {
			this.unit  = unit;
			this.value = value;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
		
		public LocalDate apply(LocalDate date) {
			switch(unit) {
			case YEAR:
				return date.minusYears(value);
			case MONTH:
				return date.minusMonths(value);
			case WEEK:
				return date.minusWeeks(value);
			case DAY:
				return date.minusDays(value);
			default:
				logger.error("Unexpected");
				logger.error("  {}", this);
				throw new UnexpectedException("Unexpected");
			}
		}
	}
	
	public static void update(Request request, LocalDate fromDate, LocalDate toDate) {
		Quote.Historical historical = Quote.Historical.getInstance(request.assetClass, request.symbol, fromDate, toDate);
		
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
		
		Map<String, Price> map = Price.getMap(request.symbol);
		
		for(var e: historical.data.tradesTable.rows) {
			// close: "194.39", date: "11/26/2021", high: "196.82", low: "194.19", open: "196.82", volume: "11,113"
			// close: "$17.86", date: "11/26/2021", high: "$18.155", low: "$17.765", open: "$18.03", volume: "1,645,865
			String date  = Quote.convertDate(e.date);
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
	}
	
	public static class Symbol {
		public String symbol;
		
		public Symbol() {
			symbol = null;
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		// build toDate and fromDate
		LocalDate toDate   = Market.getLastTradingDate();
		LocalDate fromDate;
		{
			Duration duration = Duration.Year(1);
			logger.info("duration {}", duration);
					
			fromDate = duration.apply(toDate);
			if (Market.isClosed(fromDate)) {
				fromDate = Market.getPreviousTradeDate(fromDate);
			}
			logger.info("date range {} - {}", fromDate, toDate);
		}
		
		// build requestList
		List<Request> requestList = new ArrayList<>();
		{
			List<Symbol> symbolList = CSVUtil.read(Symbol.class).file(Storage.NASDAQ.getPath("symbol.csv"));
			logger.info("symbol    {}", symbolList.size());

			String             toDateString = toDate.toString();
			List<String>       unknownList  = new ArrayList<>();
			Map<String, Stock> stockMap     = Stock.getMap();
			
			int countProcessed = 0;
			for(var e: symbolList) {
				String symbol = e.symbol;
				if (stockMap.containsKey(symbol)) {
					Stock stock = stockMap.get(symbol);
					
					Set<String> dateSet = Price.getList(symbol).stream().map(o -> o.date).collect(Collectors.toSet());
					if (dateSet.contains(toDateString)) {
						// already processed
						countProcessed++;
					} else {
						requestList.add(new Request(stock.symbol, stock.assetClass));
					}
				} else {
					unknownList.add(symbol);
//					logger.warn("Unknown symbol {}", symbol);
				}
			}
			logger.info("unknown   {} {}", unknownList.size(), unknownList);
			logger.info("processed {}", countProcessed);
			logger.info("request   {}", requestList.size());
		}
		

		{
			int count = 0;
			int toatlCount = requestList.size();
			for(var e: requestList) {
				if ((count % 100) == 0) {
					logger.info("{}", String.format("%5d / %5d %s", count, toatlCount, e.symbol));
				}
				count++;
				
				update(e, fromDate, toDate);
			}
		}
		
		logger.info("STOP");
	}
}

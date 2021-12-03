package yokwe.stock.us.nasdaq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import yokwe.stock.us.Storage;
import yokwe.stock.us.nasdaq.api.Dividends;
import yokwe.stock.us.nasdaq.api.Quote;
import yokwe.util.CSVUtil;
import yokwe.util.DoubleUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class UpdateDividend {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateDividend.class);
	
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

	public static void update(Request request) {
		logger.info("update {}", request.symbol); // FIXME
		
		Map<String, Dividend> map = Dividend.getMap(request.symbol);
		if (map.size() != 0) return; // FIXME
		
		Dividends dividends = Dividends.getInstance(request.assetClass, request.symbol, 16); // up to 1 years  16 = 12 + 4
		// {"data":{"exDividendDate":"N/A","dividendPaymentDate":"N/A","yield":"N/A","annualizedDividend":"N/A","payoutRatio":"N/A","dividends":{"headers":null,"rows":null}},
		
		if (dividends.data == null) {
			logger.warn("data is null {}", request.symbol);
			return;
		}
		if (dividends.data.dividends == null) {
			logger.warn("dividends is null {}", request.symbol);
			return;
		}
		if (dividends.data.dividends.rows == null) {
//			logger.warn("rows is null {}", request.symbol);
			return;
		}
		// if no data, just return
		if (dividends.data.dividends.rows.length == 0) {
			logger.warn("rows.length == 0 {}", request.symbol);
			return;
		}
		
		for(var e: dividends.data.dividends.rows) {
			// "exOrEffDate":"11/27/2013","type":"CASH","amount":"$1.33","declarationDate":"09/26/2013","recordDate":"12/02/2013","paymentDate":"12/27/2013
			// "exOrEffDate":"10/27/2021","type":"CASH","amount":"$0.12","declarationDate":"01/18/2021","recordDate":"10/28/2021","paymentDate":"10/29/2021"
			String type       = e.type;
			String amount     = e.amount.replace("$", "");
			String declDate   = Quote.convertDate(e.declarationDate);
			String recordDate = Quote.convertDate(e.recordDate);
			String payDate    = Quote.convertDate(e.paymentDate);
			
			if (amount.isEmpty()) {
				logger.warn("amount is empty");
				logger.warn("  {}", e);
				continue;
			}
			if (recordDate.isEmpty()) {
				logger.warn("recordDate is empty");
				logger.warn("  {}", e);
				continue;
			}
			
			// 	public Dividend(String symbol, String type, double amount, String declDate, String recordDate, String payDate) {

			Dividend dividend = new Dividend(
				request.symbol,
				type,
				Double.parseDouble(amount),
				declDate,
				recordDate,
				payDate
				);
			
			if (map.containsKey(recordDate)) {
				Dividend old = map.get(recordDate);
				if (dividend.equals(old)) {
					// OK
				} else {
					if (DoubleUtil.isAlmostEqual(old.amount, dividend.amount) || old.recordDate.equals(dividend.recordDate)) {
						// overwrite
						logger.warn("overwrite");
						logger.warn("  old {}", old);
						logger.warn("  new {}", dividend);
						map.put(recordDate, dividend);
					} else {
						logger.error("Unpexpected");
						logger.error("  old {}", old);
						logger.error("  new {}", dividend);
						throw new UnexpectedException("Unpexpected");
					}
				}
			} else {
				map.put(recordDate, dividend);
			}
		}

		// save
		Dividend.save(map.values());
	}

	public static class Symbol {
		public String symbol;
		
		public Symbol() {
			symbol = null;
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		// build symbolList from symbol.csv
		List<Symbol> symbolList = CSVUtil.read(Symbol.class).file(Storage.NASDAQ.getPath("symbol.csv"));
		logger.info("symbol    {}", symbolList.size());
		
		// build requestList
		List<Request> requestList = new ArrayList<>();
		{
			List<String> unknownList = new ArrayList<>();
			Map<String, Stock> stockMap = Stock.getMap();
			for(var e: symbolList) {
				String symbol = e.symbol;
				
				if (!Dividend.getList(symbol).isEmpty()) continue;
				
				if (stockMap.containsKey(symbol)) {
					Stock stock = stockMap.get(symbol);
					requestList.add(new Request(stock.symbol, stock.assetClass));
				} else {
					unknownList.add(symbol);
//					logger.warn("Unknown symbol {}", symbol);
				}
			}
			logger.info("unknown   {} {}", unknownList.size(), unknownList);
			logger.info("request   {}", requestList.size());
		}
		

		int count = 0;
		int toatlCount = requestList.size();
		for(var e: requestList) {
			if ((count % 100) == 0) {
				logger.info("{}", String.format("%5d / %5d %s", count, toatlCount, e.symbol));
			}
			count++;
			
			update(e);
		}
		
		logger.info("STOP");
	}

}

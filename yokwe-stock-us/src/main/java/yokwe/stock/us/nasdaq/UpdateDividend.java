package yokwe.stock.us.nasdaq;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.us.nasdaq.api.API;
import yokwe.stock.us.nasdaq.api.AssetClass;
import yokwe.stock.us.nasdaq.api.Dividends;
import yokwe.util.StringUtil;
import yokwe.util.stats.DoubleStreamUtil;

public class UpdateDividend {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateDividend.class);
	
	public static class Request {
		public String     symbol;     // normalized symbol like TRNT-A and RDS.A not like TRTN^A and RDS/A
		public AssetClass assetClass; // STOCKS or ETF
		
		public Request(String symbol, AssetClass assetClass) {
			this.symbol     = symbol;
			this.assetClass = assetClass;
		}

		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}

	public static void update(Map<String, StockDividend> stockDividendMap, Request request) {
//		logger.info("update {}", request.symbol); // FIXME
		
		Map<String, Dividend> map = Dividend.getMap(request.symbol);
		
		Dividends dividends = Dividends.getInstance(request.symbol, request.assetClass, 16); // up to 1 years  16 = 12 + 4
		// {"data":{"exDividendDate":"N/A","dividendPaymentDate":"N/A","yield":"N/A","annualizedDividend":"N/A","payoutRatio":"N/A","dividends":{"headers":null,"rows":null}},
		
		if (dividends.data == null) {
			logger.warn("data is null {}", request.symbol);
			// Add dummy entry
			StockDividend stockDividend = new StockDividend(request.symbol);
			stockDividendMap.put(request.symbol, stockDividend);
			StockDividend.save(stockDividendMap.values());
			return;
		}
		if (dividends.data.dividends == null) {
			logger.warn("dividends is null {}", request.symbol);
			// Add dummy entry
			StockDividend stockDividend = new StockDividend(request.symbol);
			stockDividendMap.put(request.symbol, stockDividend);
			StockDividend.save(stockDividendMap.values());
			return;
		}
		if (dividends.data.dividends.rows == null) {
//			logger.warn("rows is null {}", request.symbol);
			// Add dummy entry
			StockDividend stockDividend = new StockDividend(request.symbol);
			stockDividendMap.put(request.symbol, stockDividend);
			StockDividend.save(stockDividendMap.values());
			return;
		}
		// if no data, just return
		if (dividends.data.dividends.rows.length == 0) {
			logger.warn("rows.length == 0 {}", request.symbol);
			// Add dummy entry
			StockDividend stockDividend = new StockDividend(request.symbol);
			stockDividendMap.put(request.symbol, stockDividend);
			StockDividend.save(stockDividendMap.values());
			return;
		}
		
		for(var e: dividends.data.dividends.rows) {
			// "exOrEffDate":"11/27/2013","type":"CASH","amount":"$1.33","declarationDate":"09/26/2013","recordDate":"12/02/2013","paymentDate":"12/27/2013
			// "exOrEffDate":"10/27/2021","type":"CASH","amount":"$0.12","declarationDate":"01/18/2021","recordDate":"10/28/2021","paymentDate":"10/29/2021"
			String type       = e.type;
			String amount     = e.amount.replace("$", "");
			String declDate   = API.convertDate(e.declarationDate);
			String exDate     = API.convertDate(e.exOrEffDate);
			String recordDate = API.convertDate(e.recordDate);
			String payDate    = API.convertDate(e.paymentDate);
			
			if (amount.isEmpty()) {
				logger.warn("amount is empty  {}  {}", request.symbol, e);
				continue;
			}
			if (exDate.isEmpty()) {
				logger.warn("exDate is empty  {}  {}", request.symbol, e);
				continue;
			}
			
			// 	public Dividend(String symbol, String type, double amount, String declDate, String recordDate, String payDate) {

			Dividend dividend = new Dividend(
				request.symbol,
				type,
				Double.parseDouble(amount),
				declDate,
				exDate,
				recordDate,
				payDate
				);
			
			if (map.containsKey(exDate)) {
				Dividend old = map.get(exDate);
				if (dividend.equals(old)) {
					// OK
				} else {
					logger.warn("Unpexpected not equal");
					logger.warn("  old {}", old);
					logger.warn("  new {}", dividend);
					map.put(exDate, dividend);
				}
			} else {
				map.put(exDate, dividend);
			}
		}

		// save
		Dividend.save(map.values());
		
		// update stockDividendMap
		{
			List<Dividend> divList = map.values().stream().collect(Collectors.toList());
			Collections.sort(divList);
			updateStockDividendMap(stockDividendMap, request.symbol, divList);
			
			// save stockPriceMap
			StockDividend.save(stockDividendMap.values());
		}
	}
	
	private static void updateStockDividendMap(Map<String, StockDividend> stockDividendMap, String symbol, List<Dividend> dividendList) {
		final StockDividend stockDividend;
		
		if (stockDividendMap.containsKey(symbol)) {
			stockDividend = stockDividendMap.get(symbol);
		} else {
			stockDividend = new StockDividend(symbol);
			stockDividendMap.put(symbol, stockDividend);
		}

		stockDividend.annual     = 0;
		stockDividend.count      = 0;
		if (dividendList.isEmpty()) {
			stockDividend.lastExDate = StockDividend.DEFAULT_DATE;
		} else {
			// calculate annual
			LocalDate dateLast  = LocalDate.now();
			LocalDate dateFirst = dateLast.minusYears(1).minusDays(5);
			
			List<Dividend> list = new ArrayList<>();
			for(var e: dividendList) {
				// Use only CASH dividend
				if (e.type.equals("CASH")) {
					LocalDate exDate = LocalDate.parse(e.exDate);
					if ((exDate.isAfter(dateFirst) && exDate.isBefore(dateLast)) || exDate.isEqual(dateFirst) || exDate.isEqual(dateLast)) {
						list.add(e);
					}
				}
			}
			
			// FIXME remove odd data using standard deviation
			{
				// Use only CASH dividend
				double[] values = dividendList.stream().filter(o -> o.type.equals("CASH")).mapToDouble(o -> o.amount).toArray();
				DoubleStreamUtil.Stats stats = new DoubleStreamUtil.Stats(values);
				
				double mean  = stats.getMean();
				double sigma = stats.getStandardDeviation();
				double maxValue = mean + (3 * sigma);
				
				var i = list.iterator();
				while(i.hasNext()) {
					Dividend dividend = i.next();
					if (maxValue < dividend.amount) {
						i.remove();
						logger.warn("remove odd data {}", String.format("%-5s  %2d  %.4f  %.4f  %s  %.4f", symbol, values.length, mean, sigma, dividend.exDate, dividend.amount));
					}
				}
			}
			
			for(var e: list) {
				stockDividend.lastExDate = e.exDate;
				stockDividend.annual += e.amount;
				stockDividend.count++;
			}
		}
	}

	public static void main(String[] args) {
		logger.info("START");
		
		// read existing StockDividend
		Map<String, StockDividend> stockDividendMap = StockDividend.getMap();
		//  symbol
		logger.info("map       {}", stockDividendMap.size());
		
		// build requestList
		List<Request> requestList = new ArrayList<>();
		{
			List<Symbol> symbolList = Symbol.getList();
			logger.info("symbol    {}", symbolList.size());

			int countCaseA = 0;
			int countCaseB = 0;
			int countCaseC = 0;
			int countCaseD = 0;
			for(var e: symbolList) {
				String symbol = e.symbol;
				
				final StockDividend stockDividend;
				if (stockDividendMap.containsKey(symbol)) {
					// already execute once
					stockDividend = stockDividendMap.get(symbol);
					
					if (stockDividend == null) {
						logger.warn("no stockDividend {}", symbol);
						return;
					}
					
					if (stockDividend.isEmpty()) {
						countCaseA++;
						// no dividend stock
						continue;
					} else {
						// read existing dividend
						List<Dividend> dividendList = Dividend.getList(symbol);
						if (dividendList.isEmpty()) {
							countCaseB++;
							// assume data
							logger.warn("expect dividend data");
							logger.warn("  symbol {}", symbol);
						} else {
							countCaseC++;
							updateStockDividendMap(stockDividendMap, symbol, dividendList);
						}
						requestList.add(new Request(e.symbol, e.assetClass));
					}
				} else {
					countCaseD++;
					// new symbol
					// want to process
					requestList.add(new Request(e.symbol, e.assetClass));
				}
			}
			logger.info("caseA     {}", countCaseA);
			logger.info("caseB     {}", countCaseB);
			logger.info("caseC     {}", countCaseC);
			logger.info("caseD     {}", countCaseD);
			logger.info("request   {}", requestList.size());
			logger.info("map       {}", stockDividendMap.size());
			
			// save stockPriceMap
			StockDividend.save(stockDividendMap.values());
		}
		

		int count = 0;
		int toatlCount = requestList.size();
		Collections.shuffle(requestList);
		for(var e: requestList) {
			if ((count % 50) == 0) {
				logger.info("{}", String.format("%5d / %5d %s", count, toatlCount, e.symbol));
			}
			count++;
			
			update(stockDividendMap, e);
		}
		logger.info("save {} {}", stockDividendMap.size(), StockDividend.getPath());
		
		logger.info("STOP");
	}

}

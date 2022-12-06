package yokwe.stock.us.nasdaq;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.stock.us.Symbol;
import yokwe.stock.us.SymbolInfo;
import yokwe.util.DoubleUtil;
import yokwe.util.MarketHoliday;
import yokwe.util.stats.DoubleArray;
import yokwe.util.stats.DoubleStreamUtil;
import yokwe.util.stats.HV;
import yokwe.util.stats.MA;
import yokwe.util.stats.RSI;

public class UpdateStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static List<Stats> getStatsList(Set<String> dateSet) {
		List<Stats> statsList = new ArrayList<>();
		Map<String, StockPrice> map = StockPrice.getMap();
		
		Map<String, StockDividend> stockDividendMap = StockDividend.getMap();
		Map<String, SymbolInfo>    nasdaqMap        = NASDAQSymbol.getMap();
		
		for(var e: Symbol.getList()) {
			final String symbol = e.symbol;
			final String assetClass;
			final String name;
			{
				if (nasdaqMap.containsKey(symbol)) {
					SymbolInfo symbolInfo = nasdaqMap.get(symbol);
					
					assetClass = symbolInfo.type.toAssetClass().toString();
					name       = symbolInfo.name;
				} else {
					logger.warn("Skip unknown symbol {}", symbol);
					continue;
//					logger.error("Unknown symbol");
//					logger.error("  symbol {}!", symbol);
//					throw new UnexpectedException("Unknown symbol");
				}
			}
			
			List<Price> priceList = Price.getList(symbol).stream().filter(o -> dateSet.contains(o.date)).collect(Collectors.toList());
			if (priceList.isEmpty()) continue;

			int pricec = priceList.size();
			double[] closeArray  = priceList.stream().mapToDouble(o -> o.close).toArray();
			double[] volumeArray = priceList.stream().mapToDouble(o -> o.volume).toArray();
			
			StockPrice stockPrice = map.get(symbol);
			
			Price price = priceList.get(pricec - 1);
			Stats statsUS = new Stats();
			
			statsUS.stockCode = symbol;
			statsUS.type      = assetClass;
			statsUS.name      = name;
			statsUS.date      = stockPrice.dateLast;
			
			statsUS.pricec    = pricec;
			statsUS.price     = price.close;
			
			// last
			if (2 <= pricec) {
				Price last = priceList.get(pricec - 2);
				statsUS.last    = last.close;
				statsUS.lastPCT = DoubleUtil.round((statsUS.price - statsUS.last) / statsUS.last, 3) ;
			} else {
				statsUS.last    = -1;
				statsUS.lastPCT = -1;
			}
			
			// stats - sd hv rsi
			if (30 <= pricec) {
				double logReturn[] = DoubleArray.logReturn(closeArray);
				DoubleStreamUtil.Stats stats = new DoubleStreamUtil.Stats(logReturn);
				
				double sd = stats.getStandardDeviation();
				statsUS.sd = Double.isNaN(sd) ? -1 : DoubleUtil.round(sd, 4);

				HV hv = new HV(closeArray);
				statsUS.hv = Double.isNaN(hv.getValue()) ? -1 : DoubleUtil.round(hv.getValue(), 4);
			} else {
				statsUS.sd = -1;
				statsUS.hv = -1;
			}
			if (RSI.DEFAULT_PERIDO <= pricec) {
				RSI rsi = new RSI();
				Arrays.stream(closeArray).forEach(rsi);
				double rsiValue = rsi.getValue();
				if (Double.isFinite(rsiValue)) {
					statsUS.rsi = DoubleUtil.round(rsi.getValue(), 1);
				} else {
					statsUS.rsi = -1;
				}
			} else {
				statsUS.rsi = -1;
			}
			
			// min max
			statsUS.min       = priceList.stream().mapToDouble(o -> o.low).min().getAsDouble();
			statsUS.max       = priceList.stream().mapToDouble(o -> o.high).max().getAsDouble();
			statsUS.minPCT    = DoubleUtil.round((statsUS.price - statsUS.min) / statsUS.price, 3);
			statsUS.maxPCT    = DoubleUtil.round((statsUS.max - statsUS.price) / statsUS.price, 3);
			
			// dividend
			{
				StockDividend stockDividend = stockDividendMap.get(symbol);
				if (stockDividend == null) {
					statsUS.div   = 0;
					statsUS.divc  = 0;
					statsUS.yield = 0;
				} else {
					statsUS.div   = stockDividend.annual;
					statsUS.divc  = stockDividend.count;
					statsUS.yield = DoubleUtil.round(statsUS.div / statsUS.price, 3);
				}
			}
			
			// volume
			statsUS.vol       = price.volume;
			if (5 <= pricec) {
				MA vol5 = MA.sma(5, volumeArray);
				statsUS.vol5 = (long)vol5.getValue();
			} else {
				statsUS.vol5 = -1;
			}
			if (20 <= pricec) {
				MA vol21 = MA.sma(21, volumeArray);
				statsUS.vol21 = (long)vol21.getValue();
			} else {
				statsUS.vol21 = -1;
			}

			statsList.add(statsUS);
		}
		return statsList;
	}
	private static Set<String> getDateSet() {
		Set<String> dateSet = new TreeSet<>();
		{
			LocalDate lastDate = MarketHoliday.US.getLastTradingDate();
			LocalDate firstDate = lastDate.minusYears(1).plusDays(1);
			if (MarketHoliday.US.isClosed(firstDate)) {
				firstDate = MarketHoliday.US.getPreviousTradingDate(firstDate);
			}
			
			for(LocalDate date = firstDate; date.isEqual(lastDate) || date.isBefore(lastDate); date = MarketHoliday.US.getNextTradingDate(date)) {
				dateSet.add(date.toString());
			}
		}
		return dateSet;
	}
	public static void main(String[] args) {
		logger.info("START");
		
		Set<String> dateSet = getDateSet();
		logger.info("date {}  {} - {}", dateSet.size(), dateSet.stream().min(String::compareTo).get(), dateSet.stream().max(String::compareTo).get());
		
//		int dateCount = dateSet.size();
		List<Stats> statsList = getStatsList(dateSet);
		
		logger.info("save {} {}", statsList.size(), Stats.getPath());
		Stats.save(statsList);
		
		logger.info("STOP");
	}
}

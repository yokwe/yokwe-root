package yokwe.finance.chart;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.DailyValue;
import yokwe.util.UnexpectedException;
import yokwe.util.finance.DailyPriceDiv;
import yokwe.util.finance.Portfolio;

public class EfficientFrontier {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	
	private static final Set<String> stockJPCodeSet;
	private static final Set<String> stockUSCodeSet;
	private static final Set<String> fundJPCodeSet;
	
	private static final Map<String, String> codeNameMap = new HashMap<>();
	//                       code    name
	static {
		var stockInfoJPList = StorageStock.StockInfoJP.getList();
		var stockInfoUSList = StorageStock.StockInfoUSTrading.getList();
		var fundInfoJPList  = StorageFund.FundInfo.getList();
		
		stockJPCodeSet = stockInfoJPList.stream().map(o -> o.stockCode).collect(Collectors.toSet());
		stockUSCodeSet = stockInfoUSList.stream().map(o -> o.stockCode).collect(Collectors.toSet());
		fundJPCodeSet  = fundInfoJPList.stream().map(o -> o.isinCode).collect(Collectors.toSet());
		
		stockInfoJPList.stream().forEachOrdered(o -> codeNameMap.put(o.stockCode, o.name));
		stockInfoUSList.stream().forEachOrdered(o -> codeNameMap.put(o.stockCode, o.name));
		fundInfoJPList.stream().forEachOrdered(o -> codeNameMap.put(o.isinCode, o.name));
	}
	private static String getCodeName(String code) {
		if (codeNameMap.containsKey(code)) {
			return codeNameMap.get(code);
		}
		logger.error("Unexpected code");
		logger.error("  code  {}!", code);
		throw new UnexpectedException("Unexpected code");
	}
	
	private static DailyPriceDiv[] getDailyPriceDiv(String code) {
		DailyValue[] priceArray;
		DailyValue[] divArray;
		if (stockJPCodeSet.contains(code)) {
			priceArray = StorageStock.StockPriceJP.getList(code).stream().map(o -> new DailyValue(o.date, o.close)).toArray(DailyValue[]::new);
			divArray   = StorageStock.StockDivJP.getList(code).stream().toArray(DailyValue[]::new);
		} else if (stockUSCodeSet.contains(code)) {
			priceArray = StorageStock.StockPriceUS.getList(code).stream().map(o -> new DailyValue(o.date, o.close)).toArray(DailyValue[]::new);
			divArray   = StorageStock.StockDivUS.getList(code).stream().toArray(DailyValue[]::new);
		} else if (fundJPCodeSet.contains(code)) {
			priceArray = StorageFund.FundPrice.getList(code).stream().map(o -> new DailyValue(o.date, o.price)).toArray(DailyValue[]::new);
			divArray   = StorageFund.FundDiv.getList(code).stream().toArray(DailyValue[]::new);
		} else {
			logger.error("Unexpected code");
			logger.error("  code  {}!", code);
			throw new UnexpectedException("Unexpected code");
		}
		
		DailyPriceDiv[] dailyPriceDivArray = DailyPriceDiv.toDailyPriceDivArray(
			priceArray, o -> o.date, o -> o.value.doubleValue(),
			divArray,   o -> o.date, o -> o.value.doubleValue());
		
		return dailyPriceDivArray;
	}

	
	public static void main(String[] args) {
		logger.info("START");
		
		String[] codeArray = {
			"EMB",
			"PFF",
			"JEPQ",
//			"MLPA",
			"SJNK",
			"VCLT",
		};
		
		Portfolio portfolio = new Portfolio();
		// build portfolio
		for(var code: codeArray) {
			portfolio.add(code, getDailyPriceDiv(code));
		}
		for(var code: codeArray) {
			logger.info("duration  {}  {}  {}", code, portfolio.holdingDuration(), portfolio.holdingDuration(code));
		}
		
//		int nYear = portfolio.durationInYear();
		logger.info("holdingDuration  {}", portfolio.holdingDuration());
		int nMonth  = portfolio.holdingDuration();
		int nOffset = 0;
		logger.info("duration         {}", nMonth);
		portfolio.duration(nMonth, nOffset);
		
		for(int i = 0; i < codeArray.length; i++) {
			for(int j = 0; j < codeArray.length; j++) {
				if (i == j) break;
				
				String a = codeArray[i];
				String b = codeArray[j];
				
				portfolio.
					clearQuantity().
					quantity(a, 100).
					quantity(b, 100);
				
				logger.info("A {}  {}", a, getCodeName(a));
				logger.info("B {}  {}", b, getCodeName(b));
				logger.info("  cor {}", portfolio.correlation(a, b));
			}
		}
		
		String name = Arrays.stream(codeArray).collect(Collectors.joining("-")) + "-" + nMonth;
		XYSeriesCollection collection = new XYSeriesCollection();
		for(var code: codeArray) {
			XYSeries series = new XYSeries(code);

			portfolio.clearQuantity().quantity(code, 100);
			var sd  = portfolio.risk() * 100;
			var ror = portfolio.rateOfReturn() * 100;
			series.add(sd, ror);

			logger.info("{}  {}  {}", code, portfolio.holdingDuration(code), getCodeName(code));
			logger.info("  sd   {}  {}", sd,  portfolio.risk(code) * 100);
			logger.info("  ror  {}  {}", ror, portfolio.risk(code) * 100);
			collection.addSeries(series);
		}

		{
			XYSeries series  = new XYSeries(name);
			Random random = new Random(System.currentTimeMillis());
			for(int i = 0; i < 10000; i++) {
				portfolio.clearQuantity();
				for(var e: codeArray) {
					portfolio.quantity(e, random.nextInt(1000));
				}
				var sd  = portfolio.risk() * 100;
				var ror = portfolio.rateOfReturn() * 100;
				series.add(sd, ror);
			}
			collection.addSeries(series);
		}
		JFreeChart chart = ChartFactory.createScatterPlot("Efficent Frontier  duration = " + nMonth, "sd", "ror", collection);
		{
			for(var code: codeArray) {
				String text = String.format("%s  %s", code, getCodeName(code));
				TextTitle textTitle = new TextTitle(text);
				textTitle.setHorizontalAlignment(HorizontalAlignment.LEFT);
				textTitle.setMargin(0, 50, 0, 0); // in pixel ?
				chart.addSubtitle(textTitle);
			}
		}
		
		String path = String.format("tmp/chart/T004-%s.png", name);
		File file = new File(path);
		try {
			ChartUtils.saveChartAsPNG(file, chart, 600, 600);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.info("STOP");
	}
}

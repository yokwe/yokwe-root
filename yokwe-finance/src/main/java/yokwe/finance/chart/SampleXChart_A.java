package yokwe.finance.chart;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.OHLCChartBuilder;

import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.OHLCV;
import yokwe.util.MarketHoliday;
import yokwe.util.UnexpectedException;

public class SampleXChart_A {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static class DateFormatter implements Function<Double, String> {
		private final Map<Integer, LocalDate> localDateMap;
		private final DateTimeFormatter dateTimeFormatter;
		DateFormatter(Map<Integer, LocalDate> localDateMap, DateTimeFormatter dateTimeFormatter) {
			this.localDateMap      = localDateMap;
			this.dateTimeFormatter = dateTimeFormatter;
		}
		
		@Override
		public String apply(Double doubleValue) {
			Integer intValue = doubleValue.intValue();
//			logger.info("intValue  {}", intValue);
			
			if (localDateMap.containsKey(intValue)) {
				return localDateMap.get(intValue).format(dateTimeFormatter);
			} else {
				logger.error("Unexpected intValue");
				logger.error("  intValue  {}", intValue);
				throw new UnexpectedException("Unexpected intValue");
			}
		}
	}
	private static void drawChart(String title, int width, int height, List<OHLCV> list) {
    	{
    		var first = list.get(0);
    		var last  = list.get(list.size() - 1);
    		logger.info("list  {}  {}", first.date, last.date);
    	}
    	
		final File file;
		{
			var path = String.format("tmp/chart/xchart-%s.png", title);
			file = new File(path);
		}
		logger.info("file  {}", file.getPath());
		
	    var xData      = new ArrayList<Double>();
	    var openData   = new ArrayList<Double>();
	    var highData   = new ArrayList<Double>();
	    var lowData    = new ArrayList<Double>();
	    var closeData  = new ArrayList<Double>();
	    var volumeData = new ArrayList<Long>();
	    
	    for(int i = 0; i < list.size(); i++) {
	    	var e = list.get(i);
	    	
	    	xData.add((double)i);
	    	openData.add(e.open.doubleValue());
	    	highData.add(e.high.doubleValue());
	    	lowData.add(e.low.doubleValue());
	    	closeData.add(e.close.doubleValue());
	    	volumeData.add(e.volume);
	    }
	    
	    Map<Integer, LocalDate> localDateMap = new TreeMap<>();
	    {
	    	for(int i = 0; i < list.size(); i++) {
	    		localDateMap.put(i,  list.get(i).date);
	    	}
	    	{
	    		var date = list.get(0).date;
	    		for(var i = 1; i < 30; i++) {
	    			date = MarketHoliday.JP.getPreviousTradingDate(date);
	    			localDateMap.put(0 - i, date);
	    		}
	    	}
	    	{
	    		int size = list.size();
	    		var date = list.get(size - 1).date;
	    		for(var i = 0; i < 30; i++) {
	    			date = MarketHoliday.JP.getNextTradingDate(date);
	    			localDateMap.put(size + i, date);
	    		}
	    	}
	    }
	    var formatter = new DateFormatter(localDateMap, DateTimeFormatter.ofPattern("yyyy MM dd"));
	    
		var chart = new OHLCChartBuilder().width(width).height(height).title("Price").build();
		chart.getStyler().setYAxisDecimalPattern("##.00");
		chart.getStyler().setxAxisTickLabelsFormattingFunction(o -> formatter.apply(o));
		chart.getStyler().setPlotBackgroundColor(Color.BLACK);
	    chart.addSeries(title, xData, openData, highData, lowData, closeData, volumeData);
	    
		try {
			BitmapEncoder.saveBitmap(chart, file.getPath(), BitmapFormat.PNG);
		} catch (IOException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	   
	private static void drawChart(String stockCode, int width, int height) {
//		var list = StorageStock.StockPriceJP.getList(stockCode);
		var list = StorageStock.StockPriceUS.getList(stockCode);
		{
			var lastDate  = list.get(list.size() - 1).date;
			var firstDate = lastDate.minusDays(290);
			
			logger.info("date  {}  {}", firstDate, lastDate);
			list.removeIf(o -> o.date.isBefore(firstDate));
		}
		logger.info("list  {}", list.size());
		
		drawChart(stockCode, width, height, list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		drawChart("GOOG", 2000, 800);

		logger.info("STOP");
	}
}

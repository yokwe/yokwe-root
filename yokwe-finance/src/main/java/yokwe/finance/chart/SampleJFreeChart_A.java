package yokwe.finance.chart;

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.Timeline;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.OHLCDataItem;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;

import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.OHLCV;
import yokwe.util.UnexpectedException;

public class SampleJFreeChart_A {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
 	private static ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
	private static Date toDate(LocalDate localDate) {
		return Date.from(localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant());
	}
    private static OHLCDataItem toOHLCDataItem(OHLCV ohlcv) {
      	return new OHLCDataItem(
      		toDate(ohlcv.date),
      		ohlcv.open.doubleValue(),
      		ohlcv.high.doubleValue(),
      		ohlcv.low.doubleValue(),
      		ohlcv.close.doubleValue(),
      		ohlcv.volume);
    }
    private static OHLCDataItem[] toOHLCDataItem(List<OHLCV>list) {
    	return list.stream().map(o -> toOHLCDataItem(o)).toArray(OHLCDataItem[]::new);
     }
    
    public static class CustomCandlestickRenderer extends CandlestickRenderer {
        private static final long serialVersionUID = -361464112524520687L;

		@Override
        public Paint getItemPaint(int row, int column) {
            OHLCDataset dataSet = (OHLCDataset) getPlot().getDataset();
            int series = row;
            int item   = column;
            var yOpen  = dataSet.getOpen(series, item).doubleValue();
            var yClose = dataSet.getClose(series, item).doubleValue();
            
            //return the same color as that used to fill the candle
            return yOpen < yClose ? getUpPaint() : getDownPaint();
        }
    }
    
    public static class DefaultTimeLine implements Timeline {
    	private Set<Long> set = new TreeSet<>();
    	
    	public DefaultTimeLine(List<LocalDate> list) {
    		for(var e: list) {
    			set.add(toDate(e).getTime());
     		}    		
    	}
    	
		@Override
		public long toTimelineValue(long millisecond) {
			return millisecond;
		}

		@Override
		public long toTimelineValue(Date date) {
			return date.getTime();
		}

		@Override
		public long toMillisecond(long timelineValue) {
			return timelineValue;
		}

		@Override
		public boolean containsDomainValue(long millisecond) {
			var ret = set.contains(toTimelineValue(millisecond));
//			logger.info("containsDomainValue  BB {}", millisecond);
			return ret;
		}

		@Override
		public boolean containsDomainValue(Date date) {
			var ret = set.contains(toTimelineValue(date));
//			logger.info("containsDomainValue  AA  {}  {}", date, ret);
			return ret;
		}

		@Override
		public boolean containsDomainRange(long fromMillisecond, long toMillisecond) {
			Date fromDate = new Date(fromMillisecond);
			Date toDate   = new Date(fromMillisecond);
			
			logger.info("containsDomainRange  {}  {}", fromDate.toString(), toDate.toString());
			return false;
		}

		@Override
		public boolean containsDomainRange(Date fromDate, Date toDate) {
			return containsDomainRange(fromDate.getTime(), toDate.getTime());
		}
    	
    }
    
    static void drawChart(String title, int width, int height, List<OHLCV> list) {
    	{
    		var first = list.get(0);
    		var last  = list.get(list.size() - 1);
    		logger.info("list  {}  {}", first.date, last.date);
    	}
    	
		final File file;
		{
			var path = String.format("tmp/chart/%s.png", title);
			file = new File(path);
		}
		logger.info("file  {}", file.getPath());
		
		final JFreeChart chart;
		{
			var dataSet = new DefaultOHLCDataset(title, toOHLCDataItem(list));
			chart = ChartFactory.createCandlestickChart("price", "date", "price", dataSet, true);
			
			var xyPlot = chart.getXYPlot();
			// setBackgroundPaint
			xyPlot.setBackgroundPaint(Color.BLACK);
			// setRenderer
			{
				var renderer = new CustomCandlestickRenderer();
				// setDrawVolume
				renderer.setDrawVolume(false);
				xyPlot.setRenderer(renderer);
			}

			{
				var axis = (NumberAxis)xyPlot.getRangeAxis();
				// setAutoRangeIncludesZero
				axis.setAutoRangeIncludesZero(false);
			}
			{
				var dateAxis = (DateAxis)xyPlot.getDomainAxis();
//				dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
//				dateAxis.setVerticalTickLabels(true);
				dateAxis.setTickUnit(new DateTickUnit(DateTickUnitType.MONTH, 1, DateTickUnitType.DAY, 7, new SimpleDateFormat("yyyy-MM")));
//				dateAxis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, 7, new SimpleDateFormat("yyyy-MM-dd")));
				
//				var timeLine = new DefaultTimeLine(list.stream().map(o -> o.date).toList());
//				dateAxis.setTimeline(timeLine);
			}
		}
				
		try {
			ChartUtils.saveChartAsPNG(file, chart, width, height);
		} catch (IOException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	static void drawChart(String stockCode, int width, int height) {
//		var list = StorageStock.StockPriceJP.getList(stockCode);
		var list = StorageStock.StockPriceUS.getList(stockCode);
		{
			var lastDate  = list.get(list.size() - 1).date;
//			var firstDate = lastDate.minusYears(1).plusDays(1);
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

package yokwe.stock.jp.toushin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import yokwe.util.ClassUtil;
import yokwe.util.finance.DailyPriceDiv;
import yokwe.util.finance.FundStats;

public class T003 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	static void samplePie() {
		DefaultPieDataset<String> data = new DefaultPieDataset<>();
		data.setValue("支持する", 56);
		data.setValue("支持しない", 41);
		data.setValue("未回答", 3);

		JFreeChart chart = new JFreeChart(new PiePlot<String>(data));
		
		final String methodName = ClassUtil.getCallerMethodName();

		File file = new File("tmp/" + methodName + ".png");
		try {
			ChartUtils.saveChartAsPNG(file, chart, 300, 300);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void sampleScatterA() {
		List<XYSeries> list = new ArrayList<>();
		{
			XYSeries seriesY1  = new XYSeries("Y1");
			XYSeries seriesY3  = new XYSeries("Y3");
			XYSeries seriesY5  = new XYSeries("Y5");
			XYSeries seriesY10 = new XYSeries("Y10");

			List<Fund> fundList = Fund.getList();
			logger.info("fundList   {}", fundList.size());
					
			int count = 0;
			for(var fund: fundList) {
				final String isinCode = fund.isinCode;
				
				count++;
				if ((count % 500) == 1) logger.info("{}", String.format("%4d / %4d", count, fundList.size()));
				
				if (!fund.investingAsset.equals("株式")) continue;
				if (!fund.investingArea.equals("北米")) continue;

				DailyPriceDiv[] dailyPriceDivArray;
				{
					Price[] priceArray = Price.getList(isinCode).stream().toArray(Price[]::new);
					if (priceArray.length == 0) continue;
					
					Dividend[] divArray = Dividend.getList(isinCode).stream().toArray(Dividend[]::new);
					dailyPriceDivArray = DailyPriceDiv.toDailyPriceDivArray(
						priceArray, o -> o.date, o -> o.price.doubleValue(),
						divArray,   o -> o.date, o -> o.amount.doubleValue());
				}
				
				FundStats fundStats = FundStats.getInstance(isinCode, dailyPriceDivArray);
				if (fundStats == null) continue;
				
				if (12 <= fundStats.duration) {
					int nMonth  = 12;
					int nOffset =  0;
					double ror  = fundStats.rateOfReturn(nMonth, nOffset);
					double risk = fundStats.risk(nMonth, nOffset);
					
					seriesY1.add(risk, ror);
				}
				if (36 <= fundStats.duration) {
					int nMonth  = 36;
					int nOffset =  0;
					double ror  = fundStats.rateOfReturn(nMonth, nOffset);
					double risk = fundStats.risk(nMonth, nOffset);
					
					seriesY3.add(risk, ror);
				}
				if (60 <= fundStats.duration) {
					int nMonth  = 60;
					int nOffset =  0;
					double ror  = fundStats.rateOfReturn(nMonth, nOffset);
					double risk = fundStats.risk(nMonth, nOffset);
					
					seriesY5.add(risk, ror);
				}
				if (120 <= fundStats.duration) {
					int nMonth  = 120;
					int nOffset =   0;
					double ror  = fundStats.rateOfReturn(nMonth, nOffset);
					double risk = fundStats.risk(nMonth, nOffset);
					
					seriesY10.add(risk, ror);
				}
			}
			
			list.add(seriesY1);
			list.add(seriesY3);
			list.add(seriesY5);
			list.add(seriesY10);
		}
		
		final String methodName = ClassUtil.getCallerMethodName();
		for(var series: list) {
			XYSeriesCollection collection = new XYSeriesCollection(series);
			JFreeChart chart = ChartFactory.createScatterPlot("ror-risk", "risk", "ror", collection);
			
			String path = String.format("tmp/T003-%s-%s.png", methodName, series.getKey().toString());
			logger.info("save  {}", path);
			File   file = new File(path);
			try {
				ChartUtils.saveChartAsPNG(file, chart, 600, 600);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static class FundFundStats {
		final Fund      fund;
		final FundStats fundStats;
		
		FundFundStats(Fund fund, FundStats fundStats) {
			this.fund      = fund;
			this.fundStats = fundStats;
		}
	}
	static void sampleScatterB() {
		List<FundFundStats> fundFundStatsList = new ArrayList<>();
		{
			List<Fund> fundList = Fund.getList();
			logger.info("fundList   {}", fundList.size());
			
			int count = 0;
			for(var fund: fundList) {
				final String isinCode = fund.isinCode;
				
				count++;
				if ((count % 500) == 1) logger.info("{}", String.format("%4d / %4d", count, fundList.size()));
				
				// skip special fund
				if (fund.name.contains("専用") || fund.name.contains("向け")) continue;
				// skip ETF
				if (!fund.stockCode.isEmpty()) continue;

				DailyPriceDiv[] dailyPriceDivArray;
				{
					Price[] priceArray = Price.getList(isinCode).stream().toArray(Price[]::new);
					if (priceArray.length == 0) continue;
					
					Dividend[] divArray = Dividend.getList(isinCode).stream().toArray(Dividend[]::new);
					dailyPriceDivArray = DailyPriceDiv.toDailyPriceDivArray(
						priceArray, o -> o.date, o -> o.price.doubleValue(),
						divArray,   o -> o.date, o -> o.amount.doubleValue());
				}
				
				FundStats fundStats = FundStats.getInstance(isinCode, dailyPriceDivArray);
				if (fundStats == null) continue;

				fundFundStatsList.add(new FundFundStats(fund, fundStats));
			}
		}
		
		final String methodName = ClassUtil.getCallerMethodName();

		{
			int[] durationArray = {12, 36, 60, 120};
			for(var duration: durationArray) {
				Map<String, XYSeries> seriesMap = new TreeMap<>();
				//  key

				for(var fundFundStats: fundFundStatsList) {
					Fund      fund      = fundFundStats.fund;
					FundStats fundStats = fundFundStats.fundStats;
					if (duration <= fundStats.duration) {
						int nMonth  = duration;
						int nOffset = 0;

						double ror = fundStats.rateOfReturn(nMonth, nOffset)   * 100;
						double sd  = fundStats.risk(nMonth, nOffset) * 100;
						
						if (fund.isinCode.equals("JP90C0009FQ7")) logger.info("{}  {}  {}", fund.isinCode, sd, ror);
						
						String key = fund.investingAsset;
						XYSeries series;
						if (seriesMap.containsKey(key)) {
							series = seriesMap.get(key);
						} else {
							series = new XYSeries(key);
							seriesMap.put(key, series);
						}
						series.add(sd, ror);
					}
				}
				
				XYSeriesCollection collection = new XYSeriesCollection();
				for(var e: seriesMap.values()) collection.addSeries(e);
				
				JFreeChart chart = ChartFactory.createScatterPlot("ror-sd", "sd", "ror", collection);
				String path = String.format("tmp/T003-%s-%d.png", methodName, duration);
				logger.info("save  {}", path);
				
				File file = new File(path);
				try {
					ChartUtils.saveChartAsPNG(file, chart, 600, 600);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
//		samplePie();
//		sampleScatterA();
		sampleScatterB();
		    
		logger.info("STOP");
	}
}

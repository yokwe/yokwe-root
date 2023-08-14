package yokwe.stock.jp.toushin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import yokwe.util.finance.DailyPriceDiv;
import yokwe.util.finance.Portfolio;

public class T004 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	
	private static DailyPriceDiv[] getDailyPriceDiv(String isinCode) {
		Price[]    priceArray = Price.getList(isinCode).stream().toArray(Price[]::new);			
		Dividend[] divArray   = Dividend.getList(isinCode).stream().toArray(Dividend[]::new);
		
		DailyPriceDiv[] dailyPriceDivArray = DailyPriceDiv.toDailyPriceDivArray(
			priceArray, o -> o.date, o -> o.price.doubleValue(),
			divArray,   o -> o.date, o -> o.amount.doubleValue());
		
		return dailyPriceDivArray;
	}

	
	public static void main(String[] args) {
		logger.info("START");
		
		String[] isinCodeArray = {
//			"JP90C000FZD4", // ｉＦｒｅｅＮＥＸＴ　ＦＡＮＧ＋インデックス
			"JP90C0006G52", // 世界半導体株投資（野村世界業種別投資シリーズ）
//			"JP90C0009FQ7", // ｉシェアーズ米国株式（Ｓ＆Ｐ５００）インデックス・ファンド
			"JP90C0009G00", // ｉシェアーズゴールドインデックス・ファンド（為替ヘッジなし）
			"JP90C0009FZ8", // ｉシェアーズコモディティインデックス・ファンド
//			"JP90C00096Q0", // フィデリティ・ＵＳハイ・イールド・ファンド（資産成長型）
//			"JP90C0003V15", // 日本トレンド・マネーポートフォリオ（日本トレンド・セレクト）
		};
		
		Map<String, Fund> fundMap = Fund.getMap();
		
		Portfolio portfolio = new Portfolio();
		// build portfolio
		for(var isinCode: isinCodeArray) {
			portfolio.add(isinCode, getDailyPriceDiv(isinCode));
		}
		for(var isinCode: isinCodeArray) {
			logger.info("duration  {}  {}  {}", isinCode, portfolio.holdingDuration(), portfolio.holdingDuration(isinCode));
		}
		
//		int nYear = portfolio.durationInYear();
		logger.info("holdingDuration  {}", portfolio.holdingDuration());
		int duration = 118;
		logger.info("duration         {}", duration);
		portfolio.duration(duration);
		
		for(int i = 0; i < isinCodeArray.length; i++) {
			for(int j = 0; j < isinCodeArray.length; j++) {
				if (i == j) break;
				
				String a = isinCodeArray[i];
				String b = isinCodeArray[j];
				
				portfolio.
					clearQuantity().
					quantity(a, 100).
					quantity(b, 100);
				
				logger.info("A {}  {}", a, fundMap.get(a).name);
				logger.info("B {}  {}", b, fundMap.get(b).name);
				logger.info("  cor {}", portfolio.correlation(a, b));
			}
		}
		
		String name = Arrays.stream(isinCodeArray).collect(Collectors.joining("-"));
		XYSeriesCollection collection = new XYSeriesCollection();
		for(var isinCode: isinCodeArray) {
			XYSeries series = new XYSeries(isinCode);

			portfolio.clearQuantity().quantity(isinCode, 100);
			var sd  = portfolio.risk() * 100;
			var ror = portfolio.rateOfReturn() * 100;
			series.add(sd, ror);

			logger.info("{}  {}  {}", isinCode, portfolio.holdingDuration(isinCode), fundMap.get(isinCode).name);
			logger.info("  sd   {}  {}", sd, portfolio.risk(isinCode)  * 100);
			logger.info("  ror  {}  {}", ror, portfolio.risk(isinCode) * 100);
			collection.addSeries(series);
		}

		{
			XYSeries series  = new XYSeries(name);
			Random random = new Random(System.currentTimeMillis());
			for(int i = 0; i < 10000; i++) {
				portfolio.clearQuantity();
				for(var e: isinCodeArray) {
					portfolio.quantity(e, random.nextInt(1000));
				}
				var sd  = portfolio.risk() * 100;
				var ror = portfolio.rateOfReturn() * 100;
				series.add(sd, ror);
			}
			collection.addSeries(series);
		}
		JFreeChart chart = ChartFactory.createScatterPlot("Efficent Frontier  duration = " + duration, "sd", "ror", collection);
		{
			for(var isinCode: isinCodeArray) {
				Fund fund = fundMap.get(isinCode);
				String text = String.format("%s  %s", fund.isinCode, fund.name);
				TextTitle textTitle = new TextTitle(text);
				textTitle.setHorizontalAlignment(HorizontalAlignment.LEFT);
				textTitle.setMargin(0, 50, 0, 0); // in pixel ?
				chart.addSubtitle(textTitle);
			}
		}
		
		String path = String.format("tmp/T004-%s.png", name);
		File file = new File(path);
		try {
			ChartUtils.saveChartAsPNG(file, chart, 600, 600);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.info("STOP");
	}

}

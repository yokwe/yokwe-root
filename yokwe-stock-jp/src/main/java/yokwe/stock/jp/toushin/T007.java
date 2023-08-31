package yokwe.stock.jp.toushin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import yokwe.util.finance.DailyPriceDiv;
import yokwe.util.finance.FundStats;

public class T007 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	
	private static DailyPriceDiv[] getDailyPriceDiv(String isinCode) {
		Price[]    priceArray = Price.getList(isinCode).stream().toArray(Price[]::new);			
		Dividend[] divArray   = Dividend.getList(isinCode).stream().toArray(Dividend[]::new);
		
		DailyPriceDiv[] dailyPriceDivArray = DailyPriceDiv.toDailyPriceDivArray(
			priceArray, o -> o.date, o -> o.price.doubleValue(),
			divArray,   o -> o.date, o -> o.amount.doubleValue());
		
		return dailyPriceDivArray;
	}
	
	private static void drawChart(String[] isinCodeArray) {		
		int nMonth = 36;
		
		Map<String, Fund> fundMap = Fund.getMap();

		XYSeriesCollection collection = new XYSeriesCollection();
		
		for(var isinCode: isinCodeArray) {
			DailyPriceDiv[] dailyPriceDivArray = getDailyPriceDiv(isinCode);
			FundStats fundStats = FundStats.getInstance(isinCode, dailyPriceDivArray);
			logger.info("drawChart  {}  {}", fundStats.code, fundStats.duration);

			XYSeries series = new XYSeries(isinCode, false);
			for (int nOffset = 0; nOffset < 1000; nOffset++) {
				if (!fundStats.contains(nMonth, nOffset))
					break;

				LocalDate startDate = fundStats.startDate(nMonth, nOffset);
				LocalDate stopDate = fundStats.stopDate(nMonth, nOffset);
				double ror = fundStats.rateOfReturn(nMonth, nOffset);
				double risk = fundStats.riskDaily(nMonth, nOffset);

				logger.info("{}", String.format("%s - %s  %8.4f  %8.4f", startDate, stopDate, ror, risk));
				series.add(risk, ror);
			}
			collection.addSeries(series);
		}
		
		JFreeChart chart = ChartFactory.createScatterPlot("ror-sd  duration = " + nMonth, "sd", "ror", collection);
		{
			for(var isinCode: isinCodeArray) {
				Fund fund = fundMap.get(isinCode);
				String text = String.format("%s  %s", fund.isinCode, fund.name);
				TextTitle textTitle = new TextTitle(text);
				textTitle.setHorizontalAlignment(HorizontalAlignment.LEFT);
				textTitle.setMargin(0, 50, 0, 0); // in pixel ?
				chart.addSubtitle(textTitle);
			}
			
	        XYPlot plot = (XYPlot) chart.getPlot();
	        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	        renderer.setSeriesLinesVisible(0, true);
	        plot.setRenderer(renderer);

		}
		
		String name = Arrays.stream(isinCodeArray).collect(Collectors.joining("-"));
		String path = String.format("tmp/T007-%s-%d.png", name, nMonth);
		File file = new File(path);
		logger.info("save  {}", path);
		try {
			ChartUtils.saveChartAsPNG(file, chart, 600, 600);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		logger.info("START");
		
		String[] isinCodeArray = {
//				"JP90C000FZD4", // ｉＦｒｅｅＮＥＸＴ　ＦＡＮＧ＋インデックス
//				"JP90C000KLP8", // ＦＡＮＧ＋２倍ベア（ＦＡＮＧ＋ブルベアファンド）
//				"JP90C0006G52", // 世界半導体株投資（野村世界業種別投資シリーズ）
				"JP90C0009FQ7", // ｉシェアーズ米国株式（Ｓ＆Ｐ５００）インデックス・ファンド
//				"JP90C0009G00", // ｉシェアーズゴールドインデックス・ファンド（為替ヘッジなし）
//				"JP90C0009FZ8", // ｉシェアーズコモディティインデックス・ファンド
				"JP90C00096Q0", // フィデリティ・ＵＳハイ・イールド・ファンド（資産成長型）
//				"JP90C0003V15", // 日本トレンド・マネーポートフォリオ（日本トレンド・セレクト）
				"JP90C0002EK8", // ＭＳＣＩインデックス・セレクト・ファンド コクサイ・ポートフォリオ
			};

		// "JP90C0009FQ7", // ｉシェアーズ米国株式（Ｓ＆Ｐ５００）インデックス・ファンド
		drawChart(isinCodeArray);
		
		logger.info("STOP");
	}
}

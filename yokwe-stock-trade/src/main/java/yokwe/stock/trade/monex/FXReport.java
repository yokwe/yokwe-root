package yokwe.stock.trade.monex;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import yokwe.stock.trade.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class FXReport {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final String PATH = Storage.Monex.getPath("fx-report.csv");
	public static String getPath() {
		return PATH;
	}
	public static void save(List<FXReport> list) {
		CSVUtil.write(FXReport.class).file(getPath(), list);
	}
	
	
	private static class DayGroup {
		static TreeMap<String, DayGroup> buildMap(List<FXProfit> list) {
			//         date
			TreeMap<String, DayGroup> map = new TreeMap<>();
			//      date
			for(var fxProfit: list) {
				var dayGroup = getInstance(map, fxProfit);
				
				switch(fxProfit.type.kind) {
				case DEPOSIT:
					dayGroup.depositList.add(fxProfit);
					break;
				case WITHDRAW:
					dayGroup.withdrawList.add(fxProfit);
					break;
				case MINUS:
					dayGroup.minusList.add(fxProfit);
					break;
				case PLUS:
					dayGroup.plusList.add(fxProfit);
					break;
				default:
					logger.error("Unexepected");
					logger.error("  fxProfit {}", StringUtil.toString(fxProfit));
					throw new UnexpectedException("Unexepected");	
				}
			}
			
			return map;
		}
		
		static DayGroup getInstance(TreeMap<String, DayGroup> map, FXProfit fxProfit) {
			final String date = fxProfit.date;
			DayGroup ret;
			if (map.containsKey(date)) {
				ret = map.get(date);
				return ret;
			} else {
				ret = new DayGroup(fxProfit);
				map.put(date, ret);
			}
			// sanity check
			if (!ret.date.equals(fxProfit.date) || ret.fxRate != fxProfit.fxRate) {
				logger.error("Unexpected fxRate");
				logger.error("  old {} {}", ret.date, ret.fxRate);
				logger.error("  new {} {}", fxProfit.date, fxProfit.fxRate);
				throw new UnexpectedException("Unexpected fxRate");
			}
			return ret;
		}
		
		final String date;
		final double fxRate;
		List<FXProfit> depositList  = new ArrayList<>();
		List<FXProfit> withdrawList = new ArrayList<>();
		List<FXProfit> minusList    = new ArrayList<>();
		List<FXProfit> plusList     = new ArrayList<>();
		
		DayGroup(FXProfit fxProfit) {
			this.date   = fxProfit.date;
			this.fxRate = fxProfit.fxRate;
		}	
	}
	
	private static FXProfit sum(List<FXProfit> list) {
		if (list.isEmpty()) {
			logger.error("Unexpected");
			throw new UnexpectedException("Unexpected");
		}
		var ret = new FXProfit();
		{
			var first = list.get(0);
			ret.date   = first.date;
			ret.type   = first.type;
			ret.fxRate = first.fxRate;
			ret.usd    = 0;
			ret.jpy    = 0;
			ret.symbol = "";
		}
		for(var e: list) {
			ret.usd += e.usd;
			ret.jpy += e.jpy;
		}
		return ret;
	}
	
	
	private static void update() {
		
		List<FXReport> reportList = new ArrayList<>();
		
		// build GroupMap.map
		var dayGroupMap = DayGroup.buildMap(FXProfit.getList());
		
		BigDecimal totalJPY = BigDecimal.ZERO;
		BigDecimal totalUSD = BigDecimal.ZERO.setScale(2);
		BigDecimal totalAVG = BigDecimal.ZERO.setScale(2);

		for(var dayGroup: dayGroupMap.values()) {
			logger.info("dayGroup {}", dayGroup.date);
			if (!dayGroup.depositList.isEmpty()) {
//				logger.info("  deposit  {}", dayGroup.depositList.size());
				var sum = sum(dayGroup.depositList);
//				logger.info("  {}", String.format("%s %-8s %10.2f %10.2f %10d", sum.date, sum.type.kind, sum.fxRate, sum.usd, sum.jpy));
				
				BigDecimal jpy = BigDecimal.valueOf(sum.jpy);
				BigDecimal usd = BigDecimal.valueOf(sum.usd).setScale(2, RoundingMode.HALF_UP);
				
				totalJPY = totalJPY.add(jpy);
				totalUSD = totalUSD.add(usd);
				
				totalAVG = totalJPY.divide(totalUSD, 3, RoundingMode.DOWN);
				totalAVG = totalAVG.setScale(2, RoundingMode.UP);
				
				var report = new FXReport(sum);
				report.plusJPY  = jpy.intValue();
				report.plusUSD  = usd.doubleValue();
				report.totalJPY = totalJPY.intValue();
				report.totalUSD = totalUSD.doubleValue();
				report.totalAVG = totalAVG.doubleValue();
				reportList.add(report);

				logger.info("  report {}", String.format(
					"%s %-8s %6.2f %8.2f %10d %8.2f %10d %8.2f %10d %8.2f %10d %6.2f %10d",
					report.date, report.type, report.fxRate, report.usd, report.jpy, report.plusUSD, report.plusJPY, report.minusUSD, report.minusJPY, report.totalUSD, report.totalJPY, report.totalAVG, report.profit));
			}
			if (!dayGroup.withdrawList.isEmpty()) {
//				logger.info("  withdraw {}", dayGroup.withdrawList.size());
				var sum = sum(dayGroup.withdrawList);
//				logger.info("  {}", String.format("%s %-8s %10.2f %10.2f %10d", sum.date, sum.type.kind, sum.fxRate, sum.usd, sum.jpy));
				
				BigDecimal usd = BigDecimal.valueOf(sum.usd).setScale(2, RoundingMode.HALF_UP).negate();
				BigDecimal jpy = usd.multiply(totalAVG).setScale(2, RoundingMode.DOWN);
				jpy = jpy.setScale(0, RoundingMode.DOWN);
				
				totalJPY = totalJPY.subtract(jpy);
				totalUSD = totalUSD.subtract(usd);
				
				// NO NEED TO CALCULATE
				totalAVG = totalJPY.divide(totalUSD, 3, RoundingMode.DOWN);
				totalAVG = totalAVG.setScale(2, RoundingMode.UP);
				
				var report = new FXReport(sum);
				report.minusJPY = jpy.intValue();
				report.minusUSD = usd.doubleValue();
				report.totalJPY = totalJPY.intValue();
				report.totalUSD = totalUSD.doubleValue();
				report.totalAVG = totalAVG.doubleValue();
				report.profit   = (-sum.jpy) - report.minusJPY;
				reportList.add(report);

				logger.info("  report {}", String.format(
						"%s %-8s %6.2f %8.2f %10d %8.2f %10d %8.2f %10d %8.2f %10d %6.2f %10d",
						report.date, report.type, report.fxRate, report.usd, report.jpy, report.plusUSD, report.plusJPY, report.minusUSD, report.minusJPY, report.totalUSD, report.totalJPY, report.totalAVG, report.profit));
			}
			if (!dayGroup.minusList.isEmpty()) {
//				logger.info("  minus    {}", dayGroup.minusList.size());
				var sum = sum(dayGroup.minusList);
//				logger.info("  {}", String.format("%s %-8s %10.2f %10.2f %10d", sum.date, sum.type.kind, sum.fxRate, sum.usd, sum.jpy));
				
				BigDecimal usd = BigDecimal.valueOf(sum.usd).setScale(2, RoundingMode.HALF_UP).negate();
				BigDecimal jpy = usd.multiply(totalAVG).setScale(2, RoundingMode.DOWN);
				jpy = jpy.setScale(0, RoundingMode.DOWN);
				
				totalJPY = totalJPY.subtract(jpy);
				totalUSD = totalUSD.subtract(usd);
				
				// NO NEED TO CALCULATE
				totalAVG = totalJPY.divide(totalUSD, 3, RoundingMode.DOWN);
				totalAVG = totalAVG.setScale(2, RoundingMode.UP);
				
				var report = new FXReport(sum);
				report.minusJPY = jpy.intValue();
				report.minusUSD = usd.doubleValue();
				report.totalJPY = totalJPY.intValue();
				report.totalUSD = totalUSD.doubleValue();
				report.totalAVG = totalAVG.doubleValue();
				reportList.add(report);

				logger.info("  report {}", String.format(
						"%s %-8s %6.2f %8.2f %10d %8.2f %10d %8.2f %10d %8.2f %10d %6.2f %10d",
						report.date, report.type, report.fxRate, report.usd, report.jpy, report.plusUSD, report.plusJPY, report.minusUSD, report.minusJPY, report.totalUSD, report.totalJPY, report.totalAVG, report.profit));
			}
			if (!dayGroup.plusList.isEmpty()) {
//				logger.info("  plus     {}", dayGroup.plusList.size());
				var sum = sum(dayGroup.plusList);
//				logger.info("  {}", String.format("%s %-8s %10.2f %10.2f %10d", sum.date, sum.type.kind, sum.fxRate, sum.usd, sum.jpy));

				BigDecimal usd = BigDecimal.valueOf(sum.usd).setScale(2, RoundingMode.HALF_UP);
				BigDecimal jpy = usd.multiply(totalAVG).setScale(2, RoundingMode.DOWN);
				jpy = jpy.setScale(0, RoundingMode.DOWN);

				totalJPY = totalJPY.add(jpy);
				totalUSD = totalUSD.add(usd);
				
				totalAVG = totalJPY.divide(totalUSD, 3, RoundingMode.DOWN);
				totalAVG = totalAVG.setScale(2, RoundingMode.UP);
				
				var report = new FXReport(sum);
				report.plusJPY  = jpy.intValue();
				report.plusUSD  = usd.doubleValue();
				report.totalJPY = totalJPY.intValue();
				report.totalUSD = totalUSD.doubleValue();
				report.totalAVG = totalAVG.doubleValue();
				reportList.add(report);

				logger.info("  report {}", String.format(
					"%s %-8s %6.2f %8.2f %10d %8.2f %10d %8.2f %10d %8.2f %10d %6.2f %10d",
					report.date, report.type, report.fxRate, report.usd, report.jpy, report.plusUSD, report.plusJPY, report.minusUSD, report.minusJPY, report.totalUSD, report.totalJPY, report.totalAVG, report.profit));

			}
		}

		logger.info("save {} {}", reportList.size(), getPath());
		FXReport.save(reportList);
	}
	
	
	public String date = "";
	public String type = "";
	@CSVUtil.DecimalPlaces(2)
	public double fxRate = 0;
	@CSVUtil.DecimalPlaces(2)
	public double usd = 0;
	public int    jpy = 0;
	public String symbol = "";
	
	// for DEPOSIT, SELL and DIV
	public double plusUSD = 0; // jpy equivalent of usd
	public int    plusJPY = 0; // jpy equivalent of usd
	
	// for WITHDRAW, BUY and FEE
	public double minusUSD = 0; // jpy equivalent of usd
	public int    minusJPY = 0; // jpy equivalent of usd

	public int    totalJPY = 0;
	@CSVUtil.DecimalPlaces(2)
	public double totalUSD = 0;
	@CSVUtil.DecimalPlaces(2)
	public double totalAVG  = 0;
	
	public int profit = 0;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	

	private FXReport(FXProfit fxProfit) {
		this.date   = fxProfit.date;
		this.type   = fxProfit.type.name();
		this.fxRate = fxProfit.fxRate;
		this.usd    = fxProfit.usd;
		this.jpy    = fxProfit.jpy;
		this.symbol = fxProfit.symbol;
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
		System.exit(0);
	}

}

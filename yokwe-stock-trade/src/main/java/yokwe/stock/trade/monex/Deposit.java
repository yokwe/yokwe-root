package yokwe.stock.trade.monex;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import yokwe.stock.trade.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.DateMap;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public final class Deposit {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final String TIMESTAMP   = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
	public static final String NAME_REPORT = String.format("DEPOSIT_BALANCE_%s.ods", TIMESTAMP);

	public static final String PATH_TEMPLATE = Storage.Report.getPath("TEMPLATE_DEPOSIT_BALANCE.ods");
	public static final String URL_TEMPLATE  = StringUtil.toURLString(PATH_TEMPLATE);

	public static class Activity implements Comparable<Activity> {
		public enum Type {
			DEPOSIT ("ドル購入"), // deposit of USD
			WITHDRAW("ドル売却"), // withdraw of USD
			MINUS   ("ドル減少"), // decrease of USD
			PLUS    ("ドル増加"); // increase of USD
			
			public String description;
			
			Type(String description) {
				this.description = description;
			}
		}
		
		public enum Action {
			DEPOSIT (Type.DEPOSIT,  "ドル購入"), // deposit of USD
			WITHDRAW(Type.WITHDRAW, "ドル売却"), // withdraw of USD
			
			BUY(Type.MINUS, "株式購入"), // Buying of Stock
			FEE(Type.MINUS, "株式売却"), // Fee of ADR
			
			SELL(Type.PLUS, "株式売却"), // Selling of Stock
			DIV (Type.PLUS, "株式配当"); // Dividend of Stock
			
			public Type   type;
			public String description;
			Action(Type type, String description) {
				this.type        = type;
				this.description = description;
			}
		}

		public String  date;
		public Action  action;
		@CSVUtil.DecimalPlaces(2)
		public double  fxRate;
		@CSVUtil.DecimalPlaces(2)
		public double  usd;
		public int     jpy;
		public String  symbol;
		
		public Activity(
			String  date,
			Action  action,
			double  fxRate,
			double  usd,
			int     jpy,
			String  symbol
			) {
			this.date   = date;
			this.action = action;
			this.fxRate = fxRate;
			this.usd    = usd;
			this.jpy    = jpy;
			this.symbol = symbol;
		}
		public Activity() {
			this("", null, 0, 0, 0, "");
		}

		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
		@Override
		public int compareTo(Activity that) {
			int ret = 0;
			if (ret == 0) ret = this.date.compareTo(that.date);
			if (ret == 0) ret = this.action.compareTo(that.action);
			if (ret == 0) ret = this.symbol.compareTo(that.symbol);
			return ret;
		}
		@Override
		public boolean equals(Object o) {
			if (o == null) {
				return false;
			} else {
				if (o instanceof Activity) {
					Activity that = (Activity)o	;
					return this.compareTo(that) == 0;
				} else {
					return false;
				}
			}
		}
		@Override
		public int hashCode() {
			return this.date.hashCode() ^ this.action.hashCode() ^ this.symbol.hashCode();
		}
		
		private static Activity deposit(String date, double fxRate, double usd, int jpy) {
			return new Activity(date, Action.DEPOSIT, fxRate, usd, (int)jpy, "");
		}
		private static Activity withdraw(String date, double fxRate, double usd, double jpy) {
			return new Activity(date, Action.WITHDRAW, fxRate, usd, (int)jpy, "");
		}
		private static Activity buy(String date, double fxRate, double usd, String symbol) {
			return new Activity(date, Action.BUY, fxRate, usd, 0, symbol);
		}
		private static Activity fee(String date, double fxRate, double usd) {
			return new Activity(date, Action.FEE, fxRate, usd, 0, "");
		}
		private static Activity sell(String date, double fxRate, double usd, String symbol) {
			return new Activity(date, Action.SELL, fxRate, usd, 0, symbol);
		}
		private static Activity div(String date, double fxRate, double usd, String symbol) {
			return new Activity(date, Action.DIV, fxRate, usd, 0, symbol);
		}
		
		public static List<Activity> getList() {
			var fxTaxMap = new DateMap<FXTax>();
			// build fxMap
			for(var e: UpdateFXTax.load()) {
				fxTaxMap.put(e.date, e);
			}

			List<Transaction> transactionList;
			// build trasactionList
			try (SpreadSheet docActivity = new SpreadSheet(Transaction.URL_ACTIVITY, true)) {
				boolean useTradeDate = true;
				transactionList = Transaction.getTransactionList(docActivity, useTradeDate);		
			}
			
			List<Activity> list = new ArrayList<>();
			// build list
			for(var e: transactionList) {
//				logger.info("transaction {}", StringUtil.toString(e));
				String date = e.date;
				var fxTax = fxTaxMap.get(date);
				
				switch(e.type) {
				case JPY_IN:
				case JPY_OUT:
					continue;
				case USD_IN:
					list.add(deposit(date, e.fxRate, e.usd, -e.jpy));
					break;
				case USD_OUT:
					list.add(withdraw(date, e.fxRate, e.usd, -e.jpy));
					break;
				case BUY:
					list.add(buy(date, fxTax.tts, -e.total, e.symbol));
					break;
				case SELL:
					list.add(sell(date, fxTax.ttb, e.total, e.symbol));
					break;
				case DIVIDEND:
					list.add(div(date, fxTax.ttb, e.total, e.symbol));
					break;
				case FEE:
					list.add(fee(date, fxTax.tts, -e.usd));
					break;
				case CHANGE:
					break;
				default:
					logger.error("Unexepected");
					logger.error("  data {}", StringUtil.toString(e));
					throw new UnexpectedException("Unexepected");
				}
			}
			logger.info("activity {}", list.size());
			return list;
		}
	}
	
	
	private static class DayGroup {
		static TreeMap<String, DayGroup> buildMap(List<Activity> list) {
			//         date
			TreeMap<String, DayGroup> map = new TreeMap<>();
			//      date
			for(var e: list) {
				var dayGroup = getInstance(map, e);
				
				switch(e.action.type) {
				case DEPOSIT:
					dayGroup.depositList.add(e);
					break;
				case WITHDRAW:
					dayGroup.withdrawList.add(e);
					break;
				case MINUS:
					dayGroup.minusList.add(e);
					break;
				case PLUS:
					dayGroup.plusList.add(e);
					break;
				default:
					logger.error("Unexepected");
					logger.error("  activity {}", StringUtil.toString(e));
					throw new UnexpectedException("Unexepected");	
				}
			}
			
			return map;
		}
		
		static DayGroup getInstance(TreeMap<String, DayGroup> map, Activity activity) {
			final String date = activity.date;
			DayGroup ret;
			if (map.containsKey(date)) {
				ret = map.get(date);
				return ret;
			} else {
				ret = new DayGroup(activity);
				map.put(date, ret);
			}
			// sanity check
			if (!ret.date.equals(activity.date) || ret.fxRate != activity.fxRate) {
				logger.error("Unexpected fxRate");
				logger.error("  old {} {}", ret.date, ret.fxRate);
				logger.error("  new {} {}", activity.date, activity.fxRate);
				throw new UnexpectedException("Unexpected fxRate");
			}
			return ret;
		}
		
		final String date;
		final double fxRate;
		List<Activity> depositList  = new ArrayList<>();
		List<Activity> withdrawList = new ArrayList<>();
		List<Activity> minusList    = new ArrayList<>();
		List<Activity> plusList     = new ArrayList<>();
		
		DayGroup(Activity fxProfit) {
			this.date   = fxProfit.date;
			this.fxRate = fxProfit.fxRate;
		}	
	}
	
	private static Activity sum(List<Activity> list) {
		if (list.isEmpty()) {
			logger.error("Unexpected");
			throw new UnexpectedException("Unexpected");
		}
		var ret = new Activity();
		{
			var first = list.get(0);
			ret.date   = first.date;
			ret.action = first.action;
			ret.fxRate = first.fxRate;
			ret.usd    = 0;
			ret.jpy    = 0;
			ret.symbol = list.size() == 1 ? first.symbol : "";
		}
		for(var e: list) {
			ret.usd += e.usd;
			ret.jpy += e.jpy;
		}
		return ret;
	}

	
	@Sheet.SheetName("預け金")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class Report extends Sheet {
		@ColumnName("日付")
		@NumberFormat(SpreadSheet.FORMAT_DATE)
		public String date   = "";
		
		@ColumnName("処理")
		@NumberFormat(SpreadSheet.FORMAT_STRING)
		public String action = "";
		@ColumnName("処理詳細")
		@NumberFormat(SpreadSheet.FORMAT_STRING)
		public String detail = "";
		@ColumnName("レート")
		@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
		public double fxRate = 0;
		@ColumnName("ドル")
		@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
		public double usd    = 0;
		@ColumnName("円")
		@NumberFormat(SpreadSheet.FORMAT_JPY_BLANK)
		public int    jpy    = 0;
		@ColumnName("銘柄")
		@NumberFormat(SpreadSheet.FORMAT_STRING)
		public String symbol = "";
				
		// for DEPOSIT, SELL and DIV
		@ColumnName("ドル購入")
		@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
		public double plusUSD = 0; // jpy equivalent of usd
		@ColumnName("ドル購入換算")
		@NumberFormat(SpreadSheet.FORMAT_JPY_BLANK)
		public int    plusJPY = 0; // jpy equivalent of usd
		
		// for WITHDRAW, BUY and FEE
		@ColumnName("ドル売却")
		@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
		public double minusUSD = 0; // jpy equivalent of usd
		@ColumnName("ドル売却換算")
		@NumberFormat(SpreadSheet.FORMAT_JPY_BLANK)
		public int    minusJPY = 0; // jpy equivalent of usd

		@ColumnName("ドル合計")
		@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
		public double totalUSD = 0;
		@ColumnName("円合計")
		@NumberFormat(SpreadSheet.FORMAT_JPY_BLANK)
		public int    totalJPY = 0;
		@ColumnName("平均レート")
		@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
		public double totalAVG = 0;
		
		@ColumnName("為替差損益")
		@NumberFormat(SpreadSheet.FORMAT_JPY_BLANK)
		public int profitLoss = 0;

		private Report(Activity activity) {
			this.date   = activity.date;
			this.action = activity.action.type.description;
			this.detail = activity.action.description;			
			this.fxRate = activity.fxRate;
			this.usd    = activity.usd;
			this.jpy    = activity.jpy;
			this.symbol = activity.symbol;
		}

		private static List<Report> getReportList(List<Activity> activityList) {
			var map  = DayGroup.buildMap(activityList);
			
			BigDecimal totalJPY = BigDecimal.ZERO;
			BigDecimal totalUSD = BigDecimal.ZERO.setScale(2);
			BigDecimal totalAVG = BigDecimal.ZERO.setScale(2);

			List<Report> reportList = new ArrayList<>();
			for(var dayGroup: map.values()) {
				if (!dayGroup.depositList.isEmpty()) {
					if (2 <= dayGroup.depositList.size()) {
						for(var e: dayGroup.depositList) {
							var report = new Report(e);
							reportList.add(report);
						}
					}
					
					var sum = sum(dayGroup.depositList);
					
					BigDecimal jpy = BigDecimal.valueOf(sum.jpy);
					BigDecimal usd = BigDecimal.valueOf(sum.usd).setScale(2, RoundingMode.HALF_UP);
					
					totalJPY = totalJPY.add(jpy);
					totalUSD = totalUSD.add(usd);
					
					// Update totalAVG
					totalAVG = totalJPY.divide(totalUSD, 3, RoundingMode.DOWN);
					totalAVG = totalAVG.setScale(2, RoundingMode.UP);
					
					var report = new Report(sum);
					if (2 <= dayGroup.depositList.size()) report.detail = "*合計*";
					report.plusJPY  = jpy.intValue();
					report.plusUSD  = usd.doubleValue();
					report.totalJPY = totalJPY.intValue();
					report.totalUSD = totalUSD.doubleValue();
					report.totalAVG = totalAVG.doubleValue();
					reportList.add(report);
				}
				if (!dayGroup.withdrawList.isEmpty()) {
					if (2 <= dayGroup.withdrawList.size()) {
						for(var e: dayGroup.withdrawList) {
							var report = new Report(e);
							reportList.add(report);
						}
					}
					
					var sum = sum(dayGroup.withdrawList);
					
					BigDecimal usd = BigDecimal.valueOf(sum.usd).setScale(2, RoundingMode.HALF_UP).negate();
					// Calculate jpy using totalAVG
					BigDecimal jpy = usd.multiply(totalAVG).setScale(2, RoundingMode.DOWN);
					jpy = jpy.setScale(0, RoundingMode.DOWN);
					
					totalJPY = totalJPY.subtract(jpy);
					totalUSD = totalUSD.subtract(usd);
					
					var report = new Report(sum);
					if (2 <= dayGroup.withdrawList.size()) report.detail = "*合計*";
					report.minusJPY   = jpy.intValue();
					report.minusUSD   = usd.doubleValue();
					report.totalJPY   = totalJPY.intValue();
					report.totalUSD   = totalUSD.doubleValue();
					report.totalAVG   = totalAVG.doubleValue();
					report.profitLoss = (-sum.jpy) - jpy.intValue();
					reportList.add(report);
				}
				if (!dayGroup.minusList.isEmpty()) {
					if (2 <= dayGroup.minusList.size()) {
						for(var e: dayGroup.minusList) {
							var report = new Report(e);
							reportList.add(report);
						}
					}
					
					var sum = sum(dayGroup.minusList);
					
					BigDecimal usd = BigDecimal.valueOf(sum.usd).setScale(2, RoundingMode.HALF_UP).negate();
					// Calculate jpy using totalAVG
					BigDecimal jpy = usd.multiply(totalAVG).setScale(2, RoundingMode.DOWN);
					jpy = jpy.setScale(0, RoundingMode.DOWN);
					
					totalJPY = totalJPY.subtract(jpy);
					totalUSD = totalUSD.subtract(usd);
					
					var report = new Report(sum);
					if (2 <= dayGroup.minusList.size()) report.detail = "*合計*";
					report.minusJPY = jpy.intValue();
					report.minusUSD = usd.doubleValue();
					report.totalJPY = totalJPY.intValue();
					report.totalUSD = totalUSD.doubleValue();
					report.totalAVG = totalAVG.doubleValue();
					reportList.add(report);
				}
				if (!dayGroup.plusList.isEmpty()) {
					if (2 <= dayGroup.plusList.size()) {
						for(var e: dayGroup.plusList) {
							var report = new Report(e);
							reportList.add(report);
						}
					}
					
					var sum = sum(dayGroup.plusList);

					BigDecimal usd = BigDecimal.valueOf(sum.usd).setScale(2, RoundingMode.HALF_UP);
					// Calculate jpy using fxRate
					BigDecimal jpy = usd.multiply(BigDecimal.valueOf(sum.fxRate)).setScale(2, RoundingMode.DOWN);
					jpy = jpy.setScale(0, RoundingMode.DOWN);

					totalJPY = totalJPY.add(jpy);
					totalUSD = totalUSD.add(usd);
					
					// Update totalAVG
					totalAVG = totalJPY.divide(totalUSD, 3, RoundingMode.DOWN);
					totalAVG = totalAVG.setScale(2, RoundingMode.UP);
					
					var report = new Report(sum);
					if (2 <= dayGroup.plusList.size()) report.detail = "*合計*";
					report.plusJPY  = jpy.intValue();
					report.plusUSD  = usd.doubleValue();
					report.totalJPY = totalJPY.intValue();
					report.totalUSD = totalUSD.doubleValue();
					report.totalAVG = totalAVG.doubleValue();
					reportList.add(report);
				}
			}
			return reportList;
		}
		
		
		private static void generate(List<Activity> activityList) {
			var reportList = getReportList(activityList);
			
			String pathReport = Storage.Report.getPath("monex", NAME_REPORT);
			String urlReport  = StringUtil.toURLString(pathReport);

			logger.info("urlReport {}", urlReport);
			logger.info("docLoad   {}", URL_TEMPLATE);
			try (SpreadSheet docLoad = new SpreadSheet(URL_TEMPLATE, true)) {
				SpreadSheet docSave = new SpreadSheet();
				
				String sheetName = Sheet.getSheetName(Report.class);
				logger.info("sheet {}", sheetName);
				docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
				Sheet.fillSheet(docSave, reportList);
				
				// remove first sheet
				docSave.removeSheet(docSave.getSheetName(0));

				docSave.store(urlReport);
				logger.info("output {}", urlReport);
			}
		}
	}
	
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		var list = Activity.getList();
		Report.generate(list);
		
		logger.info("STOP");
		System.exit(0);
	}
}

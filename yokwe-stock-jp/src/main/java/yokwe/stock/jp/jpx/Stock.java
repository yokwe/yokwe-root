package yokwe.stock.jp.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.ListUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

@Sheet.SheetName("Sheet1")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public class Stock extends Sheet implements Comparable<Stock> {	
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final String PATH_FILE = Storage.JPX.getPath("stock.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<Stock> getList() {
		return ListUtil.getList(Stock.class, getPath());
	}
	public static Map<String, Stock> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<Stock> collection) {
		ListUtil.save(Stock.class, getPath(), collection);
	}
	public static void save(List<Stock> list) {
		ListUtil.save(Stock.class, getPath(), list);
	}
	
	public static String toStockCode4(String stockCode) {
		if (stockCode.length() == 4) {
			return stockCode;
		} else if (stockCode.length() == 5) {
			if (stockCode.endsWith("0")) {
				return stockCode.substring(0, 4);
			} else {
				return stockCode; // 25935 伊藤園 優先株式,市場第一部（内国株）
			}
		} else {
			logger.error("Unexpected stockCode");
			logger.error("  stockCode {}!", stockCode);
			throw new UnexpectedException("Unexpected stockCode");
		}
	}
	public static String toStockCode5(String stockCode) {
		if (stockCode.length() == 5) {
			return stockCode;
		} else if (stockCode.length() == 4) {
			return String.format("%s0", stockCode);
		} else {
			logger.error("Unexpected stockCode");
			logger.error("  stockCode {}!", stockCode);
			throw new UnexpectedException("Unexpected stockCode");
		}
	}
	
	public static enum Market {
		CERTIFICATE     ("出資証券"),
		ETF_ETN         ("ETF・ETN"),
		GROWTH          ("グロース（内国株式）"),
		GROWTH_FOREIGN  ("グロース（外国株式）"),
		PRIME           ("プライム（内国株式）"),
		PRIME_FOREIGN   ("プライム（外国株式）"),
		PRO_MARKET      ("PRO Market"),
		REIT_FUND       ("REIT・ベンチャーファンド・カントリーファンド・インフラファンド"),
		STANDARD        ("スタンダード（内国株式）"),
		STANDARD_FOREIGN("スタンダード（外国株式）");
		
		public final String value;
		Market(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value;
		}
	}

	@Sheet.ColumnName("日付")
	@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
	@CSVUtil.ColumnName("日付")
	public String date;
	
	@Sheet.ColumnName("コード")
	@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
	@CSVUtil.ColumnName("コード")
	public String stockCode;
	
	@Sheet.ColumnName("銘柄名")
	@CSVUtil.ColumnName("銘柄名")
	public String name;
	
	@Sheet.ColumnName("市場・商品区分")
	@CSVUtil.ColumnName("市場・商品区分")
	public Market market;
	
	@Sheet.ColumnName("33業種コード")
	@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
	@CSVUtil.ColumnName("33業種コード")
	public String sector33Code;
	
	@Sheet.ColumnName("33業種区分")
	@CSVUtil.ColumnName("33業種区分")
	public String sector33;
	
	@Sheet.ColumnName("17業種コード")
	@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
	@CSVUtil.ColumnName("17業種コード")
	public String sector17Code;
	
	@Sheet.ColumnName("17業種区分")
	@CSVUtil.ColumnName("17業種区分")
	public String sector17;
	
	@Sheet.ColumnName("規模コード")
	@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
	@CSVUtil.ColumnName("規模コード")
	public String scaleCode;
	
	@Sheet.ColumnName("規模区分")
	@CSVUtil.ColumnName("規模区分")
	public String scale;
	
	@Override
	public String toString() {
		if (this.isETF() || this.isREIT()) {
			return String.format("%s %s %s %s", date, stockCode, name, market);
		} else {
			return String.format("%s %s %s %s %s %s %s %s %s %s", date, stockCode, name, market, sector33Code, sector33, sector17Code, sector17, scale, scaleCode);
		}
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof Stock) {
			Stock that = (Stock)o;
			return this.date.equals(that.date) &&
					this.stockCode.equals(that.stockCode) &&
					this.name.equals(that.name) &&
					this.market.equals(that.market) &&
					this.sector33.equals(that.sector33) && this.sector33Code.equals(that.sector33Code) &&
					this.sector17.equals(that.sector17) && this.sector17Code.equals(that.sector17Code) &&
					this.scale.equals(that.scale) && this.scaleCode.equals(that.scaleCode);
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(Stock that) {
		int ret = this.date.compareTo(that.date);
		if (ret == 0) ret = this.stockCode.compareTo(that.stockCode);
		return ret;
	}
	
	public boolean isETF() {
		return market.equals(Market.ETF_ETN);
	}
	public boolean isREIT() {
		return market.equals(Market.REIT_FUND);
	}
	public boolean isInfraFund() {
		return market.equals(Market.REIT_FUND) && name.contains("インフラ");
	}
	public boolean isPROMarket() {
		return market.equals(Market.PRO_MARKET);
	}
	public boolean isCertificate() {
		return market.equals(Market.CERTIFICATE);
	}
}

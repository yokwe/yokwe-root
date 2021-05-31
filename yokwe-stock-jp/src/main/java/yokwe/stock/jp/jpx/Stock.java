package yokwe.stock.jp.jpx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

@Sheet.SheetName("Sheet1")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public class Stock extends Sheet implements Comparable<Stock> {	
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Stock.class);

	public static String getPath() {
		return JPX.getPath("stock.csv");
	}
	
	public static List<Stock> getList() {
		List<Stock> list = CSVUtil.read(Stock.class).file(getPath());
		if (list == null) {
			list = new ArrayList<>();
		}
		return list;
	}
	public static Map<String, Stock> getMap() {
		Map<String, Stock> map = new TreeMap<>();
		for(Stock e: getList()) {
			String stockCode = e.stockCode;
			if (map.containsKey(stockCode)) {
				logger.error("Duplicate stockCode {}", stockCode);
				throw new UnexpectedException("Duplicate stockCode");
			} else {
				map.put(stockCode, e);
			}
		}
		return map;
	}
	public static void save(Collection<Stock> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<Stock> list) {
		if (list.isEmpty()) return;
		
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(Stock.class).file(getPath(), list);
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
		ETF_ETN        ("ETF・ETN"), 
		JASDAQ_GROWTH  ("JASDAQ(グロース・内国株）"), 
		JASDAQ_STANDARD("JASDAQ(スタンダード・内国株）"),
		JASDAQ_FOREIGN ("JASDAQ(スタンダード・外国株）"),
		PRO_MARKET     ("PRO Market"),
		REIT_FUND      ("REIT・ベンチャーファンド・カントリーファンド・インフラファンド"),
		MOTHERS        ("マザーズ（内国株）"),
		MOTHERS_FOREIGN("マザーズ（外国株）"),
		CERTIFICATE    ("出資証券"),
		FIRST          ("市場第一部（内国株）"),
		FIRST_FOREIGN  ("市場第一部（外国株）"),
		SECOND         ("市場第二部（内国株）"),
		SECOND_FOREIGN ("市場第二部（外国株）");
		
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
	public Market market;            // FIXME util.libreoffice.Sheet.extractSheet cannot handle enum. need to be String
//	public String market;
	
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
	public boolean isPROMarket() {
		return market.equals(Market.PRO_MARKET);
	}
	public boolean isCertificate() {
		return market.equals(Market.CERTIFICATE);
	}
}

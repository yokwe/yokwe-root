package yokwe.stock.jp.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

@Sheet.SheetName("Sheet1")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public class JPXListing extends Sheet implements Comparable<JPXListing> {	
	private static final String PATH_FILE = Storage.JPX.getPath("jpx-listing.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<JPXListing> getList() {
		return ListUtil.getList(JPXListing.class, getPath());
	}
	public static Map<String, JPXListing> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<JPXListing> collection) {
		ListUtil.save(JPXListing.class, getPath(), collection);
	}
	public static void save(List<JPXListing> list) {
		ListUtil.save(JPXListing.class, getPath(), list);
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
	public String date;
	
	@Sheet.ColumnName("コード")
	@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public String stockCode;
	
	@Sheet.ColumnName("銘柄名")
	public String name;
	
	@Sheet.ColumnName("市場・商品区分")
	public Market market;
	
	@Sheet.ColumnName("33業種コード")
	@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public String sector33Code;
	
	@Sheet.ColumnName("33業種区分")
	public String sector33;
	
	@Sheet.ColumnName("17業種コード")
	@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public String sector17Code;
	
	@Sheet.ColumnName("17業種区分")
	public String sector17;
	
	@Sheet.ColumnName("規模コード")
	@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public String scaleCode;
	
	@Sheet.ColumnName("規模区分")
	public String scale;
	
	@Override
	public String toString() {
		return String.format("%s %s %s %s %s %s %s %s %s %s", date, stockCode, name, market, sector33Code, sector33, sector17Code, sector17, scale, scaleCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof JPXListing) {
			JPXListing that = (JPXListing)o;
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
	public int compareTo(JPXListing that) {
		int ret = this.date.compareTo(that.date);
		if (ret == 0) ret = this.stockCode.compareTo(that.stockCode);
		return ret;
	}
}

package yokwe.finance.provider.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

@Sheet.SheetName("Sheet1")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public class JPXListing extends Sheet implements Comparable<JPXListing> {	
	private static final String PATH_FILE = Storage.Provider.JPX.getPath("jpx-listing.csv");
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
		
	public static enum Kind {
		DOMESTIC_GROWTH  ("グロース（内国株式）"),
		DOMESTIC_STANDARD("スタンダード（内国株式）"),
		DOMESTIC_PRIME   ("プライム（内国株式）"),
		FOREIGN_GROWTH   ("グロース（外国株式）"),
		FOREIGN_STANDARD ("スタンダード（外国株式）"),
		FOREIGN_PRIME    ("プライム（外国株式）"),
		ETF_ETN          ("ETF・ETN"),
		REIT_FUND        ("REIT・ベンチャーファンド・カントリーファンド・インフラファンド"),
		CERTIFICATE      ("出資証券"),
		PRO_MARKET       ("PRO Market");
		
		public final String value;
		Kind(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
	
	public static enum Topix {
		CORE_30 ("TOPIX Core30"),
		LARGE_70("TOPIX Large70"),
		MID_400 ("TOPIX Mid400"),
		SMALL_1 ("TOPIX Small 1"),
		SMALL_2 ("TOPIX Small 2"),
		ETF_ETN ("-");
		
		public final String value;
		private Topix(String newValue) {
			this.value = newValue;
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
	public Kind kind;
	
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
	public Topix topix;
	
	@Override
	public String toString() {
		return String.format("%s %s %s %s %s %s %s %s %s %s", date, stockCode, name, kind, sector33Code, sector33, sector17Code, sector17, topix, scaleCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof JPXListing) {
			JPXListing that = (JPXListing)o;
			return this.date.equals(that.date) &&
					this.stockCode.equals(that.stockCode) &&
					this.name.equals(that.name) &&
					this.kind.equals(that.kind) &&
					this.sector33.equals(that.sector33) && this.sector33Code.equals(that.sector33Code) &&
					this.sector17.equals(that.sector17) && this.sector17Code.equals(that.sector17Code) &&
					this.topix.equals(that.topix) && this.scaleCode.equals(that.scaleCode);
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

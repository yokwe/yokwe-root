package yokwe.stock.jp.toushin2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.CSVUtil;

public class Report implements Comparable<Report> {
	private static final String PREFIX = "report";
		
	public static final String getPath(String name) {
		return Storage.Toushin2.getPath(PREFIX, name);
	}
	
	public static void save(List<Report> list, String name) {
		Collections.sort(list);
		CSVUtil.write(Report.class).file(getPath(name), list);
	}
	public static List<Report> load(String name) {
		return CSVUtil.read(Report.class).file(getPath(name));
	}

	
	public String isinCode = null;             // isinCode
	public String name     = null;

	public LocalDate issueDate      = null; // 設定日
	public LocalDate redemptionDate = null; // 償還日

	public String cat1 = null; // 追加型 単位型
	public String cat2 = null; // 国内 海外 内外
	public String cat3 = null; // 債券 株式 資産複合
	public String cat4 = null; // インデックス型 該当なし 特殊型
	
	public BigDecimal initialFeeMin  = null; // 購入時手数料 最小
	public BigDecimal initialFeeMax  = null; // 購入時手数料 最大
	public String     cancelationFee = null; // 解約手数料
	public String     redemptionFee  = null; // 信託財産留保額
	
	public int    priceC      = 0;
	public int    settlementC = 0; // 決算頻度
	public int    divC        = 0;

	// div
	public double div   = 0;   // last 1 year
	public double yield = 0; // div / price

	// price
	public double price       = 0; // 基準価額(円) 1口あたり
	public double minPrice    = 0;
	public double maxPrice    = 0;
	public double minPricePCT = 0;
	public double maxPricePCT = 0;
	
	// net asset value
	public double nav       = 0;   // 純資産総額
	public double minNav    = 0;
	public double maxNav    = 0;
	public double minNavPCT = 0;
	public double maxNavPCT = 0;

	// units
	public double units       = 0;   // 総口数
	public double minUnits    = 0;
	public double maxUnits    = 0;
	public double minUnitsPCT = 0;
	public double maxUnitsPCT = 0;

	
	// default constructor
	public Report() {}

	
	@Override
	public int compareTo(Report that) {
		return this.isinCode.compareTo(that.isinCode);
	}
}

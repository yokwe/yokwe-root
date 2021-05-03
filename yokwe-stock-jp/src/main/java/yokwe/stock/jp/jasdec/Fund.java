package yokwe.stock.jp.jasdec;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;

public class Fund implements Comparable<Fund> {
	public static final String INDEFINITE = "9999-12-31";
	public static final String PREFIX = "jasdec";
	
	public static final String getPath() {
		return Storage.getPath(PREFIX, "fund.csv");
	}
	
	public static void save(List<Fund> list) {
		Collections.sort(list);
		CSVUtil.write(Fund.class).file(getPath(), list);
	}
	public static List<Fund> load() {
		return CSVUtil.read(Fund.class).file(getPath());
	}

	public int        idno;           // IDNO
	public String     isinCode;       // ISINコード
	public LocalDate  issueDate;      // 設定日
	public LocalDate  redemptionDate; // 償還日
	public BigDecimal unitPrice;      // 当初1口当たり元本
	public BigDecimal minimuUnit;     // 最低発行単位口数
	public String     offerCategory;  // 募集区分
	public String     fundCategory;   // 投信区分
	public String     issuer;         // 発行者名	
	public String     name;           // 銘柄正式名称
	
	public Fund(
		int idno, String isinCode, LocalDate issueDate, LocalDate redemptionDate, BigDecimal unitPrice, BigDecimal minimuUnit, 
		String offerCategory, String fundCategory, String issuer, String name) {
		this.idno           = idno;
		this.isinCode       = isinCode;
		this.issueDate      = issueDate;
		this.redemptionDate = redemptionDate;
		this.unitPrice      = unitPrice;
		this.minimuUnit     = minimuUnit;
		this.offerCategory  = offerCategory;
		this.fundCategory   = fundCategory;
		this.issuer         = issuer;
		this.name           = name;
	}
	public Fund() {
		this(0, null, null, null, null, null, null, null, null, null);
	}

	@Override
	public String toString() {
		return StringUtil.toString(this);
	}

	@Override
	public int compareTo(Fund that) {
		return this.idno - that.idno;
	}
}

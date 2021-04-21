package yokwe.stock.jp.jasdec;

import java.util.Collections;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;

public class Fund implements Comparable<Fund> {
	public static final String NO_LIMIT = "9999-12-31";
	public static final String PREFIX = "jasdec";
	
	public static final String getPath() {
		return Storage.getPath(PREFIX, "fund.csv");
	}
	
	public static void save(List<Fund> funds) {
		Collections.sort(funds);
		CSVUtil.write(Fund.class).file(getPath(), funds);
	}
	public static List<Fund> load() {
		return CSVUtil.read(Fund.class).file(getPath());
	}

	public int    idno;           // IDNO
	public String isinCode;       // ISINコード
	public String offerDate;      // 設定日
	public String repaymentDate;  // 償還日
	public String offerCategory;  // 募集区分
	public String fundCategory;   // 投信区分
	public String issuer;         // 発行者名	
	public String name;           // 銘柄正式名称
	
	public Fund(int idno, String isinCode, String offerDate, String repaymentDate, String offerCategory, String fundCategory, String issuer, String name) {
		this.idno          = idno;
		this.isinCode      = isinCode;
		this.offerDate     = offerDate;
		this.repaymentDate = repaymentDate;
		this.offerCategory = offerCategory;
		this.fundCategory  = fundCategory;
		this.issuer        = issuer;
		this.name          = name;
	}
	public Fund() {
		this(0, null, null, null, null, null, null, null);
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

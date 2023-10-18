package yokwe.finance.provider.jreit;

import java.time.LocalDate;

import yokwe.util.StringUtil;

public class JREITInfoType implements Comparable<JREITInfoType> {
	public static final String CATEGORY_INFRA_FUND = "INFRA FUND";

	// https://www.japan-reit.com/meigara/8954/info/
	// 上場日 2002/06/12
	
	public String    stockCode;
	public LocalDate listingDate;
	public int       divFreq;
	public String    category;
	public String    name;

	public JREITInfoType(String stockCode, LocalDate listingDate, int divFreq, String category, String name) {
		this.stockCode   = stockCode;
		this.listingDate = listingDate;
		this.divFreq     = divFreq;
		this.category    = category;
		this.name        = name;
	}
	public JREITInfoType() {}
	
	@Override
	public String toString() {
	    return StringUtil.toString(this);
	}

	@Override
	public int compareTo(JREITInfoType that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	@Override
	public int hashCode() {
		return stockCode.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null) {
			if (o instanceof JREITInfoType) {
				JREITInfoType that = (JREITInfoType)o;
				return
					this.stockCode.equals(that.stockCode) &&
					this.listingDate.equals(that.listingDate) &&
					this.divFreq == that.divFreq &&
					this.category.equals(that.category) &&
					this.name.equals(that.name);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}

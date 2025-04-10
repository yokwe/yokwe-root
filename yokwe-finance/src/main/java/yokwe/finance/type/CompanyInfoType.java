package yokwe.finance.type;

import yokwe.util.ToString;

public class CompanyInfoType implements Comparable<CompanyInfoType> {
	public String stockCode;
	public String sector;
	public String industry;	
	
	public CompanyInfoType(String stockCode, String sector, String industry) {
		this.stockCode = stockCode;
		this.sector    = sector;
		this.industry  = industry;
	}
	public CompanyInfoType() {}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	@Override
	public int compareTo(CompanyInfoType that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof CompanyInfoType) {
			CompanyInfoType that = (CompanyInfoType)o;
			return
				this.stockCode.equals(that.stockCode) &&
				this.sector.equals(that.sector) &&
				this.industry.equals(that.industry);
		} else {
			return false;
		}
	}

}

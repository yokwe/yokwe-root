package yokwe.finance.provider.manebu;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.StringUtil;

public class ETFInfoType implements Comparable<ETFInfoType> {
	public String     stockCode;
	public LocalDate  listingDate;
	
	public String     category;
	public String     productType;
	
	public BigDecimal expenseRatio;
		
	public int        divFreq;
	
	public BigDecimal shintakuRyuhogaku;
	
	public BigDecimal fundUnit; // can be 1, 10, 100, 1000

	public String     name;


	public ETFInfoType(
		String stockCode, String category, BigDecimal expenseRatio, String name,
		int  divFreq, LocalDate listintDate,
		String productType, BigDecimal shintakuRyuhogaku, BigDecimal fundUnit) {
		this.stockCode    = stockCode;
		this.category     = category;
		this.expenseRatio = expenseRatio;
		this.name         = name;
		
		this.divFreq      = divFreq;
		this.listingDate  = listintDate;
		
		this.productType        = productType;
		this.shintakuRyuhogaku  = shintakuRyuhogaku;
		this.fundUnit           = fundUnit;
	}
	public ETFInfoType() {}
	
	@Override
	public String toString() {
	    return StringUtil.toString(this);
	}

	@Override
	public int compareTo(ETFInfoType that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	@Override
	public int hashCode() {
		return stockCode.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null) {
			if (o instanceof ETFInfoType) {
				ETFInfoType that = (ETFInfoType)o;
				return
					this.stockCode.equals(that.stockCode) &&
					this.category.equals(that.category) &&
					this.expenseRatio.equals(that.expenseRatio) &&
					this.name.equals(that.name);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}

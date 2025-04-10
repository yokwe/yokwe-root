package yokwe.finance.provider.prestia;

import java.math.BigDecimal;

import yokwe.finance.type.Currency;
import yokwe.util.ToString;

public class FundInfoPrestia implements Comparable<FundInfoPrestia> {
    public final String     secId;
    public final Currency   currency;
    public final String     fundCode;
    public final String     isinCode;
    public final BigDecimal salesFee;
    public final String     fundName;
    
    public FundInfoPrestia(
	    String     secId,
	    Currency   currency,
	    String     fundCode,
	    String     isinCode,
	    BigDecimal salesFee,
	    String     fundName
    	) {
    	this.secId    = secId;
    	this.currency = currency;
    	this.fundCode = fundCode;
    	this.isinCode = isinCode;
    	this.salesFee = salesFee;
    	this.fundName = fundName;
    }
    
    public boolean hasISINCode() {
    	return !isinCode.isEmpty();
    }

	@Override
	public int compareTo(FundInfoPrestia that) {
		return this.secId.compareTo(that.secId);
	}
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}

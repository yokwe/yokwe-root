package yokwe.finance.provider.sony;

import yokwe.util.ToString;

public class FundInfoSony implements Comparable<FundInfoSony> {
	public final String isinCode;
    public final String sbFundCode;
    public final String fundMei;
    
    public FundInfoSony (
		String isinCode,
		String sbFundCode,
		String fundMei
	) {
		this.isinCode   = isinCode;
		this.sbFundCode = sbFundCode;
		this.fundMei    = fundMei;
    }
    
	@Override
	public int compareTo(FundInfoSony that) {
		return this.isinCode.compareTo(that.isinCode);
	}
    @Override
    public String toString() {
    	return ToString.withFieldName(this);
    }
}

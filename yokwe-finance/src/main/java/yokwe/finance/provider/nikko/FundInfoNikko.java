package yokwe.finance.provider.nikko;

public class FundInfoNikko implements Comparable<FundInfoNikko>{
	public final String isinCode;
	public final int    prospectus;
	public final int    sougou;
	public final int    direct;
	public final String name;
	
	public FundInfoNikko(
		String isinCode,
		int    prospectus,
		int    sougou,
		int    direct,
		String name
		) {
		this.isinCode   = isinCode;
		this.prospectus = prospectus == 0 ? 0 : 1;
		this.sougou     = sougou     == 0 ? 0 : 1;
		this.direct     = direct     == 0 ? 0 : 1;
		this.name       = name;
	}
	
	public boolean hasProspectus() {
		return prospectus != 0;
	}
	public boolean isSougou() {
		return sougou != 0;
	}
	public boolean isDirect() {
		return direct != 0;
	}

	@Override
	public int compareTo(FundInfoNikko that) {
		return this.isinCode.compareTo(that.isinCode);
	}
	@Override
	public String toString() {
		return String.format("{%s  %s  %s  %s  %s}", isinCode, hasProspectus(), isSougou(), isDirect(), name);
	}
}

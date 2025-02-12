package yokwe.finance.type;

public class NISAInfoType implements Comparable<NISAInfoType> {
	public String  isinCode;
	public boolean tsumitate;
	
	public NISAInfoType(String stockCode, boolean tsumitate) {
		this.isinCode  = stockCode;
		this.tsumitate = tsumitate;
	}
	public NISAInfoType() {}
	
	@Override
	public String toString() {
		return String.format("{%s  %s  %s}", isinCode, tsumitate ? "1" : "0");
	}
	@Override
	public int compareTo(NISAInfoType that) {
		return this.isinCode.compareTo(that.isinCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof NISAInfoType) {
			NISAInfoType that = (NISAInfoType)o;
			return this.isinCode.equals(that.isinCode) && this.tsumitate == that.tsumitate;
		} else {
			return false;
		}
	}
}

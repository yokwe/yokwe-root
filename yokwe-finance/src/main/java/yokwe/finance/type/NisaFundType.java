package yokwe.finance.type;

public class NisaFundType implements Comparable<NisaFundType> {
	public enum Accumulable {
		NO(0), YES(1);
		
		public final int value;
		Accumulable(int value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}

	public String      isinCode;
	public Accumulable accumulable;
	
	public NisaFundType(String stockCode, Accumulable accumulable) {
		this.isinCode    = stockCode;
		this.accumulable = accumulable;
	}
	public NisaFundType() {}
	
	@Override
	public String toString() {
		return String.format("%s", isinCode);
	}
	@Override
	public int compareTo(NisaFundType that) {
		return this.isinCode.compareTo(that.isinCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof NisaFundType) {
			NisaFundType that = (NisaFundType)o;
			return this.isinCode.equals(that.isinCode) && this.accumulable == that.accumulable;
		} else {
			return false;
		}
	}
}

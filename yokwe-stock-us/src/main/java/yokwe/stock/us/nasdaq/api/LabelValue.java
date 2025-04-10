package yokwe.stock.us.nasdaq.api;

import yokwe.util.ToString;

public final class LabelValue {
	public String label;
	public String value;
	
	public LabelValue() {
		label = null;
		value = null;
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
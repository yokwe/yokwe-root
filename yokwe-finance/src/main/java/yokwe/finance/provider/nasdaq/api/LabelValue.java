package yokwe.finance.provider.nasdaq.api;

import yokwe.util.StringUtil;

public final class LabelValue {
	public String label;
	public String value;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}
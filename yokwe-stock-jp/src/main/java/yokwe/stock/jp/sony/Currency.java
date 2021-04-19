package yokwe.stock.jp.sony;

import yokwe.util.EnumUtil;

public enum Currency {
	AUD,
	CAD,
	JPY,
	NZD,
	USD;
	public static Currency get(String value) {
		return EnumUtil.getInstance(Currency.class, value);
	}
}
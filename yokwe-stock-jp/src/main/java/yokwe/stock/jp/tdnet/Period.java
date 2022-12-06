package yokwe.stock.jp.tdnet;

import yokwe.util.UnexpectedException;

// 期区分
public enum Period {
	ANNUAL ("a", "通期"),   // 通期
	HALF   ("s", "中間期"), // 特定事業会社第２四半期／中間期
	QUATER ("q", "四半期"); // 四半期
	
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final Period[] VALUES = Period.values();
	public static Period getInstance(String value) {
		if (value == null || value.isEmpty()) return null;
		for(Period period: VALUES) {
			if (value.equals(period.value)) return period;
		}
		logger.error("Unknown value {}!", value);
		throw new UnexpectedException("Unknown value");
	}
	
	public final String value;
	public final String message;
	
	Period(String value, String message) {
		this.value   = value;
		this.message = message;
	}
	
	@Override
	public String toString() {
		return message;
	}
}
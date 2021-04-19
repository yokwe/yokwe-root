package yokwe.stock.jp.tdnet;

import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;

// 連結・非連結区分
public enum Consolidate {
	CONSOLIDATE     ("c", "連結"), // 連結
	NOT_CONSOLIDATE ("n", "単体"); // 非連結
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Consolidate.class);

	private static final Consolidate[] VALUES = Consolidate.values();
	public static Consolidate getInstance(String value) {
		if (value == null || value.isEmpty()) return null;
		for(Consolidate consolidate: VALUES) {
			if (value.equals(consolidate.value)) return consolidate;
		}
		logger.error("Unknown value {}!", value);
		throw new UnexpectedException("Unknown value");
	}
	
	public final String value;
	public final String message;
			
	Consolidate(String value, String message) {
		this.value   = value;
		this.message = message;
	}
	
	@Override
	public String toString() {
		return message;
	}
}
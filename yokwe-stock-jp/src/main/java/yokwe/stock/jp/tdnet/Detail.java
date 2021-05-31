package yokwe.stock.jp.tdnet;

import yokwe.util.UnexpectedException;

// 報告詳細区分
public enum Detail {
	SUMMARY   ("sm", "サマリー"), // 決算短信サマリー情報
	FINANCIAL ("fr", "財務諸表"); // 決算短信財務諸表情報
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Detail.class);

	private static final Detail[] VALUES = Detail.values();
	public static Detail getInstance(String value) {
		if (value == null || value.isEmpty()) return null;
		for(Detail detail: VALUES) {
			if (value.equals(detail.value)) return detail;
		}
		logger.error("Unknown value {}!", value);
		throw new UnexpectedException("Unknown value");
	}
	
	public final String value;
	public final String message;
			
	Detail(String value, String message) {
		this.value   = value;
		this.message = message;
	}
	
	@Override
	public String toString() {
		return message;
	}
}
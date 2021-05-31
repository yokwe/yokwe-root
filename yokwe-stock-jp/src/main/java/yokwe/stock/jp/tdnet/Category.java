package yokwe.stock.jp.tdnet;

import yokwe.util.UnexpectedException;

// 報告区分
public enum Category {
	EDJP   ("edjp", "決算短信（日本基準）"),              // 決算短信（日本基準）
	EDUS   ("edus", "決算短信（米国基準）"),              // 決算短信（米国基準）
	EDIF   ("edif", "決算短信（国際会計基準）"),           // 決算短信（国際会計基準）
	EDIT   ("edit", "決算短信（国際会計基準※IFRS）"),     // 決算短信（国際会計基準） ※IFRSタクソノミを利用する場合
	
	REJP   ("rejp", "REIT決算短信（日本基準）"),          // REIT決算短信（日本基準）
	EFJP   ("efjp", "ETF決算短信（日本基準）"),           // ETF決算短信（日本基準）

	RVDF   ("rvdf", "配当予想修正に関するお知らせ"),        // 配当予想修正に関するお知らせ
	RVFC   ("rvfc", "業績予想修正に関するお知らせ"),        // 業績予想修正に関するお知らせ
	RRDF   ("rrdf", "分配予想の修正に関するお知らせ"),      // 分配予想の修正に関するお知らせ
	RRFC   ("rrfc", "運用状況の予想の修正に関するお知らせ"); // 運用状況の予想の修正に関するお知らせ
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Category.class);
	
	private static final Category[] VALUES = Category.values();
	public static Category getInstance(String value) {
		if (value == null || value.isEmpty()) return null;
		for(Category category: VALUES) {
			if (value.equals(category.value)) return category;
		}
		logger.error("Unknown value {}!", value);
		throw new UnexpectedException("Unknown value");
	}
	
	public final String value;
	public final String message;
			
	Category(String value, String message) {
		this.value   = value;
		this.message = message;
	}
	
	@Override
	public String toString() {
		return message;
	}
}
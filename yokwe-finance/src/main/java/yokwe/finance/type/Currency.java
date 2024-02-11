package yokwe.finance.type;

public enum Currency {
	JPY("日本円"),
	USD("米ドル"),
	EUR("ユーロ"),
	GBP("ポンド"),
	AUD("AUドル"),
	NZD("NZドル");
	
	public final String description;
	private Currency(String nweValue) {
		this.description = nweValue;
	}
}
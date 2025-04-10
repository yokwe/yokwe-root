package yokwe.stock.jp.nikkei;

import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;

public class CommonPrice {
	//<!-- ▼ QP-COMMON03：共通（価格） ▼ -->
	//<div class="m-stockPriceElm">
	//    <dl>
	//        <dt class="m-stockPriceElm_title">基準価格(5/2)：</dt>
	//        <dd class="m-stockPriceElm_value now">11,408<span class="m-stockPriceElm_value_unit">円</span></dd>
	//    </dl>
	//    <dl>
	//        <dt class="m-stockPriceElm_title">前日比：</dt>
	//        <dd class="m-stockPriceElm_value comparison minus">-8<span class="a-fs16 a-ml5">(-0.07%)</span></dd>
	//    </dl>
	//</div>
	public static final String HEADER = "<!-- ▼ QP-COMMON03：共通（価格） ▼ -->";
	public static final Pattern PAT = Pattern.compile(
			HEADER + "\\s+" +
			"<div .+?>\\s+" +
			"<dl>\\s+" +
			"<dt .+?>基準価格\\((?<date>.+?)\\)：</dt>\\s+" +
			"<dd .+?>(?<value>.+?)<span .+?>(?<unit>.+?)</span></dd>\\s+" +
			""
		);
		public static CommonPrice getInstance(String page) {
			return ScrapeUtil.get(CommonPrice.class, PAT, page);
		}

		public String date;
		@ScrapeUtil.AsNumber
		public String price;
		public String unit;
		
		public CommonPrice(String date, String value, String unit) {
			this.date  = date;
			this.price = value;
			this.unit  = unit;
		}
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
}
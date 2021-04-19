package yokwe.stock.jp.sony.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import yokwe.util.StringUtil;

public class Basic {
	public static class Fund {
		public static class Price {
//		    <Price
//		      KIJYUN_YMD="2020年04月15日"
//		      KIJYUNKAGAKU="14,722"
//		      TICK="1"
//		      DAYCHANGE="300"
//		      DAYCHANGE_RATIO="2.08"
//		      JYUNSHISAN="147,851"/>

			@XmlAttribute(name="KIJYUN_YMD")      public String kijunYMD;
			@XmlAttribute(name="KIJYUNKAGAKU")    public String kijunkagaku;
			@XmlAttribute(name="TICK")            public String tick;
			@XmlAttribute(name="DAYCHANGE")       public String daychange;
			@XmlAttribute(name="DAYCHANGE_RATIO") public String daychangeRatio;
			@XmlAttribute(name="JYUNSHISAN")      public String jynshisan;
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}
		
		public static class Base {
//		    <Base
//		      HYOKA_YMD="2020年03月31日"
//		      CATEGORY_NAME="国際株式・グローバル・除く日本（F）"
//		      MS_RATING="★★★★"
//		      RISKMEASURE="4（やや高い）"
//		      F50_FLAG=""/>

			@XmlAttribute(name="HYOKA_YMD")     public String hyokaYMD;
			@XmlAttribute(name="CATEGORY_NAME") public String categoryName;
			@XmlAttribute(name="MS_RATING")     public String msRating;
			@XmlAttribute(name="RISKMEASURE")   public String riskmeasure;
			@XmlAttribute(name="F50_FLAG")      public String f50Flag;
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}


//		  <Fund
//		  	MS_FUND_CODE="2013121001"
//		  	FUND_NAME="ニッセイ 外国株式インデックスファンド"
//		  	FUND_NAME_OFFICIAL="＜購入･換金手数料なし＞ニッセイ 外国株式インデックスファンド"
//		  	FUND_NICKNAME=""
//		  	FUND_NAME_KANA=""
//		  	FUND_NAME_OFFICIAL_KANA="コウニュウカンキンテスウリョウナシニッセイガイコクカブシキインデックスファンド"
//		  	COMPANY_NAME="ニッセイアセットマネジメント"
//		  	ISIN="JP90C0009VE0">

		@XmlAttribute(name="MS_FUND_CODE")            public String mdFundCode;
		@XmlAttribute(name="FUND_NAME")               public String fundName;
		@XmlAttribute(name="FUND_NAME_OFFICIAL")      public String fundNameOfficial;
		@XmlAttribute(name="FUND_NICKNAME")           public String fundNickname;
		@XmlAttribute(name="FUND_NAME_KANA")          public String fundNameKana;
		@XmlAttribute(name="FUND_NAME_OFFICIAL_KANA") public String fundNameOfficialKana;
		@XmlAttribute(name="COMPANY_NAME")            public String companyName;
		@XmlAttribute(name="ISIN")                    public String isin;
		
		@XmlElement(name = "Price") public Price price;
		@XmlElement(name = "Base")  public Base  base;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	@XmlElement(name = "Fund") public Fund fund;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}

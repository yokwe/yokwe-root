package yokwe.stock.jp.sony.xml;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import yokwe.util.ToString;

public class ChartFundData {
	public static class Fund {
		public static class Year {
			public static class Month {
				public static class Day {
//					<day
//					  value="04"
//					  year="2015"
//					  month="01"
//					  price=""
//					  volume=""
//					  return_value=""
//					  indication="0"/>

//					<day
//					  value="05"
//					  year="2015"
//					  month="01"
//					  price="12607"
//					  volume="6710"
//					  return_value=".98078419"
//					  indication="1"/>
					
					@XmlAttribute(name="value")        public String value;
					@XmlAttribute(name="year")         public String year;
					@XmlAttribute(name="month")        public String month;
					@XmlAttribute(name="price")        public String price;       // 基準価額
					@XmlAttribute(name="volume")       public String volume;      // 純資産総額
					@XmlAttribute(name="return_value") public String returnValue; // 前日比
					@XmlAttribute(name="indication")   public String indication;

					@Override
					public String toString() {
						return ToString.withFieldName(this);
					}
				}

//				<month
//				  value="01">
				
				@XmlAttribute(name="value") public String value;
				
				@XmlElement(name = "day")   public List<Day> dayList;
				
				@Override
				public String toString() {
					return ToString.withFieldName(this);
				}
			}

//			<year
//			  value="2014">
			
			@XmlAttribute(name="value") public String value;

			@XmlElement(name = "month") public List<Month> monthList;
			
			@Override
			public String toString() {
				return ToString.withFieldName(this);
			}
		}

//		<fund
//		  code="2013121001"
//		  name="ニッセイ 外国株式インデックスファンド"
//		  period_start="20131210"
//		  period_end="0       ">
		
		@XmlAttribute(name="code")         public String code;
		@XmlAttribute(name="name")         public String name;
		@XmlAttribute(name="period_start") public String periodStart;
		@XmlAttribute(name="period_end")   public String periodEnd;

		@XmlElement(name = "year")         public List<Year> yearList;
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	@XmlElement(name = "fund") public Fund fund;
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}

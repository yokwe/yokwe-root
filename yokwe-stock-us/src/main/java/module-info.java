open module yokwe.stock.us {
	exports yokwe.stock.us;
	exports yokwe.stock.us.nasdaq;
	exports yokwe.stock.us.nasdaq.api;
	exports yokwe.stock.us.monex;
	exports yokwe.stock.us.nikko;
	exports yokwe.stock.us.rakuten;
	exports yokwe.stock.us.sbi;

	// http
	requires org.apache.httpcomponents.core5.httpcore5;
	requires org.apache.httpcomponents.core5.httpcore5.h2;
	
	// json
	requires transitive jakarta.json;
	requires jakarta.json.bind;
	
	// xml binding
	requires transitive java.xml;
	requires transitive jakarta.xml.bind;
	
	// logging
	requires org.slf4j;
	
	// yokwe-util
	requires transitive yokwe.util;
}
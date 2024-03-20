open module yokwe.stock.trade {
	exports yokwe.stock.trade;
	exports yokwe.stock.trade.data;
	exports yokwe.stock.trade.gmo;
	exports yokwe.stock.trade.monex;
	exports yokwe.stock.trade.report;

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
	
	// yokwe-stock-jp
	requires transitive yokwe.stock.jp;
	
	// yokwe-stock-jp
	requires transitive yokwe.stock.us;
}
open module yokwe.stock.trade {
	exports yokwe.stock.trade;
	exports yokwe.stock.trade.data;
	exports yokwe.stock.trade.gmo;

	// http
	requires httpcore5;
	requires httpcore5.h2;
	
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
}
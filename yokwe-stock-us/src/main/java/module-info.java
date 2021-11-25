open module yokwe.stock.us {
	exports yokwe.stock.us;

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
}
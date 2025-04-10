open module yokwe.util {
	exports yokwe.util;
	exports yokwe.util.libreoffice;
	exports yokwe.util.stats;
	exports yokwe.util.xml;
	exports yokwe.util.http;
	exports yokwe.util.json;
	exports yokwe.util.finance;
	exports yokwe.util.finance.online;
	exports yokwe.util.yahoo.finance;

	requires commons.math3;
	requires org.apache.commons.net;
	
	requires transitive org.apache.httpcomponents.core5.httpcore5;
	requires org.apache.httpcomponents.core5.httpcore5.h2;
	requires org.apache.httpcomponents.client5.httpclient5;
	
	// json from jakarta ee
	requires transitive jakarta.json;
	requires transitive jakarta.json.bind;
	
	// xml bind from jakarta ee
	requires transitive java.xml;
	requires transitive jakarta.xml.bind;
	
	// mail from jakarta ee
	requires jakarta.mail;
	
	requires transitive ch.qos.logback.classic;
	requires ch.qos.logback.core;
	requires transitive org.slf4j;
	
	requires transitive org.libreoffice.uno;
	
}
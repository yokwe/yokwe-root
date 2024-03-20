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
	
	requires org.apache.httpcomponents.core5.httpcore5;
	requires org.apache.httpcomponents.core5.httpcore5.h2;
	requires httpclient;
	requires httpcore;
	
	// json from jakarta ee
	requires jakarta.json;
	requires jakarta.json.bind;
	
	// xml bind from jakarta ee
	requires transitive java.xml;
	requires jakarta.xml.bind;
	
	// mail from jakarta ee
	requires jakarta.mail;
	
	requires ch.qos.logback.classic;
	requires ch.qos.logback.core;
	requires transitive org.slf4j;
	
	requires transitive org.libreoffice.uno;
	
}
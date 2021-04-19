module yokwe.util {
	exports yokwe.util;
	exports yokwe.util.libreoffice;
	exports yokwe.util.stats;
	exports yokwe.util.xml;
	exports yokwe.util.http;
	exports yokwe.util.json;

	requires commons.math3;
	
	requires httpclient;
	requires httpcore;
	requires httpcore5;
	requires transitive httpcore5.h2;
	
	requires java.json;
	requires transitive java.xml;
	requires javax.mail.api;
	
	requires logback.classic;
	requires logback.core;
	requires org.slf4j;
	
	requires transitive org.libreoffice.uno;
	
}
open module yokwe.finance {
	exports yokwe.finance;
	
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
	
	
	// jfree chart
	requires transitive org.jfree.jfreechart;
	// for VetoableChangeListener used in jfreechart
	requires java.desktop;
	
	
	// selenium
	requires transitive org.seleniumhq.selenium.api;
	requires transitive org.seleniumhq.selenium.manager;
	requires transitive org.seleniumhq.selenium.json;
	requires transitive org.seleniumhq.selenium.http;
	requires transitive org.seleniumhq.selenium.os;
	requires transitive org.seleniumhq.selenium.remote_driver;
	//
	requires dev.failsafe.core;
	requires org.seleniumhq.selenium.chrome_driver;
	requires org.seleniumhq.selenium.safari_driver;
	requires org.seleniumhq.selenium.support;
	
	requires jul.to.slf4j;
}
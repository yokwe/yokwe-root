module yokwe.stock.jp {
	exports yokwe.stock.jp;
	exports yokwe.stock.jp.data;
	exports yokwe.stock.jp.edinet;
	exports yokwe.stock.jp.fsa;
	exports yokwe.stock.jp.jasdec;
	exports yokwe.stock.jp.jpx;
	exports yokwe.stock.jp.release;
	exports yokwe.stock.jp.smbctb;
	exports yokwe.stock.jp.smbctb.json;
	exports yokwe.stock.jp.sony;
	exports yokwe.stock.jp.sony.json;
	exports yokwe.stock.jp.sony.xml;
	exports yokwe.stock.jp.tdnet;
	exports yokwe.stock.jp.xbrl;
	exports yokwe.stock.jp.xbrl.inline;
	exports yokwe.stock.jp.xbrl.label;
	exports yokwe.stock.jp.xbrl.report;
	exports yokwe.stock.jp.xbrl.taxonomy;
	exports yokwe.stock.jp.xsd;

	// http
	requires httpcore5;
	requires httpcore5.h2;
	
	// java
	requires java.json;
	requires java.xml;
	requires transitive java.xml.bind;
	
	// logging
	requires org.slf4j;
	
	// yokwe-util
	requires transitive yokwe.util;
}
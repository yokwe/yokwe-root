open module yokwe.stock.jp {
	exports yokwe.stock.jp;
	exports yokwe.stock.jp.edinet;
	exports yokwe.stock.jp.jasdec;
	exports yokwe.stock.jp.jpx;
	exports yokwe.stock.jp.sony;
	exports yokwe.stock.jp.sony.json;
	exports yokwe.stock.jp.sony.xml;
	exports yokwe.stock.jp.tdnet;
	exports yokwe.stock.jp.toushin;
	exports yokwe.stock.jp.xbrl;
	exports yokwe.stock.jp.xbrl.inline;
	exports yokwe.stock.jp.xbrl.tdnet;
	exports yokwe.stock.jp.xbrl.tdnet.inline;
	exports yokwe.stock.jp.xbrl.tdnet.label;
	exports yokwe.stock.jp.xbrl.tdnet.report;
	exports yokwe.stock.jp.xbrl.tdnet.taxonomy;

	// yokwe-util
	requires transitive yokwe.util;
	
	
	// jfree chart
	requires transitive org.jfree.jfreechart;
	// for VetoableChangeListener used in jfreechart
	requires java.desktop;
	
}
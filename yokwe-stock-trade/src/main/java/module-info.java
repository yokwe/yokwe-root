open module yokwe.stock.trade {
	exports yokwe.stock.trade;
	exports yokwe.stock.trade.data;
	exports yokwe.stock.trade.gmo;
	exports yokwe.stock.trade.monex;
	exports yokwe.stock.trade.report;

	// yokwe-util
	requires transitive yokwe.util;
	
	// yokwe-stock-jp
	requires transitive yokwe.stock.jp;
	
	// yokwe-stock-jp
	requires transitive yokwe.stock.us;
}
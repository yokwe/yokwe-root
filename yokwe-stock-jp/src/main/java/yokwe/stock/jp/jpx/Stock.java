package yokwe.stock.jp.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.UnexpectedException;

public class Stock implements Comparable<Stock> {	
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final String PATH_FILE = Storage.JPX.getPath("stock.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<Stock> getList() {
		return ListUtil.getList(Stock.class, getPath());
	}
	public static Map<String, Stock> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<Stock> collection) {
		ListUtil.save(Stock.class, getPath(), collection);
	}
	public static void save(List<Stock> list) {
		ListUtil.save(Stock.class, getPath(), list);
	}
	
	public static String toStockCode4(String stockCode) {
		if (stockCode.length() == 4) {
			return stockCode;
		} else if (stockCode.length() == 5) {
			if (stockCode.endsWith("0")) {
				return stockCode.substring(0, 4);
			} else {
				return stockCode; // 25935 伊藤園 優先株式,市場第一部（内国株）
			}
		} else {
			logger.error("Unexpected stockCode");
			logger.error("  stockCode {}!", stockCode);
			throw new UnexpectedException("Unexpected stockCode");
		}
	}
	public static String toStockCode5(String stockCode) {
		if (stockCode.length() == 5) {
			return stockCode;
		} else if (stockCode.length() == 4) {
			return String.format("%s0", stockCode);
		} else {
			logger.error("Unexpected stockCode");
			logger.error("  stockCode {}!", stockCode);
			throw new UnexpectedException("Unexpected stockCode");
		}
	}
	
	public static boolean isPreferredStock(String stockCode) {
		// https://www.jpx.co.jp/sicc/news/nlsgeu00000329fb-att/bessi2.pdf
		//   予備コードは次のような場合に固有名コードの末尾につけて使用する。
		//   新株式 １ 第二新株式 ２ 優先株式 ５、６ 新株予約権証券 ９
		
		if (stockCode.length() == 5) {
			char char5 = stockCode.charAt(4);
			return (char5 == '5' || char5 == '6');
		} else {
			logger.error("Unexpected stockCode");
			logger.error("  stockCode {}!", stockCode);
			throw new UnexpectedException("Unexpected stockCode");
		}
	}
	
	public static enum StockKind {
		// STOCK
		STOCK_PRIME,
		STOCK_STANDARD,
		STOCK_GROWTH,
		FOREIGN_PRIME,
		FOREIGN_STANDARD,
		FOREIGN_GROWTH,
		//
		ETF,
		ETN,
		REIT,
		COUNTRY_FUND,
		INFRA_FUND,
		PRO_MARKET,
		CERTIFICATE,
		//
		UNKNOWN, // for new STOCK
	}

	public final String     stockCode;
	public final StockKind  stockKind;
	public final String     sector33;
	public final String     sector17;
	public final String     scale;
	public final String     name;

	public Stock(
		String     stockCode,
		StockKind  stockKind,
		String     sector33,
		String     sector17,
		String     scale,
		String     name
		) {
		this.stockCode = stockCode;
		this.stockKind = stockKind;
		this.sector33  = sector33;
		this.sector17  = sector17;
		this.scale     = scale;
		this.name      = name;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s %s %s %s %s", stockCode, stockKind, sector33, sector17, scale, name);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof Stock) {
			Stock that = (Stock)o;
			return
					this.stockCode.equals(that.stockCode) &&
					this.stockKind.equals(that.stockKind) &&
					this.sector33.equals(that.sector33) &&
					this.sector17.equals(that.sector17) &&
					this.scale.equals(that.scale) &&
					this.name.equals(that.name);
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(Stock that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	public boolean isETF() {
		return stockKind == StockKind.ETF;
	}
	public boolean isETN() {
		return stockKind == StockKind.ETN;
	}
	public boolean isREIT() {
		return stockKind == StockKind.REIT;
	}
	public boolean isInfraFund() {
		return stockKind == StockKind.INFRA_FUND;
	}
	public boolean isCertificate() {
		return stockKind == StockKind.CERTIFICATE;
	}
}

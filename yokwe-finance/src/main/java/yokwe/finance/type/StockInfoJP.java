package yokwe.finance.type;

import yokwe.util.UnexpectedException;

public final class StockInfoJP implements Comparable<StockInfoJP> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
	public static enum Kind {
		// STOCK
		DOMESTIC_PRIME,
		DOMESTIC_STANDARD,
		DOMESTIC_GROWTH,
		FOREIGN_PRIME,
		FOREIGN_STANDARD,
		FOREIGN_GROWTH,
		// ETF
		ETF,
		ETN,
		// REIT
		REIT,
		INFRA_FUND,
		// OTHER
		COUNTRY_FUND,
		PRO_MARKET,
		CERTIFICATE,
		// NEW
		NEW;
		
		public boolean isPrimeStock() {
			return this == Kind.DOMESTIC_PRIME || this == Kind.FOREIGN_PRIME;
		}
		public boolean isStandardStock() {
			return this == Kind.DOMESTIC_STANDARD || this == Kind.FOREIGN_STANDARD;
		}
		public boolean isGrowthStock() {
			return this == Kind.DOMESTIC_GROWTH || this == Kind.FOREIGN_GROWTH;
		}
		
		public boolean isForeignStock() {
			return this == Kind.FOREIGN_PRIME || this == Kind.FOREIGN_STANDARD || this == Kind.FOREIGN_GROWTH ;
		}
		public boolean isDomesticStock() {
			return this == Kind.DOMESTIC_PRIME || this == Kind.DOMESTIC_STANDARD || this == Kind.DOMESTIC_GROWTH ;
		}
		public boolean isStock() {
			return this == Kind.DOMESTIC_PRIME || this == Kind.DOMESTIC_STANDARD || this == Kind.DOMESTIC_GROWTH ||
				   this == Kind.FOREIGN_PRIME || this == Kind.FOREIGN_STANDARD || this == Kind.FOREIGN_GROWTH ;
		}
		
		public boolean isETF() {
			return this == Kind.ETF;
		}
		public boolean isETN() {
			return this == Kind.ETN;
		}
		public boolean isREIT() {
			return this == Kind.REIT;
		}
		public boolean isInfraFund() {
			return this == Kind.INFRA_FUND;
		}
		public boolean isCountryFUnd() {
			return this == Kind.COUNTRY_FUND;
		}
		public boolean isProMarket() {
			return this == Kind.PRO_MARKET;
		}
		public boolean isCertificate() {
			return this == Kind.CERTIFICATE;
		}
	}
	
	public static enum Topix {
		CORE_30,
		LARGE_70,
		MID_400,
		SMALL_1,
		SMALL_2,
		ETF_ENT,
		NEW;
		
		// Core Large Mid Small
		public boolean isCore30() {
			return this.equals(CORE_30);
		}
		public boolean isLarge70() {
			return this.equals(LARGE_70);
		}
		public boolean isMid400() {
			return this.equals(MID_400);
		}
		public boolean isSmall500() {
			return this.equals(SMALL_1);
		}
		public boolean isSmall() {
			return this.equals(SMALL_1) || this.equals(SMALL_2);
		}
		// 100 500 1000
		public boolean is100() {
			return isCore30() || isLarge70();
		}
		public boolean is500() {
			return isCore30() || isLarge70() || isMid400();
		}
		public boolean is1000() {
			return isCore30() || isLarge70() || isMid400() || isSmall500();
		}
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
	public static String toYahooSymbol(String stockCode) {
		if (stockCode.length() == 4) {
			return stockCode + ".T";
		} else if (stockCode.length() == 5) {
			if (stockCode.endsWith("0")) {
				return stockCode.substring(0, 4) + ".T";
			} else if (stockCode.endsWith("5")) {
				// 25935 伊藤園 優先株式,市場第一部（内国株）
				// 25935 => 2593P.T
				return stockCode.substring(0, 4) + "P.T";
			}
		}
		logger.error("Unexpected stockCode");
		logger.error("  stockCode {}!", stockCode);
		throw new UnexpectedException("Unexpected stockCode");
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

	
	public String stockCode;
	public String isinCode  = "";
	
	public int    tradeUnit = 0;
	public long   issued    = 0;

	public Kind   kind;
	public Topix  topix;
	public String sector33;
	public String sector17;
	
	public String name;
	
	@Override
	public String toString() {
		return String.format("{%s %s}", stockCode, name);
	}
	
	public String getKey() {
		return this.stockCode;
	}
	@Override
	public int compareTo(StockInfoJP that) {
		return this.getKey().compareTo(that.getKey());
	}
	@Override
	public int hashCode() {
		return this.getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof StockInfoJP) {
			StockInfoJP that = (StockInfoJP)o;
			return
				this.stockCode.equals(that.stockCode) &&
				this.kind.equals(that.kind) &&
				this.sector33.equals(that.sector33) &&
				this.sector17.equals(that.sector17) &&
				this.topix.equals(that.topix) &&
				this.isinCode.equals(that.isinCode) &&
				this.tradeUnit == that.tradeUnit &&
				this.issued == that.issued &&
				this.name.equals(that.name);
		} else {
			return false;
		}
	}
}

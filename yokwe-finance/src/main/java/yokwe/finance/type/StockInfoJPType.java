package yokwe.finance.type;

public final class StockInfoJPType implements Comparable<StockInfoJPType> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public enum SimpleType {
		STOCK,
		ETF,
		ETN,
		REIT,
		INFRA,
		OTHER,
		NEW,
	}
	public static enum Type {
		// STOCK
		DOMESTIC_PRIME(SimpleType.STOCK),
		DOMESTIC_STANDARD(SimpleType.STOCK),
		DOMESTIC_GROWTH(SimpleType.STOCK),
		FOREIGN_PRIME(SimpleType.STOCK),
		FOREIGN_STANDARD(SimpleType.STOCK),
		FOREIGN_GROWTH(SimpleType.STOCK),
		// ETF
		ETF(SimpleType.ETF),
		ETN(SimpleType.ETN),
		// REIT
		REIT(SimpleType.REIT),
		INFRA_FUND(SimpleType.INFRA),
		// OTHER
		COUNTRY_FUND(SimpleType.OTHER),
		PRO_MARKET(SimpleType.OTHER),
		CERTIFICATE(SimpleType.OTHER),
		// NEW
		NEW(SimpleType.NEW);
		
		public final SimpleType simpleType;
		
		private Type(SimpleType simpleType) {
			this.simpleType = simpleType;
		}
		
		public boolean isPrimeStock() {
			return this == Type.DOMESTIC_PRIME || this == Type.FOREIGN_PRIME;
		}
		public boolean isStandardStock() {
			return this == Type.DOMESTIC_STANDARD || this == Type.FOREIGN_STANDARD;
		}
		public boolean isGrowthStock() {
			return this == Type.DOMESTIC_GROWTH || this == Type.FOREIGN_GROWTH;
		}
		
		public boolean isForeignStock() {
			return this == Type.FOREIGN_PRIME || this == Type.FOREIGN_STANDARD || this == Type.FOREIGN_GROWTH ;
		}
		public boolean isDomesticStock() {
			return this == Type.DOMESTIC_PRIME || this == Type.DOMESTIC_STANDARD || this == Type.DOMESTIC_GROWTH ;
		}
		public boolean isStock() {
			return this == Type.DOMESTIC_PRIME || this == Type.DOMESTIC_STANDARD || this == Type.DOMESTIC_GROWTH ||
				   this == Type.FOREIGN_PRIME || this == Type.FOREIGN_STANDARD || this == Type.FOREIGN_GROWTH ;
		}
		
		public boolean isETF() {
			return this == Type.ETF;
		}
		public boolean isETN() {
			return this == Type.ETN;
		}
		public boolean isREIT() {
			return this == Type.REIT;
		}
		public boolean isInfraFund() {
			return this == Type.INFRA_FUND;
		}
		public boolean isCountryFund() {
			return this == Type.COUNTRY_FUND;
		}
		public boolean isProMarket() {
			return this == Type.PRO_MARKET;
		}
		public boolean isCertificate() {
			return this == Type.CERTIFICATE;
		}
	}
	
	public static enum Topix {
		CORE_30,
		LARGE_70,
		MID_400,
		SMALL_1,
		SMALL_2,
		OTHER,
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
	
	
	public String stockCode;
	public String isinCode  = "";
	
	public int    tradeUnit = 0;
	public long   issued    = 0;
	public long   marketCap = 0;

	public Type   type;
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
	public int compareTo(StockInfoJPType that) {
		return this.getKey().compareTo(that.getKey());
	}
	@Override
	public int hashCode() {
		return this.getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof StockInfoJPType) {
			StockInfoJPType that = (StockInfoJPType)o;
			return
				this.stockCode.equals(that.stockCode) &&
				this.type.equals(that.type) &&
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

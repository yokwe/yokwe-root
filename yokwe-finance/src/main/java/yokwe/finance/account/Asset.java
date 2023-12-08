package yokwe.finance.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import yokwe.util.UnexpectedException;

public class Asset implements Comparable<Asset> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public enum Company {
		SONY,
		SMBC,
		PRESTIA,
		NIKKO,
		RAKUTEN,
		SMBT,
		SUMISHIN,
		SBI,
		NOMURA,
		GMO_AOZORA,
	}
	public enum Type {
		CASH, // includes DEPOSIT and TIME DEPOSIT
		MRF,
		MMF,
		STOCK, // include ETF, ETN, REIT
		FUND,
		BOND,
	}
	public enum Currency {
		JPY,
		USD,
	}
	
	public static final String NAME_DEPOSIT      = "DEPOSIT";
	public static final String NAME_TERM_DEPOSIT = "TERM TEPOSIT";
	public static final String NAME_MRF          = "MRF";
	public static final String NAME_MMF          = "MMF";
	
	LocalDateTime dateTime;
	Company       company;
	Type          type;
	Currency      currency;
	BigDecimal    value;
	
	// stock, fund, bond
	int           quantity; // number of unit for stock
	String        code;     // stockCode for stock, isinCode for fund, and proprietary code for bond
	String        name;     // saving, time deopsit, name of mmf, stock, fund and bond
	
	public Asset(
		LocalDateTime dateTime, Company company, Type type, Currency currency, BigDecimal value,
		int quantity, String code, String name) {
		this.dateTime = dateTime;
		this.company  = company;
		this.type     = type;
		this.currency = currency;
		this.value    = value;
		this.quantity = quantity;
		this.code     = code;
		this.name     = name;
	}
	
	public static Asset depositJPY(LocalDateTime dateTime, Company company, BigDecimal value) {
		return new Asset(dateTime, company, Type.CASH, Currency.JPY, value, 0, "", NAME_DEPOSIT);
	}
	public static Asset depositUSD(LocalDateTime dateTime, Company company, BigDecimal value) {
		return new Asset(dateTime, company, Type.CASH, Currency.USD, value, 0, "", NAME_DEPOSIT);
	}
	public static Asset termDepositJPY(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value) {
		return new Asset(dateTime, company, Type.CASH, Currency.JPY, value, 0, "", NAME_TERM_DEPOSIT);
	}
	public static Asset termDepositUSD(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value) {
		return new Asset(dateTime, company, Type.CASH, Currency.USD, value, 0, "", NAME_TERM_DEPOSIT);
	}
	public static Asset mrfJPY(LocalDateTime dateTime, Company company, BigDecimal value) {
		return new Asset(dateTime, company, Type.MRF, Currency.JPY, value, 0, "", NAME_MRF);
	}
	public static Asset fundJP(LocalDateTime dateTime, Company company, BigDecimal factor, int units, String code, String name) {
		return new Asset(dateTime, company, Type.FUND, Currency.JPY, factor, units, code, name);
	}
	public static Asset mmf(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, String name) {
		return new Asset(dateTime, company, Type.MMF, currency, value, 0, "", name);
	}
	public static Asset stock(LocalDateTime dateTime, Company company, Currency currency, int quantity, String code, String name) {
		return new Asset(dateTime, company, Type.STOCK, currency, BigDecimal.ZERO, quantity, code, name);
	}
	public static Asset bond(LocalDateTime dateTime, Company company, Currency currency, int quantity, String code, String name) {
		return new Asset(dateTime, company, Type.BOND, currency, BigDecimal.ZERO, quantity, code, name);
	}
	
	@Override
	public String toString() {
		switch(type) {
		case CASH:
		case MRF:
		case MMF:
			return String.format("{%s  %s  %s  %s  %s  %s}", dateTime, company, type, currency, value.toPlainString(), name);
		case FUND:
			return String.format("{%s  %s  %s  %s  %s  %s  %s  %s}", dateTime, company, type, currency, value.toPlainString(), quantity, code, name);
		case STOCK:
		case BOND:
			return String.format("{%s  %s  %s  %s  %s  %s %s}", dateTime, company, type, currency, quantity, code, name);
		default:
			logger.error("Unexpected type");
			logger.error("  {}!", type);
			throw new UnexpectedException("Unexpected type");
		}
	}
	@Override
	public int compareTo(Asset that) {
		int ret = this.dateTime.compareTo(that.dateTime);
		if (ret == 0) ret = this.company.compareTo(that.company);
		if (ret == 0) ret = this.type.compareTo(that.type);
		if (ret == 0) ret = this.currency.compareTo(that.currency);
		return ret;
	}
}

package yokwe.finance.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import yokwe.finance.account.AssetRisk.Status;
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
	BigDecimal    value;    // current value of asset in above currency
	
	// stock and fund
	Status        status;   // safe unsafe or unknown
	
	// stock, fund and bond
	BigDecimal    units;    // number of unit of stock or fund
	String        code;     // stockCode for stock, isinCode for fund, and proprietary code for bond
	String        name;     // saving, time deopsit, name of mmf, stock, fund and bond
	
	public Asset(
		LocalDateTime dateTime, Company company, Type type, Currency currency, BigDecimal value, Status status,
		BigDecimal units, String code, String name) {
		this.dateTime = dateTime;
		this.company  = company;
		this.type     = type;
		this.currency = currency;
		this.value    = value;
		this.status   = status;
		this.units    = units;
		this.code     = code;
		this.name     = name;
	}
	
	public static Asset cash(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, String name) {
		return new Asset(dateTime, company, Type.CASH, currency, value, Status.SAFE, BigDecimal.ZERO, "", name);
	}
	public static Asset mrf(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value) {
		return new Asset(dateTime, company, Type.MRF, currency, value, Status.SAFE, BigDecimal.ZERO, "", NAME_MRF);
	}
	public static Asset fund(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, Status status, BigDecimal units, String code, String name) {
		return new Asset(dateTime, company, Type.FUND, currency, value, status, units, code, name);
	}
	public static Asset mmf(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, String name) {
		return new Asset(dateTime, company, Type.MMF, currency, value, Status.SAFE, BigDecimal.ZERO, "", name);
	}
	public static Asset stock(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, Status status, BigDecimal units, String code, String name) {
		return new Asset(dateTime, company, Type.STOCK, currency, value, status, units, code, name);
	}
	public static Asset bond(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, String code, String name) {
		return new Asset(dateTime, company, Type.BOND, currency, value, Status.SAFE, BigDecimal.ZERO, code, name);
	}
	
	@Override
	public String toString() {
		switch(type) {
		case CASH:
		case MRF:
		case MMF:
			return String.format("{%s  %s  %s  %s  %s  %s  %s}", dateTime, company, type, currency, value.toPlainString(), status, name);
		case FUND:
		case STOCK:
			return String.format("{%s  %s  %s  %s  %s  %s  %s  %s  %s}", dateTime, company, type, currency, value.toPlainString(), status, units.toPlainString(), code, name);
		case BOND:
			return String.format("{%s  %s  %s  %s  %s  %s  %s  %s}", dateTime, company, type, currency, value.toPlainString(), status, code, name);
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
		if (ret == 0) ret = this.value.compareTo(that.value);
		return ret;
	}
}

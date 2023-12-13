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
		DEPOSIT,
		DEPOSIT_TIME,
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
	
	
	LocalDateTime dateTime;
	Company       company;
	Type          type;
	Currency      currency;
	BigDecimal    value;    // current value of asset in currency
	
	// stock and fund
	Status        status;   // safe unsafe or unknown
	
	// stock, fund and bond
	String        code;     // stockCode for stock, isinCode for fund, and proprietary code for bond
	String        name;     // name of asset
	
	public Asset(
		LocalDateTime dateTime, Company company, Type type, Currency currency, BigDecimal value, Status status,
		String code, String name) {
		this.dateTime = dateTime;
		this.company  = company;
		this.type     = type;
		this.currency = currency;
		this.value    = value;
		this.status   = status;
		this.code     = code;
		this.name     = name;
	}
	
	// name
	public static Asset deposit(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, String name) {
		return new Asset(dateTime, company, Type.DEPOSIT, currency, value, Status.SAFE, "", name);
	}
	public static Asset depositTime(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, String name) {
		return new Asset(dateTime, company, Type.DEPOSIT_TIME, currency, value, Status.SAFE, "", name);
	}
	public static Asset mrf(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, String name) {
		return new Asset(dateTime, company, Type.MRF, currency, value, Status.SAFE, "", name);
	}
	public static Asset mmf(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, String name) {
		return new Asset(dateTime, company, Type.MMF, currency, value, Status.SAFE, "", name);
	}
	// code name
	public static Asset fund(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, Status status, String code, String name) {
		return new Asset(dateTime, company, Type.FUND, currency, value, status, code, name);
	}
	public static Asset stock(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, Status status, String code, String name) {
		return new Asset(dateTime, company, Type.STOCK, currency, value, status, code, name);
	}
	public static Asset bond(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, String code, String name) {
		return new Asset(dateTime, company, Type.BOND, currency, value, Status.SAFE, code, name);
	}
	
	@Override
	public String toString() {
		switch(type) {
		case DEPOSIT:
		case DEPOSIT_TIME:
		case MRF:
		case MMF:
			return String.format("{%s  %s  %s  %s  %s  %s  %s}", dateTime, company, type, currency, value.toPlainString(), status, name);
		case FUND:
		case STOCK:
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

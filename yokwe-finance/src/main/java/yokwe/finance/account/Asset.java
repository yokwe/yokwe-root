package yokwe.finance.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import yokwe.finance.type.Currency;
import yokwe.util.UnexpectedException;

public class Asset implements Comparable<Asset> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public enum Company {
		SONY   ("ソニー銀行"),
		SMBC   ("三井住友銀行"),
		PRESTIA("プレスティア"),
		SMTB   ("SMTB"),
		RAKUTEN("楽天証券"),
		NIKKO  ("日興証券");
		
		public final String description;
		private Company(String nweValue) {
			this.description = nweValue;
		}
	}
	
	public enum Product {
		DEPOSIT      ("預金"),      // includes saving, MRF, MMF
		TERM_DEPOSIT ("定期預金"),
		STOCK        ("株式"),      // includes ETF, ETN, REIT
		FUND         ("投資信託"),
		BOND         ("債権");
		
		public final String description;
		private Product(String nweValue) {
			this.description = nweValue;
		}
	}
	
	public final LocalDate  date;
	public final Company    company;
	public final Product    product;
	public final Currency   currency;
	public final int        units;     // units of stock or fund
	public final BigDecimal unitPrice; // price of stock or fund
	public final BigDecimal value;     // value in currency
	public final BigDecimal cost;      // value - cost = profit
	public final String     code;      // stockCode for stock, isinCode for fund, and proprietary code for bond
	public final String     name;      // name of asset
	
	public Asset(
		LocalDate date, Company company, Product product, Currency currency,
		int units, BigDecimal unitPrice, BigDecimal value, BigDecimal cost, String code, String name) {
		this.date         = date;
		this.company      = company;
		this.product      = product;
		this.currency     = currency;
		this.units        = units;
		this.unitPrice    = unitPrice;
		this.value        = value;
		this.cost         = cost;
		this.code         = code;
		this.name         = name;
	}
	
	public Asset(
		LocalDateTime dateTime, Company company, Product product, Currency currency,
		int units, BigDecimal unitPrice, BigDecimal value, BigDecimal cost, String code, String name) {
		this(dateTime.toLocalDate(), company, product, currency, units, unitPrice, value, cost, code, name);
	}
	
	public Asset(Asset that, LocalDate date) {
		this(date, that.company, that.product, that.currency, that.units, that.unitPrice, that.value, that.cost, that.code, that.name);
	}
	public Asset(Asset that, BigDecimal unitPrice, BigDecimal value) {
		this(that.date, that.company, that.product, that.currency, that.units, unitPrice, value, that.cost, that.code, that.name);
	}
	
	// name
	public static Asset deposit(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, String name) {
		return new Asset(dateTime, company, Product.DEPOSIT, currency, 0, BigDecimal.ZERO, value, value, "", name);
	}
	public static Asset termDeposit(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, String name) {
		return new Asset(dateTime, company, Product.TERM_DEPOSIT, currency, 0, BigDecimal.ZERO, value, value, "", name);
	}
	// code name
	public static Asset fund(LocalDateTime dateTime, Company company, Currency currency, int units, BigDecimal unitPrice, BigDecimal value, BigDecimal cost, String code, String name) {
		return new Asset(dateTime, company, Product.FUND, currency, units, unitPrice, value, cost, code, name);
	}
	public static Asset stock(LocalDateTime dateTime, Company company, Currency currency, int units, BigDecimal unitPrice, BigDecimal value, BigDecimal cost, String code, String name) {
		return new Asset(dateTime, company, Product.STOCK, currency, units, unitPrice, value, cost, code, name);
	}
	public static Asset bond(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, BigDecimal cost, String code, String name) {
		return new Asset(dateTime, company, Product.BOND, currency, 0, BigDecimal.ZERO, value, cost, code, name);
	}
		
	@Override
	public String toString() {
		switch(product) {
		case DEPOSIT:
		case TERM_DEPOSIT:
			return String.format("{%s  %s  %s  %s  %s  %d %s  %s}", date, company, product, currency, value.toPlainString(), units, cost.toPlainString(), name);
		case FUND:
		case STOCK:
			return String.format("{%s  %s  %s  %s  %d  %s  %s  %s  %s  %s}", date, company, product, currency, units, unitPrice.toPlainString(), value.toPlainString(), cost.toPlainString(), code, name);
		case BOND:
			return String.format("{%s  %s  %s  %s  %s  %d %s  %s  %s}", date, company, product, currency, value.toPlainString(), units, cost.toPlainString(), code, name);
		default:
			logger.error("Unexpected type");
			logger.error("  {}!", product);
			throw new UnexpectedException("Unexpected type");
		}
	}
	@Override
	public int compareTo(Asset that) {
		int ret = this.date.compareTo(that.date);
		if (ret == 0) ret = this.company.compareTo(that.company);
		if (ret == 0) ret = this.product.compareTo(that.product);
		if (ret == 0) ret = this.currency.compareTo(that.currency);
		if (ret == 0) ret = this.code.compareTo(that.code);
		if (ret == 0) ret = this.name.compareTo(that.name);
		return ret;
	}
}

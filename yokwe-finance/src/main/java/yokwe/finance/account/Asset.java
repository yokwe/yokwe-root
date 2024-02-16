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
		NIKKO  ("日興証券"),
		SBI    ("SBI証券");
		
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
	public final BigDecimal value;    // value in currency
	
	// stock and fund
	public Risk       assetRisk;      // safe unsafe or unknown
	public Risk       currencyRisk;   // safe unsafe or unknown
	public BigDecimal cost;           // value - cost = profit
	
	// stock, fund and bond
	public String     code;     // stockCode for stock, isinCode for fund, and proprietary code for bond
	public String     name;     // name of asset
	
	public Asset(
		LocalDate date, Company company, Product product,
		Currency currency, BigDecimal value,
		Risk assetRisk, Risk currencyRisk, BigDecimal cost, String code, String name) {
		this.date         = date;
		this.company      = company;
		this.product      = product;
		this.currency     = currency;
		this.value        = value;
		this.assetRisk    = assetRisk;
		this.currencyRisk = currencyRisk;
		this.cost         = cost;
		this.code         = code;
		this.name         = name;
	}
	
	public Asset(
		LocalDateTime dateTime, Company company, Product product, Currency currency, BigDecimal value,
		Risk assetRisk, Risk currencyRisk, BigDecimal cost, String code, String name) {
		this.date         = dateTime.toLocalDate();
		this.company      = company;
		this.product      = product;
		this.currency     = currency;
		this.value        = value;
		this.assetRisk    = assetRisk;
		this.currencyRisk = currencyRisk;
		this.cost         = cost;
		this.code         = code;
		this.name         = name;
	}
	
	// name
	public static Asset deposit(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, String name) {
		return new Asset(dateTime, company, Product.DEPOSIT, currency, value, Risk.SAFE, Risk.SAFE, value, "", name);
	}
	public static Asset termDeposit(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, String name) {
		return new Asset(dateTime, company, Product.TERM_DEPOSIT, currency, value, Risk.SAFE, Risk.SAFE, value, "", name);
	}
	// code name
	public static Asset fund(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, AssetRisk.Entry entry, BigDecimal cost, String code, String name) {
		return new Asset(dateTime, company, Product.FUND, currency, value, entry.assetRisk, entry.currencyRisk, cost, code, name);
	}
	public static Asset stock(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, AssetRisk.Entry entry, BigDecimal cost, String code, String name) {
		return new Asset(dateTime, company, Product.STOCK, currency, value, entry.assetRisk, entry.currencyRisk, cost, code, name);
	}
	public static Asset bond(LocalDateTime dateTime, Company company, Currency currency, BigDecimal value, BigDecimal cost, String code, String name) {
		return new Asset(dateTime, company, Product.BOND, currency, value, Risk.SAFE, Risk.SAFE, cost, code, name);
	}
	
	@Override
	public String toString() {
		switch(product) {
		case DEPOSIT:
		case TERM_DEPOSIT:
			return String.format("{%s  %s  %s  %s  %s  %s  %s  %s  %s}", date, company, product, currency, value.toPlainString(), assetRisk, currencyRisk, cost, name);
		case FUND:
		case STOCK:
		case BOND:
			return String.format("{%s  %s  %s  %s  %s  %s  %s  %s  %s  %s}", date, company, product, currency, value.toPlainString(), assetRisk, currencyRisk, cost, code, name);
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

package yokwe.finance.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Asset implements Comparable<Asset> {
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
		CASH, // includes MRF and TIME DEPOSIT
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
	BigDecimal    value;
	
	// stock, fund, bond
	int           quantity; // number of unit for stock
	String        code;     // stockCode for stock, isinCode for fund, and proprietary code for bond
	String        name;     // saving, time deopsit, name of mmf, stock, fund and bond
	
	
	@Override
	public int compareTo(Asset that) {
		int ret = this.dateTime.compareTo(that.dateTime);
		if (ret == 0) ret = this.company.compareTo(that.company);
		if (ret == 0) ret = this.type.compareTo(that.type);
		if (ret == 0) ret = this.currency.compareTo(that.currency);
		return ret;
	}
}

package yokwe.finance.trade;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.ToString;

public class AccountHistory implements Comparable<AccountHistory>	{
	// show transaction of account
	
	public enum Currency {
		JPY,
		USD,
		;
	}
	
	public enum Asset {
		CASH,
		STOCK,
		FUND,
		BOND,
		;
	}
	public enum Transaction {
		DEPOSIT,
		WITHDRAW,
		DIVIDEND,
		FEE,
		TAX,
		//
		BUY,
		SELL,
		IMPORT,
//		SPLIT,
		;
	}
	
	public LocalDate    settlementDate; // 受渡日
	public LocalDate    tradeDate;      // 約定日
	public Currency     currency;
	public Asset        asset;
	public Transaction  transaction;
	public BigDecimal   units;
	public BigDecimal   unitPrice;
	public BigDecimal   amount;         // plus for sell, dividend and deposit.  minus for withdraw, fee
	public String       code;           // code of stock or fund. blank for CASH, BOND
	public String       comment;        // name of stock, fund, bond or blank
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	@Override
	public int compareTo(AccountHistory that) {
		int ret = this.settlementDate.compareTo(that.settlementDate);
		if (ret == 0) ret = this.tradeDate.compareTo(that.tradeDate);
		if (ret == 0) ret = this.currency.compareTo(that.currency);
		if (ret == 0) ret = this.asset.compareTo(that.asset);
		if (ret == 0) ret = this.transaction.compareTo(that.transaction);
		if (ret == 0) ret = this.code.compareTo(that.code);
		if (ret == 0) ret = this.comment.compareTo(that.comment);
		if (ret == 0) ret = this.units.compareTo(that.units);
		if (ret == 0) ret = this.unitPrice.compareTo(that.unitPrice);
		if (ret == 0) ret = this.amount.compareTo(that.amount);
		return ret;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof AccountHistory) {
			AccountHistory that = (AccountHistory)o;
			if (!this.settlementDate.equals(that.settlementDate)) return false;
			if (!this.tradeDate.equals(that.tradeDate)) return false;
			if (!this.currency.equals(that.currency)) return false;
			if (!this.asset.equals(that.asset)) return false;
			if (!this.transaction.equals(that.transaction)) return false;
			if (!this.units.equals(that.units)) return false;
			if (!this.unitPrice.equals(that.unitPrice)) return false;
			if (!this.amount.equals(that.amount)) return false;
			if (!this.code.equals(that.code)) return false;
			if (!this.comment.equals(that.comment)) return false;
			return true;
		} else {
			return false;
		}
	}
}

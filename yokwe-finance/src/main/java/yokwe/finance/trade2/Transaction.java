package yokwe.finance.trade2;

import java.time.LocalDate;

import yokwe.util.ToString;

public class Transaction implements Comparable<Transaction> {
	public enum Currency {
		JPY,
		USD,
		;
	}
	
	public enum Type {
		DEPOSIT,
		WITHDRAW,
		//
		WITHDRAW_TRANSFER,
		DEPOSIT_TRANSFER,
		//
		DIVIDEND,
		//
		TAX,
		TAX_REFUND,
		//
		BUY,
		SELL,
		//
		BALANCE,
		;
	}

	public enum Asset {
		CASH,
		//
		STOCK_JP,
		FUND_JP,
		//
		STOCK_US,
		BOND_US,
		MMF_US,
		;
	}

	public LocalDate settlementDate; // 受渡日
	public LocalDate tradeDate;      // 約定日
	public Currency  currency;
	public Type      type;
	public Asset     asset;
	public int       units;
	public int       amount;         // in USD units is cent
	public String    code;           // code of stock or fund. blank for CASH, BOND
	public String    comment;        // name of stock, fund, bond or description of transaction
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	@Override
	public int compareTo(Transaction that) {
		int ret = this.settlementDate.compareTo(that.settlementDate);
		if (ret == 0) ret = this.tradeDate.compareTo(that.tradeDate);
		if (ret == 0) ret = this.type.compareTo(that.type);
		if (ret == 0) ret = this.asset.compareTo(that.asset);
		if (ret == 0) ret = this.currency.compareTo(that.currency);
		if (ret == 0) ret = this.code.compareTo(that.code);
		if (ret == 0) ret = this.comment.compareTo(that.comment);
		if (ret == 0) ret = Integer.compare(this.units, that.units);
		if (ret == 0) ret = Integer.compare(this.amount, that.amount);
		return ret;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Transaction) {
			var that = (Transaction)o;
			return
				this.settlementDate.equals(that.settlementDate) &&
				this.tradeDate.equals(that.tradeDate) &&
				this.currency.equals(that.currency) &&
				this.type.equals(that.type) &&
				this.asset.equals(that.asset) &&
				this.units == that.units &&
				this.amount == that.amount &&
				this.code.equals(that.code) &&
				this.comment.equals(that.comment);
		} else {
			return false;
		}
	}
}

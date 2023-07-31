package yokwe.util.yahoo.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class Dividend implements Comparable<Dividend> {
	public LocalDate  date;
	public BigDecimal amount;
	
	public Dividend(
		LocalDate  date,
		BigDecimal amount) {
		this.date   = date;
		this.amount = amount;
	}
	
	@Override
	public String toString() {
		return String.format(
			"{%s  %s}",
			date,
			amount.toPlainString());
	}

	@Override
	public int compareTo(Dividend that) {
		return this.date.compareTo(that.date);
	}

}

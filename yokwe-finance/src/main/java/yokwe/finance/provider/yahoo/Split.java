package yokwe.finance.provider.yahoo;

import java.time.LocalDate;

public class Split implements Comparable<Split>{
	public LocalDate  date;
	public String     detail;
	
	public Split(
		LocalDate date,
		String    detail) {
		this.date   = date;
		this.detail = detail;
	}
	
	@Override
	public String toString() {
		return String.format(
			"{%s  %s}",
			date,
			detail);
	}

	@Override
	public int compareTo(Split that) {
		return this.date.compareTo(that.date);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Split) {
			Split that = (Split)o;
			return
				that.date.equals(that.date) &&
				that.detail.equals(that.detail);
		} else {
			return false;
		}
	}
}

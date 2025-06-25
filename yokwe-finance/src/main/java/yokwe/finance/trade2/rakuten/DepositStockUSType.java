package yokwe.finance.trade2.rakuten;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.ToString;

public class DepositStockUSType implements Comparable<DepositStockUSType> {
	public final LocalDate  date;
	public final String     code;
	public final int        units;
	public final BigDecimal costUSD;
	public final int        costJPY;
	
	public DepositStockUSType(LocalDate date, String code, int units, BigDecimal costUSD, int costJPY) {
		this.date    = date;
		this.code    = code;
		this.units   = units;
		this.costUSD = costUSD;
		this.costJPY = costJPY;
	}
	
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	@Override
	public int compareTo(DepositStockUSType o) {
		// TODO Auto-generated method stub
		return 0;
	}
}

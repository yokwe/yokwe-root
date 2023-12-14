package yokwe.finance.type;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public final class FXRate implements Comparable<FXRate> {
	public final LocalDate  date;
	public final BigDecimal usd;
	public final BigDecimal eur;
	public final BigDecimal gbp;
	public final BigDecimal aud;
	public final BigDecimal nzd;
	
	public FXRate(LocalDate date, BigDecimal usd, BigDecimal eur, BigDecimal gbp, BigDecimal aud, BigDecimal nzd) {
		this.date = date;
		this.usd  = usd;
		this.eur  = eur;
		this.gbp  = gbp;
		this.aud  = aud;
		this.nzd  = nzd;
	}
	
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public BigDecimal rate(Currency currency) {
		switch(currency) {
		case USD: return usd;
		case EUR: return eur;
		case GBP: return gbp;
		case AUD: return aud;
		case NZD: return nzd;
		default:
			logger.error("Unexpected currency");
			logger.error("  currency {}!", currency);
			throw new UnexpectedException("Unexpected currency");
		}
	}
	
	@Override
	public int compareTo(FXRate that) {
		return this.date.compareTo(that.date);
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}

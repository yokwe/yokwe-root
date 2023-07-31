package yokwe.util.yahoo.finance;

public final class Symbol implements Comparable<Symbol> {
	public String symbol;
	public String type;
	public String exchange;
	public String exchDisp;
	public String name;
	
	public Symbol(
		String symbol,
		String type,
		String exchange,
		String exchDisp,
		String name
		) {
		this.symbol   = symbol;
		this.type     = type;
		this.exchange = exchange;
		this.exchDisp = exchDisp;
		this.name     = name;
	}
	
	@Override
	public String toString() {
		return String.format("{%s  %s  %s  \"%s\"  \"%s\"}", symbol, type, exchange, exchDisp, name);
	}
	
	@Override
	public int compareTo(Symbol that) {
		return this.symbol.compareTo(that.symbol);
	}
}

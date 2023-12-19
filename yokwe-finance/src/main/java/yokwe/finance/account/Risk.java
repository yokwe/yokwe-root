package yokwe.finance.account;

public enum Risk {
	UNKNOWN,
	SAFE,
	UNSAFE;
	
	public boolean isSafe() {
		return this.equals(SAFE);
	}
}
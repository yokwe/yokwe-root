package yokwe.finance.provider.nasdaq.api;

import yokwe.util.StringUtil;

public class Status {
	public static class CodeMessage {
		public int    code         = 0;
		public String errorMessage = "";
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public int           rCode;
	public CodeMessage[] bCodeMessage;
	public String        developerMessage;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}

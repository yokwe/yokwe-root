package yokwe.stock.us.nasdaq.api;

import yokwe.util.StringUtil;

public class Status {
	public static class CodeMessage {
		int    code;
		String errorMessage;
		
		public CodeMessage() {
			code         = 0;
			errorMessage = "";
		}
		
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
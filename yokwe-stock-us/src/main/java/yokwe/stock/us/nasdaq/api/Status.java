package yokwe.stock.us.nasdaq.api;

import yokwe.util.ToString;

public class Status {
	public static class CodeMessage {
		public int    code;
		public String errorMessage;
		
		public CodeMessage() {
			code         = 0;
			errorMessage = "";
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	public int           rCode;
	public CodeMessage[] bCodeMessage;
	public String        developerMessage;
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}

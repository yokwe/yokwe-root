package yokwe.finance.account;

import java.io.File;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.json.JSON;

public class Secret {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static class Type3 {
		public String branch;
		public String account;
		public String password;
		
		public Type3() {
			branch = account = password = "";
		}
	}
	public static class Type2 {
		public String account;
		public String password;
		
		public Type2() {
			account = password = "";
		}
	}
	
	public static final String SECRET_PATH = "tmp/secret.json";
	
	public static Secret read(String path) {
		var file = new File(path);
		if (!file.canRead()) {
			logger.error("Cannot read secret file");
			logger.error("  path  {}", path);
			throw new UnexpectedException("Cannot read secret file");
		}
		
		String string = FileUtil.read().file(file);
		return JSON.unmarshal(Secret.class, string);
	}
	
	public static Secret read() {
		return read(SECRET_PATH);
	}
	
	public static void write(String path, Secret secret) {
		var file = new File(path);
		String string = JSON.toJSONString(secret);
		FileUtil.write().file(file, string);
	}
	
	public Type3 nikko;
	public Type2 prestia;
	public Type2 rakuten;
	public Type2 sbi;
	public Type3 smbc;
	public Type2 smtb;
	public Type2 sony;
	
	public Secret() {
	}
}

package yokwe.finance.account;

import java.io.File;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.json.JSON;

public class Secret {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static class Nikko {
		public String branch;
		public String account;
		public String password;
		
		public Nikko( ) {
			branch = account = password = "";
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
	
	public Nikko nikko;
	
	public Secret() {
		nikko = new Nikko();
	}
	
	public static void main(String[] args) {
		Secret secret = new Secret();
		secret.nikko.branch   = "";
		secret.nikko.account  = "";
		secret.nikko.password = "";
		
		//write(SECRET_PATH, secret);
	}
}

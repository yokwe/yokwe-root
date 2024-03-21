package yokwe.finance.util.webbrowser;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

import org.openqa.selenium.BuildInfo;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.manager.SeleniumManager;

import yokwe.util.UnexpectedException;
import yokwe.util.json.JSON;

public class Manager {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String VERSION_BETA = "0";
	
	public static class Log {
		public String level;
		public long   timestamp;
		public String message;
		
		@Override
		public String toString() {
			LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone.getDefault().toZoneId());					
			return String.format("{%s  %s  %s}", level, localDateTime, message);
		}
	}
	
	public static class Result {
		@JSON.Name("code")                      public int    code;
	    @JSON.Name("message")      @JSON.Ignore public String message;
	    @JSON.Name("driver_path")               public String driverPath;
	    @JSON.Name("browser_path")              public String browserPath;
	    
	    @Override
	    public String toString() {
	    	return String.format("{%d  %s  %s}", code, driverPath, browserPath);
	    }
	}
	
	public static class Data {			
		@JSON.Name("logs")    @JSON.Ignore public Log[]  logs;
		@JSON.Name("result")               public Result result;
	}
	
	private static String getCachePath() {
		var env  = System.getenv("SE_CACHE_PATH");
		var home = System.getProperty("user.home");
		
		final String path = (env != null) ? env : String.format("%s/.cache/selenium", home);
		// sanity check
		{
			var file = new File(path);
			if (!file.isDirectory()) {
				logger.error("path is not directory");
				logger.error("  path  {}", path);
				logger.error("  env   {}", env);
				logger.error("  home  {}", home);
				throw new UnexpectedException("path is not directory");
			}
		}
		return path;
	}
	
	private static String getVersion() {
		var label = new BuildInfo().getReleaseLabel();
		int pos   = label.lastIndexOf('.');
		return String.format("%s.%s", VERSION_BETA, label.substring(0, pos));
	}
	
	public static String geSeleniumManagerPath() {
		// ~/.cache/selenium/manager/0.4.15/selenium-manager
		var cachePath = getCachePath();
		var version   = getVersion();
		var path      = String.format("%s/manager/%s/selenium-manager", cachePath, version);
		// sanity check
		{
			var file = new File(path);
			if (!file.isFile()) {
				logger.error("path is not file");
				logger.error("  path  {}", path);
				throw new UnexpectedException("path is not file");
			}
		}
		return path;
	}
	
	public static Manager.Result getResult(Capabilities capabilities) {
		try {
			var m = SeleniumManager.getInstance();
			m.getDriverPath(capabilities, false);
		} catch (Exception e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.warn("SeleniumManager.getInstance() throws exception {}", exceptionName);
		}
		
		try {
			var out = new StringWriter();
			{
				String[] args    = {geSeleniumManagerPath(), "--browser", capabilities.getBrowserName(), "--output", "JSON"};
				Process  process = Runtime.getRuntime().exec(args);
				try (var stdin = process.inputReader()) {
					stdin.transferTo(out);
				}
			}
			var data = JSON.unmarshal(Manager.Data.class, out.toString());
			if (data.result.code != 0) {
				logger.error("Unexpected");
				logger.error("  browser  {}", capabilities);
				logger.error("  out      {}", out);
				logger.error("  result   {}", data.result);
				throw new UnexpectedException("Unexpected");
			}
			return data.result;
		} catch (IOException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
}
package yokwe.finance.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.TimeZone;

import org.openqa.selenium.Alert;
import org.openqa.selenium.BuildInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.manager.SeleniumManager;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariDriverService;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.bridge.SLF4JBridgeHandler;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.json.JSON;

public class WebBrowser implements Closeable{
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// redirect java.util.logging to slf4j
	static {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}
	
	public static class Manager {
		private static final String VERSION_BETA = "0";
		
		public static class Log {
			public String level;
			public long   timestamp;
			public String message;
			
			@Override
			public String toString() {
				LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone.getDefault().toZoneId());					
				return String.format("{}", level, localDateTime, message);
			}
		}
		public static class Result {
			@JSON.Name("code")         public int    code;
		    @JSON.Name("message")      public String message;
		    @JSON.Name("driver_path")  public String driverPath;
		    @JSON.Name("browser_path") public String browserPath;
		    
		    @Override
		    public String toString() {
		    	return String.format("{%d  %s  %s  %s}", code, message, driverPath, browserPath);
		    }
		}
		public static class Data {			
			@JSON.Ignore public Log[]  logs;
			             public Result result;
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
			int pos = label.lastIndexOf('.');
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
		
		public static Result getResult(Capabilities capabilities) {
			try {
				var m = SeleniumManager.getInstance();
				m.getDriverPath(capabilities, false);
			} catch (Exception e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.warn("excetion {}", exceptionName);
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
				var data = JSON.unmarshal(Data.class, out.toString());
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
	
	
	private static final int      DEFAULT_WINDOW_Y        = 0;
	private static final int      DEFAULT_WINDOW_WIDTH    = 1000;
	
	public static WebDriver getWebDriverSafari(boolean logging) {
		var options = new SafariOptions();
		var result  = Manager.getResult(options);
		
		var service = new SafariDriverService.Builder().withLogging(logging).build();		
		// NOTE need to invoke setExecutable.
		service.setExecutable(result.driverPath);
		
		return new SafariDriver(service, options);
	}
	
	public static WebDriver getWebDriverChrome() {
		var options = new ChromeOptions();
		
		var result = Manager.getResult(options);
		
		var service = new ChromeDriverService.Builder().build();
		service.setExecutable(result.driverPath);
		
		return new ChromeDriver(service, options);
	}
	
	public static WebDriver getWebDriver() {
		// FIXME after update to macos 14.1.1, safari webdriver don't start
//		return getWebDriverSafari(false);
		return getWebDriverChrome(); 
	}
	
	protected final WebDriver driver;

	public WebBrowser() {
		this(getWebDriver());
	}
	public WebBrowser(WebDriver driver) {
		this.driver = driver;
				
		// change position and size
		setPosition(new Point(getPosition().x, DEFAULT_WINDOW_Y));
		setSize(new Dimension(DEFAULT_WINDOW_WIDTH, getSize().height));
	}
	
	public JavascriptExecutor getJavascriptExecutor() {
		return (JavascriptExecutor)driver;
	}
	
	
	//
	// close
	//
	@Override
	public void close() {
		// use driver.quit() instead of driver.close()
		driver.quit();
	}
	
	
	//
	// window position and size
	//
	public Point getPosition() {
		return driver.manage().window().getPosition();
	}
	public void setPosition(Point point) {
		driver.manage().window().setPosition(point);
	}
	public Dimension getSize() {
		return driver.manage().window().getSize();
	}
	public void setSize(Dimension size) {
		driver.manage().window().setSize(size);
	}
	
	
	//
	// page
	//
	public String getPage() {
		return driver.getPageSource();
	}
	public void savePage(String path) {
		FileUtil.write().file(path, getPage());
	}
	
	
	//
	// sleep
	//
	public static void sleep(Duration duration) {
		sleep(duration.toMillis());
	}
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			//
		}
	}
	//
	// sleepRandom
	//
	private final Long   seed = Long.valueOf(0);
	private final Random random = new Random(seed.hashCode() ^ System.currentTimeMillis());
	public void sleepRandom(long mills, double randomWeight) {
		double factor = 1.0 + (random.nextDouble() - 0.5) * randomWeight;
//		double factor = 1.0 + random.nextDouble() * randomWeight;
		sleep((long)(mills * factor));
	}
	private static final long   DEFAULT_SLEEP_MILLS         = 3000;
	private static final double DEFAULT_SLEEP_RANDOM_WEIGHT = 0.3;
	public void sleepRandom(long mills) {
		sleepRandom(mills, DEFAULT_SLEEP_RANDOM_WEIGHT);
	}
	public void sleepRandom() {
		sleepRandom(DEFAULT_SLEEP_MILLS);
	}
	
	
	//
	// get
	//
	public void get(String url) {
		// NOTE driver.get is synchronous
		driver.get(url);
	}
	//
	// click
	//
	public void click(By locator) {
		wait.untilPresenceOfElement(locator).click();
	}
	public void clickAndWait(By locator) {
		String page = getPage();
		click(locator);
		wait.untilPageUpdate(page);
	}
	//
	// javaScript
	//
	public Object javaScript(String script) {
		// NOTE javascriptExecutor.executeScript is synchronous
		return getJavascriptExecutor().executeScript(script);
	}
	public <E> E javaScript(Class<E> clazz, String script) {
        Object result = javaScript(script);
        
        if (clazz.isInstance(result)) {
        	@SuppressWarnings("unchecked")
			E ret = (E)result;
        	return ret;
        } else {
			logger.error("Unexpected result class");
			logger.error("  clazz  {}", clazz.getName());
			logger.error("  result {}", result.getClass().getName());
			throw new UnexpectedException("no JavascriptExecutor");
        }
	}
	
	
	
	//
	// WindowInfo
	//
	public static class WindowInfo {
		public static class Entry {
			public final String handle;
			public final String title;
			public final String url;
			
			public Entry(WebDriver driver) {
				handle = driver.getWindowHandle();
				title  = driver.getTitle();
				url    = driver.getCurrentUrl();
			}
			
			public boolean titleContains(String string) {
				return title.contains(string);
			}
			public boolean urlContains(String string) {
				return url.contains(string);
			}
			
			@Override
			public String toString() {
				return String.format("{%s  %s  %s}", handle, title, url);
			}
		}

		private final Entry[]   array;
		
		public WindowInfo(WebDriver driver) {
			var list = new ArrayList<Entry>();
			{
				// save current window
				String save = driver.getWindowHandle();

				for(var e: driver.getWindowHandles()) {
					driver.switchTo().window(e);
					list.add(new Entry(driver));
				}
				// restore current window
				driver.switchTo().window(save);
			}
			array = list.stream().toArray(Entry[]::new);
		}
		
		public boolean titleContaisn(String string) {
			for(var e: array) {
				if (e.titleContains(string)) return true;
			}
			return false;
		}
		public boolean urlContaisn(String string) {
			for(var e: array) {
				if (e.urlContains(string)) return true;
			}
			return false;
		}
		public String getHandleByTitleContains(String string) {
			for(var e: array) {
				if (e.titleContains(string)) {
					return e.handle;
				}
			}
			return null;
		}
		public String getHandleByURLContains(String string) {
			for(var e: array) {
				if (e.urlContains(string)) {
					return e.handle;
				}
			}
			return null;
		}
	}
	
	//
	// switchToByTitleContains
	//
	private static ExpectedCondition<String> getHandleByTitleContains(String string_) {
		return new ExpectedCondition<String>() {
			private String string = string_;
			
			@Override
			public String apply(WebDriver driver) {
				WindowInfo windowInfo = new WindowInfo(driver);
				return windowInfo.getHandleByTitleContains(string);
			}

			@Override
			public String toString() {
				return "wait page title contains " + string;
			}
		};
	}
	public void switchToByTitleContains(String string, Duration timeout) {
		var handle = wait.untilExpectedCondition(getHandleByTitleContains(string), timeout);
		driver.switchTo().window(handle);
	}
	public void switchToByTitleContains(String string) {
		switchToByTitleContains(string, Wait.DEFAULT_WAIT_TIMEOUT);
	}

	
	//
	// wait
	//
	public final Wait wait = new Wait();
	public final class Wait {
		//
		// untilCondition
		//
		private static final Duration DEFAULT_WAIT_TIMEOUT = Duration.ofSeconds(5);
		private static final Duration DEFAULT_WAIT_SLEEP   = Duration.ofMillis(500);
		public <E> E untilExpectedCondition(ExpectedCondition<E> isTrue, Duration timeout) {
			return new WebDriverWait(driver, timeout, DEFAULT_WAIT_SLEEP).until(isTrue);
		}
		//
		// untilPresenceOfElement
		//
		public WebElement untilPresenceOfElement(By locator, Duration timeout) {
			return untilExpectedCondition(ExpectedConditions.presenceOfElementLocated(locator), timeout);
		}
		public WebElement untilPresenceOfElement(By locator) {
			return untilPresenceOfElement(locator, DEFAULT_WAIT_TIMEOUT);
		}
		//
		// untilTitleContains
		//
		public Boolean untilTitleContains(String string, Duration timeout) {
			return untilExpectedCondition(ExpectedConditions.titleContains(string), timeout);
		}
		public Boolean untilTitleContains(String string) {
			return untilTitleContains(string, DEFAULT_WAIT_TIMEOUT);
		}
		//
		// untilPageUpdate
		//
		private static ExpectedCondition<Boolean> pageUpdate(String page) {
			return new ExpectedCondition<Boolean>() {
				private String oldPage = page;
				
				@Override
				public Boolean apply(WebDriver driver) {
					return !oldPage.equals(driver.getPageSource());
				}

				@Override
				public String toString() {
					return "wait page transtin using oldPage";
				}
			};
		}
		public Boolean untilPageUpdate(String page, Duration timeout) {
			return untilExpectedCondition(pageUpdate(page), timeout);
		}
		public Boolean untilPageUpdate(String page) {
			return untilPageUpdate(page, DEFAULT_WAIT_TIMEOUT);
		}
		//
		// untilPresenseOfWindow
		//
		public static ExpectedCondition<Boolean> presenceOfWindow(String string_) {
			return new ExpectedCondition<Boolean>() {
				private String string = string_;
				
				@Override
				public Boolean apply(WebDriver driver) {
					WindowInfo windowInfo = new WindowInfo(driver);
					return windowInfo.titleContaisn(string);
				}

				@Override
				public String toString() {
					return "wait page title contains " + string;
				}
			};
		}
		public Boolean untilPresenceOfWindow(String string, Duration timeout) {
			return untilExpectedCondition(presenceOfWindow(string), timeout);
		}
		public Boolean untilPresenceOfWindow(String string) {
			return untilPresenceOfWindow(string, DEFAULT_WAIT_TIMEOUT);
		}
		//
		// untilAlertIsPresent
		//
		public Alert untilAlertIsPresent(Duration timeout) {
			return untilExpectedCondition(ExpectedConditions.alertIsPresent(), timeout);
		}
		public Alert untilAlertIsPresent() {
			return untilAlertIsPresent(DEFAULT_WAIT_TIMEOUT);
		}
	}
	
}

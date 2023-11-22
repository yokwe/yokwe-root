package yokwe.finance.util.webbrowser;

import java.io.Closeable;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Random;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariDriverService;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.bridge.SLF4JBridgeHandler;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class WebBrowser implements Closeable{
	static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// redirect java.util.logging to slf4j
	static {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}
	
	private static final int      DEFAULT_WINDOW_Y        = 0;
	private static final int      DEFAULT_WINDOW_WIDTH    = 1000;
	
	// FIXME after update to macos 14.1.1, safari webdriver don't start
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
		// NOTE need to invoke setExecutable.
		service.setExecutable(result.driverPath);
		
		return new ChromeDriver(service, options);
	}
	
	public static WebDriver getWebDriver() {
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
	public void savePage(File file) {
		var string = getPage();
		// string is already UTF-8, so remove charset from content-type
		string = string.replace(
			"<meta http-equiv=\"content-type\" content=\"text/html; charset=Shift_JIS\">",
			"<meta http-equiv=\"content-type\" content=\"text/html\">");
		string = string.replace(
			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=Shift_JIS\">",
			"<meta http-equiv=\"Content-Type\" content=\"text/html\">");
		FileUtil.write().file(file, string);
	}
	
	
	//
	// findElement
	//
	public List<WebElement> findElements(By locator) {
		return driver.findElements(locator);
	}
	public WebElement findElement(By locator) {
		return driver.findElement(locator);
	}
	
	
	//
	// sleep
	//
	public void sleep(Duration duration) {
		sleep(duration.toMillis());
	}
	public void sleep(long millis) {
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
//		double factor = 1.0 + (random.nextDouble() - 0.5) * randomWeight;
		double factor = 1.0 + random.nextDouble() * randomWeight;
		sleep((long)(mills * factor));
	}
	static final long   DEFAULT_SLEEP_MILLS         = 500;
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
	protected void get(String url) {
		// NOTE driver.get is synchronous
		driver.get(url);
	}
	public void get(File file) {
		if (file.canRead()) {
			// FIXME remove 
			get("file://" + file.getAbsolutePath());
		} else {
			logger.error("cannot read file");
			logger.error("  file  {}", file.getAbsolutePath());
			throw new UnexpectedException("cannot read file");
		}
	}
	//
	// javaScript
	//
	protected Object javaScript(String script) {
		// NOTE javascriptExecutor.executeScript is synchronous
		return getJavascriptExecutor().executeScript(script);
	}
	protected <E> E javaScript(Class<E> clazz, String script) {
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
	// sendKey
	//
	public void sendKey(By locator, String string, long sleep) {
		wait.untilPresenceOfElement(locator).sendKeys(string);
		sleepRandom(sleep);
	}
	public void sendKey(By locator, String string) {
		sendKey(locator, string, DEFAULT_SLEEP_MILLS);
	}
	
	
	//
	// switchToByTitleContains
	//
	private static ExpectedCondition<String> getWindoHandleTitleContains(String string_) {
		return new ExpectedCondition<String>() {
			private String string = string_;
			
			@Override
			public String apply(WebDriver driver) {
				WindowInfo windowInfo = new WindowInfo(driver);
				return windowInfo.getWindowHandleTitleContains(string);
			}

			@Override
			public String toString() {
				return "wait page title contains " + string;
			}
		};
	}
	public void switchToWindoTitleContains(String string, Duration timeout) {
		var handle = wait.untilExpectedCondition(getWindoHandleTitleContains(string), timeout);
		driver.switchTo().window(handle);
	}
	public void switchToByTitleContains(String string) {
		switchToWindoTitleContains(string, Wait.DEFAULT_WAIT_TIMEOUT);
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
					return !driver.getPageSource().equals(oldPage);
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
		// untilPageContains
		//
		private static ExpectedCondition<Boolean> pageContains(String string_) {
			return new ExpectedCondition<Boolean>() {
				private String string = string_;
				
				@Override
				public Boolean apply(WebDriver driver) {
					return driver.getPageSource().contains(string);
				}

				@Override
				public String toString() {
					return "wait page contains " + string;
				}
			};
		}
		public Boolean untilPageContains(String page, Duration timeout) {
			return untilExpectedCondition(pageContains(page), timeout);
		}
		public Boolean untilPageContains(String page) {
			return untilPageContains(page, DEFAULT_WAIT_TIMEOUT);
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
					return windowInfo.titleContains(string);
				}

				@Override
				public String toString() {
					return "wait window that page title contains " + string;
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

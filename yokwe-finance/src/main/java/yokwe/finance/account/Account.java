package yokwe.finance.account;

import java.io.Closeable;
import java.io.File;
import java.time.Duration;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariDriverService;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public abstract class Account implements Closeable {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static WebDriver getWebDriverSafari(boolean logging) {		
		var service = new SafariDriverService.Builder().withLogging(false).build();
		File driverExecutable = service.getDriverExecutable();
		// sanity check
		{
			if (!driverExecutable.canExecute()) {
				logger.error("No driver file");
				logger.error("  path  {}", driverExecutable.getPath());
				throw new UnexpectedException("No driver file");
			}
		}

		// NOTE need to invoke setExecutable.
		service.setExecutable(driverExecutable.getPath());
		return new SafariDriver(service);
	}
	public static WebDriver getWebDriverSafari() {
		return getWebDriverSafari(false);
	}
	
	public static WebDriver getWebDriver() {
		return getWebDriverSafari();
	}
	
	public WebDriver driver;
	
	public Account(WebDriver driver) {
		this.driver  = driver;
	}
	public Account() {
		this(getWebDriverSafari());
	}
	
	public abstract void login();
	public abstract void logout();
	
	@Override
	public void close() {
		// use driver.quit() instead of driver.close()
		driver.quit();
	}
	
	
	//
	// get
	//
	public void get(String url) {
		driver.get(url);
		sleepShort();
	}
	
	//
	// click
	//
	public void click(By locator) {
		waitPresence(driver, locator).click();
		sleepShort();
	}
	
	
	//
	// getPageSource
	//
	public String getPageSource() {
		return driver.getPageSource();
	}
	
	
	//
	// savePageSource
	//
	public void savePageSource(String path) {
		FileUtil.write().file(path, getPageSource());
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
	// sleepShort and sleepLong
	//
	private final Long   seed = Long.valueOf(0);
	private final Random random = new Random(seed.hashCode() ^ System.currentTimeMillis());
	public void sleepShort() {
		long milli = (long)(random.nextDouble() * 3000 + 2000);
		sleep(milli);
	}
	public void sleepLong() {
		long milli = (long)(random.nextDouble() * 5000 + 5000);
		sleep(milli);
	}
	
	
	//
	// waitPresence
	//
	private static final Duration DEFAULT_WAIT_TIMEOUT = Duration.ofSeconds(5);
	private static final Duration DEFAULT_WAIT_SLEEP   = Duration.ofMillis(500);
	public WebElement waitPresence(By locator, Duration timeout, Duration sleep) {
		return new WebDriverWait(driver, timeout, sleep).until(ExpectedConditions.presenceOfElementLocated(locator));
	}
	public WebElement waitPresence(WebDriver driver, By locator, Duration timeout) {
		return waitPresence(locator, timeout, DEFAULT_WAIT_SLEEP);
	}
	public WebElement waitPresence(WebDriver driver, By locator) {
		return waitPresence(locator, DEFAULT_WAIT_TIMEOUT, DEFAULT_WAIT_SLEEP);
	}
	//
	// waitTitleContains
	//
	public boolean waitTitleContains(String title, Duration timeout, Duration sleep) {
		return new WebDriverWait(driver, timeout, sleep).until(ExpectedConditions.titleContains(title));
	}
	public boolean waitTitleContains(String title, Duration timeout) {
		return waitTitleContains(title, timeout, DEFAULT_WAIT_SLEEP);
	}
	public boolean waitTitleContains(String title) {
		return waitTitleContains(title, DEFAULT_WAIT_TIMEOUT, DEFAULT_WAIT_SLEEP);
	}

}

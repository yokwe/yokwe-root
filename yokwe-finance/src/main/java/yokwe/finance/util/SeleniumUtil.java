package yokwe.finance.util;

import java.io.File;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariDriverService;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import yokwe.util.UnexpectedException;

public class SeleniumUtil {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String PATH_SAFARI_DRIVER = "/usr/bin/safaridriver";

	public static WebDriver getWebDriverSafari(boolean logging) {
		// sanity check
		{
			var file = new File(PATH_SAFARI_DRIVER);
			if (!file.canExecute()) {
				logger.error("No safari driver file");
				logger.error("  path  {}", PATH_SAFARI_DRIVER);
				throw new UnexpectedException("No safari driver file");
			}
		}
		
		var service = new SafariDriverService.Builder().withLogging(false).build();
		// NOTE need to invoke setExecutable.
		service.setExecutable(PATH_SAFARI_DRIVER);
		return new SafariDriver(service);
	}
	public static WebDriver getWebDriverSafari() {
		return getWebDriverSafari(false);
	}
	
	public static WebDriver getWebDriver() {
		return getWebDriverSafari();
	}
	
	private static final Duration SLEEP_BEFORE_CLOSE = Duration.ofMillis(1000);
	public static void closeDriver(WebDriver driver) {
		sleep(SLEEP_BEFORE_CLOSE);
		driver.close();
	}
	
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
	
	public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
	public static final Duration DEFAULT_SLEEP   = Duration.ofMillis(500);
	
	public static WebElement waitUntilPresence(WebDriver driver, By locator, Duration timeout, Duration sleep) {
		return new WebDriverWait(driver, timeout, sleep).until(ExpectedConditions.presenceOfElementLocated(locator));
	}
	public static WebElement waitUntilPresence(WebDriver driver, By locator, Duration timeout) {
		return waitUntilPresence(driver, locator, timeout, DEFAULT_SLEEP);
	}
	public static WebElement waitUntilPresence(WebDriver driver, By locator) {
		return waitUntilPresence(driver, locator, DEFAULT_TIMEOUT, DEFAULT_SLEEP);
	}
	
	//
	// waitUntilTitleIs
	//
	public static boolean waitUntilTitleIs(WebDriver driver, String title, Duration timeout, Duration sleep) {
		return new WebDriverWait(driver, timeout, sleep).until(ExpectedConditions.titleIs(title));
	}
	public static boolean waitUntilTitleIs(WebDriver driver, String title, Duration timeout) {
		return waitUntilTitleIs(driver, title, timeout, DEFAULT_SLEEP);
	}
	public static boolean waitUntilTitleIs(WebDriver driver, String title) {
		return waitUntilTitleIs(driver, title, DEFAULT_TIMEOUT, DEFAULT_SLEEP);
	}
	
	//
	// waitUntilTitleContains
	//
	public static boolean waitUntilTitleContains(WebDriver driver, String title, Duration timeout, Duration sleep) {
		return new WebDriverWait(driver, timeout, sleep).until(ExpectedConditions.titleContains(title));
	}
	public static boolean waitUntilTitleContains(WebDriver driver, String title, Duration timeout) {
		return waitUntilTitleContains(driver, title, timeout, DEFAULT_SLEEP);
	}
	public static boolean waitUntilTitleContains(WebDriver driver, String title) {
		return waitUntilTitleContains(driver, title, DEFAULT_TIMEOUT, DEFAULT_SLEEP);
	}
	
	//
	//
	//
	
	
	
}

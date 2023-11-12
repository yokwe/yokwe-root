package yokwe.finance.util;

import java.io.Closeable;
import java.io.File;
import java.time.Duration;
import java.util.Random;
import java.util.function.Consumer;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariDriverService;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class WebBrowser implements Closeable {
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
	
	public static WebDriver getWebDriver() {
		return getWebDriverSafari(false);
	}
	
	protected final WebDriver          driver;
	protected final JavascriptExecutor javascriptExecutor;
	
	public WebBrowser(WebDriver driver) {
		this.driver             = driver;
		this.javascriptExecutor = (JavascriptExecutor)driver;
	}
	public WebBrowser() {
		this(getWebDriver());
	}
	
	
	public void driverInfo() {
		logger.info("diriver");
		logger.info("  class                {}", driver.getClass().getTypeName());

		var timeout = driver.manage().timeouts();
		logger.info("timeout");
		logger.info("  implicitWaitTimeout  {}", timeout.getImplicitWaitTimeout());
		logger.info("  pageLoadTimeout      {}", timeout.getPageLoadTimeout());
		logger.info("  scriptTimeout        {}", timeout.getScriptTimeout());
		
		var window  = driver.manage().window();
		logger.info("window");
		logger.info("  position             {}", window.getPosition());
		logger.info("  size                 {}", window.getSize());
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
	// consumeAndWait
	//
	protected <E> void acceptAndWait(Consumer<E> consumer, E arg) {
		var oldPage = getPage();
		consumer.accept(arg);
		waitPageTransition(oldPage);
	}
	protected <E> void acceptAndWait(Consumer<E> consumer, E arg, String title) {
		acceptAndWait(consumer, arg);
		waitUntilTitleContains(title);
	}
	
	//
	// get
	//
	public void get(String url) {
		driver.get(url);
		sleepShort();
	}
	public void getAndWait(String url) {
		acceptAndWait(o -> get(o), url);
	}
	public void getAndWait(String url, String title) {
		acceptAndWait(o -> get(o), url, title);
	}
	
	
	//
	// click
	//
	public void click(By locator) {
		waitUntilPresence(locator).click();
		sleepShort();
	}
	public void clickAndWait(By locator) {
		acceptAndWait(o -> click(o), locator);
	}
	public void clickAndWait(By locator, String title) {
		acceptAndWait(o -> click(o), locator, title);
	}
	
	
	//
	// javaScript
	//
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
	public Object javaScript(String script) {
		var ret = javascriptExecutor.executeScript(script);
		sleepShort();
		return ret;
	}
	public void javaScriptAndWait(String script) {
		acceptAndWait(o -> javaScript(o), script);
	}
	public void javaScriptAndWait(String script, String title) {
		acceptAndWait(o -> javaScript(o), script, title);
	}

	
	//
	// window
	//
	public void setPosition(int x, int y) {
		setPosition(new Point(x, y));
	}
	public void setPosition(Point point) {
		driver.manage().window().setPosition(point);
	}
	public Point getPosition() {
		return driver.manage().window().getPosition();
	}
	public void setSize(int width, int height) {
		setSize(new Dimension(width, height));
	}
	public void setSize(Dimension size) {
		driver.manage().window().setSize(size);
	}
	public Dimension getSize() {
		return driver.manage().window().getSize();
	}

	
	
	//
	// getPage
	//
	public String getPage() {
		return driver.getPageSource();
	}
	//
	// savePage
	//
	public void savePage(String path) {
		FileUtil.write().file(path, getPage());
	}
		
	
	//
	// waitUntilPresence
	//
	private static final Duration DEFAULT_WAIT_TIMEOUT = Duration.ofSeconds(5);
	private static final Duration DEFAULT_WAIT_SLEEP   = Duration.ofMillis(500);
	protected WebElement waitUntilPresence(By locator, Duration timeout, Duration sleep) {
		return new WebDriverWait(driver, timeout, sleep).until(ExpectedConditions.presenceOfElementLocated(locator));
	}
	protected WebElement waitUntilPresence(By locator, Duration timeout) {
		return waitUntilPresence(locator, timeout, DEFAULT_WAIT_SLEEP);
	}
	protected WebElement waitUntilPresence(By locator) {
		return waitUntilPresence(locator, DEFAULT_WAIT_TIMEOUT, DEFAULT_WAIT_SLEEP);
	}
	//
	// waitUntilTitleContains
	// 
	protected boolean waitUntilTitleContains(String title, Duration timeout, Duration sleep) {
		return new WebDriverWait(driver, timeout, sleep).until(ExpectedConditions.titleContains(title));
	}
	protected boolean waitUntilTitleContains(String title, Duration timeout) {
		return waitUntilTitleContains(title, timeout, DEFAULT_WAIT_SLEEP);
	}
	protected boolean waitUntilTitleContains(String title) {
		return waitUntilTitleContains(title, DEFAULT_WAIT_TIMEOUT, DEFAULT_WAIT_SLEEP);
	}
	//
	// waitUntilPageTransition
	//
	private static ExpectedCondition<Boolean> pageTransition(String page) {
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
	protected boolean waitPageTransition(String oldPage, Duration timeout, Duration sleep) {
		return new WebDriverWait(driver, timeout, sleep).until(pageTransition(oldPage));
	}
	protected boolean waitPageTransition(String oldPage, Duration timeout) {
		return waitPageTransition(oldPage, timeout, DEFAULT_WAIT_SLEEP);
	}
	public boolean waitPageTransition(String oldPage) {
		return waitPageTransition(oldPage, DEFAULT_WAIT_TIMEOUT, DEFAULT_WAIT_SLEEP);
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

}

package yokwe.util.selenium;

import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class WebDriverWrapper<T extends WebDriver & Interactive & JavascriptExecutor > implements WebDriver, Interactive, JavascriptExecutor {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
	
	private final T driver;
	
	public WebDriverWrapper(T driver) {
		this.driver = driver;
	}
	
	
	//
	// utility methods
	//
	public void sleep(Duration duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			//
		}
	}
	//
	public void savePage(File file) {
		FileUtil.write().file(file, driver.getPageSource());
	}
	//
	public void getAndWait(String url) {
		get(url);
		wait.pageTransition();
	}
	//
	public void click(By locator) {
		wait.untilPresenceOfElement(locator).click();
	}
	public void clickAndWait(By locator) {
		click(locator);
		wait.pageTransition();
	}
	//
	public void sendKey(By locator, String string) {
		wait.untilPresenceOfElement(locator).sendKeys(string);
	}
	public void sendKeyAndWait(By locator, String string) {
		sendKey(locator, string);
		wait.pageTransition();
	}
	//
	public void moveToElement(By locator) {
		new Actions(driver).moveToElement(wait.untilPresenceOfElement(locator)).perform();
	}
	public void moveToElementAndWait(By locator) {
		moveToElement(locator);
		wait.pageTransition();
	}
	//
	public void submit(By locator) {
		wait.untilPresenceOfElement(locator).submit();
	}
	public void submitAndWait(By locator) {
		submit(locator);
		wait.pageTransition();
	}
	//
	public Select select(By locator) {
		return new Select(wait.untilPresenceOfElement(locator));
	}
	
	//
	// switchToWindow
	//
	public final SwitchToWindow switchToWindow = new SwitchToWindow();
	public final class SwitchToWindow {
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
					return "get window title contains " + string;
				}
			};
		}
		public void titleContains(String string, Duration timeout) {
			if (driver.getTitle().contains(string)) return;  // no need to switch window
			var handle = wait.untilExpectedCondition(getWindoHandleTitleContains(string), timeout);
			driver.switchTo().window(handle);
		}
		public void titleContains(String string) {
			titleContains(string, DEFAULT_TIMEOUT);
		}
	}
	
	
	//
	// wait
	//
	public final Wait wait = new Wait();
	public final class Wait {
		private static final Duration DEFAULT_WAIT_SLEEP  = Duration.ofMillis(500);
		private static final int      DEFAULT_EQUAL_COUNT = 2;
		//
		// untilCondition
		//
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
			return untilPresenceOfElement(locator, DEFAULT_TIMEOUT);
		}
		//
		// untilClickable
		//
		public WebElement untilClickable(By locator, Duration timeout) {
			return untilExpectedCondition(ExpectedConditions.elementToBeClickable(locator), timeout);
		}
		public WebElement untilClickable(By locator) {
			return untilPresenceOfElement(locator, DEFAULT_TIMEOUT);
		}
		//
		// untilTitleContains
		//
		public Boolean untilTitleContains(String string, Duration timeout) {
			return untilExpectedCondition(ExpectedConditions.titleContains(string), timeout);
		}
		public Boolean untilTitleContains(String string) {
			return untilTitleContains(string, DEFAULT_TIMEOUT);
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
			return untilPageUpdate(page, DEFAULT_TIMEOUT);
		}
		//
		// pageTransition
		//
		private static ExpectedCondition<Boolean> pageTransition(String page, int equalCount) {
			return new ExpectedCondition<Boolean>() {
				private int oldPage = page.length();
				private int count   = 0;
				@Override
				public Boolean apply(WebDriver driver) {
					var newPage = driver.getPageSource().length();
					if (newPage == oldPage) {
						count++;
//						logger.info("equalCount  {}", equalCount);
					} else {
//						logger.info("XX  {}  {}", oldPage.length(), newPage.length());
						oldPage = newPage;
						count = 0;
//						logger.info("equalCount  {}", equalCount);
					}
					return count == equalCount;
				}

				@Override
				public String toString() {
					return "wait finish page transitin using page length";
				}
			};
		}
		public Boolean pageTransition(int equalCount, Duration timeout) {
			return untilExpectedCondition(pageTransition(driver.getPageSource(), equalCount), timeout);
		}
		public Boolean pageTransition(int equalCount) {
			return pageTransition(equalCount, DEFAULT_TIMEOUT);
		}
		public Boolean pageTransition() {
			return pageTransition(DEFAULT_EQUAL_COUNT);
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
			return untilPageContains(page, DEFAULT_TIMEOUT);
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
			return untilPresenceOfWindow(string, DEFAULT_TIMEOUT);
		}
		//
		// untilAlertIsPresent
		//
		public Alert untilAlertIsPresent(Duration timeout) {
			return untilExpectedCondition(ExpectedConditions.alertIsPresent(), timeout);
		}
		public Alert untilAlertIsPresent() {
			return untilAlertIsPresent(DEFAULT_TIMEOUT);
		}
		//
		// untilDownloadFinish
		//
		public static ExpectedCondition<Boolean> downloadFinish(File file_) {
			return new ExpectedCondition<Boolean>() {
				private File file   = file_;
				private long length = -1;
				private int  count  = 0;
				
				@Override
				public Boolean apply(WebDriver driver) {
					if (file.exists()) {
						var newLength = file.length();
						if (length == newLength) {
							count++;
						} else {
							count = 0;
							length = newLength;
						}
						return 3 <= count;
					} else {
						return false;
					}
				}

				@Override
				public String toString() {
					return "wait download finish " + file.getPath();
				}
			};
		}
		public Boolean untilDownloadFinish(File file, Duration timeout) {
			return untilExpectedCondition(downloadFinish(file), timeout);
		}
		public Boolean untilDownloadFinish(File file) {
			return untilDownloadFinish(file, DEFAULT_TIMEOUT);
		}
	}
	
	
	//
	// check
	//
	public final Check check = new Check();
	public final class Check {
		public void titleContains(String expect) {
			var actual = driver.getTitle();
			if (actual.contains(expect)) return;
			logger.error("Unexpected window title");
			logger.error("  expect  {}!", expect);
			logger.error("  actual  {}!", actual);
			throw new UnexpectedException("Unexpected window title");
		}
		public void pageContains(String expect) {
			var actual = driver.getPageSource();
			if (actual.contains(expect)) return;
			logger.error("Unexpected page source");
			logger.error("  expect  {}!", expect);
			logger.error("  actual  {}!", actual);
			throw new UnexpectedException("Unexpected page source");
		}
	}
	
	
	//
	// export to WebDriver
	//
	@Override
	public void get(String url) {
		driver.get(url);
	}
	@Override
	public String getCurrentUrl() {
		return driver.getCurrentUrl();
	}
	@Override
	public String getTitle() {
		return driver.getTitle();
	}
	@Override
	public List<WebElement> findElements(By by) {
		return driver.findElements(by);
	}
	@Override
	public WebElement findElement(By by) {
		return driver.findElement(by);
	}
	@Override
	public String getPageSource() {
		return driver.getPageSource();
	}
	@Override
	public void close() {
		driver.close();
	}
	@Override
	public void quit() {
		driver.quit();
	}
	@Override
	public Set<String> getWindowHandles() {
		return driver.getWindowHandles();
	}
	@Override
	public String getWindowHandle() {
		return driver.getWindowHandle();
	}
	@Override
	public TargetLocator switchTo() {
		return driver.switchTo();
	}
	@Override
	public Navigation navigate() {
		return driver.navigate();
	}
	@Override
	public Options manage() {
		return driver.manage();
	}
	//
	// export to Interactive
	//
	@Override
	public void perform(Collection<Sequence> actions) {
		driver.perform(actions);
	}
	@Override
	public void resetInputState() {
		driver.resetInputState();
	}
	//
	// export to JavascriptExecutor
	//
	@Override
	public Object executeScript(String script, Object... args) {
		return driver.executeScript(script, args);
	}
	@Override
	public Object executeAsyncScript(String script, Object... args) {
		return driver.executeAsyncScript(script, args);
	}
}

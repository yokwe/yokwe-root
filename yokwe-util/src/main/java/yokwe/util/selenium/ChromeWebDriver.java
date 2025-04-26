package yokwe.util.selenium;

import java.io.Closeable;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.bridge.SLF4JBridgeHandler;

import yokwe.util.UnexpectedException;

public class ChromeWebDriver implements Closeable, WebDriver, JavascriptExecutor {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// redirect java.util.logging to slf4j
	static {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}
	
	// path of chrome for testing
	private static final File BROWSER_FILE  = StorageSelenium.chromeForTesting.getFile("chrome-mac-arm64/Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing");
	private static final File DRIVER_FILE   = StorageSelenium.chromeForTesting.getFile("chromedriver-mac-arm64/chromedriver");
	private static final File USER_DATA_DIR = StorageSelenium.chromeForTesting.getFile("user-data-dir");
	
//	private static final File BROWSER_FILE  = new File("tmp/chrome-for-testing/chrome-mac-arm64/Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing");
//	private static final File DRIVER_FILE   = new File("tmp/chrome-for-testing/chromedriver-mac-arm64/chromedriver");
//	private static final File USER_DATA_DIR = new File("tmp/chrome-for-testing/user-data-dir");
	static {
		// sanity check
		boolean hasError = false;
		if (!BROWSER_FILE.exists()) {
			logger.error("browser file does not exist");
			logger.error("  {}", BROWSER_FILE.getAbsolutePath());
			hasError = true;
		}
		if (!DRIVER_FILE.exists()) {
			logger.error("driver file does not exist");
			logger.error("  {}", DRIVER_FILE.getAbsolutePath());
			hasError = true;
		}
		if (!USER_DATA_DIR.isDirectory()) {
			logger.error("user data dir does not exist");
			logger.error("  {}", USER_DATA_DIR.getAbsolutePath());
			hasError = true;
		}
		if (hasError) {
			throw new UnexpectedException("unpexpected");
		}
	}

	public final ChromeDriver        driver;
	public final ChromeDriverService service;
	public final ChromeOptions       options;
	public final File                browserFile;
	public final File                driverFile;
	public final File                userDataDir;
	public final File                downloadDir;
	
	private ChromeWebDriver(Builder buider) {
		this.driver      = buider.driver;
		this.service     = buider.service;
		this.options     = buider.options;
		this.browserFile = buider.browserFile;
		this.driverFile  = buider.driverFile;
		this.userDataDir = buider.userDataDir;
		this.downloadDir = buider.downloadDir;
	}
	
	public static class Builder {
		private ChromeDriver        driver;
		private ChromeDriverService service;
		private ChromeOptions       options;
		private File                browserFile;
		private File                driverFile;
		private File                userDataDir;
		private File                downloadDir;
		
		private Map<String, Object>	prefsMap;
		private boolean				enableDownload;
		
		private Builder() {
			driver         = null;
			service        = new ChromeDriverService.Builder().build();
			options        = new ChromeOptions();
			browserFile    = BROWSER_FILE;
			driverFile     = DRIVER_FILE;
			userDataDir    = USER_DATA_DIR;
			downloadDir    = new File(".");
			prefsMap       = new TreeMap<>();
			enableDownload = true;
		}
		
		public Builder withBrowserFile(File file) {
			// sanity check
			if (!file.isFile()) {
				logger.error("no file");
				logger.error("  dir  {}!", file.getAbsolutePath());
				throw new UnexpectedException("no file");
			}
			
			browserFile = file;
			return this;
		}
		public Builder withDriverFile(File file) {
			// sanity check
			if (!file.isFile()) {
				logger.error("no file");
				logger.error("  dir  {}!", file.getAbsolutePath());
				throw new UnexpectedException("no file");
			}
			
			driverFile = file;
			return this;
		}
		public Builder withUserDataDir(File dir) {
			// sanity check
			if (!dir.isDirectory()) {
				logger.error("no directory");
				logger.error("  dir  {}!", dir.getAbsolutePath());
				throw new UnexpectedException("no directory");
			}
			
			userDataDir = dir;
			return this;
		}
		public Builder withDownloadDir(File dir) {
			// sanity check
			if (!dir.isDirectory()) {
				logger.error("no directory");
				logger.error("  dir  {}!", dir.getAbsolutePath());
				throw new UnexpectedException("no directory");
			}
			
			downloadDir = dir;
			return this;
		}
		public Builder withArguments(String... args) {
			options.addArguments(args);
			return this;
		}
		public Builder withPrefs(String name, Object value) {
			prefsMap.put(name, value);
			return this;
		}
		public Builder withEnableDownload(boolean newValue) {
			enableDownload = newValue;
			return this;
		}
		
		public ChromeWebDriver build() {
			// build options
			{
				prefsMap.put("profile.default_content_settings.popups", 0);
				prefsMap.put("download.default_directory",              downloadDir.getAbsolutePath());
				prefsMap.put("plugins.always_open_pdf_externally",      1);

				options.setExperimentalOption("prefs", prefsMap);
				options.setBinary(browserFile.getAbsolutePath());
				options.addArguments("--user-data-dir=" + userDataDir.getAbsolutePath());
				options.setEnableDownloads(enableDownload);
			}
			// build service
			{
				service.setExecutable(driverFile.getAbsolutePath());
			}
			
			// build driver
			driver = new ChromeDriver(service, options);
			
			return new ChromeWebDriver(this);
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	
	//
	// WebDriver
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
	// JavascriptExecutor
	//
	@Override
	public Object executeScript(String script, Object... args) {
		return driver.executeScript(script, args);
	}

	@Override
	public Object executeAsyncScript(String script, Object... args) {
		return driver.executeAsyncScript(script, args);
	}
	
	
	//
	// utility methods
	//
	void sleep(Duration duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			//
		}
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
		// untilClickable
		//
		public WebElement untilClickable(By locator, Duration timeout) {
			return untilExpectedCondition(ExpectedConditions.elementToBeClickable(locator), timeout);
		}
		public WebElement untilClickable(By locator) {
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
		// untilPageTransitionFinish
		//
		private static ExpectedCondition<Boolean> pageTransitionFinish(String page, int equalCount) {
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
		public Boolean untilPageTansitionFinish(int equalCount, Duration timeout) {
			return untilExpectedCondition(pageTransitionFinish(driver.getPageSource(), equalCount), timeout);
		}
		public Boolean untilPageTansitionFinish(int equalCount) {
			return untilPageTansitionFinish(equalCount, DEFAULT_WAIT_TIMEOUT);
		}
		public Boolean untilPageTansitionFinish() {
			return untilPageTansitionFinish(2);
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
			return untilDownloadFinish(file, DEFAULT_WAIT_TIMEOUT);
		}
	}
	
	
	//
	// WindowInfo
	//
	public static class WindowInfo {
		private static class Entry {
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

		private final Entry[] array;
		
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
		
		public boolean titleContains(String string) {
			for(var e: array) {
				if (e.titleContains(string)) return true;
			}
			return false;
		}
		public boolean urlContains(String string) {
			for(var e: array) {
				if (e.urlContains(string)) return true;
			}
			return false;
		}
		public String getWindowHandleTitleContains(String string) {
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
}

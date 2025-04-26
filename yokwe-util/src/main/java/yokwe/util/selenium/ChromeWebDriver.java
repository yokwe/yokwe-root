package yokwe.util.selenium;

import java.io.Closeable;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.bridge.SLF4JBridgeHandler;

import yokwe.util.UnexpectedException;

public class ChromeWebDriver implements Closeable, WebDriver {
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
	
	
	public static void main(String[] args) throws InterruptedException {
		logger.info("START");
		
		var downloadDir = new File("tmp/download");
		
		var builder = ChromeWebDriver.builder();
		builder.withDownloadDir(downloadDir);
		builder.withEnableDownload(true);
		
//		builder.withArguments("--headless");
		
		// download dialog
		builder.withPrefs("profile.default_content_settings.popups", 0);
		builder.withPrefs("download.default_directory",              downloadDir.getAbsolutePath());
		builder.withPrefs("plugins.always_open_pdf_externally",      1);

		try (var browser = builder.build()) {
			browser.driver.get("chrome://version");
			Thread.sleep(Duration.ofSeconds(10));
		}
		
		logger.info("STOP");
	}

}

package yokwe.finance.account.smbc;

import java.io.File;
import java.nio.charset.Charset;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;

import yokwe.finance.Storage;
import yokwe.finance.account.Secret;
import yokwe.finance.account.UpdateAsset;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.selenium.ChromeDriverBuilder;
import yokwe.util.selenium.WebDriverWrapper;

public class UpdateAssetSMBC implements UpdateAsset {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.smbc;
	
	private static final File FILE_LOGIN     = storage.getFile("login.html");
	private static final File FILE_TOP       = storage.getFile("top.html");
	private static final File FILE_BALANCE   = storage.getFile("balance.html");
	
	private static final File DIR_DOWNLOAD   = storage.getFile("download");
	private static final File FILE_CSV       = storage.getFile("meisai.csv");
	
	private static final Charset CHARSET_CSV = Charset.forName("Shift_JIS");

	@Override
	public Storage getStorage() {
		return storage;
	}
	
	@Override
	public void download() {
		logger.info("download");
		
		// delete file in download
		for(var e: DIR_DOWNLOAD.listFiles()) e.delete();
		
		var builder = ChromeDriverBuilder.builder();
//		builder.withArguments("--headless");
		builder.withDownloadDir(DIR_DOWNLOAD);
		var driver = new WebDriverWrapper<ChromeDriver>(builder.build());
		try {
			// login
			{
				logger.info("login");
				driver.getAndWait("https://direct.smbc.co.jp/aib/aibgsjsw5001.jsp");
				driver.savePage(FILE_LOGIN);
				// sanity check
				driver.check.titleContains("SMBCダイレクトログイン");
				
				var secret = Secret.read().smbc;
				driver.sendKey(By.name("branchNo"),  secret.branch);
				driver.sendKey(By.name("accountNo"), secret.account);
				driver.sendKey(By.name("password"),  secret.password);
				
				driver.executeScriptAndWait("directib.LLDLDIL.login()");
				driver.savePage(FILE_TOP);
				
				if (driver.getTitle().contains("追加の本人確認")) {
					logger.info("sleep 20 seconds");
					driver.sleep(Duration.ofSeconds(20));
				}
				
				driver.check.titleContains("トップ");
			}
			
			//balance
			{
				logger.info("balance");
				driver.clickAndWait(By.xpath("//div[@class='top-column01']/ul/li/a"));
				// sanity check
				driver.check.titleContains("明細照会");
				
				// さらに3ヵ月表示
				logger.info("3 more month");
				driver.clickAndWait(By.xpath("//button[@class='btn-type02 js-moreBtn']"));
				// さらに3ヵ月表示
				logger.info("3 more month");
				driver.clickAndWait(By.xpath("//button[@class='btn-type02 js-moreBtn']"));
				// さらに3ヵ月表示
				logger.info("3 more month");
				driver.clickAndWait(By.xpath("//button[@class='btn-type02 js-moreBtn']"));
				// さらに3ヵ月表示
				logger.info("3 more month");
				driver.clickAndWait(By.xpath("//button[@class='btn-type02 js-moreBtn']"));
				//
				driver.savePage(FILE_BALANCE);
				
				logger.info("download");
				driver.click(By.xpath("//li[@class='item js-csvBtnArea']/div/a"));
				
				// wait 1 second
				driver.sleep(Duration.ofSeconds(5));
				File[] files = DIR_DOWNLOAD.listFiles(o -> o.getName().endsWith(".csv"));
				if (files.length == 1) {
					var file = files[0];
					logger.info("file  {}", file.getPath());
					driver.wait.untilDownloadFinish(file);
					
					String string = FileUtil.read().withCharset(CHARSET_CSV).file(file);
					FileUtil.write().file(FILE_CSV, string);
					logger.info("save  {}  {}", string.length(), FILE_CSV.getPath());
				} else {
					logger.error("Unexpected download file");
					logger.error("  files  {}", files.length);
					for(var i = 0; i < files.length; i++) {
						logger.error("  files  {}  {}", i, files[i].getPath());
					}
					throw new UnexpectedException("Unexpected download file");
				}
				
				logger.info("back");
				driver.click(By.xpath("//div[@class='btn-back']/a"));
			}
			
			// logout
			{
				logger.info("logout");
				driver.clickAndWait(By.xpath("//div[@class='header-info']/ul/li[2]/a"));
				driver.sleep(Duration.ofSeconds(2));
			}
		} catch (WebDriverException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.warn("{} {}", exceptionName, e);
		} finally {
			driver.quit();
		}
	}
	
	@Override
	public void update() {
		// FIXME
		logger.info("update");
	}
	
	
	private static final UpdateAssetSMBC instance = new UpdateAssetSMBC();
	public static UpdateAsset getInstance() {
		return instance;
	}
	
	public static void main(String[] args) {
		logger.info("START");
				
		instance.download();
		instance.update();
		
		logger.info("STOP");
	}
}

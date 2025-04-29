package yokwe.finance.account.smbc;

import org.openqa.selenium.WebDriverException;

import yokwe.finance.Storage;
import yokwe.finance.account.UpdateAsset;
import yokwe.util.selenium.ChromeDriverBuilder;

public class UpdateAssetSMBC implements UpdateAsset {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.smbc;
	
	@Override
	public Storage getStorage() {
		return storage;
	}
	
	@Override
	public void download() {
		logger.info("download");

		var builder = ChromeDriverBuilder.builder();
//		builder.withArguments("--headless");
		var driver = builder.build();
		try {
			// login
			
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

package yokwe.finance.account;

import yokwe.finance.account.nikko.UpdateAssetNikko;
import yokwe.finance.account.prestia.UpdateAssetPrestia;
import yokwe.finance.account.rakuten.UpdateAssetRakuten;
import yokwe.finance.account.sbi.UpdateAssetSBI;
import yokwe.finance.account.smtb.UpdateAssetSMTB;
import yokwe.finance.account.sony.UpdateAssetSony;

public class AssetReport {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		UpdateAsset[] array = {
			UpdateAssetNikko.instance,	
			UpdateAssetPrestia.instance,	
			UpdateAssetRakuten.instance,	
			UpdateAssetSBI.instance,	
			UpdateAssetSMTB.instance,	
			UpdateAssetSony.instance,	
		};
		
		for(var e: array) {
			logger.info("storage {}", e.getStorage().getFile().getName());
		}
		
		for(var e: array) {
			logger.info("download {}", e.getStorage().getFile().getName());
			e.download();
		}
		
		for(var e: array) {
			logger.info("update {}", e.getStorage().getFile().getName());
			e.update();
		}
		
		logger.info("STOP");
	}
}

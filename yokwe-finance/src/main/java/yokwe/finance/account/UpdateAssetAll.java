package yokwe.finance.account;

import java.io.File;
import java.util.ArrayList;

import yokwe.finance.Storage;
import yokwe.finance.account.nikko.UpdateAssetNikko;
import yokwe.finance.account.prestia.UpdateAssetPrestia;
import yokwe.finance.account.rakuten.UpdateAssetRakuten;
import yokwe.finance.account.sbi.UpdateAssetSBI;
import yokwe.finance.account.smtb.UpdateAssetSMTB;
import yokwe.finance.account.sony.UpdateAssetSony;
import yokwe.util.ListUtil;

public final class UpdateAssetAll {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage        = Storage.account.base;
	public  static final File    FILE_ASSET_ALL = storage.getFile("asset-all.csv");

	
	public static void main(String[] args) {
		logger.info("START");
		
		UpdateAsset[] array = {
			UpdateAssetNikko.getInstance(),
			UpdateAssetPrestia.getInstance(),
			UpdateAssetRakuten.getInstance(),
			UpdateAssetSBI.getInstance(),
			UpdateAssetSMTB.getInstance(),
			UpdateAssetSony.getInstance(),
		};
		
		for(var e: array) e.download();
		for(var e: array) e.update();
		
		{
			var list = new ArrayList<Asset>();
			for(var e: array) {
				var subList = e.getList();
				logger.info("asset  {}  {}", subList.size(), e.getStorage().getFile().getName());
				list.addAll(subList);
			}
			logger.info("save  {}  {}", list.size(), FILE_ASSET_ALL.getPath());
			ListUtil.save(Asset.class, FILE_ASSET_ALL, list);
		}
		
		logger.info("STOP");
	}
}

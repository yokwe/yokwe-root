package yokwe.finance.account;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yokwe.finance.Storage;
import yokwe.finance.account.nikko.UpdateAssetNikko;
import yokwe.finance.account.prestia.UpdateAssetPrestia;
import yokwe.finance.account.rakuten.UpdateAssetRakuten;
import yokwe.finance.account.sbi.UpdateAssetSBI;
import yokwe.finance.account.smtb.UpdateAssetSMTB;
import yokwe.finance.account.sony.UpdateAssetSony;
import yokwe.util.ListUtil;
import yokwe.util.UnexpectedException;

public final class UpdateAssetAll {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.base;
	
	private static int THIS_YEAR = LocalDate.now().getYear();
	
	private static File getFile(int year) {
		var name = String.format("asset-%d.csv", year);
		return storage.getFile(name);
	}
	private static File getFile() {
		return getFile(THIS_YEAR);
	}
	public static List<Asset> getList(int year) {
		var list = ListUtil.getList(Asset.class, getFile(year));
		// sanity check
		for(var e: list) {
			if (e.date.getYear() != THIS_YEAR) {
				logger.error("Unexpected");
				logger.error("THIS_YEAR  {}", THIS_YEAR);
				logger.error("asset      {}", e);
				throw new UnexpectedException("Unexpected");
			}
		}
		return list;
	}
	public static List<Asset> getList() {
		return getList(THIS_YEAR);
	}
	public static List<Asset> getListLast() {
		var list = getList();
		if (!list.isEmpty()) {
			var last = list.get(list.size() - 1);
			list.removeIf(o -> !o.date.equals(last.date));
		}
		
		return list;
	}
	public static void save(List<Asset> list) {
		// sanity check
		for(var e: list) {
			if (e.date.getYear() != THIS_YEAR) {
				logger.error("Unexpected");
				logger.error("THIS_YEAR  {}", THIS_YEAR);
				logger.error("asset      {}", e);
				throw new UnexpectedException("Unexpected");
			}
		}
		ListUtil.save(Asset.class, getFile(), list);
	}
	
	
	private static final UpdateAsset[] array = {
		UpdateAssetNikko.getInstance(),
		UpdateAssetPrestia.getInstance(),
		UpdateAssetRakuten.getInstance(),
		UpdateAssetSBI.getInstance(),
		UpdateAssetSMTB.getInstance(),
		UpdateAssetSony.getInstance(),
	};
	
	public static void download() {
		for(var e: array) e.download();
	}
	public static void update() {
		for(var e: array) e.update();
	}
	
	public static List<Asset> getUpdate() {
		var list = new ArrayList<Asset>();
		
		for(var e: array) list.addAll(e.getList());		
		Collections.sort(list);
		
		for(var e: list) {
			logger.info("list  {}", e);
		}
		
		// sanity check
		if (list.isEmpty()) {
			throw new UnexpectedException("Unexpected");
		}
		var first = list.get(0);
		var last  = list.get(list.size() - 1);
		if (!first.date.equals(last.date)) {
			logger.error("Unexpected date");
			logger.error("first  {}", first);
			logger.error("last   {}", last);
			throw new UnexpectedException("Unexpected date");
		}
		if (last.date.getYear() != THIS_YEAR) {
			logger.error("Unexpected year");
			logger.error("THIS_YEAR  {}", THIS_YEAR);
			logger.error("last       {}", last);
			throw new UnexpectedException("Unexpected year");
		}

		return list;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		download();
		update();
		
		List<Asset> assetList = getList();
		// update list
		{
			List<Asset> updateList = getUpdate();
			var updateDate = updateList.get(0).date;
			
			// remove entry if date is same as lastDate
			assetList.removeIf(o -> o.date.equals(updateDate));
			// add update
			assetList.addAll(updateList);
		}
		
		// sanity check for assetInfo of asset
		for(var asset: assetList) {
			var assetInfo = AssetInfo.getInstance(asset);
			if (assetInfo.hasUnknownRisk()) {
				logger.warn("Has UNKNOWN  assetInfo  {}", assetInfo);
			}
		}
		
		logger.info("save  {}  {}", assetList.size(), getFile().getPath());
		save(assetList);
				
		logger.info("STOP");
	}
}

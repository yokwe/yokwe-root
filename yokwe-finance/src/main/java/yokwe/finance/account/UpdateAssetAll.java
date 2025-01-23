package yokwe.finance.account;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yokwe.finance.Storage;
import yokwe.finance.account.nikko.UpdateAssetNikko;
import yokwe.finance.account.prestia.UpdateAssetPrestia;
import yokwe.finance.account.rakuten.UpdateAssetRakuten;
import yokwe.finance.account.smtb.UpdateAssetSMTB;
import yokwe.finance.account.sony.UpdateAssetSony;
import yokwe.util.FileUtil;
import yokwe.util.ListUtil;
import yokwe.util.UnexpectedException;

public final class UpdateAssetAll {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.base;
	
	private static int THIS_YEAR = LocalDate.now().getYear();
	private static int LAST_YEAR = THIS_YEAR - 1;
	
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
			if (e.date.getYear() != THIS_YEAR && e.date.getYear() != LAST_YEAR) {
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
	
	
	private static final Duration DEFAULT_GRACE_PERIOD_FILE = Duration.ofHours(6);
	private static boolean needsUpdateFile(File file) {
		if (file.exists()) {
			var lastModified = FileUtil.getLastModified(file);
			var duration     = Duration.between(lastModified, Instant.now()).truncatedTo(ChronoUnit.SECONDS);
			// if duration is greater than DEFAULT_GRACE_PERIOD_FILE, return true
			return 0 < duration.compareTo(DEFAULT_GRACE_PERIOD_FILE);
		} else {
			return true;
		}
	}
	
	private static final UpdateAsset[] array = {
		UpdateAssetPrestia.getInstance(),
		UpdateAssetNikko.getInstance(),
		UpdateAssetRakuten.getInstance(),
		UpdateAssetSony.getInstance(),
		UpdateAssetSMTB.getInstance(),
	};
	
	
	public static List<Asset> getUpdate() {
		var list = new ArrayList<Asset>();
		
		for(var e: array) list.addAll(e.getList());		
		Collections.sort(list);
		
//		for(var e: list) {
//			logger.info("list  {}", e);
//		}
		
		// sanity check
		if (list.isEmpty()) {
			throw new UnexpectedException("Unexpected");
		}
		var last = list.get(list.size() - 1);
		
		if (last.date.getYear() != THIS_YEAR) {
			logger.error("Unexpected year");
			logger.error("THIS_YEAR  {}", THIS_YEAR);
			logger.error("last       {}", last);
			throw new UnexpectedException("Unexpected year");
		}
		
		// fix list if list contains old data
		{
			var lastDate = last.date;
			
			var newList = new ArrayList<Asset>();
			for(var e: list) {
				if (e.date.equals(lastDate)) {
					newList.add(e);
				} else {
					// Treat old data as new
					logger.warn("Treat old data as {}  {}", lastDate, e);
					newList.add(new Asset(e, lastDate));
				}
			}
			Collections.sort(newList);
			list = newList;
		}
		
		return list;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		{
			var list = new ArrayList<UpdateAsset>();
			logger.info("check  start");
			for(var e: array) {
				var file = e.getFile();
				if (needsUpdateFile(file)) {
					logger.info("update  {}", file.getPath());
					list.add(e);
				} else {
					logger.info("skip    {}", file.getPath());
				}
			}
			logger.info("check  stop");
			if (!list.isEmpty()) {
				logger.info("update start");
				for(var e: list) {
					e.download();
					e.update();
				}
				logger.info("update stop");
			}
		}
		
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

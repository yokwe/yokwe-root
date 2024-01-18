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
	
	public static List<Asset> getList() {
		var list = ListUtil.getList(Asset.class, getFile());
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
	public static List<Asset> getListLast() {
		var list = ListUtil.getList(Asset.class, getFile());
		if (!list.isEmpty()) {
			var lastDate = list.get(list.size() - 1).date;
			list.removeIf(o -> !o.date.equals(lastDate));
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
	
	public static List<Asset> getLast() {
		var list = new ArrayList<Asset>();
		
		for(var e: array) {
			list.addAll(e.getList());
		}
		
		Collections.sort(list);
		
		// sanity check
		if (list.isEmpty()) {
			throw new UnexpectedException("Unexpected");
		}
		var firstDate = list.get(0).date;
		var lastDate  = list.get(list.size() - 1).date;
		if (!firstDate.equals(lastDate)) {
			logger.error("Unexpected date");
			logger.error("firstDate  {}", firstDate);
			logger.error("lastDate   {}", lastDate);
			throw new UnexpectedException("Unexpected date");
		}
		if (lastDate.getYear() != THIS_YEAR) {
			logger.error("Unexpected year");
			logger.error("THIS_YEAR  {}", THIS_YEAR);
			logger.error("lastDate   {}", lastDate);
			throw new UnexpectedException("Unexpected year");
		}

		return list;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		for(var e: array) e.download();
		for(var e: array) e.update();
		
		{
			List<Asset> lastList = getLast();
			var lastDate = lastList.get(0).date;
			
			List<Asset> list = getList();
			// remove entry if date is same as lastDate
			list.removeIf(o -> o.date.equals(lastDate));
			
			list.addAll(lastList);
			
			var file = getFile();
			logger.info("save  {}  {}", list.size(), file.getPath());
			ListUtil.save(Asset.class, file, list);
		}
		
		logger.info("STOP");
	}
}

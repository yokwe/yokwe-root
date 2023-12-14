package yokwe.finance.fx;

import yokwe.finance.Storage;
import yokwe.finance.type.FXRate;

public class StorageFX {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final Storage storage = Storage.fx;
	
	// fxrate
	public static final Storage.LoadSaveList<FXRate> FXRate =
		new Storage.LoadSaveList.Impl<>(FXRate.class, storage, "fxrate.csv");
	
	private static FXRate latest = null;
	public static FXRate getLatest() {
		if (latest == null) {
			var list = FXRate.getList();
			latest = list.get(list.size() - 1);
			logger.info("latest  {}", latest);
		}
		return latest;
	}
}

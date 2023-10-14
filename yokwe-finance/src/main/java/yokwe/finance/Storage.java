package yokwe.finance;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public interface Storage {
	public static final String DATA_PATH_FILE = "data/DataPathLocation";
	
	public static void initialize() {}
	
	public static  String getDataPath() {
		org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
		logger.info("DATA_PATH_FILE  !{}!", DATA_PATH_FILE);
		// Sanity check
		if (!FileUtil.canRead(DATA_PATH_FILE)) {
			throw new UnexpectedException("Cannot read file");
		}
		
		String dataPath = FileUtil.read().file(DATA_PATH_FILE);
		logger.info("DATA_PATH       !{}!", dataPath);
		// Sanity check
		if (dataPath.isEmpty()) {
			logger.error("Empty dataPath");
			throw new UnexpectedException("Empty dataPath");
		}		
		if (!FileUtil.isDirectory(dataPath)) {
			logger.error("Not directory");
			throw new UnexpectedException("Not directory");
		}		
		return dataPath;
	}
	public static final String DATA_PATH = getDataPath();

	
	public String getPath();
	public String getPath(String path);
	public String getPath(String prefix, String path);
	
	public class Impl implements Storage {
		org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

		private final String basePath;
		
		public Impl(String basePath) {			
			if (!FileUtil.isDirectory(basePath)) {
				logger.error("Not directory");
				logger.error("  basePath  {}!", basePath);
				throw new UnexpectedException("Not directory");
			}

			this.basePath = basePath;
		}
		public Impl(String parent, String prefix) {
			this(parent + "/" + prefix);
		}
		public Impl(Storage parent, String prefix) {
			this(parent.getPath(), prefix);
		}
		
		@Override
		public String getPath() {
			return basePath;
		}

		@Override
		public String getPath(String path) {
			return basePath + "/" + path;
		}

		@Override
		public String getPath(String prefix, String path) {
			return basePath + "/" + prefix + "/" + path;
		}
	}
	
	public static Storage root            = new Impl(DATA_PATH);
	
	public static Storage provider        = new Impl(root, "provider");
	public static Storage stock           = new Impl(root, "stock");
	public static Storage fund            = new Impl(root, "fund");
	public static Storage report          = new Impl(root, "report");

	// provider
	public static Storage provider_bats   = new Impl(provider, "bats");
	public static Storage provider_nasdaq = new Impl(provider, "nasdaq");
	public static Storage provider_nyse   = new Impl(provider, "nyse");
	
	public static Storage provider_jpx    = new Impl(provider, "jpx");
	public static Storage provider_jita   = new Impl(provider, "jita");
	public static Storage provider_jreit  = new Impl(provider, "jreit");
	public static Storage provider_manebu = new Impl(provider, "manebu");
	
	public static Storage provider_click   = new Impl(provider, "click");
	public static Storage provider_monex   = new Impl(provider, "monex");
	public static Storage provider_moomoo  = new Impl(provider, "moomoo");
	public static Storage provider_nikko   = new Impl(provider, "nikko");
	public static Storage provider_nomura  = new Impl(provider, "nomura");
	public static Storage provider_prestia = new Impl(provider, "prestia");
	public static Storage provider_rakuten = new Impl(provider, "rakuten");
	public static Storage provider_sbi     = new Impl(provider, "sbi");
	public static Storage provider_sony    = new Impl(provider, "sony");
	
	public static Storage provider_yahoo   = new Impl(provider, "yahoo");
	
	// report
	public static Storage report_stock_stats_jp   = new Impl(report, "stock-stats-jp");
	public static Storage report_stock_stats_us   = new Impl(report, "stock-stats-us");

	
	public static void main(String[] args) {
		org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
		logger.info("START");
		logger.info("DATA_PATH       {}", DATA_PATH);
		
		logger.info("root            {}", Storage.root.getPath());
		logger.info("fund            {}", Storage.fund.getPath());
		logger.info("stock           {}", Storage.stock.getPath());
		logger.info("provider        {}", Storage.provider.getPath());
				
		logger.info("privider_jpx    {}", Storage.provider_jpx.getPath());
		logger.info("privider_jita   {}", Storage.provider_jita.getPath());
		logger.info("privider_bats   {}", Storage.provider_bats.getPath());
		logger.info("privider_nasdaq {}", Storage.provider_nasdaq.getPath());
		logger.info("privider_nyse   {}", Storage.provider_nyse.getPath());
		
		logger.info("STOP");
	}
}

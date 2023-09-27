package yokwe.finance;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public interface Storage {
	public static final String DATA_PATH_FILE = "data/DataPathLocation";
	public static  String getDataPath() {
		org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
		logger.info("DATA_PATH_FILE  {}", DATA_PATH_FILE);
		// Sanity check
		if (!FileUtil.canRead(DATA_PATH_FILE)) {
			throw new UnexpectedException("Cannot read file");
		}
		
		String dataPath = FileUtil.read().file(DATA_PATH_FILE);
		logger.info("dataPath        {}", dataPath);
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

	// provider
	public static Storage provider_jpx    = new Impl(provider, "jpx");
	public static Storage provider_jita   = new Impl(provider, "jita");
	public static Storage provider_bats   = new Impl(provider, "bats");
	public static Storage provider_nasdaq = new Impl(provider, "nasdaq");
	public static Storage provider_nyse   = new Impl(provider, "nyse");
	public static Storage provider_yahoo  = new Impl(provider, "yahoo");
	
	// stock
	public static Storage stock_jp        = new Impl(stock, "jp");
	public static Storage stock_us        = new Impl(stock, "us");

	// fund
	public static Storage fund_jp         = new Impl(fund, "jp");
	
	
	public static void main(String[] args) {
		org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
		logger.info("START");
		logger.info("DATA_PATH       {}", DATA_PATH);
		
		logger.info("root            {}", Storage.root.getPath());
		logger.info("fund            {}", Storage.fund.getPath());
		logger.info("stock           {}", Storage.stock.getPath());
		logger.info("provider        {}", Storage.provider.getPath());
		
		logger.info("fund_jp         {}", Storage.fund_jp.getPath());
		logger.info("stock_jp        {}", Storage.stock_jp.getPath());
		logger.info("stock_us        {}", Storage.stock_us.getPath());
		
		logger.info("privider_jpx    {}", Storage.provider_jpx.getPath());
		logger.info("privider_jita   {}", Storage.provider_jita.getPath());
		logger.info("privider_bats   {}", Storage.provider_bats.getPath());
		logger.info("privider_nasdaq {}", Storage.provider_nasdaq.getPath());
		logger.info("privider_nyse   {}", Storage.provider_nyse.getPath());
		
		logger.info("STOP");
	}
}

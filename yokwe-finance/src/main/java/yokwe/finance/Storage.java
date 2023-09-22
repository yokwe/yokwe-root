package yokwe.finance;

import yokwe.util.SystemUtil;

public interface Storage {
	public static final String PREFIX    = "finance";
	public static final String PATH_BASE = SystemUtil.getMountPoint(PREFIX);
	
	public String getPath();
	public String getPath(String path);
	public String getPath(String prefix, String path);
	
	public class Impl implements Storage {
		private final String basePath;
		
		public Impl(String basePath) {
			this.basePath = basePath;
		}
		public Impl(Storage storage, String prefix) {
			this.basePath =storage.getPath() + "/" + prefix;
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
	
	public static Storage root            = new Impl(PATH_BASE);
	
	public static Storage provider        = new Impl(root, "provider");
	public static Storage stock           = new Impl(root, "stock");
	public static Storage fund            = new Impl(root, "fund");

	// provider
	public static Storage provider_jpx    = new Impl(provider, "jpx");
	public static Storage provider_jita   = new Impl(provider, "jita");
	public static Storage provider_bats   = new Impl(provider, "bats");
	public static Storage provider_nasdaq = new Impl(provider, "nasdaq");
	public static Storage provider_nyse   = new Impl(provider, "nyse");
	
	// stock
	public static Storage stock_jp        = new Impl(stock, "jp");
	public static Storage stock_us        = new Impl(stock, "us");

	// fund
	public static Storage fund_jp         = new Impl(fund, "jp");
	
	
	public static void main(String[] args) {
		org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
		logger.info("START");
		logger.info("PATH_BASE       {}", PATH_BASE);
		
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

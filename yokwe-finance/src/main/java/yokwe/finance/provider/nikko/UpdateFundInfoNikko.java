package yokwe.finance.provider.nikko;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.fund.StorageFund;
import yokwe.util.FileUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.json.JSON.Name;

public class UpdateFundInfoNikko {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.provider.nikko;
	
	private static final File FILE_JSON = storage.getFile("trading-fund-nikko.json");
	
	private static final String URL = "https://fund2.smbcnikko.co.jp/smbc_nikko_hp/fund/jcgi/wrapcf/qjsonp.aspx?F=ctl%2Ffnd_list";
	
	private static final Charset CHARSET = Charset.forName("Shift_JIS");

	private static final boolean DEBUG_USE_FILE = false;
	private static String download(String url, Charset charset, File file, boolean useFile) {
		final String page;
		{
			if (useFile && file.exists()) {
				page = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().withCharset(charset).download(url);
				if (result == null || result.result == null) {
					logger.error("Unexpected");
					logger.error("  result  {}", result);
					throw new UnexpectedException("Unexpected");
				}
				// ({ -> {  }) -> }
				page = result.result.replace("fnd_list({", "{").replace("})", "}");
				
				// debug
				if (DEBUG_USE_FILE) logger.info("save  {}  {}", page.length(), file.getPath());
				FileUtil.write().file(file, page);
			}
		}
		return page;
	}
	
	private static void download() {
		var page = download(URL, CHARSET, FILE_JSON, DEBUG_USE_FILE);
		logger.info("page  {}", page.length());
	}
	
	
	public static class FundList {
		public static class Section {
			@Name("currentpage") public int    currentpage;
			@Name("hitcount")	 public int    hitcount;
			@Name("pagecount")	 public int    pagecount;
			@Name("recordcount") public int    recordcount;
			@Name("status")	     public int    status;
			@Name("type")	     public String ver;
			@Name("data")        public Data[] data;
		}
		
		public static class Data {
			@Name("Area")                         public String[]   Area;
			@Name("Categories")                   public String[]   Categories;
			@Name("ChangeValue")                  public String     ChangeValue;
			@Name("ChangeValueRaw")               public BigDecimal ChangeValueRaw;
			@Name("ChangeValueUnit")              public String     ChangeValueUnit;
			@Name("CompanyCode")                  public String     CompanyCode;
			@Name("CompanyName")                  public String     CompanyName;
			@Name("CompanySort")                  public int        CompanySort;
			@Name("Currency")                     public String     Currency;
			@Name("DividendProfit")               public String     DividendProfit;
			@Name("DividendProfitRaw")            public BigDecimal DividendProfitRaw;
			@Name("DividendTimes")                public String     DividendTimes;
			@Name("DividendYears")                public String     DividendYears;
			@Name("DividendYearsRaw")             public BigDecimal DividendYearsRaw;
			@Name("FundCode")                     public String     FundCode;
			@Name("FundName")                     public String     FundName;
			@Name("FundNameRaw")                  public String     FundNameRaw;
			@Name("FundType")                     public String     FundType;
			@Name("HasFavoriteButton")            public boolean    HasFavoriteButton;
			@Name("HasFundCompareButton")         public boolean    HasFundCompareButton;
			@Name("HasInvestmentReportLink")      public boolean    HasInvestmentReportLink;
			@Name("HasMokuromiLink")              public boolean    HasMokuromiLink;
			@Name("HasPurchaseOrderButton")       public boolean    HasPurchaseOrderButton;
			@Name("HasReserveApplicationButton")  public boolean    HasReserveApplicationButton;
			@Name("HasReserveSimulationButton")   public boolean    HasReserveSimulationButton;
			@Name("InvestmentReportUrl")          public String     InvestmentReportUrl;
			@Name("IsBullBear")                   public boolean    IsBullBear;
			@Name("IsDirectCourse")               public String     IsDirectCourse;
			@Name("IsDirectCourseOnly")           public boolean    IsDirectCourseOnly;
			@Name("IsESG")                        public boolean    IsESG;
			@Name("IsGeneralCourse")              public String     IsGeneralCourse;
			@Name("IsIndexType")                  public boolean    IsIndexType;
			@Name("IsNISA")                       public boolean    IsNISA;
			@Name("IsNoload")                     public boolean    IsNoload;
			@Name("IsTsumiNISA")                  public boolean    IsTsumiNISA;
			@Name("IsTsumitate")                  public boolean    IsTsumitate;
			@Name("IsiDeCo")                      public boolean    IsiDeCo;
			@Name("MokuromiUrl")                  public String     MokuromiUrl;
			@Name("MorningstarRating")            public String     MorningstarRating;
			@Name("NetAssetValue")                public String     NetAssetValue;
			@Name("NetAssetValueRaw")             public BigDecimal NetAssetValueRaw;
			@Name("NetAssetValueUnit")            public String     NetAssetValueUnit;
			@Name("NickNameRaw")                  public String     NickNameRaw;
			@Name("NikkoCode")                    public String     NikkoCode;
			@Name("NikkoSort")                    public BigDecimal nikkoSort;
			@Name("QuickFundRisk")                public String     QuickFundRisk;
			@Name("ReferenceDate")                public String     ReferenceDate;
			@Name("ReturnMonth1Raw")              public BigDecimal ReturnMonth1Raw;
			@Name("ReturnMonth3")                 public String     ReturnMonth3;
			@Name("ReturnMonth3Raw")              public BigDecimal ReturnMonth3Raw;
			@Name("ReturnMonth6Raw")              public BigDecimal ReturnMonth6Raw;
			@Name("ReturnSettingRaw")             public BigDecimal ReturnSettingRaw;
			@Name("ReturnYear1")                  public String     ReturnYear1;
			@Name("ReturnYear10Raw")              public BigDecimal ReturnYear10Raw;
			@Name("ReturnYear1Raw")               public BigDecimal ReturnYear1Raw;
			@Name("ReturnYear3Raw")               public BigDecimal ReturnYear3Raw;
			@Name("ReturnYear5Raw")               public BigDecimal ReturnYear5Raw;
			@Name("SharpeRatioYear1Raw")          public BigDecimal SharpeRatioYear1Raw;
			@Name("SharpeRatioYear3Raw")          public BigDecimal SharpeRatioYear3Raw;
			@Name("SharpeRatioYear5Raw")          public BigDecimal SharpeRatioYear5Raw;
			@Name("TotalAssetValue")              public String     TotalAssetValue;
			@Name("TotalAssetValueRaw")           public BigDecimal TotalAssetValueRaw;
			@Name("line_no")                      public int        line_no;
			
			public String toString() {
				return ToString.withFieldName(this);
			}
		}
		
		@Name("cputime")  public String  cputime;
		@Name("status")	  public int     status;
		@Name("ver")	  public String  ver;
		@Name("section1") public Section section;
	}
	
	private static void update() {
		var fundCodeMap = StorageFund.FundInfo.getList().stream().collect(Collectors.toMap(o -> o.fundCode, o -> o.isinCode));
		
		var page = FileUtil.read().file(FILE_JSON);
		var fundList = JSON.unmarshal(FundList.class, page);
		
		logger.info("fundList  {}", fundList.section.data.length);
		
		var list = new ArrayList<FundInfoNikko>();
		
		int countFund = 0;
		for (var data: fundList.section.data) {
			if (!data.Currency.equals("--")) continue;
			
			var isinCode = fundCodeMap.get(data.FundCode);
			if (isinCode == null) {
				// In case fund become redemption, it will disappeared from fundList
				logger.warn("Unpexpected fundCode  {}", data);
				continue;
			}
			int prospectus = data.MokuromiUrl.isEmpty()     ? 0 : 1;
			int sougou     = data.IsGeneralCourse.isEmpty() ? 0 : 1;
			int direct     = data.IsDirectCourse.isEmpty()  ? 0 : 1;
			var name       = data.FundName;
			
			list.add(new FundInfoNikko(isinCode, prospectus, sougou, direct, name));
			
			countFund++;
		}
		logger.info("countFund  {}", countFund);
		
		logger.info("save  {}  {}", list.size(), StorageNikko.FundInfoNikko.getPath());
		StorageNikko.FundInfoNikko.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		download();
		update();
		
		logger.info("STOP");
	}
}

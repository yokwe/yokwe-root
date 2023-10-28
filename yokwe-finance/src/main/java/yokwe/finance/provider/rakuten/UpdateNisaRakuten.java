package yokwe.finance.provider.rakuten;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.NisaFundType;
import yokwe.finance.type.StockCodeType;
import yokwe.finance.type.StockInfoJPType;
import yokwe.util.FileUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateNisaRakuten {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = false;
	
	private static byte[] download(String url, String filePath, boolean useFile) {
		final byte[] data;
		{
			File file = new File(filePath);
			if (useFile && file.exists()) {
				data = FileUtil.rawRead().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().withRawData(true).download(url);
				if (result == null || result.rawData == null) {
					logger.error("Unexpected");
					logger.error("  result  {}", result);
					throw new UnexpectedException("Unexpected");
				}
				data = result.rawData;
			}
		}
		return data;
	}
	
	
	@Sheet.SheetName("使用_一覧")
	@Sheet.HeaderRow(1)
	@Sheet.DataRow(2)
	public static class NisaFund extends Sheet implements Comparable<NisaFund> {
		private static final String URL       = "https://www.rakuten-sec.co.jp/web/info/xlsx/info20230621-01.xlsx";
		private static final String FILE_PATH = StorageRakuten.getPath("info20230621-01.xlsx");
		private static final String FILE_URL  = StringUtil.toURLString(FILE_PATH);

		private static final String NAME_IFA = "※個人型確定拠出年金、IFA（金融仲介業者）コースのみの取扱となります。";
		private static final int ACCUMULABLE_YES = 1;
		private static final int ACCUMULABLE_NO  = 2;

		// リスト更新日	追加・変更の別	ISINコード	当社取扱	投信協会ファンドコード	投信会社コード	運用会社ファンドコード	ファンド名称	運用会社名	設定日	償還日	成長投資枠取扱開始日	要件適合日	決算回数	追加型・単位型	つみたて投資枠の対象・非対象	詳細ページ
		
		@Sheet.ColumnName("ISINコード")                   public String      isinCode;
		
		@Sheet.ColumnName("リスト更新日")                 public int    updateDate;    // YYYYMMDD
		@Sheet.ColumnName("追加・変更の別")               public int    flagUpdate;    // 1 or 2
//		@Sheet.ColumnName("ISINコード")                   public String isinCode;
		@Sheet.ColumnName("当社取扱")                     public int    flagRakuten;   // 1
		@Sheet.ColumnName("投信協会ファンドコード")       public String fundCode;
		@Sheet.ColumnName("投信会社コード")               public String companyCode;
		@Sheet.ColumnName("運用会社ファンドコード")       public String internalCode;
		@Sheet.ColumnName("ファンド名称")                 public String name;
		@Sheet.ColumnName("運用会社名")                   public String companyName;
		@Sheet.ColumnName("設定日")                       public int    inceptionDate;
		@Sheet.ColumnName("償還日")                       public int    redmptionDate;  // YYYYMMDD or blank
		@Sheet.ColumnName("成長投資枠取扱開始日")         public int    tradeStartDate; // 20240104
		@Sheet.ColumnName("要件適合日")                   public int    complianceDate; // YYYYMMDD or blank
		@Sheet.ColumnName("決算回数")                     public int    divc;           // 1 - once a year, 2 - twice a year, 3 - 4 times a year, 4 - 6 times a year
		@Sheet.ColumnName("追加型・単位型")               public int    fundType;       // 1
		@Sheet.ColumnName("つみたて投資枠の対象・非対象") public int    flagAccumulate; // 1 - is tsumitate, 2 - is not tsumitate
		@Sheet.ColumnName("詳細ページ")                   public String detailLink;     // https://www.rakuten-sec.co.jp/web/fund/detail/?ID=JP90C00093P9
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
		
		@Override
		public int compareTo(NisaFund that) {
			return this.isinCode.compareTo(that.isinCode);
		}
	}
	
	
	@Sheet.SheetName("対象商品一覧")
	@Sheet.HeaderRow(1)
	@Sheet.DataRow(2)
	public static class NisaETFUS extends Sheet implements Comparable<NisaETFUS> {
		private static final String URL       = "https://www.rakuten-sec.co.jp/web/info/xlsx/info20230621-01-foreign.xlsx";
		private static final String FILE_PATH = StorageRakuten.getPath("info20230621-01-foreign.xlsx");
		private static final String FILE_URL  = StringUtil.toURLString(FILE_PATH);
		
		public static enum Country {
			US("米国"),
			HK("香港"),
			SG("シンガポール");
			
			public String value;
			
			Country(String value) {
				this.value = value;
			}
			
			@Override
			public String toString() {
				return value;
			}
		}

		
		// ティッカー/コード	ファンド名称	運用会社名	市場
		@Sheet.ColumnName("ティッカー/コード") public String  stockCode;
		@Sheet.ColumnName("ファンド名称")      public String  name;
		@Sheet.ColumnName("運用会社名")        public String  companyName;
		@Sheet.ColumnName("市場")              public Country country;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
		
		@Override
		public int compareTo(NisaETFUS that) {
			return this.stockCode.compareTo(that.stockCode);
		}
	}
	
	
	@Sheet.SheetName("対象商品一覧")
	@Sheet.HeaderRow(1)
	@Sheet.DataRow(2)
	public static class NisaETFJP extends Sheet  implements Comparable<NisaETFJP> {
		private static final String URL       = "https://www.rakuten-sec.co.jp/web/info/xlsx/info20230621-01-domestic.xlsx";
		private static final String FILE_PATH = StorageRakuten.getPath("info20230621-01-domestic.xlsx");
		private static final String FILE_URL  = StringUtil.toURLString(FILE_PATH);
		
		// 銘柄コード	ファンド名称	運用会社名
		@Sheet.ColumnName("銘柄コード")   public String stockCode;
		@Sheet.ColumnName("ファンド名称") public String name;
		@Sheet.ColumnName("運用会社名")   public String companyName;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
		
		@Override
		public int compareTo(NisaETFJP that) {
			return this.stockCode.compareTo(that.stockCode);
		}
	}
	
	
	private static void update() {		
		{
			byte[] data = download(NisaFund.URL, NisaFund.FILE_PATH, DEBUG_USE_FILE);
			logger.info("save  {}  {}", data.length, NisaFund.FILE_PATH);
			FileUtil.rawWrite().file(NisaFund.FILE_PATH, data);
			
			try (SpreadSheet spreadSheet = new SpreadSheet(NisaFund.FILE_URL, true)) {
				List<NisaFund> list = Sheet.extractSheet(spreadSheet, NisaFund.class);
				for(var e: list) {					
					e.name = e.name.replaceFirst("　+$", "");					
				}
				
				// build stockCodeList
				var nisaFundList = new ArrayList<NisaFundType>();
				{
					int countA = 0;
					int countB = 0;
					int countC = 0;
					int countD = 0;
					var isinCodeSet = StorageFund.FundInfo.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
					for(var e: list) {
						if (e.name.contains(NisaFund.NAME_IFA)) {
							countA++;
							continue;
						}
						if (isinCodeSet.contains(e.isinCode)) {
							if (e.flagAccumulate == NisaFund.ACCUMULABLE_YES || e.flagAccumulate == NisaFund.ACCUMULABLE_NO) {
								var accumulate = e.flagAccumulate == NisaFund.ACCUMULABLE_YES ? NisaFundType.Accumulable.YES : NisaFundType.Accumulable.NO;
								nisaFundList.add(new NisaFundType(e.isinCode, accumulate));
								countB++;
							} else {
								logger.warn("Unknown flagAccumulate  {}  {}  {}", e.flagAccumulate, e.isinCode, e.name);
								countC++;
							}
						} else {
							logger.warn("Unknown isinCode      {}  {}", e.isinCode, e.name);
							countD++;
						}
					}
					logger.info("countA  {}", countA);
					logger.info("countB  {}", countB);
					logger.info("countC  {}", countC);
					logger.info("countD  {}", countD);
				}
				
				logger.info("save  {}  {}", nisaFundList.size(), StorageRakuten.NisaFundRakuten.getPath());
				StorageRakuten.NisaFundRakuten.save(nisaFundList);
			}
		}
		{
			byte[] data = download(NisaETFUS.URL, NisaETFUS.FILE_PATH, DEBUG_USE_FILE);
			logger.info("save  {}  {}", data.length, NisaETFUS.FILE_PATH);
			FileUtil.rawWrite().file(NisaETFUS.FILE_PATH, data);
			
			try (SpreadSheet spreadSheet = new SpreadSheet(NisaETFUS.FILE_URL, true)) {
				List<NisaETFUS> list = Sheet.extractSheet(spreadSheet, NisaETFUS.class);
				for(var e: list) {
					e.stockCode = e.stockCode.replaceFirst(".0$", "");
					e.name      = e.name.replaceFirst("　+$", "");
				}
								
				// build stockCodeList
				var stockCodeList = new ArrayList<StockCodeType>();
				{
					int countA = 0;
					int countB = 0;
					int countC = 0;
					var stockCodeSet  = StorageStock.StockInfoUSTrading.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
					for(var e: list) {
						if (e.country != NisaETFUS.Country.US) {
							countA++;
							continue;
						}
						
						if (stockCodeSet.contains(e.stockCode)) {
							stockCodeList.add(new StockCodeType(e.stockCode));
							countB++;
						} else {
							logger.warn("Unknown stockCode US  {}  {}", e.stockCode, e.name);
							countC++;
						}
					}
					logger.info("countA  {}", countA);
					logger.info("countB  {}", countB);
					logger.info("countC  {}", countC);
				}
				logger.info("save  {}  {}", stockCodeList.size(), StorageRakuten.NisaETFUSRakuten.getPath());
				StorageRakuten.NisaETFUSRakuten.save(stockCodeList);
			}
		}
		{
			byte[] data = download(NisaETFJP.URL, NisaETFJP.FILE_PATH, DEBUG_USE_FILE);
			logger.info("save  {}  {}", data.length, NisaETFJP.FILE_PATH);
			FileUtil.rawWrite().file(NisaETFJP.FILE_PATH, data);
			
			try (SpreadSheet spreadSheet = new SpreadSheet(NisaETFJP.FILE_URL, true)) {
				List<NisaETFJP> list = Sheet.extractSheet(spreadSheet, NisaETFJP.class);
				for(var e: list) {
					e.stockCode = e.stockCode.replaceFirst(".0$", "");
					e.name      = e.name.replaceFirst("　+$", "");
				}
				
				// build stockCodeList
				var stockCodeList = new ArrayList<StockCodeType>();
				{
					int countA = 0;
					int countB = 0;
					var stockCodeSet = StorageStock.StockInfoJP.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
					for(var e: list) {
						String stockCode = StockInfoJPType.toStockCode5(e.stockCode);
						
						if (stockCodeSet.contains(stockCode)) {
							stockCodeList.add(new StockCodeType(stockCode));
							countA++;
						} else {
							logger.warn("Unknown stockCode JP  {}  {}", e.stockCode, e.name);
							countB++;
						}
					}
					logger.info("countA  {}", countA);
					logger.info("countB  {}", countB);
				}
				logger.info("save  {}  {}", stockCodeList.size(), StorageRakuten.NisaETFJPRakuten.getPath());
				StorageRakuten.NisaETFJPRakuten.save(stockCodeList);
			}
		}
	}
	
	
	public static void main(String[] args) {
		try {
			logger.info("START");
			
			// start LibreOffice process
			LibreOffice.initialize();
			
			update();
			
			logger.info("STOP");
		} catch (Throwable e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
		} finally {
			LibreOffice.terminate();
		}
	}
}

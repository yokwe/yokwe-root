package yokwe.finance.provider.manebu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoJP;
import yokwe.util.FileUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateETFInfo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static class ETF {
		public String status;
		public Data[] data;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static class Data {
		             public Category           category;
		             public String             targetIndex;
		             public String             stockCode;
		             public String             stockName;
		@JSON.Ignore public ManagementCompany  managementCompany;
		
		@JSON.Ignore public BigDecimal         minInvest;
		@JSON.Ignore public BigDecimal         netAssets;
		             public BigDecimal         managementFee;
		@JSON.Ignore public BigDecimal         deviation;
		@JSON.Ignore public BigDecimal         dividendYield;
		@JSON.Ignore public int                isFavorite;
		@JSON.Ignore public int                marketMake;
		
		@JSON.Ignore public Icons              icons;

		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static class Category {
		             public int    categoryCode;
		             public String categoryName;
		@JSON.Ignore public int    orderNum;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static class ManagementCompany {
		public String code;
		public String name;
		public String url;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	public static class Icons {
		public int genbutsu;
		public int foreign;
		public int otcswap;
		public int linked;
		public int etn;
		public int lev;
		public int inv;
		public int jdr;
		public int longterm;
		public int financialfuture;
		public int activefund;
		public int newNISA;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	private static Map<String, String> categoryMap = new HashMap<>();
	static {
		categoryMap.put("商品ETF",                   "商品");
		categoryMap.put("不動産ETF",                 "不動産");
		categoryMap.put("国内株ETF",                 "国内株");
		categoryMap.put("外国株ETF",                 "外国株");
		categoryMap.put("国内債券ETF",               "国内債券");
		categoryMap.put("外国債券ETF",               "外国債券");
		categoryMap.put("バランス型ETF",             "バランス");
		categoryMap.put("インバース商品",            "インバース");
		categoryMap.put("レバレッジ商品",            "レバレッジ");
		categoryMap.put("エンハンスト型ETF",         "エンハンスト");
		categoryMap.put("ボラティリティETF",         "ボラティリティ");
		categoryMap.put("アクティブ運用型ETF",       "アクティブ運用");
		categoryMap.put("商品（外国投資法人債）ETF", "商品");
	}
	
	private static void update() {
		final String page;
		{
			var url         = "https://jpx.cloud.qri.jp/tosyo-moneybu/api/list/etf";
			var body        = "{\"uid\":\"" + UUID.randomUUID() + "\"}";
			var contentType = "application/json;charset=UTF-8";
			var filePath = Storage.provider_manebu.getPath("etf.json");

			HttpUtil.Result result = HttpUtil.getInstance().withPost(body, contentType).download(url);
			if (result == null || result.result == null) {
				logger.error("Unexpected result");
				logger.error("  result  {}", result);
				throw new UnexpectedException("Unexpected result");
			}
			page = result.result;
			FileUtil.write().file(filePath, page);
		}
		
		{
			ETF etf = JSON.unmarshal(ETF.class, page);
			// sanity check
			if (!etf.status.equals("0")) {
				logger.error("Unexpected etf.status");
				logger.error("  status  {}", etf.status);
				throw new UnexpectedException("Unexpected etf.status");
			}
			logger.info("etf  data  {}", etf.data.length);
			
			var list = new ArrayList<ETFInfo>();
			for(var e: etf.data) {
				String     stockCode    = StockInfoJP.toStockCode5(e.stockCode);
				String     category     = categoryMap.get(e.category.categoryName);
				BigDecimal expenseRatio = e.managementFee.movePointLeft(2); // change to percent value
				String     name         = e.stockName;
				// sanity check
				if (category == null) {
					logger.error("Unexpected categoryName");
					logger.error("  {}  {}  {}", stockCode, name, e.category.categoryName);
					throw new UnexpectedException("Unexpected categoryName");
				}
				if (expenseRatio == null) {
					logger.error("Unexpected expenseRatio");
					logger.error("  {}  {}", stockCode, name);
					throw new UnexpectedException("Unexpected expenseRatio");
				}
				
				list.add(new ETFInfo(stockCode, category, expenseRatio, name));
			}
			
			logger.info("save  {}  {}", list.size(), ETFInfo.getPath());
			ETFInfo.save(list);
		}
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}

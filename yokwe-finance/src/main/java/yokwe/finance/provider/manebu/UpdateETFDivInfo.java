package yokwe.finance.provider.manebu;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.StockInfoJP;
import yokwe.util.FileUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateETFDivInfo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = false;
	
	private static final String URL          = "https://jpx.cloud.qri.jp/tosyo-moneybu/api/detail/info";
	private static final String CONTENT_TYPE = "application/json;charset=UTF-8";

	private static String download(String url, String filePath, String body, String contentType, boolean useFile) {
		final String page;
		{
			File file = new File(filePath);
			if (useFile && file.exists()) {
				page = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().withPost(body, contentType).download(url);
				if (result == null || result.result == null) {
					logger.error("Unexpected result");
					logger.error("  result  {}", result);
					throw new UnexpectedException("Unexpected result");
				}
				page = result.result;
				// debug
				if (DEBUG_USE_FILE) logger.info("save  {}  {}", page.length(), file.getPath());
				FileUtil.write().file(file, page);
			}
		}
		return page;
	}

	private static LocalDate toLocalDate(String string) {
		return LocalDate.parse(string.replace("/", "-"));
	}
	
	public static final class Info {
	    public static final class Data {
	        public static final class DividendHist {
	            public String     date; // YYYY/MM/DD
	            public BigDecimal dividend;
	            
	            @Override
	            public String toString() {
	                return StringUtil.toString(this);
	            }
	        }
	        
	        public static final class Icons {
	            public int etn;
	            public int financialfuture;
	            public int foreign;
	            public int genbutsu;
	            public int inv;
	            public int jdr;
	            public int lev;
	            public int linked;
	            public int longterm;
	            public int otcswap;

	            @Override
	            public String toString() {
	                return StringUtil.toString(this);
	            }
	        }
	        
	        public static final class ManagementCompany {
	            public String code;
	            public String name;
	            public String url;

	            @Override
	            public String toString() {
	                return StringUtil.toString(this);
	            }
	        }

	        public static final class PcfWeight {
	            public String     code;
	            public String     name;
	            public BigDecimal rank;
	            public BigDecimal weight;

	            @Override
	            public String toString() {
	                return StringUtil.toString(this);
	            }
	        }

	        @JSON.Ignore public int               categoryCode;
	                     public String            categoryName;
	                     public String            date;         // YYYY/MM/DD
	        @JSON.Ignore public BigDecimal        depth;
	        @JSON.Ignore public String            depthDate;
	        
	        @JSON.Ignore public BigDecimal        deviation;     // 乖離率…前日市場価格÷前日基準価額を1口あたりに換算した値の割合を表示
	        @JSON.Ignore public String[]          disclaimer;
	        @JSON.Ignore public BigDecimal        dividend;      // 分配金…直近1年間の分配金額を表示
                         public String            dividendDate;  // （年4回）
	                     public DividendHist[]    dividendHist;
	        @JSON.Ignore public BigDecimal        dividendYield; // 分配金利回り
	        @JSON.Ignore public int               exType;        // 0 for ETF
	        @JSON.Ignore public int               favoriteCount;
	        @JSON.Ignore public String            feature;       // explanation
	        @JSON.Ignore public boolean           hasNav;
	        @JSON.Ignore public String            iNavDate;
	        
	        @JSON.Ignore public Icons             icons;
	        
	        @JSON.Ignore public int               inav;
	        @JSON.Ignore public int               isFavorite;
	        @JSON.Ignore public BigDecimal        liquidity;
	                     public String            listingDate;       // YYYY/MM/DD
	        @JSON.Ignore public ManagementCompany managementCompany;
	                     public BigDecimal        managementFee;     // 信託報酬 in percent
	        @JSON.Ignore public int               marketMake;
	        @JSON.Ignore public int               minInvest;
	        @JSON.Ignore public String            nav;               // 基準価額 "2,042.94"
	        @JSON.Ignore public long              netAssets;         // 純資産総額
	        @JSON.Ignore public String            netAssetsDate;     // YYYY/MM/DD
	        @JSON.Ignore public String            notice;
	        @JSON.Ignore public String            nriDate;
	        @JSON.Ignore public String            otherExpense;
	        
	        @JSON.Ignore public String            pcfDataDate;
	        @JSON.Ignore public String            pcfFundDate;
	        
	        @JSON.Ignore public PcfWeight         pcfWeight;

	        @JSON.Ignore public BigDecimal        price;     // 終値・直近取引値
	        @JSON.Ignore public String            priceDate; // YYYY/MM/DD
	        
	        @JSON.Ignore public int               productCode;
	                     public String            productType;
	        
	        @JSON.Ignore public long              quarterTradingValue;  // 平均売買代金（直近90日）
	        @JSON.Ignore public long              quarterTradingVolume; // 平均売買高（直近90日）
	        
	        @JSON.Ignore public int               reserve;
	        
	        @JSON.Ignore public long              rightUnit;  // 受益権口数
	        @JSON.Ignore public String            sharesDate; // YYYY/MM/DD
	        
	                     public String            shintakuRyuhogaku;
	        
	        @JSON.Ignore public BigDecimal        spread;     // スプレッド…最良の売気配値段と買気配値段の価格差（%）
	        @JSON.Ignore public String            spreadDate; // STRING STRING
	        
	                     public String            stockCode; // NNNN
	                     public String            stockName;
	        
	        @JSON.Ignore public String            targetIndex;      // 対象指標
	        @JSON.Ignore public String            underlierOutline; // explanation of underline index
	        
	        @JSON.Ignore public BigDecimal        tradingValue; // 売買代金
	        @JSON.Ignore public String            tvDate;       // YYYY/MM/DD
	        
	        @JSON.Ignore public int               unit;   // 売買単位
	        @JSON.Ignore public long              volume; // 売買高

	        @JSON.Ignore public String            yokogaoLink;
	        
	        @Override
	        public String toString() {
	            return StringUtil.toString(this);
	        }
	    }

	    @JSON.Optional public Data   data;
	                   public String status;
	    @JSON.Optional public String message;

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
		var stockCodeList = yokwe.finance.provider.jpx.StockInfo.getList().stream().filter(o -> o.kind.isETF()).map(o -> o.stockCode).collect(Collectors.toList());
		
		logger.info("etf  {}", stockCodeList.size());
		
		var list = new ArrayList<ETFInfo>();
		int count = 0;
		for(var stockCode: stockCodeList) {
			if ((++count % 10) == 1) logger.info("{}  /  {}", count, stockCodeList.size());
			
			String stockCode4 = StockInfoJP.toStockCode4(stockCode);

			final String page;
			{
				String body     = "{\"stockCode\":\"" + stockCode4 + "\"}";
				String filePath = Storage.provider_manebu.getPath("page", stockCode + ".json");
				
				page = download(URL, filePath, body, CONTENT_TYPE, DEBUG_USE_FILE);
			}
			
			if (page.contains("銘柄が見つかりませんでした。")) {
				logger.warn("not found  {}", stockCode);
				continue;
			}
			{
				Info info = JSON.unmarshal(Info.class, page);
				// sanity check
				if (info.status.equals("0")) {
					// sanity check
					if (!stockCode4.equals(info.data.stockCode)) {
						logger.error("Unpexpected stockCode");
						logger.error("  stockCode  {}", stockCode);
						logger.error("  stockCode4 {}", stockCode4);
						logger.error("  info.data  {}", info.data.stockCode);
						throw new UnexpectedException("Unpexpected stockCode");
					}
					
					String     name              = info.data.stockName;
					String     category          = categoryMap.get(info.data.categoryName);
					BigDecimal expenseRatio      = info.data.managementFee.movePointLeft(2); // change to percent value
					int        divFreq           = info.data.dividendDate.isEmpty() ? 1 : Integer.valueOf(info.data.dividendDate.replace("（年", "").replace("回）", ""));
					LocalDate  listingDate       = toLocalDate(info.data.listingDate);
					String     productType       = info.data.productType;
					BigDecimal shintakuRyuhogaku = info.data.shintakuRyuhogaku.isEmpty() ? BigDecimal.ZERO : new BigDecimal(info.data.shintakuRyuhogaku).movePointLeft(2);

					// sanity check
					if (category == null) {
						logger.error("Unexpected categoryName");
						logger.error("  {}  {}  {}", stockCode, name, info.data.categoryName);
						throw new UnexpectedException("Unexpected categoryName");
					}
					
					if (info.data.dividendHist != null) {
						var divList = new ArrayList<DailyValue>();
						for(var e: info.data.dividendHist) {
							LocalDate  date  = toLocalDate(e.date);
							BigDecimal value = e.dividend;
							divList.add(new DailyValue(date, value));
						}
						if (!divList.isEmpty()) {
//							logger.info("save  {}  {}", divList.size(), ETFDiv.getPath(stockCode));
							ETFDiv.save(stockCode, divList);
						}
					}
					
					list.add(new ETFInfo(stockCode, category, expenseRatio, name, divFreq, listingDate, productType, shintakuRyuhogaku));
				} else if (info.status.equals("-1") && info.message.equals("銘柄が見つかりませんでした。")) {
					// not found
				} else {
					logger.error("Unexpected etf.status");
					logger.error("  status  {}", info.status);
					throw new UnexpectedException("Unexpected etf.status");
				}
			}
		}
		logger.info("save  {}  {}", list.size(), ETFInfo.getPath());
		ETFInfo.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}

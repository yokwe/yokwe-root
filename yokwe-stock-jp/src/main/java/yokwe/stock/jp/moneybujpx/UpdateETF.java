package yokwe.stock.jp.moneybujpx;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.jpx.Stock;
import yokwe.util.FileUtil;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.json.JSON.Ignore;

public class UpdateETF {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final class RAW {
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

	        public int               categoryCode;
	        public String            categoryName;
	        public String            date;         // YYYY/MM/DD
	        public BigDecimal        depth;
	        public String            depthDate;
	        
	        public BigDecimal        deviation;     // 乖離率…前日市場価格÷前日基準価額を1口あたりに換算した値の割合を表示
	        public String[]          disclaimer;
	        public BigDecimal        dividend;      // 分配金…直近1年間の分配金額を表示
	        public String            dividendDate;  // （年4回）
	        public DividendHist[]    dividendHist;
	        public BigDecimal        dividendYield; // 分配金利回り
	        public int               exType;        // 0 for ETF
	        public int               favoriteCount;
	        public String            feature;       // explanation
	        public boolean           hasNav;
	        public String            iNavDate;
	        
	        @Ignore
	        public Icons             icons;
	        
	        public int               inav;
	        public int               isFavorite;
	        public BigDecimal        liquidity;
	        public String            listingDate;       // YYYY/MM/DD
	        public ManagementCompany managementCompany;
	        public BigDecimal        managementFee;     // 信託報酬 in percent
	        public int               marketMake;
	        public int               minInvest;
	        public String            nav;               // 基準価額 "2,042.94"
	        public long              netAssets;         // 純資産総額
	        public String            netAssetsDate;     // YYYY/MM/DD
	        public String            notice;
	        public String            nriDate;
	        public String            otherExpense;
	        
	        public String            pcfDataDate;
	        public String            pcfFundDate;
	        
	        @Ignore
	        public PcfWeight         pcfWeight;

	        public BigDecimal        price;     // 終値・直近取引値
	        public String            priceDate; // YYYY/MM/DD
	        
	        public int               productCode;
	        public String            productType;
	        
	        public long              quarterTradingValue;  // 平均売買代金（直近90日）
	        public long              quarterTradingVolume; // 平均売買高（直近90日）
	        
	        public int               reserve;
	        
	        public long              rightUnit;  // 受益権口数
	        public String            sharesDate; // YYYY/MM/DD
	        
	        public String            shintakuRyuhogaku;
	        
	        public BigDecimal        spread;     // スプレッド…最良の売気配値段と買気配値段の価格差（%）
	        public String            spreadDate; // STRING STRING
	        
	        public String            stockCode; // NNNN
	        public String            stockName;
	        
	        public String            targetIndex;      // 対象指標
	        public String            underlierOutline; // explanation of underline index
	        
	        public BigDecimal        tradingValue; // 売買代金
	        public String            tvDate;       // YYYY/MM/DD
	        
	        public int               unit;   // 売買単位
	        public long              volume; // 売買高

	        public String            yokogaoLink;
	        
	        @Override
	        public String toString() {
	            return StringUtil.toString(this);
	        }
	    }

	    public Data   data;
	    public String status;

	    @Override
	    public String toString() {
	        return StringUtil.toString(this);
	    }
	}

	public static String convertDate(String string) {
		// 2021/12/07 => 2021-12-07
		String[] mdy = string.split("\\/");
		if (mdy.length != 3) {
			logger.error("Unpexpected");
			logger.error("  date {}", string);
			throw new UnexpectedException("Unpexpected");
		}
		return mdy[0] + "-" + mdy[1] + "-" + mdy[2];
	}
	
	private static final Pattern PAT_DIVIDEND_DATE = Pattern.compile("（年([0-9]+)回）");
	
	private static ETF getInstance(RAW raw) {
		String     date              = convertDate(raw.data.date);
		String     stockCode         = Stock.toStockCode5(raw.data.stockCode);
		String     listingDate       = convertDate(raw.data.listingDate);
		BigDecimal expenseRatio      = raw.data.managementFee.scaleByPowerOfTen(-2);
		
		int divFreq;
		if (raw.data.dividendDate == null) {
			divFreq = 0;
		} else {
			if (raw.data.dividendDate.isEmpty()) {
				divFreq = 0;
			} else {
				var m = PAT_DIVIDEND_DATE.matcher(raw.data.dividendDate);
				if (m.matches()) {
					String group1 = m.group(1);
					divFreq = Integer.parseInt(group1);
				} else {
					logger.warn("Unpexpected dividendDate {}!", raw.data.dividendDate);
					divFreq = 0;
				}
			}
		}
		
		String     categoryName      = raw.data.categoryName;
		String     stockName         = raw.data.stockName;

		ETF etf = new ETF(
				date, stockCode, listingDate, expenseRatio,
				divFreq,
				categoryName,
				stockName
				);
		
		return etf;
	}
	
	private static final String URL          = "https://jpx.cloud.qri.jp/tosyo-moneybu/api/detail/info";
	private static final String CONTENT_TYPE = "application/json;charset=UTF-8";
	
	public static ETF getInstance(String stockCode, List<ETFDiv> list) {
		String body   = String.format("{\"stockCode\":\"%s\"}", Stock.toStockCode4(stockCode));
		String string = HttpUtil.getInstance().withPost(body, CONTENT_TYPE).download(URL).result;
		// debug
		FileUtil.write().file(Storage.MoneyBuJPX.getPath("file", stockCode + ".json"), string);
		
		if (string == null) {
			logger.warn("failed to download {}", stockCode);
			return null;
		}
		if (string.contains("銘柄が見つかりませんでした")) {
			logger.warn("not found {}", stockCode);
			return null;
		}

		RAW raw = JSON.unmarshal(RAW.class, string);
		if (raw == null) {
			logger.warn("failed to parse {}", stockCode);
			return null;
		} else {
			if (raw.status.equals("0")) {
				if (raw.data.dividendHist != null) {
					for(var e: raw.data.dividendHist) {
						var div = new ETFDiv(convertDate(e.date), stockCode, e.dividend);
						list.add(div);
					}
				}
				
				return getInstance(raw);
			} else {
				logger.warn("status is no zero {}", stockCode);
				return null;
			}
		}
	}

	public static void main(String[] args) {
		logger.info("START");
		
		List<Stock> stockList = Stock.getList().stream().filter(o -> o.isETF()).collect(Collectors.toList());
		logger.info("stockList {}", stockList.size());
		Collections.shuffle(stockList);
		
		List<ETF> etfList = new ArrayList<>();
		
		int count = 0;
		for(var e: stockList) {
			if ((count % 10) == 0) logger.info(String.format("%4d / %4d  %s", count, stockList.size(), e.stockCode));
			count++;

			String stockCode = e.stockCode;
			var divList = new ArrayList<ETFDiv>();
			ETF etf = getInstance(stockCode, divList);
			if (etf == null) continue;
			
			if (!divList.isEmpty()) {
				// sanity check
				ListUtil.checkDuplicate(divList, o -> o.date);
				for(var div: divList) {
					double value = div.amount.doubleValue();
					if (Double.isFinite(value)) continue;
					logger.error("Contains not number");
					logger.error("  stockCode {}", stockCode);
					logger.error("  divList   {}", divList);
					throw new UnexpectedException("Contains not number");
				}

				// load old data to merger new data
				var map = ETFDiv.getList(stockCode).stream().collect(Collectors.toMap(o -> o.date, o -> o));
				for(var newDiv: divList) {
					if (map.containsKey(newDiv.date)) {
						var old = map.get(newDiv.date);
						// sanity check of old value
						if (!old.stockCode.equals(newDiv.stockCode)) {
							logger.error("Unexpected stockCode");
							logger.error("  old {}", old);
							logger.error("  new {}", newDiv);
							throw new UnexpectedException("Unexpected stockCode");
						}
						if (!old.amount.equals(newDiv.amount)) {
							logger.error("Unexpected amount");
							logger.error("  old {}", old);
							logger.error("  new {}", newDiv);
							throw new UnexpectedException("Unexpected amount");
						}
					} else {
						map.put(newDiv.date, newDiv);
					}
				}
				logger.info("save dividend {} {}", stockCode, map.size());
				ETFDiv.save(stockCode, map.values());
			}

//			logger.info("etf {}", etf.toString());
			etfList.add(etf);
		}
		
		logger.info("save {} {}", etfList.size(), ETF.getPath());
		ETF.save(etfList);
		
		logger.info("STOP");
	}
	
}

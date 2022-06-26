package yokwe.stock.jp.jpx;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.json.JSON.Ignore;

public class UpdateETF {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateETF.class);

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
		BigDecimal divAnnual         = (raw.data.dividend == null) ? BigDecimal.ZERO : raw.data.dividend;
		String     stockName         = raw.data.stockName;
		String     categoryName      = raw.data.categoryName;
		String     productType       = raw.data.productType;
		String     targetName        = raw.data.targetIndex;
		String     targetDescription = raw.data.underlierOutline;
		
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
		
		ETF etf = new ETF(
				date, stockCode, listingDate, expenseRatio,
				divAnnual, divFreq,
				stockName, categoryName, productType,
				targetName, targetDescription
				);
		
		return etf;
	}
	
	private static final String URL          = "https://jpx.cloud.qri.jp/tosyo-moneybu/api/detail/info";
	private static final String CONTENT_TYPE = "application/json;charset=UTF-8";
	
	public static ETF getInstance(String stockCode) {
		String body   = String.format("{\"stockCode\":\"%s\"}", Stock.toStockCode4(stockCode));
		String string = HttpUtil.getInstance().withPost(body, CONTENT_TYPE).download(URL).result;
		
		if (string == null) {
			logger.warn("failed to download {}", stockCode);
			return null;
		}

		RAW raw = JSON.unmarshal(RAW.class, string);
		if (raw == null) {
			logger.warn("failed to parse {}", stockCode);
			return null;
		} else {
			if (raw.status.equals("0")) {
				if (raw.data.dividendHist != null) {
					var map = ETFDiv.getMap(stockCode);
					int count = 0;
					for(var e: raw.data.dividendHist) {
						var div = new ETFDiv(stockCode, e.dividend, convertDate(e.date));
						if (map.containsKey(div.payDate)) {
							// sanity check
							var old = map.get(div.payDate);
							if (div.equals(old)) {
								// same value
							} else {
								logger.error("Unexpected value");
								logger.error("  div {}", div);
								logger.error("  old {}", old);
								throw new UnexpectedException("Unexpected value");
							}
						} else {
							map.put(div.payDate, div);
							count++;
						}
					}
					
					if (0 < count) {
						logger.info("save {} {}", stockCode, map.size());
						ETFDiv.save(map.values());
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

			ETF etf = getInstance(e.stockCode);
//			logger.info("etf {}", etf.toString());
			etfList.add(etf);
		}
		
		logger.info("save {} {}", etfList.size(), ETF.getPath());
		ETF.save(etfList);
		
		logger.info("STOP");
	}
	
}

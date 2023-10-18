package yokwe.finance.provider.sony;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.type.TradingFundType;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateTradingFundSony {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE  = false;

	private static final String URL     = "https://moneykit.net/data/fund/SFBA1700F471.js";
	private static final String CHARSET = "MS932";
	
	
	private static String download(String url, String charset, String filePath, boolean useFile) {
		final String page;
		{
			File file = new File(filePath);
			if (useFile && file.exists()) {
				page = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().withCharset(charset).download(url);
				if (result == null || result.result == null) {
					logger.error("Unexpected");
					logger.error("  result  {}", result);
					throw new UnexpectedException("Unexpected");
				}
				page = result.result;
				// debug
				if (DEBUG_USE_FILE) logger.info("save  {}  {}", page.length(), file.getPath());
				FileUtil.write().file(file, page);
			}
		}
		return page;
	}
	
	
	public static class FundData {
		public static class Entry {
	        @JSON.Name("SBFundCode")         @JSON.Ignore public String sbFundCode;         // STRING INT
	        @JSON.Name("FundMei")                         public String fundMei;            // STRING STRING
	        @JSON.Name("FundAisyo")          @JSON.Ignore public String fundAisyo;          // STRING STRING
	        @JSON.Name("KanaCode")           @JSON.Ignore public String kanaCode;           // STRING INT
	        @JSON.Name("TukaCode")           @JSON.Ignore public String tukaCode;           // STRING STRING
	        @JSON.Name("MSCategoryCodeDai")  @JSON.Ignore public String msCategoryCodeDai;  // STRING INT
	        @JSON.Name("CategoryMeisyo")     @JSON.Ignore public String categoryMeisyo;     // STRING STRING
	        @JSON.Name("ToshiTarget")        @JSON.Ignore public String toshiTarget;        // STRING INT
	        @JSON.Name("ToshiArea")          @JSON.Ignore public String toshiArea;          // STRING INT
	        @JSON.Name("KijyunKagaku")       @JSON.Ignore public String kijyunKagaku;       // STRING INT
	        @JSON.Name("KijyunKagakuFM")     @JSON.Ignore public String kijyunKagakuFM;     // STRING STRING
	        @JSON.Name("ZenjituhiFM")        @JSON.Ignore public String zenjituhiFM;        // STRING STRING
	        @JSON.Name("TotalReturn")        @JSON.Ignore public String totalReturn;        // STRING REAL
	        @JSON.Name("TotalReturnFM")      @JSON.Ignore public String totalReturnFM;      // STRING STRING
	        @JSON.Name("JyunsisanEn")        @JSON.Ignore public String jyunsisanEn;        // STRING INT
	        @JSON.Name("JyunsisanEnFM")      @JSON.Ignore public String jyunsisanEnFM;      // STRING STRING
	        @JSON.Name("KessanHindo")        @JSON.Ignore public String kessanHindo;        // STRING INT
	        @JSON.Name("KessanHindoFM")      @JSON.Ignore public String kessanHindoFM;      // STRING STRING
	        @JSON.Name("HanbaiTesuryo")      @JSON.Ignore public String hanbaiTesuryo;      // STRING STRING
	        @JSON.Name("HanbaiTesuryoFM")    @JSON.Ignore public String hanbaiTesuryoFM;    // STRING STRING
	        @JSON.Name("TumitatePlan")       @JSON.Ignore public String tumitatePlan;       // STRING INT
	        @JSON.Name("TumitatePlanFM")     @JSON.Ignore public String tumitatePlanFM;     // STRING STRING
	        @JSON.Name("FlgHanbaiteisi")     @JSON.Ignore public String flgHanbaiteisi;     // STRING INT
	        @JSON.Name("FlgFromSSeimei")     @JSON.Ignore public String flgFromSSeimei;     // STRING INT
	        @JSON.Name("FlgHRiskFund")       @JSON.Ignore public String flgHRiskFund;       // STRING INT
	        @JSON.Name("FlgSWOnly")          @JSON.Ignore public String flgSWOnly;          // STRING INT
	        @JSON.Name("SintakHosyu")        @JSON.Ignore public String sintakHosyu;        // STRING REAL
	        @JSON.Name("SintakHosyuFM")      @JSON.Ignore public String sintakHosyuFM;      // STRING STRING
	        @JSON.Name("HanbaigakuFM")       @JSON.Ignore public String hanbaigakuFM;       // STRING STRING
	        @JSON.Name("NISAHanbaigakuFM")   @JSON.Ignore public String nisaHanbaigakuFM;   // STRING STRING
	        @JSON.Name("TumitateKensu")      @JSON.Ignore public String tumitateKensu;      // STRING INT
	        @JSON.Name("BunpaiKinriFM")      @JSON.Ignore public String bunpaiKinriFM;      // STRING STRING
	        @JSON.Name("SougouRating")       @JSON.Ignore public String sougouRating;       // STRING INT
	        @JSON.Name("SougouRatingFM")     @JSON.Ignore public String sougouRatingFM;     // STRING STRING
	        @JSON.Name("HyokaKijyunbi")      @JSON.Ignore public String hyokaKijyunbi;      // STRING STRING
	        @JSON.Name("FundRyaku")          @JSON.Ignore public String fundRyaku;          // STRING STRING
	        @JSON.Name("HanbaigakuRanking")  @JSON.Ignore public String hanbaigakuRanking;  // STRING INT
	        @JSON.Name("NISAHanbaigakuRank") @JSON.Ignore public String nisaHanbaigakuRank; // STRING INT
	        @JSON.Name("TumitateKensuRank")  @JSON.Ignore public String tumitateKensuRank;  // STRING INT
	        @JSON.Name("JyunsisanRank")      @JSON.Ignore public String jyunsisanRank;      // STRING INT
	        @JSON.Name("ReturnRank")         @JSON.Ignore public String returnRank;         // STRING INT
	        @JSON.Name("BunpaiKinriRank")    @JSON.Ignore public String bunpaiKinriRank;    // STRING INT

	        @Override
	        public String toString() {
	            return String.format("{\"%s\"}", fundMei);
	        }
		}
		
		public Map<String, Entry> map;
		
		@Override
		public String toString() {
			return map == null ? "null" : map.toString();
		}
	}

	private static void update() {
		LocalDateTime dateTime;
		String        jsonString;
		{
			String page;
			{
				String filePath = StorageSony.getPath("page", "SFBA1700F471.js");
				page = download(URL, CHARSET, filePath, DEBUG_USE_FILE);
			}
			
			{
				Pattern p = Pattern.compile("YrMonDate='(?<yyyy>2[0-9]{3})/(?<mm>[0-9]{2})/(?<dd>[0-9]{2}) (?<hhmmss>[0-9:]{8})';");
				Matcher m = p.matcher(page);
				if (m.find()) {
					String yyyy   = m.group("yyyy");
					String mm     = m.group("mm");
					String dd     = m.group("dd");
					String hhmmss = m.group("hhmmss");
					
					String dateTimeString = String.format("%s-%s-%s %s", yyyy, mm, dd, hhmmss);
					dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
				} else {
					throw new UnexpectedException("no match PAT_YRMONDATE");
				}
			}

			Pattern p = Pattern.compile(";FundData= (?<json>\\{.+\\});");
			Matcher m = p.matcher(page);
			if (m.find()) {
				jsonString = String.format("{\"map\": %s}", m.group("json").replace("[", "").replace("]", ""));
			} else {
				throw new UnexpectedException("no match PAT_FUNDDATA");
			}
		}
		
		FundData fundData = JSON.unmarshal(FundData.class, jsonString);
		logger.info("dateTime  {}", dateTime);
		logger.info("fundData  {}", fundData.map.size());
		
		var list = new ArrayList<TradingFundType>();
		{
			var isinCodeSet = StorageFund.FundInfo.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
			
			int countA = 0;
			int countB = 0;
			int countC = 0;
			for(var e: fundData.map.entrySet()) {
				String isinCode = e.getKey();
				String fundName = e.getValue().fundMei;
				
				if (isinCodeSet.contains(isinCode)) {
					list.add(new TradingFundType(isinCode, BigDecimal.ZERO));
					countA++;
				} else {
					if (isinCode.startsWith("JP")) {
						logger.warn("Unexpected isinCode  {}  {}", isinCode, fundName);
						countB++;
					} else {
						// ignore foreign registered fund
						logger.warn("Ignore foreign fund  {}  {}", isinCode, fundName);
						countC++;
					}
				}
			}
			logger.info("countA    {}", countA);
			logger.info("countB    {}", countB);
			logger.info("countC    {}", countC);
		}
		
		logger.info("save  {}  {}", list.size(), StorageSony.TradingFundSony.getPath());
		StorageSony.TradingFundSony.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}

package yokwe.stock.jp.sony;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yokwe.stock.jp.sony.Fund.Region;
import yokwe.stock.jp.sony.Fund.Target;
import yokwe.stock.jp.sony.json.FundData;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateFund {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static class RawData {
		public Map<String, FundData.JP90C00002U0> map;
		
		public RawData() {
			map = null;
		}
	}
	
//	private static String toString(BigDecimal value) {
//		return value.compareTo(BigDecimal.ZERO) == 0 ? "0" : value.toPlainString();
//	}
	
	public static Fund getInstance(LocalDateTime dateTime, String isinCode, FundData.JP90C00002U0 raw) {
		Fund ret = new Fund();
        ret.dateTime           = dateTime;
        ret.isinCode           = isinCode;
        //
//        ret.divRatio           = raw.bunpaiKinriFM.equals("-") ? "" : toString(new BigDecimal(raw.bunpaiKinriFM.replace("%", "")).movePointLeft(2));
//        ret.divRatio           = raw.bunpaiKinriFM.equals("-") ? "" : raw.bunpaiKinriFM;
//      ret.bunpaiKinriRank    = raw.bunpaiKinriRank;
        ret.category           = raw.categoryMeisyo;
//      ret.flgFromSSeimei     = raw.flgFromSSeimei;
//      ret.flgHanbaiteisi     = raw.flgHanbaiteisi;
//      ret.flgHRiskFund       = raw.flgHRiskFund;
//      ret.flgSWOnly          = raw.flgSWOnly;
//      ret.fundAisyo          = raw.fundAisyo;
        ret.fundName           = raw.fundMei;
//      ret.fundRyaku          = raw.fundRyaku;
//      ret.hanbaigakuFM       = raw.hanbaigakuFM;
//      ret.hanbaigakuRanking  = raw.hanbaigakuRanking;
//        ret.salesFee           = raw.hanbaiTesuryo.equals("-") ? "" : toString(new BigDecimal(raw.hanbaiTesuryo));
//        ret.salesFee           = raw.hanbaiTesuryo.equals("-") ? "" : raw.hanbaiTesuryo;
//      ret.hanbaiTesuryoFM    = raw.hanbaiTesuryoFM;
//        ret.hyokaKijyunbi      = raw.hyokaKijyunbi.replace("年", "-").replace("月", "");
//        ret.marketCap          = raw.jyunsisanEn.equals("-") ? "" : toString(new BigDecimal(raw.jyunsisanEn).movePointRight(6));
//        ret.marketCap          = raw.jyunsisanEn.equals("-") ? "" : raw.jyunsisanEn;
//      ret.jyunsisanEnFM      = raw.jyunsisanEnFM;
//      ret.jyunsisanRank      = raw.jyunsisanRank;
        ret.company            = Company.get(raw.kanaCode);
//        ret.divFreq            = raw.kessanHindo.equals("-") ? "" : toString(new BigDecimal(raw.kessanHindo));
        ret.divFreq            = raw.kessanHindo.equals("-") ? "" : raw.kessanHindo;
//      ret.kessanHindoFM      = raw.kessanHindoFM;
//        ret.price              = raw.kijyunKagaku.equals("-") ? "" : toString(new BigDecimal(raw.kijyunKagaku));
//        ret.price              = raw.kijyunKagaku.equals("-") ? "" : raw.kijyunKagaku;
//      ret.kijyunKagakuFM     = raw.kijyunKagakuFM;
//      ret.msCategoryCodeDai  = raw.msCategoryCodeDai;
//      ret.nisaHanbaigakuFM   = raw.nisaHanbaigakuFM;
//      ret.nisaHanbaigakuRank = raw.nisaHanbaigakuRank;
//      ret.returnRank         = raw.returnRank;
//      ret.sbFundCode         = raw.sbFundCode;
//        ret.expenseRatio       = toString(new BigDecimal(raw.sintakHosyu).movePointLeft(2));
 //       ret.expenseRatio       = raw.sintakHosyu;
//      ret.sintakHosyuFM      = raw.sintakHosyuFM;
//      ret.sougouRating       = raw.sougouRating.equals("-") ? MISSING_DATA : Integer.parseInt(raw.sougouRating);
//      ret.sougouRatingFM     = raw.sougouRatingFM;
        ret.region             = Region.get(raw.toshiArea);
        ret.target             = Target.get(raw.toshiTarget);
//      ret.totalReturn        = raw.totalReturn.equals("-") ? MISSING_DATA : DoubleUtil.round(Double.parseDouble(raw.totalReturn) * 0.01, 4);
//      ret.totalReturnFM      = raw.totalReturnFM;
        ret.currency           = Currency.get(raw.tukaCode);
//      ret.tumitateKensu      = raw.tumitateKensu;
//      ret.tumitateKensuRank  = raw.tumitateKensuRank;
//      ret.tumitatePlan       = raw.tumitatePlan;
//      ret.tumitatePlanFM     = raw.tumitatePlanFM;
//      this.zenjituhiFM       = raw.zenjituhiFM.equals("-") ? "" : toString(new BigDecimal(raw.zenjituhiFM.replace(",", "").replace("円", "").replace("USD", "")));
        
        return ret;
	}

	private static final String URL = "https://moneykit.net/data/fund/SFBA1700F471.js";

	public static List<Fund> updateList() {
		HttpUtil.Result result = HttpUtil.getInstance().withCharset("MS932").download(URL);
		
		String string = result.result;
		
		LocalDateTime dateTime;
		{
			Pattern p = Pattern.compile("YrMonDate='(?<yyyy>2[0-9]{3})/(?<mm>[0-9]{2})/(?<dd>[0-9]{2}) (?<hhmmss>[0-9:]{8})';");
			Matcher m = p.matcher(string);
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

		String jsonString;
		{
			Pattern p = Pattern.compile(";FundData= (?<json>\\{.+\\});");
			Matcher m = p.matcher(string);
			if (m.find()) {
				jsonString = String.format("{\"map\": %s}", m.group("json").replace("[", "").replace("]", ""));
			} else {
				throw new UnexpectedException("no match PAT_FUNDDATA");
			}
		}
		RawData rawData = JSON.unmarshal(RawData.class, jsonString);

		List<Fund> list = new ArrayList<>();
		for(Map.Entry<String, FundData.JP90C00002U0> entry: rawData.map.entrySet()) {
			String                isinCode = entry.getKey();
			FundData.JP90C00002U0 fundData = entry.getValue();
			list.add(getInstance(dateTime, isinCode, fundData));
		}
		
		return list;
	}

	public static void main(String[] arsg) {
		logger.info("START");

		{
			List<Fund> list = updateList();
			
			logger.info("save {} {}", list.size(), Fund.getPath());
			Fund.save(list);
		}
		
		{
			List<Fund> list = Fund.getList();
			logger.info("list {}", list.size());
		}
		
		logger.info("STOP");
	}

}

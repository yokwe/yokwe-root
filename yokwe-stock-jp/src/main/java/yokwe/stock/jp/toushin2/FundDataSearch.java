package yokwe.stock.jp.toushin2;

import java.math.BigDecimal;

import yokwe.util.StringUtil;
import yokwe.util.json.JSON.Ignore;
import yokwe.util.json.JSON.Name;

public final class FundDataSearch {
    public static final class ResultInfo {
        public static final class DividendInfo {
			//"dividend" : 100,
            public BigDecimal dividend;
			//"setlDate" : "2022-07-11 00:00:00"
            public String     setlDate;

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public static final class InstitutionInfo {
    		//          "fdsInstCd" : "22560",
            public String     fdsInstCd;
    		//          "instName" : "松井証券",
            public String     instName;
    		//          "kanaName" : "マツイ",
            public String     kanaName;
    		//          "salesFee" : 0,
            public BigDecimal salesFee;
   		    //          "salesInstDiv" : "1"
            public String     salesInstDiv;

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public static final class SettlementInfo {
    		//          "setlDate" : "2017-07-10 00:00:00",
            public String     setlDate;
   		    //          "settleTermNo" : 2
            public BigDecimal settleTermNo;

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        
        // "isinCd" : "JP90C000DJ15",
        public String            isinCd;
        // "associFundCd" : "AE313167",
        public String            associFundCd;
        // "establishedDate" : "2016-07-29 00:00:00",
        public String            establishedDate;
		//  "redemptionDate" : "99999999",
        public String            redemptionDate;
		//  "setlFqcy" : "002",
        public String            setlFqcy;
        // "fundNm" : "あおぞら・グローバル・バランス・ファンド（部分為替ヘッジあり）",
        public String            fundNm;
        

        // 運用管理費用 (信託報酬)
		// "trustReward" : 0.925,
        public BigDecimal        trustReward;
        // 運用管理費用（信託報酬）運用会社
        // "entrustTrustReward" : 0.45,
        public BigDecimal        entrustTrustReward;
        // 運用管理費用（信託報酬）販売会社	
        // "bondTrustReward" : 0.45,
        public BigDecimal        bondTrustReward;
        // 運用管理費用（信託報酬） 信託銀行
        // "custodyTrustReward" : 0.025,
        public BigDecimal        custodyTrustReward;

        // 購入時手数料（上限）
        // "buyFee" : 0,
        public BigDecimal        buyFee;
        
        // "cancelLationFeeCd" : "1",
        public String            cancelLationFeeCd;
		//  "retentionMoneyCd" : "1",
        public String            retentionMoneyCd;
        
        // 決算日
		//    "setlDate" : "01/10, 07/10",
        public String            setlDate;

        // 基準価額 評価基準日
		//    "standardDate" : "2022-12-06 00:00:00",
        public String            standardDate;
        // 基準価額
		//    "standardPrice" : 12824,
        public BigDecimal        standardPrice;


        public DividendInfo[]    dividendInfo;
        public InstitutionInfo[] institutionInfo;

        
        // Field below are ignored
         
        // dividend
        @Ignore
//      "dividend1m" : null,
        public BigDecimal        dividend1m;
        @Ignore
//      "dividend1y" : 200,
        public BigDecimal        dividend1y;
        @Ignore
//      "dividendFrom" : null,
        public BigDecimal        dividendFrom;
        @Ignore
//      "dividendTo" : null,
        public BigDecimal        dividendTo;
        @Ignore
//      "dividendLast" : 100,
        public BigDecimal        dividendLast;
        @Ignore
//      "dividendLastSelDate" : "2022-07-11 00:00:00",
        public String            dividendLastSelDate;
       
        @Ignore
//      "entrustCmpNm" : "あおぞら投信",
        public String            entrustCmpNm;
        @Ignore
        // 運用年数
//      "establishedDateToNow" : "0077",
        public String            establishedDateToNow;
        
        @Ignore
//      "fundCategory" : "11",
        public String            fundCategory;
//      "fundNm" : "あおぞら・グローバル・バランス・ファンド（部分為替ヘッジあり）",
        @Ignore
//      "fundNkNm" : "星のしずく",
        public String            fundNkNm;
        @Ignore
//      "fundStNm" : "あおぞら・グローバル・バランスＦ（部分為替ヘッジあり）《星のしずく》",
        public String            fundStNm;
        @Ignore
        // 運用会社
//      "instCd" : "100AE",
        public String            instCd;
        @Ignore
//      "instName" : null,
        public String            instName;
        
        
        // 投資対象地域
        @Ignore
//      "investArea10kindCd1" : "1",
        public String            investArea10kindCd1;
        @Ignore
		//    "investArea10kindCd2" : "0",
        public String            investArea10kindCd2;
        @Ignore
		//    "investArea10kindCd3" : "0",
        public String            investArea10kindCd3;
        @Ignore
		//    "investArea10kindCd4" : "0",
        public String            investArea10kindCd4;
        @Ignore
		//    "investArea10kindCd5" : "0",
        public String            investArea10kindCd5;
        @Ignore
		//    "investArea10kindCd6" : "0",
        public String            investArea10kindCd6;
        @Ignore
		//    "investArea10kindCd7" : "0",
        public String            investArea10kindCd7;
        @Ignore
		//    "investArea10kindCd8" : "0",
        public String            investArea10kindCd8;
        @Ignore
		//    "investArea10kindCd9" : "0",
        public String            investArea10kindCd9;
        @Ignore
//      "investArea10kindCd10" : "0",
        public String            investArea10kindCd10;
        
        
        @Ignore
		//    "investArea3kindCd" : "3",
        public String            investArea3kindCd;
        @Ignore
		//    "investAssetKindCd" : "5",
        public String            investAssetKindCd;
        
        
        @Ignore
		//    "kanaName" : null,
        public String            kanaName;
        @Ignore
		//    "lastUpdYmd" : "2022-12-07 07:46:57",
        public String            lastUpdYmd;
        @Ignore
		//    "lastUpdater" : "wixjdmja",
        public String            lastUpdater;
        
        
        // 資金流出入
        @Ignore
		//    "monthlyCancelCreateVal" : -4,
        public BigDecimal        monthlyCancelCreateVal;
        @Ignore
		//    "monthlyCancelCreateVal3" : -5,
        public BigDecimal        monthlyCancelCreateVal3;
        @Ignore
		//    "monthlyCancelCreateVal6" : -4,
        public BigDecimal        monthlyCancelCreateVal6;
        @Ignore
		//    "monthlyCancelCreateVal9" : -6,
        public BigDecimal        monthlyCancelCreateVal9;
        @Ignore
		//    "monthlyCancelCreateVal12" : -10,
        public BigDecimal        monthlyCancelCreateVal12;
        
        
        @Ignore
		//    "myInstFundCd" : "0000110114",
        public String            myInstFundCd;
        @Ignore
		//    "nisaFlg" : "2",
        public String            nisaFlg;
        @Ignore
        //    償還までの期間
		//    "nowToRedemptionDate" : "9999",
        public String            nowToRedemptionDate;
        @Ignore
		//    "openDiv" : "0",
        public String            openDiv;
        @Ignore
		//    "renzokuCancelCreateValFlg" : -1,
        public BigDecimal        renzokuCancelCreateValFlg;
        @Ignore
		//    "repordNo" : "51222481",
        public String            repordNo;
        @Ignore
		//    "reportDiv" : "1",
        public String            reportDiv;
        @Ignore
		//    "reportUrl" : "http://www.aozora-im.co.jp/file/report/aaa7e8102cc7e83a4a58f494ec7b90e2496e3819.pdf",
        public String            reportUrl;
        @Ignore
		//    "returnRaY" : null,
        public BigDecimal        returnRaY;
        
        // リスク（標準偏差）
        @Ignore
		//    "riskRa6m" : 0.1186,
        public BigDecimal        riskRa6m;
       @Ignore
		//    "riskRa1y" : 0.0995,
        public BigDecimal        riskRa1y;
        @Ignore
		//    "riskRa3y" : 0.1139,
        public BigDecimal        riskRa3y;
        @Ignore
		//    "riskRa5y" : 0.1034,
        public BigDecimal        riskRa5y;
        @Ignore
		//    "riskRa10y" : null,
        public BigDecimal        riskRa10y;
        @Ignore
		//    "riskRa20y" : null,
        public BigDecimal        riskRa20y;
        
        @Ignore
 		//    "salesFee" : null,
        public BigDecimal        salesFee;
        @Ignore
        // 販売会社
		//    "salesInstDiv" : null,
        public BigDecimal        salesInstDiv;
        @Ignore
		//    "separateseparateDiv" : "0",
        public String            separateseparateDiv;
        @Ignore
        public SettlementInfo[]  settlementInfo;
        
        
        // シャープレシオ
        @Ignore
		//    "sharpRa6m" : 0.04,
        public BigDecimal        sharpRa6m;
        @Ignore
		//    "sharpRa1y" : 0.01,
        public BigDecimal        sharpRa1y;
        @Ignore
		//    "sharpRa3y" : 0.51,
        public BigDecimal        sharpRa3y;
        @Ignore
		//    "sharpRa5y" : 0.41,
        public BigDecimal        sharpRa5y;
        @Ignore
		//    "sharpRa10y" : null,
        public BigDecimal        sharpRa10y;
        @Ignore
		//    "sharpRa20y" : null,
        public BigDecimal        sharpRa20y;
        
                
        // 騰落率
        @Ignore
		//    "standardPriceRa6m" : -0.06,
        public BigDecimal        standardPriceRa6m;
        @Ignore
		//    "standardPriceRa1y" : -0.36,
        public BigDecimal        standardPriceRa1y;
        @Ignore
		//    "standardPriceRa3y" : 16.73,
        public BigDecimal        standardPriceRa3y;
        @Ignore
		//    "standardPriceRa5y" : 20.35,
        public BigDecimal        standardPriceRa5y;
        @Ignore
		//    "standardPriceRa10y" : null,
        public BigDecimal        standardPriceRa10y;
        @Ignore
		//    "standardPriceRa20y" : null,
        public BigDecimal        standardPriceRa20y;
        
        
        @Ignore
		//    "stdCostPointFrmDayBfr" : -16,
        public BigDecimal        stdCostPointFrmDayBfr;
        @Ignore
		//    "stdCostPointFrmDayBfrRt" : -0.12,
        public BigDecimal        stdCostPointFrmDayBfrRt;
        
        
        // インデックスファンド区分
        @Ignore
		//    "supplementKindCd" : "0",
        public String            supplementKindCd;
        @Ignore
        //    純資産総額(百万円)
		//    "totalNetAssets" : 126,
        public BigDecimal        totalNetAssets;
        @Ignore
		//    "unitOpenDiv" : "2"
        public String            unitOpenDiv;

        @Override
        public String toString() {
            return StringUtil.toString(this);
        }
    }

    public int          allPageNo;
    public int          draw;
    public int          pageSize;
    public int          recordsFiltered;
    public int          recordsTotal;
    @Name("resultInfoMapList")
    public ResultInfo[] resultInfoArray;
    public String       showRecordText;
    @Ignore
    public String       sortKey1;
    @Ignore
    public String       sortKey2;
    @Ignore
    public String       sortOrder1;
    @Ignore
    public String       sortOrder2;
    @Ignore
    public String       standardDate;
    public int          startNo;

    @Override
    public String toString() {
        return StringUtil.toString(this);
    }
}

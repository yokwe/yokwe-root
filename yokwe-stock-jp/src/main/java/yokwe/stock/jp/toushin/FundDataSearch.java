package yokwe.stock.jp.toushin;

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
            public String     fdsInstCd;
            public String     instName;
            public String     kanaName;
            public BigDecimal salesFee;
            public String     salesInstDiv;

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public static final class SettlementInfo {
            public String     setlDate;
            public BigDecimal settleTermNo;

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        @Ignore
        public String            associFundCd;
        @Ignore
        public BigDecimal        bondTrustReward;
        @Ignore
        public BigDecimal        buyFee;
        @Ignore
        public String            cancelLationFeeCd;
        @Ignore
        public BigDecimal        custodyTrustReward;
        @Ignore
        public BigDecimal        dividend1m;
        @Ignore
        public BigDecimal        dividend1y;
        @Ignore
        public BigDecimal        dividendFrom;
        @Ignore
        public DividendInfo[]    dividendInfo;
        @Ignore
        public BigDecimal        dividendLast;
        @Ignore
        public String            dividendLastSelDate;
        @Ignore
        public BigDecimal        dividendTo;
        @Ignore
        public String            entrustCmpNm;
        @Ignore
        public BigDecimal        entrustTrustReward;
        // "establishedDate" : "2016-07-29 00:00:00",
        public String            establishedDate;
        @Ignore
        public String            establishedDateToNow;
        @Ignore
        public String            fundCategory;
        @Ignore
        public String            fundNkNm;
        // "fundNm" : "あおぞら・グローバル・バランス・ファンド（部分為替ヘッジあり）",
        public String            fundNm;
        @Ignore
        public String            fundStNm;
        @Ignore
        public String            instCd;
        @Ignore
        public String            instName;
        @Ignore
        public InstitutionInfo[] institutionInfo;
        @Ignore
        public String            investArea10kindCd1;
        @Ignore
        public String            investArea10kindCd2;
        @Ignore
        public String            investArea10kindCd3;
        @Ignore
        public String            investArea10kindCd4;
        @Ignore
        public String            investArea10kindCd5;
        @Ignore
        public String            investArea10kindCd6;
        @Ignore
        public String            investArea10kindCd7;
        @Ignore
        public String            investArea10kindCd8;
        @Ignore
        public String            investArea10kindCd9;
        @Ignore
        public String            investArea10kindCd10;
        @Ignore
        public String            investArea3kindCd;
        @Ignore
        public String            investAssetKindCd;
        // "isinCd" : "JP90C000DJ15",
        public String            isinCd;
        @Ignore
        public String            kanaName;
        @Ignore
        public String            lastUpdYmd;
        @Ignore
        public String            lastUpdater;
        @Ignore
        public BigDecimal        monthlyCancelCreateVal;
        @Ignore
        public BigDecimal        monthlyCancelCreateVal3;
        @Ignore
        public BigDecimal        monthlyCancelCreateVal6;
        @Ignore
        public BigDecimal        monthlyCancelCreateVal9;
        @Ignore
        public BigDecimal        monthlyCancelCreateVal12;
        @Ignore
        public String            myInstFundCd;
        @Ignore
        public String            nisaFlg;
        @Ignore
        public String            nowToRedemptionDate;
        @Ignore
        public String            openDiv;
        @Ignore
        public String            redemptionDate;
        @Ignore
        public BigDecimal        renzokuCancelCreateValFlg;
        @Ignore
        public String            repordNo;
        @Ignore
        public String            reportDiv;
        @Ignore
        public String            reportUrl;
        @Ignore
        public String            retentionMoneyCd;
        @Ignore
        public BigDecimal        returnRaY;
        @Ignore
        public BigDecimal        riskRa1y;
        @Ignore
        public BigDecimal        riskRa3y;
        @Ignore
        public BigDecimal        riskRa5y;
        @Ignore
        public BigDecimal        riskRa10y;
        @Ignore
        public BigDecimal        riskRa20y;
        @Ignore
        public BigDecimal        riskRa6m;
        @Ignore
        public BigDecimal        salesFee;
        @Ignore
        public BigDecimal        salesInstDiv;
        @Ignore
        public String            separateseparateDiv;
        // "setlDate" : "01/25, 02/25, 03/25, 04/25, 05/25, 06/25, 07/25, 08/25, 09/25, 10/25, 11/25, 12/25",
        public String            setlDate;
        @Ignore
        public String            setlFqcy;
        @Ignore
        public SettlementInfo[]  settlementInfo;
        @Ignore
        public BigDecimal        sharpRa1y;
        @Ignore
        public BigDecimal        sharpRa3y;
        @Ignore
        public BigDecimal        sharpRa5y;
        @Ignore
        public BigDecimal        sharpRa10y;
        @Ignore
        public BigDecimal        sharpRa20y;
        @Ignore
        public BigDecimal        sharpRa6m;
        @Ignore
        public String            standardDate;
        @Ignore
        public BigDecimal        standardPrice;
        @Ignore
        public BigDecimal        standardPriceRa1y;
        @Ignore
        public BigDecimal        standardPriceRa3y;
        @Ignore
        public BigDecimal        standardPriceRa5y;
        @Ignore
        public BigDecimal        standardPriceRa10y;
        @Ignore
        public BigDecimal        standardPriceRa20y;
        @Ignore
        public BigDecimal        standardPriceRa6m;
        @Ignore
        public BigDecimal        stdCostPointFrmDayBfr;
        @Ignore
        public BigDecimal        stdCostPointFrmDayBfrRt;
        @Ignore
        public String            supplementKindCd;
        @Ignore
        public BigDecimal        totalNetAssets;
        @Ignore
        public BigDecimal        trustReward;
        @Ignore
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

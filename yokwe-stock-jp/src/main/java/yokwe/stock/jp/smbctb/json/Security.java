/*******************************************************************************
 * Copyright (c) 2020, Yasuhiro Hasegawa
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. The name of the author may not be used to endorse or promote products derived
 *      from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *******************************************************************************/
package yokwe.stock.jp.smbctb.json;

import java.math.BigDecimal;

import yokwe.util.StringUtil;
import yokwe.util.json.JSON.Name;

public final class Security {
    public static final class FundNetAssetValues {
        public @Name("CurrencyId")    String     currencyId;    // STRING STRING
        public @Name("DayEndDate")    String     dayEndDate;    // STRING DATE
        public @Name("DayEndValue")   BigDecimal dayEndValue;   // NUMBER REAL
        public @Name("MonthEndDate")  String     monthEndDate;  // STRING DATE
        public @Name("MonthEndValue") BigDecimal monthEndValue; // NUMBER REAL

        @Override
        public String toString() {
            return StringUtil.toString(this);
        }
    }

    public static final class NetAssetValues {
        public @Name("CurrencyId")    String     currencyId;    // STRING STRING
        public @Name("DayEndDate")    String     dayEndDate;    // STRING DATE
        public @Name("DayEndValue")   BigDecimal dayEndValue;   // NUMBER REAL
        public @Name("MonthEndDate")  String     monthEndDate;  // STRING DATE
        public @Name("MonthEndValue") BigDecimal monthEndValue; // NUMBER REAL

        @Override
        public String toString() {
            return StringUtil.toString(this);
        }
    }

    public static final class Custom {
        public @Name("CustomCategoryId")                String     customCategoryId;                // STRING INT
        public @Name("CustomCategoryIdName")            String     customCategoryIdName;            // STRING STRING
        public @Name("CustomCategoryId2")               String     customCategoryId2;               // STRING INT
        public @Name("CustomCategoryId2Name")           String     customCategoryId2Name;           // STRING STRING
        public @Name("CustomCategoryId3")               String     customCategoryId3;               // STRING INT
        public @Name("CustomCategoryId3Name")           String     customCategoryId3Name;           // STRING STRING
        public @Name("CustomCategoryId5")               String     customCategoryId5;               // STRING INT
        public @Name("CustomCategoryId5Name")           String     customCategoryId5Name;           // STRING STRING
        public @Name("CustomCategoryId6")               String     customCategoryId6;               // STRING INT
        public @Name("CustomCategoryId6Name")           String     customCategoryId6Name;           // STRING STRING
        public @Name("CustomBenchmarkName")             String     customBenchmarkName;             // STRING STRING
        public @Name("CustomExternalURL1")              String     customExternalURL1;              // STRING STRING
        public @Name("CustomExternalURL2")              String     customExternalURL2;              // STRING STRING
        public @Name("CustomExternalURL3")              String     customExternalURL3;              // STRING STRING
        public @Name("CustomExternalURL4")              String     customExternalURL4;              // STRING STRING
        public @Name("CustomExternalURL5")              String     customExternalURL5;              // STRING STRING
        public @Name("CustomClassification1")           String     customClassification1;           // STRING STRING
        public @Name("CustomClassification2")           String     customClassification2;           // STRING STRING
        public @Name("CustomClassification3")           String     customClassification3;           // STRING STRING
        public @Name("CustomClassification4")           String     customClassification4;           // STRING STRING
        public @Name("CustomInvestmentAdvisorComments") String     customInvestmentAdvisorComments; // STRING STRING
        public @Name("CustomExpenseText")               String     customExpenseText;               // STRING STRING
        public @Name("CustomFundName")                  String     customFundName;                  // STRING STRING
        public @Name("CustomMarketCommentary")          String     customMarketCommentary;          // STRING STRING
        public @Name("CustomNote1")                     String     customNote1;                     // STRING STRING
        public @Name("CustomNote2")                     String     customNote2;                     // STRING STRING
        public @Name("CustomNote3")                     String     customNote3;                     // STRING STRING
        public @Name("CustomNote4")                     String     customNote4;                     // STRING STRING
        public @Name("CustomNote5")                     String     customNote5;                     // STRING STRING
        public @Name("CustomDisclaimer")                String     customDisclaimer;                // STRING STRING
        public @Name("CustomValuationFrequency")        String     customValuationFrequency;        // STRING STRING
        public @Name("CustomFootnote")                  String     customFootnote;                  // STRING STRING
        public @Name("CustomFurtherInformation")        String     customFurtherInformation;        // STRING STRING
        public @Name("CustomBuyFeeNote")                String     customBuyFeeNote;                // STRING STRING
        public @Name("CustomSellFeeNote")               String     customSellFeeNote;               // STRING STRING
        public @Name("CustomPerformanceFeeNote")        String     customPerformanceFeeNote;        // STRING STRING
        public @Name("CustomEarlyRedemptionPeriodNote") String     customEarlyRedemptionPeriodNote; // STRING STRING
        public @Name("CustomInstitutionSecurityId")     String     customInstitutionSecurityId;     // STRING INT
        public @Name("CustomMinimumPurchaseAmount")     BigDecimal customMinimumPurchaseAmount;     // NUMBER REAL
        public @Name("CustomRisk")                      BigDecimal customRisk;                      // NUMBER INT
        public @Name("CustomShowBuyButton")             boolean    customShowBuyButton;             // BOOLEAN

        @Override
        public String toString() {
            return StringUtil.toString(this);
        }
    }

    public static final class Currency {
        public @Name("Id") String id; // STRING STRING

        @Override
        public String toString() {
            return StringUtil.toString(this);
        }
    }

    public static final class CategoryBroadAssetClass {
        public @Name("Id")   String id;   // STRING STRING
        public @Name("Name") String name; // STRING STRING

        @Override
        public String toString() {
            return StringUtil.toString(this);
        }
    }

    public static final class LastPrice {
        public static final class Currency {
            public @Name("Id") String id; // STRING STRING

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public @Name("Date")     String     date;     // STRING DATE
        public @Name("Value")    BigDecimal value;    // NUMBER REAL
        public @Name("Currency") Currency   currency; // OBJECT

        @Override
        public String toString() {
            return StringUtil.toString(this);
        }
    }

    public static final class TrailingPerformance {
        public static final class Return {
            public @Name("Value")      BigDecimal value;      // NUMBER REAL
            public @Name("Date")       String     date;       // STRING DATE
            public @Name("TimePeriod") String     timePeriod; // STRING STRING
            public @Name("Annualized") boolean    annualized; // BOOLEAN

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public @Name("CurrencyId") String   currencyId; // STRING STRING
        public @Name("Date")       String   date;       // STRING DATE
        public @Name("Type")       String   type;       // STRING STRING
        public @Name("ReturnType") String   returnType; // STRING STRING
        public @Name("Return")     Return[] return_;    // ARRAY 18

        @Override
        public String toString() {
            return StringUtil.toString(this);
        }
    }

    public static final class RiskStatistics {
        public static final class Alphas {
            public @Name("Value")      BigDecimal value;      // NUMBER REAL
            public @Name("Frequency")  String     frequency;  // STRING STRING
            public @Name("TimePeriod") String     timePeriod; // STRING STRING

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public static final class Betas {
            public @Name("Value")      BigDecimal value;      // NUMBER REAL
            public @Name("Frequency")  String     frequency;  // STRING STRING
            public @Name("TimePeriod") String     timePeriod; // STRING STRING

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public static final class RSquareds {
            public @Name("Value")      BigDecimal value;      // NUMBER REAL
            public @Name("Frequency")  String     frequency;  // STRING STRING
            public @Name("TimePeriod") String     timePeriod; // STRING STRING

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public static final class InformationRatios {
            public @Name("Value")      BigDecimal value;      // NUMBER REAL
            public @Name("Frequency")  String     frequency;  // STRING STRING
            public @Name("TimePeriod") String     timePeriod; // STRING STRING

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public static final class TrackingErrors {
            public @Name("Value")      BigDecimal value;      // NUMBER REAL
            public @Name("Frequency")  String     frequency;  // STRING STRING
            public @Name("TimePeriod") String     timePeriod; // STRING STRING

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public static final class SortinoRatios {
            public @Name("Value")      BigDecimal value;      // NUMBER REAL
            public @Name("Frequency")  String     frequency;  // STRING STRING
            public @Name("TimePeriod") String     timePeriod; // STRING STRING

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public static final class StandardDeviations {
            public @Name("Value")      BigDecimal value;      // NUMBER REAL
            public @Name("Frequency")  String     frequency;  // STRING STRING
            public @Name("TimePeriod") String     timePeriod; // STRING STRING

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public static final class SharpeRatios {
            public @Name("Frequency")  String     frequency;  // STRING STRING
            public @Name("TimePeriod") String     timePeriod; // STRING STRING
            public @Name("Value")      BigDecimal value;      // NUMBER REAL

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public static final class MaximumReturns {
            public @Name("Value")      BigDecimal value;      // NUMBER REAL
            public @Name("Frequency")  String     frequency;  // STRING STRING
            public @Name("TimePeriod") String     timePeriod; // STRING STRING
            public @Name("Date")       String     date;       // STRING STRING

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public static final class MinimumReturns {
            public @Name("Value")      BigDecimal value;      // NUMBER REAL
            public @Name("Frequency")  String     frequency;  // STRING STRING
            public @Name("TimePeriod") String     timePeriod; // STRING STRING
            public @Name("Date")       String     date;       // STRING STRING

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public static final class NumberOfPositives {
            public @Name("Count")      BigDecimal count;      // NUMBER REAL
            public @Name("Frequency")  String     frequency;  // STRING STRING
            public @Name("TimePeriod") String     timePeriod; // STRING STRING

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public static final class NumberOfNegatives {
            public @Name("Count")      BigDecimal count;      // NUMBER REAL
            public @Name("Frequency")  String     frequency;  // STRING STRING
            public @Name("TimePeriod") String     timePeriod; // STRING STRING

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public @Name("CurrencyId")         String               currencyId;         // STRING STRING
        public @Name("CurrencyOption")     String               currencyOption;     // STRING STRING
        public @Name("Date")               String               date;               // STRING STRING
        public @Name("Type")               String               type;               // STRING STRING
        public @Name("ReturnType")         String               returnType;         // STRING STRING
        public @Name("Alphas")             Alphas[]             alphas;             // ARRAY 3
        public @Name("Betas")              Betas[]              betas;              // ARRAY 3
        public @Name("RSquareds")          RSquareds[]          rSquareds;          // ARRAY 3
        public @Name("InformationRatios")  InformationRatios[]  informationRatios;  // ARRAY 3
        public @Name("TrackingErrors")     TrackingErrors[]     trackingErrors;     // ARRAY 3
        public @Name("SortinoRatios")      SortinoRatios[]      sortinoRatios;      // ARRAY 3
        public @Name("StandardDeviations") StandardDeviations[] standardDeviations; // ARRAY 3
        public @Name("SharpeRatios")       SharpeRatios[]       sharpeRatios;       // ARRAY 3
        public @Name("MaximumReturns")     MaximumReturns[]     maximumReturns;     // ARRAY 3
        public @Name("MinimumReturns")     MinimumReturns[]     minimumReturns;     // ARRAY 3
        public @Name("NumberOfPositives")  NumberOfPositives[]  numberOfPositives;  // ARRAY 3
        public @Name("NumberOfNegatives")  NumberOfNegatives[]  numberOfNegatives;  // ARRAY 3

        @Override
        public String toString() {
            return StringUtil.toString(this);
        }
    }

    public @Name("dbgtime")                 String                  dbgtime;                 // STRING INT
    public @Name("Id")                      String                  id;                      // STRING STRING
    public @Name("InceptionDate")           String                  inceptionDate;           // STRING STRING
    public @Name("Isin")                    String                  isin;                    // STRING STRING
    public @Name("InvestmentType")          String                  investmentType;          // STRING STRING
    public @Name("HoldingType")             String                  holdingType;             // STRING INT
    public @Name("Type")                    String                  type;                    // STRING STRING
    public @Name("Name")                    String                  name;                    // STRING STRING
    public @Name("FundNetAssetValues")      FundNetAssetValues[]    fundNetAssetValues;      // ARRAY 1
    public @Name("NetAssetValues")          NetAssetValues[]        netAssetValues;          // ARRAY 1
    public @Name("Domicile")                String                  domicile;                // STRING STRING
    public @Name("NetChange")               BigDecimal              netChange;               // NUMBER REAL
    public @Name("NetChangePercent")        BigDecimal              netChangePercent;        // NUMBER REAL
    public @Name("Custom")                  Custom                  custom;                  // OBJECT
    public @Name("Currency")                Currency                currency;                // OBJECT
    public @Name("CategoryBroadAssetClass") CategoryBroadAssetClass categoryBroadAssetClass; // OBJECT
    public @Name("LastPrice")               LastPrice               lastPrice;               // OBJECT
    public @Name("LegalStructure")          String                  legalStructure;          // STRING STRING
    public @Name("TrailingPerformance")     TrailingPerformance[]   trailingPerformance;     // ARRAY 1
    public @Name("RiskStatistics")          RiskStatistics[]        riskStatistics;          // ARRAY 1

    @Override
    public String toString() {
        return StringUtil.toString(this);
    }
}


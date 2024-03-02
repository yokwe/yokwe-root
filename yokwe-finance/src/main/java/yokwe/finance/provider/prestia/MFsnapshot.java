package yokwe.finance.provider.prestia;

import java.io.File;
import java.math.BigDecimal;
import java.util.regex.Pattern;

import yokwe.util.UnexpectedException;
import yokwe.util.json.JSON;

public class MFsnapshot {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_PATTERN = "https://gllt.morningstar.com/api/rest.svc/smbctbfund/security_details/%s?viewId=MFsnapshot&idtype=msid&responseViewFormat=json";
	public static String getURL(String secId) {
		return String.format(URL_PATTERN, secId);
	}
	private static String PREFIX = "fund";
	public static File getFile(String secId) {
		return UpdateTradingFundPrestia.storage.getFile(PREFIX, secId + ".json");
	}
	
	public static MFsnapshot getInstance(String secId) {
		var url    = getURL(secId);
		var file   = getFile(secId);
		var string = UpdateTradingFundPrestia.download(url, Screener.CHARSET, file, UpdateTradingFundPrestia.DEBUG_USE_FILE);
		
		var list = JSON.getList(MFsnapshot.class, string);
		if (list.size() != 1) {
			logger.error("Unepxected list");
			logger.error("  secId   {}", secId);
			logger.error("  list    {}", list .size());
			logger.error("  string  {}", string);
			throw new UnexpectedException("Unepxected list");
		}
		return list.get(0);
	}
	
    @JSON.Name("dbgtime")                  @JSON.Ignore   public String dbgtime;
    @JSON.Name("Id")                       @JSON.Ignore   public String id;
    @JSON.Name("InceptionDate")            @JSON.Ignore   public String inceptionDate;
    @JSON.Name("Isin")                     @JSON.Optional public String isin;
    @JSON.Name("InvestmentType")           @JSON.Ignore   public String investmentType;
    @JSON.Name("HoldingType")              @JSON.Ignore   public String holdingType;
    @JSON.Name("IndexFund")                @JSON.Ignore   public String indexFund;
    @JSON.Name("Type")                     @JSON.Ignore   public String type;
    @JSON.Name("Name")                                    public String name;
    @JSON.Name("LegalName")                @JSON.Ignore   public String legalName;
    @JSON.Name("NetAssetValues")           @JSON.Ignore   public String netAssetValues;
    @JSON.Name("Domicile")                 @JSON.Ignore   public String domicile;
    @JSON.Name("NetChange")                @JSON.Ignore   public String netChange;
    @JSON.Name("NetChangePercent")         @JSON.Ignore   public String netChangePercent;
    @JSON.Name("LatestDistribution")       @JSON.Ignore   public String latestDistribution;
    @JSON.Name("Custom")                                 public Custom custom;
    @JSON.Name("Currency")                 @JSON.Ignore   public String currency;
    @JSON.Name("CategoryBroadAssetClass")  @JSON.Ignore   public String categoryBroadAssetClass;
    @JSON.Name("RiskAndRating")            @JSON.Ignore   public String riskAndRating;
    @JSON.Name("LastPrice")                @JSON.Ignore   public String lastPrice;
    @JSON.Name("LegalStructure")           @JSON.Ignore   public String legalStructure;
    @JSON.Name("TrailingPerformance")      @JSON.Ignore   public String trailingPerformance;
    @JSON.Name("RiskStatistics")           @JSON.Ignore   public String riskStatistics;
    @JSON.Name("GrowthOf10K")              @JSON.Ignore   public String growthOf10K;
    @JSON.Name("Portfolios")               @JSON.Ignore   public String portfolios;
    @JSON.Name("FundNetAssetValues")       @JSON.Ignore   public String fundNetAssetValues;
    
    
    private static final Pattern PAT_PERCENT = Pattern.compile("(?<percent>[0-9]+\\.[0-9]+)[%％]");
    public BigDecimal getBuyFee() {
    	BigDecimal buyFee;
    	
		var buyFeeNotes = custom.customBuyFeeNote;
		if (buyFeeNotes.equals("なし")) {
			buyFee = BigDecimal.ZERO;
		} else {
			var m = PAT_PERCENT.matcher(buyFeeNotes);
			if (m.find()) {
				buyFee = new BigDecimal(m.group("percent")).movePointLeft(2);
			} else {
				logger.error("Unexpected buyFeeNotes");
				logger.error("  id           {}", id);
				logger.error("  isin         {}", isin);
				logger.error("  buyFeeNotes  {}", buyFeeNotes);
				throw new UnexpectedException("Unexpected buyFeeNotes");
			}
		}
		return buyFee;
    }
    
    @Override
    public String toString() {
    	return String.format("{%s  %s  %s}", isin, custom.toString(), name);
    }
    
	public static class Custom {
	      @JSON.Name("CustomCategoryId")                 @JSON.Ignore   public String customCategoryId;
	      @JSON.Name("CustomCategoryIdName")             @JSON.Ignore   public String customCategoryIdName;
	      @JSON.Name("CustomCategoryId2")                @JSON.Ignore   public String customCategoryId2;
	      @JSON.Name("CustomCategoryId2Name")            @JSON.Ignore   public String customCategoryId2Name;
	      @JSON.Name("CustomCategoryId3")                @JSON.Ignore   public String customCategoryId3;
	      @JSON.Name("CustomCategoryId3Name")            @JSON.Ignore   public String customCategoryId3Name;
	      @JSON.Name("CustomCategoryId5")                @JSON.Ignore   public String customCategoryId5;
	      @JSON.Name("CustomCategoryId5Name")            @JSON.Ignore   public String customCategoryId5Name;
	      @JSON.Name("CustomCategoryId6")                @JSON.Ignore   public String customCategoryId6;
	      @JSON.Name("CustomCategoryId6Name")            @JSON.Ignore   public String customCategoryId6Name;
	      @JSON.Name("CustomCategoryId7")                @JSON.Ignore   public String customCategoryId7;
	      @JSON.Name("CustomCategoryId7Name")            @JSON.Ignore   public String customCategoryId7Name;
	      @JSON.Name("CustomCategoryId8")                @JSON.Ignore   public String customCategoryId8;
	      @JSON.Name("CustomCategoryId8Name")            @JSON.Ignore   public String customCategoryId8Name;
	      @JSON.Name("CustomCategoryId9")                @JSON.Ignore   public String customCategoryId9;
	      @JSON.Name("CustomCategoryId9Name")            @JSON.Ignore   public String customCategoryId9Name;
	      @JSON.Name("CustomBenchmarkName")              @JSON.Ignore   public String customBenchmarkName;
	      @JSON.Name("CustomExternalURL1")               @JSON.Ignore   public String customExternalURL1;
	      @JSON.Name("CustomExternalURL2")               @JSON.Ignore   public String customExternalURL2;
	      @JSON.Name("CustomExternalURL3")               @JSON.Ignore   public String customExternalURL3;
	      @JSON.Name("CustomExternalURL4")               @JSON.Ignore   public String customExternalURL4;
	      @JSON.Name("CustomExternalURL5")               @JSON.Ignore   public String customExternalURL5;
	      @JSON.Name("CustomExternalURL6")               @JSON.Ignore   public String customExternalURL6;
	      @JSON.Name("CustomExternalURL7")               @JSON.Ignore   public String customExternalURL7;
	      @JSON.Name("CustomClassification1")            @JSON.Ignore   public String customClassification1;
	      @JSON.Name("CustomClassification2")            @JSON.Ignore   public String customClassification2;
	      @JSON.Name("CustomClassification3")            @JSON.Ignore   public String customClassification3;
	      @JSON.Name("CustomClassification4")            @JSON.Ignore   public String customClassification4;
	      @JSON.Name("CustomInvestmentAdvisorComments")  @JSON.Ignore   public String customInvestmentAdvisorComments;
	      @JSON.Name("CustomExpenseText")                @JSON.Ignore   public String customExpenseText;
	      @JSON.Name("CustomFundName")                   @JSON.Ignore   public String customFundName;
	      @JSON.Name("CustomMarketCommentary")           @JSON.Ignore   public String customMarketCommentary;
	      @JSON.Name("CustomNote1")                      @JSON.Ignore   public String customNote1;
	      @JSON.Name("CustomNote2")                      @JSON.Ignore   public String customNote2;
	      @JSON.Name("CustomNote3")                      @JSON.Ignore   public String customNote3;
	      @JSON.Name("CustomNote4")                      @JSON.Ignore   public String customNote4;
	      @JSON.Name("CustomNote5")                      @JSON.Ignore   public String customNote5;
	      @JSON.Name("CustomDisclaimer")                 @JSON.Ignore   public String customDisclaimer;
	      @JSON.Name("CustomValuationFrequency")         @JSON.Ignore   public String customValuationFrequency;
	      @JSON.Name("CustomFootnote")                   @JSON.Ignore   public String customFootnote;
	      @JSON.Name("CustomFurtherInformation")         @JSON.Ignore   public String customFurtherInformation;
	      @JSON.Name("CustomBuyFeeNote")                                public String customBuyFeeNote;
	      @JSON.Name("CustomSellFeeNote")                @JSON.Ignore   public String customSellFeeNote;
	      @JSON.Name("CustomPerformanceFeeNote")         @JSON.Ignore   public String customPerformanceFeeNote;
	      @JSON.Name("CustomEarlyRedemptionPeriodNote")  @JSON.Ignore   public String customEarlyRedemptionPeriodNote;
	      
	      // CustomValue7
	      @JSON.Name("CustomValue1")                     @JSON.Ignore   public String customValue1;
	      @JSON.Name("CustomValue2")                     @JSON.Ignore   public String customValue2;
	      @JSON.Name("CustomValue3")                     @JSON.Ignore   public String customValue3;
	      @JSON.Name("CustomValue4")                     @JSON.Ignore   public String customValue4;
	      @JSON.Name("CustomValue5")                     @JSON.Ignore   public String customValue5;
	      @JSON.Name("CustomValue6")                     @JSON.Ignore   public String customValue6;
	      @JSON.Name("CustomValue7")                     @JSON.Ignore   public String customValue7;
	      @JSON.Name("CustomValue8")                     @JSON.Ignore   public String customValue8;
	      @JSON.Name("CustomValue9")                     @JSON.Ignore   public String customValue9;
	      @JSON.Name("CustomValue10")                    @JSON.Ignore   public String customValue10;
	      @JSON.Name("CustomValue11")                    @JSON.Ignore   public String customValue11;
	      @JSON.Name("CustomValue12")                    @JSON.Ignore   public String customValue12;
	      @JSON.Name("CustomInstitutionSecurityId")      @JSON.Ignore   public String customInstitutionSecurityId;
	      @JSON.Name("CustomMinimumPurchaseAmount")      @JSON.Ignore   public String customMinimumPurchaseAmount;
	      @JSON.Name("CustomRisk")                       @JSON.Ignore   public String customRisk;
	      @JSON.Name("CustomShowBuyButton")              @JSON.Ignore   public String customShowBuyButton;
	      @JSON.Name("CustomDistributionStatus")         @JSON.Ignore   public String customDistributionStatus;
	      @JSON.Name("CustomIsRecommended")              @JSON.Ignore   public String customIsRecommended;
	      
	      @Override
	      public String toString() {
	    	  return String.format("{%s}", customBuyFeeNote);
	      }
	}
}
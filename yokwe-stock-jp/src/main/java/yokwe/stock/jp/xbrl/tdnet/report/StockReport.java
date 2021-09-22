package yokwe.stock.jp.xbrl.tdnet.report;

import static yokwe.stock.jp.xbrl.tdnet.inline.Context.ANNUAL_MEMBER;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.CONSOLIDATED_MEMBER;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.CURRENT_YEAR_DURATION;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.FIRST_QUARTER_MEMBER;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.FORECAST_MEMBER;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.LOWER_MEMBER;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.NON_CONSOLIDATED_MEMBER;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.PRIOR_ACCUMULATED_Q_1_DURATION;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.PRIOR_ACCUMULATED_Q_2_DURATION;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.PRIOR_ACCUMULATED_Q_3_DURATION;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.PRIOR_ACCUMULATED_Q_3_INSTANT;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.PRIOR_YEAR_DURATION;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.PRIOR_YEAR_INSTANT;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.RESULT_MEMBER;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.SECOND_QUARTER_MEMBER;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.THIRD_QUARTER_MEMBER;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.UPPER_MEMBER;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.YEAR_END_MEMBER;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_ED_T_LABEL.ANNUAL_SECURITIES_REPORT_FILING_DATE_AS_PLANNED;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_ED_T_LABEL.COMPANY_NAME;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_ED_T_LABEL.DATE_OF_GENERAL_SHAREHOLDERS_MEETING_AS_PLANNED;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_ED_T_LABEL.DIVIDEND_PAYABLE_DATE_AS_PLANNED;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_ED_T_LABEL.DIVIDEND_PER_SHARE;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_ED_T_LABEL.FILING_DATE;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_ED_T_LABEL.FISCAL_YEAR_END;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_ED_T_LABEL.NET_INCOME_PER_SHARE;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_ED_T_LABEL.NET_SALES;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_ED_T_LABEL.NUMBER_OF_ISSUED_AND_OUTSTANDING_SHARES_AT_THE_END_OF_FISCAL_YEAR_INCLUDING_TREASURY_STOCK;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_ED_T_LABEL.QUARTERLY_PERIOD;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_ED_T_LABEL.SECURITIES_CODE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.jp.tdnet.SummaryFilename;
import yokwe.stock.jp.xbrl.inline.BaseElement;
import yokwe.stock.jp.xbrl.inline.Document;
import yokwe.stock.jp.xbrl.tdnet.TDNET;
import yokwe.util.CSVUtil;
import yokwe.util.CSVUtil.ColumnName;
import yokwe.util.UnexpectedException;

public class StockReport extends BaseReport implements Comparable<StockReport> {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StockReport.class);

	public static final String PATH_FILE = TDNET.getPath("stock-report.csv");

	public static List<StockReport> getList() {
		List<StockReport> ret = CSVUtil.read(StockReport.class).file(PATH_FILE);
		return (ret == null) ? new ArrayList<>() : ret;
	}
	public static Map<String, StockReport> getMap() {
		Map<String, StockReport> ret = new TreeMap<>();
		for(StockReport e: getList()) {
			String key = e.filename;
			if (ret.containsKey(key)) {
				logger.error("Duplicate key {}", key);
				logger.error("  new {}", e);
				logger.error("  old {}", ret.get(key));
				throw new UnexpectedException("Duplicate key");
			} else {
				ret.put(key, e);
			}
		}
		return ret;
	}
	
	public static void save(Collection<StockReport> collection) {
		List<StockReport> list = new ArrayList<>(collection);
		save(list);
	}
	public static void save(List<StockReport> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(StockReport.class).file(PATH_FILE, list);
	}
	

	@TSE_ED(label = FILING_DATE,
			acceptNullOrEmpty = true)
	@ColumnName("提出日")
	public String filingDate;
	
	@TSE_ED(label = COMPANY_NAME)
	@ColumnName("会社名")
	public String companyName;
	
	@TSE_ED(label = SECURITIES_CODE)
	@ColumnName("コード番号")
	public String stockCode;
	
	@TSE_ED(label = FISCAL_YEAR_END)
	@ColumnName("決算期")
	public String yearEnd;
	
	@TSE_ED(label = QUARTERLY_PERIOD,
			acceptNullOrEmpty = true)
	@ColumnName("四半期")
	public Integer quarterlyPeriod;
	
	// DATE_OF_GENERAL_SHAREHOLDERS_MEETING_AS_PLANNED
	@TSE_ED(label = DATE_OF_GENERAL_SHAREHOLDERS_MEETING_AS_PLANNED,
			acceptNullOrEmpty = true)
	@ColumnName("株主総会")
	public String dateOfGeneralShareholdersMeetingAsPlanned;

	@TSE_ED(label = ANNUAL_SECURITIES_REPORT_FILING_DATE_AS_PLANNED,
			acceptNullOrEmpty = true)
	@ColumnName("有価証券報告書")
	public String annualSecuritiesReportFilingDateAsPlanned;
	
	@TSE_ED(label = DIVIDEND_PAYABLE_DATE_AS_PLANNED,
			acceptNullOrEmpty = true)
	@ColumnName("配当支払日")
	public String dividendPayableDateAsPlanned;

	@TSE_ED(label = DIVIDEND_PER_SHARE,
			contextIncludeAll = {CURRENT_YEAR_DURATION, FIRST_QUARTER_MEMBER},
			contextExcludeAny = {LOWER_MEMBER, UPPER_MEMBER},
			acceptNullOrEmpty = true)
	@ColumnName("配当Q1")
	public BigDecimal dividendPerShareQ1; // PriorYearDuration/CurrentYearDuration FirstQuarterMember/SecondQuarterMember/ThirdQuarterMember/YearEndMember/AnnualMember

	@TSE_ED(label = DIVIDEND_PER_SHARE,
			contextIncludeAll = {CURRENT_YEAR_DURATION, SECOND_QUARTER_MEMBER},
			contextExcludeAny = {LOWER_MEMBER, UPPER_MEMBER},
			acceptNullOrEmpty = true)
	@ColumnName("配当Q2")
	public BigDecimal dividendPerShareQ2; // PriorYearDuration/CurrentYearDuration FirstQuarterMember/SecondQuarterMember/ThirdQuarterMember/YearEndMember/AnnualMember

	@TSE_ED(label = DIVIDEND_PER_SHARE,
			contextIncludeAll = {CURRENT_YEAR_DURATION, THIRD_QUARTER_MEMBER},
			contextExcludeAny = {LOWER_MEMBER, UPPER_MEMBER},
			acceptNullOrEmpty = true)
	@ColumnName("配当Q3")
	public BigDecimal dividendPerShareQ3; // PriorYearDuration/CurrentYearDuration FirstQuarterMember/SecondQuarterMember/ThirdQuarterMember/YearEndMember/AnnualMember

	@TSE_ED(label = DIVIDEND_PER_SHARE,
			contextIncludeAll = {CURRENT_YEAR_DURATION, YEAR_END_MEMBER},
			contextExcludeAny = {LOWER_MEMBER, UPPER_MEMBER},
			acceptNullOrEmpty = true)
	@ColumnName("配当Q4")
	public BigDecimal dividendPerShareQ4; // PriorYearDuration/CurrentYearDuration FirstQuarterMember/SecondQuarterMember/ThirdQuarterMember/YearEndMember/AnnualMember

	@ColumnName("配当")
	public BigDecimal dividendPerShare;

	@TSE_ED(label = DIVIDEND_PER_SHARE,
			contextIncludeAll = {CURRENT_YEAR_DURATION, ANNUAL_MEMBER, FORECAST_MEMBER},
			acceptNullOrEmpty = true)
	@ColumnName("配当年間予想")
	public BigDecimal annualDividendPerShareForeast; // PriorYearDuration/CurrentYearDuration FirstQuarterMember/SecondQuarterMember/ThirdQuarterMember/YearEndMember/AnnualMember

	// CurrentYearDuration_AnnualMember_NonConsolidatedMember_ResultMember
	@TSE_ED(label = DIVIDEND_PER_SHARE,
			contextIncludeAll = {CURRENT_YEAR_DURATION, ANNUAL_MEMBER, RESULT_MEMBER},
			acceptNullOrEmpty = true)
	@ColumnName("配当年間実績")
	public BigDecimal annualDividendPerShareResult; // PriorYearDuration/CurrentYearDuration FirstQuarterMember/SecondQuarterMember/ThirdQuarterMember/YearEndMember/AnnualMember	

	@ColumnName("配当年間")
	public BigDecimal annualDividendPerShare;
	
	
	@TSE_ED(label = NET_SALES,
			contextIncludeAll = {RESULT_MEMBER, CONSOLIDATED_MEMBER},
			contextExcludeAny = {PRIOR_ACCUMULATED_Q_1_DURATION, PRIOR_ACCUMULATED_Q_2_DURATION, PRIOR_ACCUMULATED_Q_3_DURATION, PRIOR_YEAR_DURATION},
			acceptNullOrEmpty = true)
	@ColumnName("売上高連結")
	public BigDecimal netSalesConsolidated;	

	@TSE_ED(label = NET_SALES,
			contextIncludeAll = {RESULT_MEMBER, NON_CONSOLIDATED_MEMBER},
			contextExcludeAny = {PRIOR_ACCUMULATED_Q_1_DURATION, PRIOR_ACCUMULATED_Q_2_DURATION, PRIOR_ACCUMULATED_Q_3_DURATION, PRIOR_YEAR_DURATION},
			acceptNullOrEmpty = true)
	@ColumnName("売上高非連結")
	public BigDecimal netSalesNonCosolidated;	

	@TSE_ED(label = NET_INCOME_PER_SHARE,
			contextIncludeAll = {RESULT_MEMBER, CONSOLIDATED_MEMBER},
			contextExcludeAny = {PRIOR_ACCUMULATED_Q_1_DURATION, PRIOR_ACCUMULATED_Q_2_DURATION, PRIOR_ACCUMULATED_Q_3_DURATION, PRIOR_YEAR_DURATION},
			acceptNullOrEmpty = true)
	@ColumnName("純利益連結") // 1株当たり当期純利益
	public BigDecimal netIncomPerShareConsolidated;	

	@TSE_ED(label = NET_INCOME_PER_SHARE,
			contextIncludeAll = {RESULT_MEMBER, NON_CONSOLIDATED_MEMBER},
			contextExcludeAny = {PRIOR_ACCUMULATED_Q_1_DURATION, PRIOR_ACCUMULATED_Q_2_DURATION, PRIOR_ACCUMULATED_Q_3_DURATION, PRIOR_YEAR_DURATION},
			acceptNullOrEmpty = true)
	@ColumnName("純利益非連結") // 1株当たり当期純利益
	public BigDecimal netIncomPerShareNonCosolidated;	

	@TSE_ED(label = NUMBER_OF_ISSUED_AND_OUTSTANDING_SHARES_AT_THE_END_OF_FISCAL_YEAR_INCLUDING_TREASURY_STOCK,
			contextIncludeAll = {RESULT_MEMBER},
			contextExcludeAny = {PRIOR_ACCUMULATED_Q_3_INSTANT, PRIOR_YEAR_INSTANT},
			acceptNullOrEmpty = true)
	@ColumnName("株式数") // 期末発行済株式数（自己株式を含む）
	public BigDecimal numberOfShares;


	public String filename;

	public static StockReport getInstance(Document document) {
		StockReport ret = BaseReport.getInstance(StockReport.class, document);

		switch(ret.quarterlyPeriod) {
		case 0:
			ret.quarterlyPeriod        = 4;
			ret.dividendPerShare       = ret.dividendPerShareQ4;
			ret.annualDividendPerShare = ret.annualDividendPerShareResult;
			break;
		case 1:
			ret.dividendPerShare       = ret.dividendPerShareQ1;
			ret.annualDividendPerShare = ret.annualDividendPerShareForeast;
			break;
		case 2:
			ret.dividendPerShare       = ret.dividendPerShareQ2;
			ret.annualDividendPerShare = ret.annualDividendPerShareForeast;
			break;
		case 3:
			ret.dividendPerShare       = ret.dividendPerShareQ3;
			ret.annualDividendPerShare = ret.annualDividendPerShareForeast;
			break;
		default:
			logger.error("Unexpected quarterlyPeriod {}", ret.quarterlyPeriod);
			throw new UnexpectedException("Unexpected quarterlyPeriod");
		}
		
		// FIXME If annual dividend is zero, calculate annual from q1-4
		if (ret.annualDividendPerShare.equals(BigDecimal.ZERO)) {
			ret.annualDividendPerShare = ret.dividendPerShareQ1.add(ret.dividendPerShareQ2).add(ret.dividendPerShareQ3).add(ret.dividendPerShareQ4);
		}
		
		SummaryFilename summaryFileName = SummaryFilename.getInstance(ret.filename);
		if (!ret.stockCode.equals(summaryFileName.tdnetCode)) {
			logger.warn("fix wrong stockCode  stockCode {}  tdnetCode {}  file ", ret.stockCode, summaryFileName.tdnetCode, ret.filename);
			ret.stockCode = summaryFileName.tdnetCode;
		}

		ret.stockCode = BaseElement.normalizeNumberCharacter(ret.stockCode);

		// Sanity check
		if (ret.filingDate.isEmpty()) {
			SummaryFilename filename = SummaryFilename.getInstance(document.filename);
			String yyyy = filename.id.substring(0, 4);
			String mm   = filename.id.substring(4, 6);
			String dd   = filename.id.substring(6, 8);
			ret.filingDate = String.format("%s-%s-%s", yyyy, mm, dd);
			logger.warn("filingDate is empty. Create filingDate from filename  {}", document.filename);
		}

		return ret;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s %s %d %s %s %s %s",
			stockCode, filingDate, yearEnd, quarterlyPeriod, annualSecuritiesReportFilingDateAsPlanned,
			annualDividendPerShareForeast, annualDividendPerShareResult, companyName);
	}

	// Define natural ordering of DividendBriefReport
	@Override
	public int compareTo(StockReport that) {
		int ret = this.stockCode.compareTo(that.stockCode);
		if (ret == 0) ret = this.yearEnd.compareTo(that.yearEnd);
		if (ret == 0) ret = this.quarterlyPeriod - that.quarterlyPeriod;
		if (ret == 0) ret = this.filingDate.compareTo(that.filingDate);
		return ret;
	}
}

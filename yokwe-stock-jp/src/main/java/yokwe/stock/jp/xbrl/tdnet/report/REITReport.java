package yokwe.stock.jp.xbrl.tdnet.report;

import static yokwe.stock.jp.xbrl.tdnet.inline.Context.CURRENT_YEAR_DURATION;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.CURRENT_YEAR_INSTANT;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.FORECAST_MEMBER;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.NEXT_2_YEAR_DURATION;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.NEXT_YEAR_DURATION;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.RESULT_MEMBER;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_RE_T_LABEL.DISTRIBUTIONS_IN_EXCESS_OF_PROFIT_PER_UNIT_REIT;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_RE_T_LABEL.DISTRIBUTIONS_PAYABLE_DATE_AS_PLANNED_REIT;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_RE_T_LABEL.DISTRIBUTIONS_PER_UNIT_EXCLUDING_DISTRIBUTIONS_IN_EXCESS_OF_PROFIT_REIT;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_RE_T_LABEL.FILING_DATE;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_RE_T_LABEL.FISCAL_YEAR_END;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_RE_T_LABEL.ISSUER_NAME_REIT;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_RE_T_LABEL.NET_INCOME_PER_UNIT_REIT;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_RE_T_LABEL.NUMBER_OF_INVESTMENT_UNITS_INCLUDING_TREASURY_INVESTMENT_UNITS_ISSUED_AT_END_OF_PERIOD_REIT;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_RE_T_LABEL.OPERATING_REVENUES_REIT;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_RE_T_LABEL.PAYOUT_RATIO;
import static yokwe.stock.jp.xbrl.tdnet.taxonomy.TSE_RE_T_LABEL.SECURITIES_CODE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import yokwe.stock.jp.tdnet.SummaryFilename;
import yokwe.stock.jp.xbrl.inline.BaseElement;
import yokwe.stock.jp.xbrl.inline.Document;
import yokwe.stock.jp.xbrl.tdnet.TDNET;
import yokwe.util.CSVUtil;
import yokwe.util.CSVUtil.ColumnName;

public class REITReport extends BaseReport implements Comparable<REITReport> {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(REITReport.class);
	
	public static final String PATH_FILE = TDNET.getPath("reit-report.csv");

	public static List<REITReport> getList() {
		List<REITReport> ret = CSVUtil.read(REITReport.class).file(PATH_FILE);
		return (ret == null) ? new ArrayList<>() : ret;
	}
	
	public static void save(Collection<REITReport> collection) {
		List<REITReport> list = new ArrayList<>(collection);
		save(list);
	}
	public static void save(List<REITReport> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(REITReport.class).file(PATH_FILE, list);
	}


	@TSE_RE(label = FILING_DATE)
	@ColumnName("?????????")
	public String filingDate;
	
	@TSE_RE(label = ISSUER_NAME_REIT)
	@ColumnName("????????????")
	public String company;
	
	@TSE_RE(label = SECURITIES_CODE)
	@ColumnName("???????????????")
	public String stockCode;
	
	@TSE_RE(label = FISCAL_YEAR_END)
	@ColumnName("?????????")
	public String yearEnd;
	
	// DIVIDEND
	@TSE_RE(label = DISTRIBUTIONS_PAYABLE_DATE_AS_PLANNED_REIT)
	@ColumnName("???????????????")
	public String distributionsDate;
	
	// CURRENT YEAR
	@TSE_RE(label = DISTRIBUTIONS_PER_UNIT_EXCLUDING_DISTRIBUTIONS_IN_EXCESS_OF_PROFIT_REIT,
			contextIncludeAll = {CURRENT_YEAR_DURATION, RESULT_MEMBER})
	@ColumnName("?????????")
	public BigDecimal distributionsPerUnit;

	@TSE_RE(label = DISTRIBUTIONS_IN_EXCESS_OF_PROFIT_PER_UNIT_REIT,
			contextIncludeAll = {CURRENT_YEAR_DURATION, RESULT_MEMBER},
			acceptNullOrEmpty = true)
	@ColumnName("?????????????????????")
	public BigDecimal distributionsInExcessOfProfitPerUnit;

	
	// NEXT_YEAR
	@TSE_RE(label = DISTRIBUTIONS_PER_UNIT_EXCLUDING_DISTRIBUTIONS_IN_EXCESS_OF_PROFIT_REIT,
			contextIncludeAll = {NEXT_YEAR_DURATION, FORECAST_MEMBER},
			acceptNullOrEmpty = true)
	@ColumnName("???????????????")
	public BigDecimal distributionsPerUnitNextYear;

	@TSE_RE(label = DISTRIBUTIONS_IN_EXCESS_OF_PROFIT_PER_UNIT_REIT,
			contextIncludeAll = {NEXT_YEAR_DURATION, FORECAST_MEMBER},
			acceptNullOrEmpty = true)
	@ColumnName("???????????????????????????")
	public BigDecimal distributionsInExcessOfProfitPerUnitNextYear;

	// NEXT_2YEAR
	@TSE_RE(label = DISTRIBUTIONS_PER_UNIT_EXCLUDING_DISTRIBUTIONS_IN_EXCESS_OF_PROFIT_REIT,
			contextIncludeAll = {NEXT_2_YEAR_DURATION, FORECAST_MEMBER},
			acceptNullOrEmpty = true)
	@ColumnName("??????????????????")
	public BigDecimal distributionsPerUnitNext2Year;

	@TSE_RE(label = DISTRIBUTIONS_IN_EXCESS_OF_PROFIT_PER_UNIT_REIT,
			contextIncludeAll = {NEXT_2_YEAR_DURATION, FORECAST_MEMBER},
			acceptNullOrEmpty = true)
	@ColumnName("??????????????????????????????")
	public BigDecimal distributionsInExcessOfProfitPerUnitNext2Year;

	@TSE_RE(label = PAYOUT_RATIO,
			contextIncludeAll = {CURRENT_YEAR_DURATION, RESULT_MEMBER},
			acceptNullOrEmpty = true)
	@ColumnName("????????????")
	public BigDecimal payoutRatio;
	
	
	@TSE_RE(label = OPERATING_REVENUES_REIT,
			contextIncludeAll = {CURRENT_YEAR_DURATION, RESULT_MEMBER})
	@ColumnName("????????????")
	public BigDecimal operatingRevenues;	

	@TSE_RE(label = NET_INCOME_PER_UNIT_REIT,
			contextIncludeAll = {CURRENT_YEAR_DURATION, RESULT_MEMBER})
	@ColumnName("?????????") // 1???????????????????????????
	public BigDecimal netIncomPerShareConsolidated;	

	
	@TSE_RE(label = NUMBER_OF_INVESTMENT_UNITS_INCLUDING_TREASURY_INVESTMENT_UNITS_ISSUED_AT_END_OF_PERIOD_REIT,
			contextIncludeAll = {CURRENT_YEAR_INSTANT, RESULT_MEMBER})
	@ColumnName("??????") // ??????????????????????????????????????????????????????????????????
	public BigDecimal numberOfShares;

	
	public String filename;

	
	public static REITReport getInstance(Document document) {
		REITReport ret = BaseReport.getInstance(REITReport.class, document);
		
		ret.stockCode = BaseElement.normalizeNumberCharacter(ret.stockCode);
		
		// There are too many error of stockCode, overwrite with tdnetCode.
		SummaryFilename summaryFileName = SummaryFilename.getInstance(ret.filename);
		ret.stockCode = summaryFileName.tdnetCode;

		return ret;
	}
	
	@Override
	public String toString() {
		return String.format("{%s %s %s %s %s %s %s %s}",  stockCode, yearEnd, filingDate, distributionsDate,
			distributionsPerUnit, distributionsInExcessOfProfitPerUnit,
			distributionsPerUnitNextYear, distributionsInExcessOfProfitPerUnitNextYear);
	}

	// Define natural ordering of DividendBriefReport
	@Override
	public int compareTo(REITReport that) {
		int ret = this.stockCode.compareTo(that.stockCode);
		if (ret == 0) ret = this.yearEnd.compareTo(that.yearEnd);
		if (ret == 0) ret = this.filingDate.compareTo(that.filingDate);
		if (ret == 0) ret = this.filename.compareTo(that.filename);
		return ret;
	}
}

package yokwe.stock.jp.xbrl.tdnet.report;

import static yokwe.stock.jp.xbrl.tdnet.inline.Context.CURRENT_YEAR_DURATION;
import static yokwe.stock.jp.xbrl.tdnet.inline.Context.CURRENT_YEAR_INSTANT;
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
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.stock.jp.tdnet.SummaryFilename;
import yokwe.stock.jp.xbrl.tdnet.inline.Document;
import yokwe.stock.jp.xbrl.tdnet.inline.InlineXBRL;
import yokwe.util.CSVUtil;
import yokwe.util.CSVUtil.ColumnName;

public class REITReport extends AbstractReport implements Comparable<REITReport> {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(REITReport.class);
	
	public static final String PATH_FILE = "tmp/data/reit-report.csv";

	public static List<REITReport> getList() {
		List<REITReport> ret = CSVUtil.read(REITReport.class).file(PATH_FILE);
		return (ret == null) ? new ArrayList<>() : ret;
	}
	public static Map<SummaryFilename, REITReport> getMap() {
		Map<SummaryFilename, REITReport> ret = new TreeMap<>();
		for(REITReport e: getList()) {
			SummaryFilename key = e.filename;
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
	@ColumnName("提出日")
	public String filingDate;
	
	@TSE_RE(label = ISSUER_NAME_REIT)
	@ColumnName("発行者名")
	public String company;
	
	@TSE_RE(label = SECURITIES_CODE)
	@ColumnName("コード番号")
	public String stockCode;
	
	@TSE_RE(label = FISCAL_YEAR_END)
	@ColumnName("決算期")
	public String yearEnd;
	
	// DIVIDEND
	@TSE_RE(label = DISTRIBUTIONS_PAYABLE_DATE_AS_PLANNED_REIT)
	@ColumnName("分配金支日")
	public String distributionsDate;
	
	@TSE_RE(label = DISTRIBUTIONS_PER_UNIT_EXCLUDING_DISTRIBUTIONS_IN_EXCESS_OF_PROFIT_REIT,
			contextIncludeAll = {CURRENT_YEAR_DURATION, RESULT_MEMBER})
	@ColumnName("分配金")
	public BigDecimal distributionsPerUnit;

	@TSE_RE(label = DISTRIBUTIONS_IN_EXCESS_OF_PROFIT_PER_UNIT_REIT,
			contextIncludeAll = {CURRENT_YEAR_DURATION, RESULT_MEMBER},
			acceptNullOrEmpty = true)
	@ColumnName("利益超過分配金")
	public BigDecimal distributionsInExcessOfProfitPerUnit;

	@TSE_RE(label = PAYOUT_RATIO,
			contextIncludeAll = {CURRENT_YEAR_DURATION, RESULT_MEMBER})
	@ColumnName("配当性向")
	public BigDecimal payoutRatio;
	
	
	@TSE_RE(label = OPERATING_REVENUES_REIT,
			contextIncludeAll = {CURRENT_YEAR_DURATION, RESULT_MEMBER})
	@ColumnName("営業収益")
	public BigDecimal operatingRevenues;	

	@TSE_RE(label = NET_INCOME_PER_UNIT_REIT,
			contextIncludeAll = {CURRENT_YEAR_DURATION, RESULT_MEMBER})
	@ColumnName("純利益") // 1口当たり当期純利益
	public BigDecimal netIncomPerShareConsolidated;	

	
	@TSE_RE(label = NUMBER_OF_INVESTMENT_UNITS_INCLUDING_TREASURY_INVESTMENT_UNITS_ISSUED_AT_END_OF_PERIOD_REIT,
			contextIncludeAll = {CURRENT_YEAR_INSTANT, RESULT_MEMBER})
	@ColumnName("口数") // 期末発行済投資口の総口数（自己投資口を含む）
	public BigDecimal numberOfShares;

	
	public SummaryFilename filename;

	
	public static REITReport getInstance(Document document) {
		REITReport ret = AbstractReport.getInstance(REITReport.class, document);
		
		ret.stockCode = InlineXBRL.normalizeNumberCharacter(ret.stockCode);

		return ret;
	}
	
	@Override
	public String toString() {
		return String.format("{%s %s %s %s %s %s}",  stockCode, yearEnd, filingDate, distributionsDate, distributionsPerUnit, distributionsInExcessOfProfitPerUnit);
	}

	// Define natural ordering of DividendBriefReport
	@Override
	public int compareTo(REITReport that) {
		int ret = this.stockCode.compareTo(that.stockCode);
		if (ret == 0) ret = this.yearEnd.compareTo(that.yearEnd);
		if (ret == 0) ret = this.filingDate.compareTo(that.filingDate);
		return ret;
	}
}

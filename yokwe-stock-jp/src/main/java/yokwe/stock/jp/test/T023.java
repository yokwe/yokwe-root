package yokwe.security.japan.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.StringUtil;
import yokwe.util.http.HttpUtil;

public class T023 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T023.class);

	public static final String PATH_DOWNLOAD_DIR = "tmp/download/jasdec/fund";
	public static final String PATH_DATA_FILE    = "tmp/data/jasdec-fund.csv";
	
	public static final String NO_LIMIT = "9999-12-31";

	public String fundName;   // 銘柄正式名称
	public String issuerName; // 発行者名
	public String isinCode;   // ISINコード
	public String fundCode;   // ファンドコード
	
	public String offerCategory; // 募集区分
	public String fundCategory;  // 投信区分
	
	public String offerDate;      // 設定日
	public String repaymentDate; // 償還日
	
	public long initialPrincipal;  // 当初設定元本
	public long initialUtit; // 当初総発行口数
	
	public double initialPrincipalPerUnit; // 当初1口当たり元本
	public long capitalLimit;            // 追加信託金限度額
	
	public long minimumUnit; // 最低発行単位口数
	public long issuedUnit;  // 総発行口数
	
	public T023(
			String fundName,
			String issuerName,
			String isinCode,
			String fundCode,
			String offerCategory,
			String fundCategory,
			String offerDate,
			String repaymentDate,
			long initialPrincipal,
			long initialUtit,
			long initialPrincipalPerUnit,
			long capitalLimit,
			long minimumUnit,
			long issuedUnit
			) {
		this.fundName                = fundName;
		this.issuerName              = issuerName;
		this.isinCode                = isinCode;
		this.fundCode                = fundCode;
		this.offerCategory           = offerCategory;
		this.fundCategory            = fundCategory;
		this.offerDate               = offerDate;
		this.repaymentDate           = repaymentDate;
		this.initialPrincipal        = initialPrincipal;
		this.initialUtit             = initialUtit;
		this.initialPrincipalPerUnit = initialPrincipalPerUnit;
		this.capitalLimit            = capitalLimit;
		this.minimumUnit             = minimumUnit;
		this.issuedUnit              = issuedUnit;
	}
	public T023() {
		this("", "", "", "", "", "", "", "", 0, 0, 0, 0, 0, 0);
	}
	
	public static String getDetail(HttpUtil httpUtil, int n) {
		String url = String.format("http://www.jasdec.com/reading/it_details.php?idno_1=%d", n);
		HttpUtil.Result result = httpUtil.download(url);
		
		if (result.result.contains("ID、またはパスワードが異なります。")) {
			return null;
		}
		if (result.result.contains("データベースの接続に失敗しています。")) {
			return null;
		}
		return result.result;
	}
	
	public static void download() {
		HttpUtil httpUtil = HttpUtil.getInstance().withCharset("MS932");

		for(int i = 0; i < 5900; i++) {
			String result = getDetail(httpUtil, i);
			if (result == null) {
				logger.info("return null {}", i);
				continue;
			}
			String path = String.format("%s/%04d", PATH_DOWNLOAD_DIR, i);
			logger.info("save {}  {}", path, result.length());
			FileUtil.write().file(path, result);
		}
	}
	
	private static final Pattern PAT_NAME = Pattern.compile(
			"<tr>\\s+" +
			"<th .+?>\\s+" +
			"<span .+?>\\s+" +
			"銘柄正式名称\\s+" +
			"</span>\\s+" +
			"</td>\\s+" +
			"<td .+?>\\s+" +
			"<span .+?>\\s+" +
			"(.+?)\\s*</span><br>\\s+" +
			"</td>\\s+" +
			"</tr>"
	);
	private static final Pattern PAT_ISSUER = Pattern.compile(
			"<tr>\\s+" +
			"<th .+?>\\s+" +
			"<span .+?>\\s+" +
			"発行者名\\s+" +
			"</span>\\s+" +
			"</td>\\s+" +
			"<td .+?>\\s+" +
			"<span .+?>(.+?)</span><br>\\s+" +
			"</td>\\s+" +
			"</tr>"
	);
	private static final Pattern PAT_ISIN_FUND_CODE = Pattern.compile(
			"<tr>\\s+" +
			"<th .+?>\\s+" +
			"<img .+?><br>\\s+" +
			"<span .+?>\\s+" +
			"ISINコード\\s+" +
			"</span>\\s+" +
			"</td>\\s+" +
			"<td .+?>\\s+" +
			"<img .+?><br>\\s+" +
			"<span .+?>(.+?)</span><br></td>\\s+" +
			"<th .+?>\\s+" +
			"<img .+?><br>\\s+" +
			"<span .+?>\\s+" +
			"ファンドコード\\s+" +
			"</span>\\s+" +
			"</td>\\s+" +
			"<td .+?>\\s+" +
			"<img .+?><br>\\s+" +
			"<span .+?>\\s+" +
			"(.+?)\\s+</span><br>\\s+" +
			"</td>\\s+" +
			"</tr>"
	);
	private static final Pattern PAT_OFFER_FUND_CATEGORY = Pattern.compile(
			"<tr>\\s+" +
			"<th .+?>\\s+" +
			"<span .+?>\\s+" +
			"募集区分\\s+" +
			"</span>\\s+" +
			"</td>\\s+" +
			"<td .+?>\\s+" +
			"<span .+?>\\s+" +
			"(.+?)\\s+</span><br>\\s+" +
			"</td>\\s+" +
			"<th .+?>\\s+" +
			"<span .+?>\\s+" +
			"投信区分\\s+" +
			"</span>\\s+" +
			"</td>\\s+" +
			"<td .+?>\\s+" +
			"<span .+?>\\s+" +
			"(.+?)\\s+</span><br>\\s+" +
			"</td>\\s+" +
			"</tr>"
	);
	private static final Pattern PAT_OFFER_REPAYMENT_DATE = Pattern.compile(
			"<tr>\\s+" +
			"<th .+?>\\s+" +
			"<span .+?>\\s+" +
			"設定日\\s+" +
			"</span>\\s+" +
			"</td>\\s+" +
			"<td .+?>\\s+" +
			"<span .+?>\\s+" +
			"(.+?)\\s+</span><br>\\s+" +
			"</td>\\s+" +
			"<th .+?>\\s+" +
			"<span .+?>\\s+" +
			"償還日\\s+" +
			"</span>\\s+" +
			"</td>\\s+" +
			"<td .+?>\\s+" +
			"<span .+?>\\s+" +
			"(.+?)\\s+</span><br>\\s+" +
			"</td>\\s+" +
			"</tr>"
	);
	private static final Pattern PAT_INITIAL_PRINCIPAL_UNIT = Pattern.compile(
			"<tr>\\s+" +
			"<th .+?>\\s+" +
			"<span .+?>\\s+" +
			"当初設定元本\\s+" +
			"</span>\\s+" +
			"</td>\\s+" +
			"<td .+?>\\s+" +
			"<span .+?>(.+?)</span><br></td>\\s+" +
			"<th .+?>\\s+" +
			"<span .+?>\\s+" +
			"当初総発行口数\\s+" +
			"</span>\\s+" +
			"</td>\\s+" +
			"<td .+?>\\s+" +
			"<span .+?>(.+?)</span><br></td>\\s+" +
			"</tr>"
	);
	private static final Pattern PAT_PRINCIPAL_PER_UNIT_CAPITAL_LIMIT = Pattern.compile(
			"<tr>\\s+" +
			"<th .+?>\\s+" +
			"<span .+?>\\s+" +
			"当初1口当たり元本\\s+" +
			"</span>\\s+" +
			"</td>\\s+" +
			"<td .+?>\\s+" +
			"<span .+?>(.+?)</span><br></td>\\s+" +
			"<th .+?>\\s+" +
			"<span .+?>\\s+" +
			"追加信託金限度額\\s+" +
			"</span>\\s+" +
			"</td>\\s+" +
			"<td .+?>\\s+" +
			"<span .+?>(.+?)</span><br></td>\\s+" +
			"</tr>"
	);
	private static final Pattern PAT_MINIMUM_UNIT_ISSUED_UNIT = Pattern.compile(
			"<tr>\\s+" +
			"<th .+?>\\s+" +
			"<span .+?>\\s+" +
			"最低発行単位口数\\s+" +
			"</span>\\s+" +
			"</td>\\s+" +
			"<td .+?>\\s+" +
			"<span .+?>(.+?)</span><br></td>\\s+" +
			"<th .+?>\\s+" +
			"<span .+?>\\s+" +
			"総発行口数\\s+" +
			"</span>\\s+" +
			"</td>\\s+" +
			"<td .+?>\\s+" +
			"<span .+?>(.+?)</span><br></td>\\s+" +
			"</tr>"
	);


	public static T023 getInstance(String page) {
		T023 ret = new T023();
		
		ret.fundName   = StringUtil.getGroupOne(PAT_NAME, page);
		ret.issuerName = StringUtil.getGroupOne(PAT_ISSUER, page);
		{
			String[] array = StringUtil.getGroup(PAT_ISIN_FUND_CODE, page);
			ret.isinCode = array[0];
			ret.fundCode = array[1];
		}
		{
			String[] array = StringUtil.getGroup(PAT_OFFER_FUND_CATEGORY, page);
			ret.offerCategory = array[0];
			ret.fundCategory = array[1];
		}
		{
			String[] array = StringUtil.getGroup(PAT_OFFER_REPAYMENT_DATE, page);
			ret.offerDate = array[0].replace("/", "-");
			ret.repaymentDate = array[1].replace("/", "-");
			
			if (ret.repaymentDate.equals("無期限")) {
				ret.repaymentDate = NO_LIMIT;
			}
			
		}
		{
			String[] array = StringUtil.getGroup(PAT_INITIAL_PRINCIPAL_UNIT, page);
			ret.initialPrincipal = Long.parseLong(array[0].replace(",", ""));
			ret.issuedUnit = Long.parseLong(array[1].replace(",", ""));
		}
		{
			String[] array = StringUtil.getGroup(PAT_PRINCIPAL_PER_UNIT_CAPITAL_LIMIT, page);
			ret.initialPrincipalPerUnit = Double.parseDouble(array[0].replace(",", ""));
			ret.capitalLimit = Long.parseLong(array[1].replace(",", ""));
		}
		{
			String[] array = StringUtil.getGroup(PAT_MINIMUM_UNIT_ISSUED_UNIT, page);
			ret.minimumUnit = Long.parseLong(array[0].replace(",", ""));
			ret.issuedUnit = Long.parseLong(array[1].replace(",", ""));
		}
		
		return ret;
	}
	public static void main(String[] args) {
		logger.info("START");
		
		List<T023> list = new ArrayList<>();
		for(File file: FileUtil.listFile(PATH_DOWNLOAD_DIR)) {
			String page = FileUtil.read().file(file);
			T023 fund = T023.getInstance(page);
			list.add(fund);
		}
		logger.info("list {} {}", PATH_DATA_FILE, list.size());
		CSVUtil.write(T023.class).file(PATH_DATA_FILE, list);
		
		logger.info("STOP");
	}
}

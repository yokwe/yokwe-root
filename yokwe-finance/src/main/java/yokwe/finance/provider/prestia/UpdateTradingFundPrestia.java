package yokwe.finance.provider.prestia;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.type.Currency;
import yokwe.finance.type.TradingFundType;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateTradingFundPrestia {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	static final Storage storage = StoragePrestia.storage;
	
	public static final boolean DEBUG_USE_FILE  = false;
	
	public static String download(String url, Charset charset, File file, boolean useFile) {
		final String page;
		{
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
	
	private static void update() {
		var screener = Screener.getInstance();
		logger.info("screener  {}  {}  {}  {}", screener.page, screener.pageSize, screener.rows.length, screener.total);
		
		// build fundInfoList
		var fundInfoList = new ArrayList<FundInfoPrestia>();
		{			
			for(int i = 0; i < screener.rows.length; i++) {
				var row = screener.rows[i];
				logger.info("{}  /  {}  {}", i, screener.rows.length, row.secId);
				if (row.priceCurrency == null) {
					logger.info("no priceCurrency");
					logger.info("  row  {}", row);
					continue;
				}
				
			    var secId    = row.secId;
			    var currency = Currency.getInstance(row.priceCurrency);
			    var fundCode = row.customInstitutionSecurityId;
			    var isinCode = row.isinCode == null ? "" : row.isinCode;
			    
			    var mfSnapshot = MFsnapshot.getInstance(secId);
			    var salesFee = mfSnapshot.getBuyFee();
			    
			    var fundName = row.fundName;
			    
			    fundInfoList.add(new FundInfoPrestia(secId, currency, fundCode, isinCode, salesFee, fundName));
			}
			
			logger.info("save  {}  {}", fundInfoList.size(), StoragePrestia.FundInfoPrestia.getPath());
			StoragePrestia.FundInfoPrestia.save(fundInfoList);
		}
		
		// build tradingFundList from fundInfoList
		var tradingFundList = new ArrayList<TradingFundType>();
		{
			var isinSet = StorageFund.FundInfo.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
			
			int countA = 0;
			int countB = 0;
			int countC = 0;
			int countD = 0;
			for(var e: fundInfoList) {
				var isinCode = e.isinCode;
				if (isinCode.isEmpty()) {
					countA++;
				} else {
					if (isinSet.contains(isinCode)) {
						tradingFundList.add(new TradingFundType(e.isinCode, e.salesFee));
						countB++;
					} else {
						if (isinCode.startsWith("JP")) {
							logger.warn("Unexpected isinCode  {}  {}", e.isinCode, e.fundName);
							countC++;
						} else {
//							logger.warn("Unexpected isinCode  {}  {}", e.isinCode, e.fundName);
							countD++;
						}
					}
				}
			}
			
			logger.info("countA    {}", countA);
			logger.info("countB    {}", countB);
			logger.info("countC    {}", countC);
			logger.info("countD    {}", countD);
			
			logger.info("save  {}  {}", tradingFundList.size(), StoragePrestia.TradingFundPrestia.getPath());
			StoragePrestia.TradingFundPrestia.save(tradingFundList);
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}

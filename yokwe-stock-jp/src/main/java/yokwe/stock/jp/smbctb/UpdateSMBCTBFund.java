package yokwe.stock.jp.smbctb;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.LoggerFactory;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.smbctb.SMBCTBFund.Currency;
import yokwe.stock.jp.smbctb.json.Screener;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateSMBCTBFund {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateSMBCTBFund.class);

	public static final String URL_SCREENER_BASE = "https://lt.morningstar.com/api/rest.svc/smbctbfund/security/screener";
	
	public static final String getURL() {
		try {
			return new URIBuilder(URL_SCREENER_BASE).
					addParameter("page",               "1").
					addParameter("pageSize",           "1000").
					addParameter("outputType",         "json").
					addParameter("languageId",         "ja-JP").
					addParameter("securityDataPoints", "customFundName|currencyId|isin|secId").
					build().toASCIIString();
		} catch (URISyntaxException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	private static final Map<String, Currency> currencyMap;
	static {
		currencyMap = new TreeMap<>();
		currencyMap.put("CU$$$$$AUD", Currency.AUD);
		currencyMap.put("CU$$$$$EUR", Currency.EUR);
		currencyMap.put("CU$$$$$JPY", Currency.JPY);
		currencyMap.put("CU$$$$$NZD", Currency.NZD);
		currencyMap.put("CU$$$$$USD", Currency.USD);
	}
	private static Currency toCurrency(String string) {
		if (currencyMap.containsKey(string)) {
			return currencyMap.get(string);
		} else {
    		logger.error("Unexpecteed string");
    		logger.error("  string {}", string);
    		throw new UnexpectedException("Unexpecteed string");	
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");

		String url = getURL();
		logger.info("url {}", url);
		
		HttpUtil.Result result = HttpUtil.getInstance().download(url);
		logger.info("result {} {} {} {}", result.code, result.reasonPhrase, result.version, result.rawData.length);
		{
			String name = "screener.json";
			String path = Storage.SMBCTB.getPath(name);
			FileUtil.write().file(path, result.result);
		}

		Screener screener = JSON.unmarshal(Screener.class, result.result);

		List<SMBCTBFund> list = new ArrayList<>();
		for (var e : screener.rows) {
		    String   secId = e.secId;
		    String   isin = e.isin;
		    Currency currency = toCurrency(e.currencyId);
		    String   fundName = e.customFundName;
		    
		    list.add(new SMBCTBFund(secId, isin, currency, fundName));
		}

		logger.info("save {} {}", list.size(), SMBCTBFund.getPath());
		CSVUtil.write(SMBCTBFund.class).file(SMBCTBFund.getPath(), list);
		
		logger.info("STOP");
	}
}

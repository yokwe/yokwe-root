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
package yokwe.stock.jp.smbctb;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hc.core5.net.URIBuilder;

import yokwe.stock.jp.smbctb.Fund.Currency;
import yokwe.stock.jp.smbctb.json.Screener;
import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateFund {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateFund.class);

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
	
	private static final Map<String, Fund.Currency> currencyMap;
	static {
		currencyMap = new TreeMap<>();
		currencyMap.put("CU$$$$$AUD", Fund.Currency.AUD);
		currencyMap.put("CU$$$$$EUR", Fund.Currency.EUR);
		currencyMap.put("CU$$$$$JPY", Fund.Currency.JPY);
		currencyMap.put("CU$$$$$USD", Fund.Currency.USD);
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

		Screener screener = JSON.unmarshal(Screener.class, result.result);

		List<Fund> list = new ArrayList<>();
		for (var e : screener.rows) {
		    String   secId = e.secId;
		    String   isin = e.isin;
		    Currency currency = toCurrency(e.currencyId);
		    String   fundName = e.customFundName;
		    
		    list.add(new Fund(secId, isin, currency, fundName));
		}

		logger.info("save {} {}", list.size(), Fund.FILE_PATH);
		CSVUtil.write(Fund.class).file(Fund.FILE_PATH, list);
		
		logger.info("STOP");
	}
}

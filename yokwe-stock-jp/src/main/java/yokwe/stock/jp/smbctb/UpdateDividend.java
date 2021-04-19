package yokwe.stock.jp.smbctb;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.stock.jp.smbctb.json.Dividend.Security.DividendSeries;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateDividend {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateDividend.class);

	//https://gllt.morningstar.com/api/rest.svc/timeseries_dividend/smbctbfund?currencyId=BAS&endDate=2020-06-23&frequency=daily&id=F000000MU9&idType=Morningstar&outputType=json&startDate=1900-01-01
	public static final String URL_PRICE_BASE = "https://gllt.morningstar.com/api/rest.svc/timeseries_dividend/smbctbfund";

	public static String getURL(String secId) {
		try {
			return new URIBuilder(URL_PRICE_BASE).
					addParameter("currencyId",  "BAS").
					addParameter("frequency",   "daily").
					addParameter("id",          secId).
					addParameter("idType",      "Morningstar").
					addParameter("outputType",  "json").
					addParameter("startDate",   "1900-01-01").
					addParameter("endDate",     LocalDate.now().toString()).
					build().toASCIIString();
		} catch (URISyntaxException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	public static void main(String[] args) {
		logger.info("START");
		
		List<Fund> fundList = Fund.getList();
		int size = fundList.size();
		logger.info("fund {}", size);
		
		// Shuffle fundList
		Collections.shuffle(fundList);
		
		int i = 0;
		int countSave = 0;
		for(var e: fundList) {
			if ((i++ % 10) == 0) {
				logger.info("{}", String.format("%4d / %4d %s", i, size, e.secId));
			}
			
			String url = getURL(e.secId);
			HttpUtil.Result result = HttpUtil.getInstance().download(url);
//			logger.info("result {} {} {} {}", result.code, result.reasonPhrase, result.version, result.rawData.length);
			
			yokwe.stock.jp.smbctb.json.Dividend jsonDividend = JSON.unmarshal(yokwe.stock.jp.smbctb.json.Dividend.class, result.result);
			
			List<Dividend> divList = new ArrayList<>();
			
			if (jsonDividend.security.length == 1) {
				yokwe.stock.jp.smbctb.json.Dividend.Security security = jsonDividend.security[0];
				if (security.dividendSeries == null) {
//					logger.warn("{} dividendSeries is null", e.secId);
					continue;
				}
				if (security.id.equals(e.secId)) {
					if (security.dividendSeries.length == 1) {
						DividendSeries dividendSeries = security.dividendSeries[0];
						for(yokwe.stock.jp.smbctb.json.Dividend.Security.DividendSeries.HistoryDetail historyDeatil: dividendSeries.historyDetail) {
							if (historyDeatil.value.length == 1) {
								yokwe.stock.jp.smbctb.json.Dividend.Security.DividendSeries.HistoryDetail.Value value = historyDeatil.value[0];
								divList.add(new Dividend(historyDeatil.endDate, e.secId, e.currency, new BigDecimal(value.value)));
							} else {
								logger.error("Unexpected value.length");
								logger.error("  secId        {}", e.secId);
								logger.error("  value.length {}", historyDeatil.value.length);
								throw new UnexpectedException("Unexpected value.length");
							}
						}
					} else {
						logger.error("Unexpected dividendSeries.length");
						logger.error("  secId                 {}", e.secId);
						logger.error("  dividendSeries.length {}", security.dividendSeries.length);
						throw new UnexpectedException("Unexpected value.length");
					}
				} else {
					// Unexpected security.id
					logger.error("Unexpected security.id");
					logger.error("  secId       {}", e.secId);
					logger.error("  security.id {}", security.id);
					throw new UnexpectedException("Unexpected security.id");
				}
			} else {
				// Unexpected size of jsonPrice.timeSeries.security
				logger.error("Unexpected security.length");
				logger.error("  length       {}", jsonDividend.security.length);
				throw new UnexpectedException("Unexpected security.length");
			}
			
//			logger.info("save {} {}", divList.size(), Dividend.getPath(e.secId));
			Dividend.save(divList);
			countSave++;
		}
		logger.info("save {} / {}", countSave, size);

		logger.info("STOP");
	}
}

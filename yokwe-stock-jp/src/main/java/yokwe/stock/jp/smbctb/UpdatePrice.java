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
import yokwe.stock.jp.smbctb.json.Price.TimeSeries.Security.HistoryDetail;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdatePrice {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdatePrice.class);

	// https://gllt.morningstar.com/api/rest.svc/timeseries_price/smbctbfund?currencyId=BAS&endDate=2020-06-23&forwardFill=false&frequency=daily&id=F000000MU9&idType=Morningstar&outputType=json&startDate=1900-01-01
	public static final String URL_PRICE_BASE = "https://gllt.morningstar.com/api/rest.svc/timeseries_price/smbctbfund";

	public static String getURL(String secId) {
		try {
			return new URIBuilder(URL_PRICE_BASE).
					addParameter("currencyId",  "BAS").
					addParameter("forwardFill", "false").
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
			
			yokwe.stock.jp.smbctb.json.Price jsonPrice = JSON.unmarshal(yokwe.stock.jp.smbctb.json.Price.class, result.result);
			
			List<Price> priceList = new ArrayList<>();
			
			if (jsonPrice.timeSeries.security.length == 1) {
				yokwe.stock.jp.smbctb.json.Price.TimeSeries.Security security = jsonPrice.timeSeries.security[0];
				if (security.historyDetail == null) {
					logger.warn("{} historyDetail is null", e.secId);
					continue;
				}
				if (security.id.equals(e.secId)) {
					for(HistoryDetail historyDetail: security.historyDetail) {
						priceList.add(new Price(historyDetail.endDate, e.secId, e.currency, new BigDecimal(historyDetail.value)));
					}
				} else {
					// Unexpected security.id
					logger.error("Unexpected security.id");
					logger.error("  secId       {}", e.secId);
					logger.error("  security.id {}", security.id);
					throw new UnexpectedException("Unexpected security.id");
				}
			} else {
				// Unexpect size of jsonPrice.timeSeries.security
				logger.error("Unexpected security length");
				logger.error("  length       {}", jsonPrice.timeSeries.security.length);
				throw new UnexpectedException("Unexpected security length");
			}
			
//			logger.info("save {} {}", priceList.size(), Price.getPath(e.secId));
			Price.save(priceList);
			countSave++;
		}
		logger.info("save {} / {}", countSave, size);

		logger.info("STOP");
	}
}

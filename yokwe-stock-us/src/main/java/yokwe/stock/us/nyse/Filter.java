package yokwe.stock.us.nyse;

import java.util.Collections;
import java.util.List;

import yokwe.stock.us.Storage;
import yokwe.util.ListUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public final class Filter {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public enum Context {
		ETF  ("EXCHANGE_TRADED_FUND", Storage.NYSE.getPath("etf.csv")),
		STOCK("EQUITY",               Storage.NYSE.getPath("stock.csv"));
		
		public final String instrumentType;
		public final String path;
		
 		Context(String instrumentType, String path) {
			this.instrumentType = instrumentType;
			this.path           = path;
		}
	}
	
	public static final class RawData implements Comparable<RawData> {
		public String exchangeId;
		public String instrumentName;
		public String instrumentType;
		public String micCode;
		public String normalizedTicker;
		public String symbolEsignalTicker;
		public String symbolExchangeTicker;
		public String symbolTicker;
		public int    total;
		public String url;
		
		public RawData() {}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}

		public String getKey() {
			return normalizedTicker;
		}
		@Override
		public int compareTo(RawData that) {
			return this.getKey().compareTo(that.getKey());
		}
		@Override
		public int hashCode() {
			return getKey().hashCode();
		}
		@Override
		public boolean equals(Object o) {
			if (o != null) {
				if (o instanceof RawData) {
					RawData that = (RawData)o;
					return this.compareTo(that) == 0;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	public static class Impl {
		public static final String URL          = "https://www.nyse.com/api/quotes/filter";
		public static final String BODY_FORMAT  = "{\"instrumentType\":\"%s\",\"pageNumber\":1,\"sortColumn\":\"NORMALIZED_TICKER\",\"sortOrder\":\"ASC\",\"maxResultsPerPage\":10000,\"filterToken\":\"\"}";
		public static final String CONTENT_TYPE = "application/json";

		public static List<RawData> download(Context context) {
			logger.info("download {}", context);
			String body = String.format(BODY_FORMAT, context.instrumentType);
			
			HttpUtil.Result result = HttpUtil.getInstance().withPost(body, CONTENT_TYPE).download(URL);
			
			if (result == null) {
				throw new UnexpectedException("result == null");
			}
			if (result.result == null) {
				throw new UnexpectedException("result.result == null");
			}
			
//			logger.info("result  {}", result.result.length());
			List<RawData> list = JSON.getList(RawData.class, result.result);
//			logger.info("list  {}", list.size());
			return list;
		}
		
		private final Context   context;

		Impl(Context context) {
			this.context = context;
		}
		public String getPath() {
			return context.path;
		}
		public void save(List<RawData> list) {
			Collections.sort(list);
			ListUtil.save(RawData.class, getPath(), list);
		}
		public List<RawData> getList() {
			return ListUtil.getList(RawData.class, getPath());
		}
		
		public void download() {
			List<RawData> list = download(context);
			logger.info("save  {}  {}", list.size(), getPath());
			save(list);
		}
	}
	
	public static final Impl STOCK = new Impl(Context.STOCK);
	public static final Impl ETF   = new Impl(Context.ETF);
	
}

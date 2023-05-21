package yokwe.stock.us.nyse;

import java.util.Collections;
import java.util.List;

import yokwe.stock.us.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;
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
	
	public static final class Data implements Comparable<Data> {
		public enum Type {
			CEF    ("CLOSED_END_FUND",              NYSESymbol.Type.CEF),
			COMMON ("COMMON_STOCK",                 NYSESymbol.Type.COMMON),
			ADR    ("DEPOSITORY_RECEIPT",           NYSESymbol.Type.ADR),
			ETF    ("EXCHANGE_TRADED_FUND",         NYSESymbol.Type.ETF),
			ETN    ("EXCHANGE_TRADED_NOTE",         NYSESymbol.Type.ETN),
			LP     ("LIMITED_PARTNERSHIP",          NYSESymbol.Type.LP),
			PREF   ("PREFERRED_STOCK",              NYSESymbol.Type.PREF),
			REIT   ("REIT",                         NYSESymbol.Type.REIT),
			TRUST  ("TRUST",                        NYSESymbol.Type.TRUST),
			UNIT   ("UNIT",                         NYSESymbol.Type.UNIT),
			UBI    ("UNITS_OF_BENEFICIAL_INTEREST", NYSESymbol.Type.UBI);
			
			public final String          value;
			public final NYSESymbol.Type type;
			
			Type(String value, NYSESymbol.Type type) {
				this.value = value;
				this.type = type;
			}
			
			@Override
			public String toString() {
				return value;
			}
		}
		
		// Market Identifier Code
		public enum MIC {			
			ARCX("ARCX", NYSESymbol.Market.NYSE),   // NYSE ARCA
			BATS("BATS", NYSESymbol.Market.BATS),   // CBOE BZX U.S. EQUITIES EXCHANGE
			XASE("XASE", NYSESymbol.Market.NYSE),   // NYSE MKT LLC
			XNAS("XNAS", NYSESymbol.Market.NASDAQ), // NASDAQ ??
			XNCM("XNCM", NYSESymbol.Market.NASDAQ), // NASDAQ CAPITAL MARKET
			XNGS("XNGS", NYSESymbol.Market.NASDAQ), // NASDAQ/NGS (GLOBAL SELECT MARKET)
			XNMS("XNMS", NYSESymbol.Market.NASDAQ), // NASDAQ/NMS (GLOBAL MARKET)
			XNYS("XNYS", NYSESymbol.Market.NYSE);   // NEW YORK STOCK EXCHANGE, INC.

			public final String            value;
			public final NYSESymbol.Market market;
			MIC(String value, NYSESymbol.Market market) {
				this.value  = value;
				this.market = market;
			}
			
			@Override
			public String toString() {
				return value;
			}
		}
		
		public String exchangeId;
		public String instrumentName;
		public Type   instrumentType;
		public MIC    micCode;
		public String normalizedTicker;
		public String symbolEsignalTicker;
		public String symbolExchangeTicker;
		public String symbolTicker;
		public int    total;
		public String url;
		
		public Data() {}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}

		public String getKey() {
			return normalizedTicker;
		}
		@Override
		public int compareTo(Data that) {
			return this.getKey().compareTo(that.getKey());
		}
		@Override
		public int hashCode() {
			return getKey().hashCode();
		}
		@Override
		public boolean equals(Object o) {
			if (o != null) {
				if (o instanceof Data) {
					Data that = (Data)o;
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

		public static List<Data> download(Context context) {
			logger.info("download {}", context);
			String body = String.format(BODY_FORMAT, context.instrumentType);
			
			HttpUtil.Result result = HttpUtil.getInstance().withPost(body, CONTENT_TYPE).download(URL);
			
			if (result == null) {
				throw new UnexpectedException("result == null");
			}
			if (result.result == null) {
				throw new UnexpectedException("result.result == null");
			}
			
			logger.info("result  {}", result.result.length());
			List<Data> list = JSON.getList(Data.class, result.result);
			logger.info("list  {}", list.size());
			return list;
		}
		
		private final Context   context;

		Impl(Context context) {
			this.context = context;
		}
		public String getPath() {
			return context.path;
		}
		public void save(List<Data> list) {
			Collections.sort(list);
			ListUtil.save(Data.class, getPath(), list);
		}
		public List<Data> getList() {
			return ListUtil.getList(Data.class, getPath());
		}
		
		public void download() {
			List<Data> list = download(context);
			logger.info("save  {}  {}", list.size(), getPath());
			save(list);
		}
	}
	
	public static final Impl Stock = new Impl(Context.STOCK);
	public static final Impl ETF   = new Impl(Context.ETF);
	
}

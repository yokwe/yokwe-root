package yokwe.finance.provider.prestia;

import java.io.File;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.util.ToString;
import yokwe.util.json.JSON;

public class Screener {
	private static final Storage storage = StoragePrestia.storage;
	
	public static final Charset CHARSET = StandardCharsets.UTF_8;
	
	public static File getFile() {
		return storage.getFile("screener.json");
	}
	
	private static final String  URL_PATTERN     = "https://lt.morningstar.com/api/rest.svc/smbctbfund/security/screener?%s";
	public static String getURL() {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("page",        "1");
		map.put("pageSize",    "1000");
		map.put("outputType",  "json");
		map.put("languageId",  "ja-JP");
		
		map.put("securityDataPoints", "SecId|PriceCurrency|isin|customInstitutionSecurityId|customFundName");
		String queryString = map.entrySet().stream().map(o -> o.getKey() + "=" + URLEncoder.encode(o.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
		
		return String.format(URL_PATTERN, queryString);
	}
	
	public static Screener getInstance() {
		var url    = Screener.getURL();
		var file   = Screener.getFile();
		var string = UpdateTradingFundPrestia.download(url, Screener.CHARSET, file, UpdateTradingFundPrestia.DEBUG_USE_FILE);
		
		return JSON.unmarshal(Screener.class, string);
	}
	
    public static final class Rows {
        @JSON.Name("SecId")                         public String secId;                       // STRING STRING
        @JSON.Name("PriceCurrency")                 public String priceCurrency;               // STRING STRING
        @JSON.Name("isin")           @JSON.Optional public String isinCode;                    // STRING STRING
        @JSON.Name("customInstitutionSecurityId")   public String customInstitutionSecurityId; // STRING STRING
        @JSON.Name("customFundName")                public String fundName;                    // STRING STRING

        @Override
        public String toString() {
            return ToString.withFieldName(this);
        }
    }

    @JSON.Name("page")     public BigDecimal page;     // NUMBER INT
    @JSON.Name("pageSize") public BigDecimal pageSize; // NUMBER INT
    @JSON.Name("rows")     public Screener.Rows[]     rows;     // ARRAY 168
    @JSON.Name("total")    public BigDecimal total;    // NUMBER INT

    @Override
    public String toString() {
        return ToString.withFieldName(this);
    }
}
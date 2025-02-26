package yokwe.finance.provider.jpx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import yokwe.finance.type.StockInfoJPType;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.json.JSON.Ignore;

public class UpdateStockList {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = false;
	
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit";
	
	private static final String URL_FORMAT  = "https://quote.jpx.co.jp/jpxhp/jcgi/wrap/qjsonp.aspx?F=ctl/stock_list&page=%d&refindex=%%2BTTCODE&maxdisp=100";
	private static final String REF_FORMAT  = "https://quote.jpx.co.jp/jpxhp/main/index.aspx?f=stock_list&key7=";
	private static final String PATH_PRIFIX = "stockList";
	private static final String NAME_FORMAT = "%02d.json";
	
	public static class Result {
		public String   cputime;
		public Section1 section1;
		public int      status;
		public String   ver;
		@Ignore
		public Object   urlparam;
	}
	
	public static class Section1 {
		public int    currentpage;
		public Data[] data;
		public int    hitcount;
		public int    pagecount;
		public int    recordcount;
		public int    status;
		public String type;
	}
	
	public static class Data {
        public String BICD;      // "1301"
        public String DPP;       // 現在値 "4,110"
        public String DPPT;      // 現在値（時刻） "15:30"
        public String DV;        // 出来高 1000株 "31.8"
        @Ignore
        public String DYRP;      // 前日比パーセント "+0.12"
        @Ignore
        public String DYWP;      // 前日比 "+5"
        @Ignore
        public String EXDV;      // "0000"
        public String FLLN;      // 名前 "極洋"
        @Ignore
        public String FLLNE;     // 名前英語 "KYOKUYO CO., LTD."
        @Ignore
        public String JSEC;      // sectr33Code "50"
        public String LISS;      // "ﾌﾟﾗｲﾑ"
        @Ignore
        public String LISSE;     // "Prime"
        @Ignore
        public String PSTS;      // ""
        @Ignore
        public String TTCODE;    // "1301/T"
        @Ignore
        public String TTCODE2;   // "1301"
        public String ZXD;       // 日付 "2025/02/26"
        @Ignore
        public int    line_no;   // レコード番号 1
        @Ignore
        public String JSEC_CNV;  // sector33 "水産・農林業"
        @Ignore
        public String JSECE_CNV; // sector33英語 "Fishery, Agriculture & Forestry"
        @Ignore
        public int    DYWP_FLG;  // 1
        @Ignore
        public int    DYRP_FLG;  // 1
        @Ignore
        public String LISS_CNV;  // 市場 "プライム"
        @Ignore
        public String LISSE_CNV; // 市場英語 "Prime"
        @Ignore
        public String EXDV_CNV;  // ""
        @Ignore
        public String EXDVE_CNV; // ""
        @Ignore
        public String PSTSE;     // ""
        @Ignore
        public String ROE;       // "11.1"
        @Ignore
        public String PER;       // "7.48"
        @Ignore
        public String PBR;       // "0.83"
	}
	
	private static int countSkip = 0;
	
	public static void download(List<StockListType> list) {
		download(list, 1);
	}
	public static void download(List<StockListType> list, int page) {
		logger.info("download  {}", page);
		var url  = String.format(URL_FORMAT, page);
		var ref  = String.format(REF_FORMAT, page);
		var file = StorageJPX.storage.getFile(PATH_PRIFIX, String.format(NAME_FORMAT, page));
		
		if (DEBUG_USE_FILE && file.exists()) {
			// use old file
		} else {
			HttpUtil.Result result = HttpUtil.getInstance().withUserAgent(USER_AGENT).withReferer(ref).download(url);
			if (result == null || result.result == null) {
				logger.error("Unexpected  result  {}", result);
				throw new UnexpectedException("Unexpected");
			}
//			logger.info("save  {}  {}", result.result.length(), file.getPath());
			FileUtil.write().file(file, result.result);
		}
		String string = FileUtil.read().file(file);
		var result = JSON.unmarshal(Result.class, string);
		for(var e: result.section1.data) {
			if (e.LISS.equals("TPM")) {
				// Tokyo Pro Market
//				logger.warn("skip  {}  {}", e.BICD, e.FLLN);
				continue;
			}
			if (e.ZXD.equals("")) {
				// skip stock before trade
				logger.warn("skip  {}  {}", e.BICD, e.FLLN);
				continue;
			}
			
			String     stockCode = StockInfoJPType.toStockCode5(e.BICD);
			LocalDate  date      = LocalDate.parse(e.ZXD.replace("/", "-"));
			LocalTime  time      = e.DPPT.equals("-") ? LocalTime.of(0, 0) : LocalTime.parse(e.DPPT);
			BigDecimal price     = e.DPP.equals("-") ? BigDecimal.ZERO : new BigDecimal(e.DPP.replace(",", ""));
			long       volume    = e.DV.equals("-") ? 0 : new BigDecimal(e.DV.replace(",", "")).scaleByPowerOfTen(3).longValue();
			String     name      = e.FLLN;

			var stockList = new StockListType(stockCode, date, time, price, volume, name);
			list.add(stockList);
		}
		
		// advance to next page
		if (page < result.section1.pagecount) download(list, page + 1);
	}
	
	private static void update() {
		var list = new ArrayList<StockListType>();
		download(list);
		logger.info("countSkip  {}", countSkip);
		logger.info("save  {}  {}", list.size(), StorageJPX.StockList.getPath());
		StorageJPX.StockList.save(list);
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}

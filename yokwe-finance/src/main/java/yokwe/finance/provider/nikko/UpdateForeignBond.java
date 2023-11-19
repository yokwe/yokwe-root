package yokwe.finance.provider.nikko;

import java.util.List;
import java.util.regex.Pattern;

import yokwe.finance.Storage;
import yokwe.finance.account.nikko.WebBrowserNikko;
import yokwe.finance.provider.nikko.UpdateTradingStockNikko.StockInfo;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;

public class UpdateForeignBond {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	
	private static String getPath(int pageNo) {
		return StorageNikko.getPath("foreign-bond", String.format("%03d.html", pageNo));
	}

	private static int download() {
		try(var browser = new WebBrowserNikko()) {
			logger.info("login");
			browser.login();
			
			logger.info("trade");
			browser.trade();
			
			logger.info("listForeignBond");
			browser.listForeignBond();
			
			int pageNo = 0;
			for(;;) {
				logger.info("page  {}", pageNo);
				String page = browser.getPage();
				FileUtil.write().file(getPath(pageNo), page);
				
				if (page.contains("次の30件")) {
					pageNo++;
					browser.next30Items();
					continue;
				}
				break;
			}
			
			return pageNo;
		}
	}
	
	
	public static class BondInfo {
		//<tr>
		//  <td rowspan="2" align="center" nowrap class="td01_4"><span class="txt_01_1">
		//	USD<br>
		//  	<img src="/common/img/ico_flagusd.gif"  width="23" height="16" alt="USD"> </span></td>
		//  <td rowspan="2" align="left" class="td01"><span class="txt_01_1">アメリカンホンダファイナンス<br></span></td>
		//  <td align="center" nowrap class="td01"><span class="txt_01_1">5.8％</span></td>
		//  <td rowspan="2" align="center" nowrap class="td01"><span class="txt_01_1">100.85</span></td>
		//  <td align="center" nowrap class="td01"><span class="txt_01_1">2000USD</span></td>
		//  <td align="center" nowrap class="td01"><span class="txt_01_1">2025/10/03</span></td>
		//  <td rowspan="2" align="center" nowrap class="td01"><span class="txt_01_1">4/3<BR>10/3</span></td>
		//  <td rowspan="2" align="center" nowrap class="td01"><span class="txt_01_1">A-(S&P)/<BR>*A3(Moody's)</span></td>             
		//  <td rowspan="2" align="center" nowrap class="td01">
		//  
		//  <a href="/StockOrderConfirmation/550ED0305294/foreignbond/kihatu/order/memo?meigCd=  0091612343&hikTorkno=954&ukewatasibi=20231115&tanka=100.85&corsebetuToriatK=03&odrExecYoteiYmd=20231113"><img src="/rsc/image/btn_trade_mmf-01_01.gif" alt="" width="43" height="18" border="0"></a>
		//  				
		//  </td></tr>
		//<tr>          
		//  <td align="center" nowrap class="td01"><span class="txt_01_1">5.31％</span></td>
		//  <td align="center" nowrap class="td01"><span class="txt_01_1">2000USD</span></td>
		//  <td align="center" nowrap class="td01"><span class="txt_01_1">1年11ヶ月</span></td>
		//</tr>
		
		public static List<StockInfo> getInstance(String page) {
			return ScrapeUtil.getList(StockInfo.class, PAT, page);
		}

		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s+" +
				"</td><tr>\\s+" +
				"<tr>\\s+" +
				"<td .+><span .+?>(?<yield>.+?)</span></td>\\s+" +
				"<td .+><span .+?>(?<unitPrice>.+?)</span></td>\\s+" +
				"<td .+><span .+?>.+?</span></td>\\s+" +
				"</tr>" +
			
				"", Pattern.DOTALL);
		
		public String currency;       // 通貨
		public String name;           // 銘柄名
		public String interestRate;   // 利率
		public String price;          // 申込価格
		public String initialPrice;   // 申込価格
		public String redemptionDate; // 申込単位
		public String paymentDate;    // 利払日
		public String url;
		
		public String yield;          // 利回り
		public String unitPrice;      // 申込単位

		
		public BondInfo(
			String currency,
			String name,
			String interestRate,
			String price,
			String initialPrice,
			String redemptionDate,
			String paymentDate,
			String url,
			String yield,
			String unitPrice
			) {
			this.currency      = currency;
			this.name           = name;
			this.interestRate   = interestRate;
			this.price          = price;
			this.initialPrice   = initialPrice;
			this.redemptionDate = redemptionDate;
			this.paymentDate    = paymentDate;
			this.url            = url;
			this.yield          = yield;
			this.unitPrice      = unitPrice;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	
	private static void update() {
		Storage.initialize();
		
		var pageNoMax = download();
		logger.info("pageNoMax  {}", pageNoMax);
		
		// FIXME
	}
	
	public static void main(String[] args) {
		logger.info("START");
				
		update();
		
		logger.info("STOP");
	}
}

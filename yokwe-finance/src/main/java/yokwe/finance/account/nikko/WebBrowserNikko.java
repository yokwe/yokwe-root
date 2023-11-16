package yokwe.finance.account.nikko;

import java.util.regex.Pattern;

import org.openqa.selenium.By;

import yokwe.finance.account.Secret;
import yokwe.finance.util.WebBrowser;
import yokwe.util.UnexpectedException;

public class WebBrowserNikko extends WebBrowser {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public WebBrowserNikko() {
		super();
	}
	
	public static final String URL_LOGIN = "https://trade.smbcnikko.co.jp/Login/0/login/ipan_web/hyoji/";
	
	public void login() {
		var secret = Secret.read().nikko;
		login(secret.branch, secret.account, secret.password);
	}
	public void login(String branch, String account, String password) {
		get(URL_LOGIN);
		wait.untilTitleContains("ログイン");
		
		var elementBranch   = wait.untilPresenceOfElement(By.name("koza1"));
		var elementAccount  = wait.untilPresenceOfElement(By.name("koza2"));
		var elementPassword = wait.untilPresenceOfElement(By.name("passwd"));
		
		elementBranch.sendKeys(branch);
		elementAccount.sendKeys(account);
		elementPassword.sendKeys(password);
		
		click(By.name("logIn"));
		wait.untilTitleContains("トップ");
	}
	
	
	private class GetInfo {
		public static final Pattern PAT  = Pattern.compile("/Logout/(?<id>[0-9A-Z]+)/login/ipan_logout/exec");
		final protected String format;
		final protected String title;
		
		GetInfo(String format, String title) {
			this.format  = format;
			this.title   = title;
		}
		
		protected String getID() {
			var page = getPage();
			var m = PAT.matcher(page);
			if (m.find()) {
				return m.group("id");
			} else {
				logger.error("no logout url");
				logger.error("page {}", page);
				throw new UnexpectedException("no logout url");
			}
		}
		private String getURL() {
			return String.format(format, getID());
		}
		void getAndWait(WebBrowser browser) {
			browser.get(getURL());
			browser.wait.untilTitleContains(title);
		}
	}
	private class GetInfo2 extends GetInfo {
		GetInfo2(String format, String title) {
			super(format, title);
		}
		private String getURL(int no) {
			return String.format(format, getID(), no);
		}
		void getAndWait(WebBrowser browser, int no) {
			browser.get(getURL(no));
			browser.wait.untilTitleContains(title);
		}
	}
	
	private final GetInfo logout =
		new GetInfo("https://trade.smbcnikko.co.jp/Logout/%s/login/ipan_logout/exec",
			"ログアウト");
	private final GetInfo trade =
		new GetInfo("https://trade.smbcnikko.co.jp/MoneyManagement/%s/syohin/torihikiguide",
			"お取引");
	private final GetInfo balance =
		new GetInfo("https://trade.smbcnikko.co.jp/MoneyManagement/%s/sisan/zan_sykai/hyji",
			"口座残高");
	private final GetInfo listStockUS =
		new GetInfo("https://trade.smbcnikko.co.jp/StockOrderConfirmation/%s/usa/meig/toriatukai/ichiran/search?kenskF=1",
			"米国株式 - 取扱銘柄一覧");
	private final GetInfo2 listBondForeign =
		new GetInfo2("https://trade.smbcnikko.co.jp/StockOrderConfirmation/%s/foreignbond/kihatu/meigara/ichiran?hyojiPage=%d",
			"外国債券 - 取扱銘柄一覧");
	
	public void logout() {
		logout.getAndWait(this);
	}
	public void balance() {
		balance.getAndWait(this);
	}
	public void trade() {
		trade.getAndWait(this);
	}
	public void listStockUS() {
		listStockUS.getAndWait(this);
	}
	public void listBondForeign(int hyojiPage) {
		listBondForeign.getAndWait(this, hyojiPage);
	}
}

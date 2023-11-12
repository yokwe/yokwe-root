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
	
	public static final String URL_LOGIN = "https://trade.smbcnikko.co.jp/Etc/1/webtoppage/";
	
	public void login() {
		var secret = Secret.read().nikko;
		login(secret.branch, secret.account, secret.password);
	}
	public void login(String branch, String account, String password) {
		getAndWait(URL_LOGIN, "ログイン");
		
		var elementBranch   = waitUntilPresence(By.name("koza1"));
		var elementAccount  = waitUntilPresence(By.name("koza2"));
		var elementPassword = waitUntilPresence(By.name("passwd"));
		
		elementBranch.sendKeys(branch);
		elementAccount.sendKeys(account);
		elementPassword.sendKeys(password);
		
		clickAndWait(By.name("logIn"), "トップ");		
	}
	
	
	private static class GetInfo {
		public static final Pattern PAT  = Pattern.compile("/Logout/(?<id>[0-9A-F]+)/login/ipan_logout/exec");
		
		final String format;
		final String title;
		
		GetInfo(String format, String title) {
			this.format  = format;
			this.title   = title;
		}
		
		private String getID(WebBrowser browser) {
			var page = browser.getPage();
			var m = PAT.matcher(page);
			if (m.find()) {
				return m.group("id");
			} else {
				logger.error("no logout url");
				throw new UnexpectedException("no logout url");
			}
		}
		private String getURL(WebBrowser browser) {
			return String.format(format, getID(browser));
		}
		void getAndWait(WebBrowser browser) {
			browser.getAndWait(getURL(browser), title);
		}
	}
	
	private static final GetInfo logout =
		new GetInfo("https://trade.smbcnikko.co.jp/Logout/%s/login/ipan_logout/exec",
			"ログアウト");
	private static final GetInfo trade =
		new GetInfo("https://trade.smbcnikko.co.jp/MoneyManagement/%s/syohin/torihikiguide",
			"お取引");
	private static final GetInfo balance =
		new GetInfo("https://trade.smbcnikko.co.jp/MoneyManagement/%s/sisan/zan_sykai/hyji",
			"口座残高");
	private static final GetInfo listStockUS =
		new GetInfo("https://trade.smbcnikko.co.jp/StockOrderConfirmation/%s/usa/meig/toriatukai/ichiran/search?kenskF=1",
			"米国株式 - 取扱銘柄一覧");
	private static final GetInfo listBondForeign =
		new GetInfo("https://trade.smbcnikko.co.jp/StockOrderConfirmation/%s/usa/meig/toriatukai/ichiran/search?kenskF=1",
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
	public void listBondForein() {
		listBondForeign.getAndWait(this);
	}
}

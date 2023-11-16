package yokwe.finance.account.rakuten;

import org.openqa.selenium.By;

import yokwe.finance.account.Secret;
import yokwe.finance.util.WebBrowser;

public class WebBrowserRakuten extends WebBrowser {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public WebBrowserRakuten() {
		super();
	}
	
	public static final String URL_LOGIN = "https://www.rakuten-sec.co.jp/ITS/V_ACT_Login.html";
	
	public void login() {
		var secret = Secret.read().rakuten;
		login(secret.account, secret.password);
	}
	public void login(String account, String password) {
		get(URL_LOGIN);
		wait.untilTitleContains("総合口座ログイン | 楽天証券");
		
		var elementAccount  = wait.untilPresenceOfElement(By.name("loginid"));
		var elementPassword = wait.untilPresenceOfElement(By.name("passwd"));
		
		elementAccount.sendKeys(account);
		elementPassword.sendKeys(password);
		
		// name contains "$" this makes error
		// use id instead
		click(By.id("login-btn"));
		wait.untilTitleContains("ホーム");
	}
	
	public void logout() {
		logger.info("logout");
		javaScript("logoutDialog()");
		
		wait.untilAlertIsPresent().accept();
		wait.untilPresenceOfWindow("ログアウト");
	}
}

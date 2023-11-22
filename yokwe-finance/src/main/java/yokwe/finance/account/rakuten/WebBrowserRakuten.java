package yokwe.finance.account.rakuten;

import org.openqa.selenium.By;

import yokwe.finance.account.Secret;
import yokwe.finance.util.webbrowser.Target;
import yokwe.finance.util.webbrowser.WebBrowser;

public class WebBrowserRakuten extends WebBrowser {
	private static final Target LOGIN_A = new Target.Get("https://www.rakuten-sec.co.jp/ITS/V_ACT_Login.html", "総合口座ログイン | 楽天証券");
	private static final Target LOGIN_B = new Target.Click(By.id("login-btn"), "ホーム");
	private static final Target LOGOUT  = new Target.Javascript("logoutDialog()");
	
	public WebBrowserRakuten() {
		super();
	}
	
	public void login() {
		var secret = Secret.read().rakuten;
		login(secret.account, secret.password);
	}
	public void login(String account, String password) {
		LOGIN_A.action(this);
		
		sendKey(By.name("loginid"), account);
		sendKey(By.name("passwd"),  password);
		
		LOGIN_B.action(this);
	}
	
	public void logout() {
		LOGOUT.action(this);
		wait.untilAlertIsPresent().accept();
	}
}

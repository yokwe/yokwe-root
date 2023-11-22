package yokwe.finance.account.smtb;

import org.openqa.selenium.By;

import yokwe.finance.account.Secret;
import yokwe.finance.util.webbrowser.Target;
import yokwe.finance.util.webbrowser.WebBrowser;

public class WebBrowserSMTB extends WebBrowser {
	public WebBrowserSMTB() {
		super();
	}
	
	private static final Target LOGIN_A = new Target.Get("https://direct.smtb.jp/ap1/ib/login.do", "ログイン");
	private static final Target LOGIN_B = new Target.Javascript("onPrepareElementForIbLogin('ibLoginActionForm', 'login', '10', this)", "トップページ");
	private static final Target LOGOUT  = new Target.Javascript("linkSubmitAction('header_myPageForm', 'initial')", "ログアウト");
	private static final Target BALANCE = new Target.Click(By.linkText("お取引・残高照会"), "お取引・残高照会");
	
	public void login() {
		var secret = Secret.read().smtb;
		login(secret.account, secret.password);
	}
	public void login(String account, String password) {
		LOGIN_A.action(this);
		
		sendKey(By.name("kaiinNo"),    account);
		sendKey(By.name("ibpassword"), password);
		
		LOGIN_B.action(this);
	}
	
	public void logout() {
		LOGOUT.action(this);
	}
	
	public void balance() {
		BALANCE.action(this);
	}
}

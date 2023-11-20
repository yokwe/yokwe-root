package yokwe.finance.account.smtb;

import org.openqa.selenium.By;

import yokwe.finance.account.Secret;
import yokwe.finance.util.WebBrowser;

public class WebBrowserSMTB extends WebBrowser {
	public WebBrowserSMTB() {
		super();
	}
	
	private static final Target LOGIN_A = new Target.GetImpl("https://direct.smtb.jp/ap1/ib/login.do", "ログイン");
	private static final Target LOGIN_B = new Target.JavascriptImpl("onPrepareElementForIbLogin('ibLoginActionForm', 'login', '10', this)", "トップページ");
	private static final Target LOGOUT  = new Target.JavascriptImpl("linkSubmitAction('header_myPageForm', 'initial')", "ログアウト");

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
}

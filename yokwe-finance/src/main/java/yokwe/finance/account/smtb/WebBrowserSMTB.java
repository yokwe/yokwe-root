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
	private static final Target LOGIN_B = new Target.Click(By.xpath("//input[contains(@value, 'ログイン')]"), "トップページ");
	
	private static final Target LOGOUT  = new Target.Click(By.xpath("//img[@alt='ログアウト']"), "ログアウト");
	
	// お取引き・残高照会
	private static final Target BALANCE = new Target.Click(By.xpath("//img[@alt='お取引き・残高照会']"), "お取引・残高照会");
	
	//  残高明細・売却
	private static final Target FUND = new Target.Click(By.xpath("//input[contains(@value, '残高明細・売却')]"), "投資信託売却｜保管残高明細");
	
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
	
	public void fund() {
		FUND.action(this);
	}
	
}

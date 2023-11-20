package yokwe.finance.account.sony;

import org.openqa.selenium.By;

import yokwe.finance.account.Secret;
import yokwe.finance.util.WebBrowser;

public class WebBrowserSony extends WebBrowser {
	private static final Target LOGIN_A = new Target.GetImpl("https://o2o.moneykit.net/NBG100001G01.html", "ログイン");
	private static final Target LOGIN_B = new Target.ClickImpl(By.linkText("ログイン"), "MONEYKit - ソニー銀行");
	
	private static final Target LOGOUT_A = new Target.ClickImpl(By.id("logout"));
	private static final Target LOGOUT_B = new Target.JavascriptImpl("subYes()", "THANK YOU");
	private static final Target LOGOUT_C = new Target.JavascriptImpl("allClose()");
	
	private static final Target TOP     = new Target.JavascriptImpl("hometop(1)");
	
	private static final Target BALANCE                 = new Target.JavascriptImpl("hometop(10)");
	private static final Target BALANCE_DEPOSIT         = new Target.JavascriptImpl("balancecommon(1)");
	private static final Target BALANCE_DEPOSIT_FOREIGN = new Target.JavascriptImpl("balancecommon(2)");
	private static final Target BALANCE_FUND            = new Target.JavascriptImpl("balancecommon(3)");
	
	
	public WebBrowserSony() {
		super();
	}
	
	public void login() {
		var secret = Secret.read().sony;
		login(secret.account, secret.password);
	}
	public void login(String account, String password) {
		LOGIN_A.action(this);
		sendKey(By.name("KozaNo"),   account);
		sendKey(By.name("Password"), password);
		
		LOGIN_B.action(this);
	}
	
	public void logout() {
		LOGOUT_A.action(this);
		switchToByTitleContains("ログアウト");
		
		LOGOUT_B.action(this);
		LOGOUT_C.action(this);
	}
	
	public void top() {
		TOP.action(this);
	}
	
	public void balance() {
		BALANCE.action(this);
	}
	public void balanceDeopsit() {
		BALANCE_DEPOSIT.action(this);
	}
	public void balanceDeopsitForeign() {
		BALANCE_DEPOSIT_FOREIGN.action(this);
	}
	public void balanceFund() {
		BALANCE_FUND.action(this);
	}
}

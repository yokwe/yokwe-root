package yokwe.finance.account.sbi;

import org.openqa.selenium.By;

import yokwe.finance.account.Secret;
import yokwe.finance.util.webbrowser.Target;
import yokwe.finance.util.webbrowser.WebBrowser;

public class WebBrowserSBI extends WebBrowser {
	public WebBrowserSBI() {
		super();
	}
	
	private static final Target LOGIN_A = new Target.Get("https://www.sbisec.co.jp/ETGate", "SBI証券");
	private static final Target LOGIN_B = new Target.Click(By.name("ACT_login"));
	private static final Target LOGOUT  = new Target.Click(By.id("logout"));

	private static final Target BALANCE_JPY      = new Target.Click(By.linkText("口座(円建)"));
	private static final Target BALANCE_FOREIGN  = new Target.Click(By.linkText("口座(外貨建)"));

	public void login() {
		var secret = Secret.read().sbi;
		login(secret.account, secret.password);
	}
	public void login(String account, String password) {
		LOGIN_A.action(this);
		
		sendKey(By.name("user_id"),       account);
		sendKey(By.name("user_password"), password);
		
		LOGIN_B.action(this);
	}
	
	public void logout() {
		LOGOUT.action(this);
		wait.untilPageContains("ご利用いただきありがとうございました");
	}
	
	public void balanceJPY() {
		BALANCE_JPY.action(this);
	}
	public void balanceForeign() {
		BALANCE_FOREIGN.action(this);
	}
	
	// Use xpath to locate element
	// click rectangle area in map
	// driver.action.move_to(driver.find_element(:css, '.foo img'), 10, 10).click
	
	
}

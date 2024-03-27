package yokwe.finance.account.prestia;

import org.openqa.selenium.By;

import yokwe.finance.account.Secret;
import yokwe.finance.util.webbrowser.Target;
import yokwe.finance.util.webbrowser.WebBrowser;

public class WebBrowserPrestia extends WebBrowser {
	private static final Target LOGIN_A = new Target.Get("https://login.smbctb.co.jp/ib/portal/POSNIN1prestiatop.prst", "プレスティア オンライン");
	private static final Target LOGIN_B = new Target.Click(By.linkText("サインオン"));
	private static final Target LOGOUT  = new Target.Click(By.linkText("サインオフ"));
	
	private static final Target BALANCE_A = new Target.Click(By.id("header-nav-label-0"));
	private static final Target BALANCE_B = new Target.Click(By.linkText("口座残高"));
	
	private static final Target FUND_ENTER_A = new Target.Click(By.id("header-nav-label-3"));
	private static final Target FUND_ENTER_B = new Target.Click(By.linkText("投資信託サービス"), "インターネットバンキング投資信託");
	private static final Target FUND_RETURNS = new Target.Click(By.xpath("//*[@id=\"navi02_03\"]/li[4]/a")); // トータルリターン
	private static final Target FUND_EXIT    = new Target.Click(By.xpath("//*[@id=\"header\"]/img[1]"), "プレスティア オンライン");
	
	public WebBrowserPrestia() {
		super();
	}
	
	public void login() {
		var secret = Secret.read().prestia;
		login(secret.account, secret.password);
	}
	public void login(String account, String password) {
		LOGIN_A.action(this);
		
		// To prevent pop up dialog for new login, use 0 for sleep
		sendKey(By.id("dispuserId"),   account, 0);
		sendKey(By.id("disppassword"), password, 0);
		
		LOGIN_B.action(this);
		wait.untilPageContains("代表口座");
	}
	//*[@id="header-nav-menu-0"]/li[1]/a
	public void logout() {
		LOGOUT.action(this);
		wait.untilPageContains("サインオフが完了しました");
	}
	public void balance() {
		BALANCE_A.action(this);
		BALANCE_B.action(this);
	}
	public void fundEnter() {
		FUND_ENTER_A.action(this);
		FUND_ENTER_B.action(this);
	}
	public void fundReturns() {
		// hover mouse to navi02_03_active
		moveMouse(By.id("navi02_03_active"));
		
		// click 
		FUND_RETURNS.action(this);
	}
	public void fundExit() {
		FUND_EXIT.action(this);
	}
}

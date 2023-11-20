package yokwe.finance.account.prestia;

import org.openqa.selenium.By;

import yokwe.finance.account.Secret;
import yokwe.finance.util.WebBrowser;

public class WebBrowserPrestia extends WebBrowser {
	private static final Target LOGIN_A = new Target.GetImpl("https://login.smbctb.co.jp/ib/portal/POSNIN1prestiatop.prst", "プレスティア オンライン");
	private static final Target LOGIN_B = new Target.ClickImpl(By.linkText("サインオン"));
	private static final Target LOGOUT  = new Target.ClickImpl(By.linkText("サインオフ"));
	
	
	public WebBrowserPrestia() {
		super();
	}
	
	public void login() {
		var secret = Secret.read().prestia;
		login(secret.account, secret.password);
	}
	public void login(String account, String password) {
		LOGIN_A.action(this);
		
		sendKey(By.id("dispuserId"),   account);
		sendKey(By.id("disppassword"), password);
		
		LOGIN_B.action(this);
		wait.untilPageContains("代表口座");
	}
	
	public void logout() {
		LOGOUT.action(this);
		wait.untilPageContains("サインオフが完了しました");
	}


}

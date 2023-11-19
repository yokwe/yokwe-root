package yokwe.finance.account.smbc;

import org.openqa.selenium.By;

import yokwe.finance.account.Secret;
import yokwe.finance.util.WebBrowser;

public class WebBrowserSMBC extends WebBrowser {
	public WebBrowserSMBC() {
		super();
	}
	
	private static final Target LOGIN_A = new Target.GetImpl("https://direct.smbc.co.jp/aib/aibgsjsw5001.jsp", "ログイン");
	private static final Target LOGIN_B = new Target.JavascriptImpl("directib.LLDLDIL.login();", "トップ");
	private static final Target LOGOUT  = new Target.JavascriptImpl("doTransaction('/loginlogout/TPALTOPlogout1',null,false,null,DIRECTHEADERFORM,null,null);", "ご利用ありがとうございました");

	public void login() {
		var secret = Secret.read().smbc;
		login(secret.branch, secret.account, secret.password);
	}
	public void login(String branch, String account, String password) {
		LOGIN_A.action(this);
		
		sendKey(By.name("branchNo"),  branch);
		sendKey(By.name("accountNo"), account);
		sendKey(By.name("password"),  password);
		
		LOGIN_B.action(this);
	}
	
	public void logout() {
		LOGOUT.action(this);
	}


}

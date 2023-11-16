package yokwe.finance.account.smbc;

import org.openqa.selenium.By;

import yokwe.finance.account.Secret;
import yokwe.finance.util.WebBrowser;

public class WebBrowserSMBC extends WebBrowser {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public WebBrowserSMBC() {
		super();
	}
	
	public static final String URL_LOGIN = "https://direct.smbc.co.jp/aib/aibgsjsw5001.jsp";
	
	public void login() {
		var secret = Secret.read().smbc;
		login(secret.branch, secret.account, secret.password);
	}
	public void login(String branch, String account, String password) {
		get(URL_LOGIN);
		wait.untilTitleContains("ログイン");
		
		var elementBranch   = wait.untilPresenceOfElement(By.name("branchNo"));
		var elementAccount  = wait.untilPresenceOfElement(By.name("accountNo"));
		var elementPassword = wait.untilPresenceOfElement(By.name("password"));
		
		elementBranch.sendKeys(branch);
		sleepRandom(500);
		elementAccount.sendKeys(account);
		sleepRandom(500);
		elementPassword.sendKeys(password);
		sleepRandom(500);
		
		javaScript("directib.LLDLDIL.login();");
		sleepRandom();

		wait.untilTitleContains("トップ");
	}
	
	public void logout() {
		logger.info("logout");
		javaScript("doTransaction('/loginlogout/TPALTOPlogout1',null,false,null,DIRECTHEADERFORM,null,null);");
		logger.info("AA");
		wait.untilTitleContains("ご利用ありがとうございました");
		logger.info("BB");
	}


}

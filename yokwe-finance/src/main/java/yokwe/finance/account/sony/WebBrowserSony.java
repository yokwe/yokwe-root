package yokwe.finance.account.sony;

import org.openqa.selenium.By;

import yokwe.finance.account.Secret;
import yokwe.finance.util.WebBrowser;

public class WebBrowserSony extends WebBrowser {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public WebBrowserSony() {
		super();
	}
	
	public static final String URL_LOGIN = "https://o2o.moneykit.net/NBG100001G01.html";
	
	public void login() {
		var secret = Secret.read().sony;
		login(secret.account, secret.password);
	}
	public void login(String account, String password) {
		get(URL_LOGIN);
		wait.untilTitleContains("ログイン");
		
		var elementAccount  = wait.untilPresenceOfElement(By.name("KozaNo"));
		var elementPassword = wait.untilPresenceOfElement(By.name("Password"));
		
		elementAccount.sendKeys(account);
		elementPassword.sendKeys(password);
		
//		clickAndWait(By.linkText("ログイン"), "");
		javaScript("mySubmitNBG100001G01(document.HOST, 1)");
		sleepRandom();
	}
	
	public void logout() {
		logger.info("logout");
		javaScript("logout()");
		wait.untilTitleContains("");
		
		logger.info("switchTo");
		switchToByTitleContains("ログアウト");
		sleepRandom(1000);
		
		logger.info("subYes");
		javaScript("subYes()");
		
		logger.info("switchTo");
		switchToByTitleContains("THANK YOU");
		sleepRandom(1000);
		
		logger.info("allClose");
		javaScript("allClose()");
	}
}

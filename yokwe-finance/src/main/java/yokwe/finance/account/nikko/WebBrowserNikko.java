package yokwe.finance.account.nikko;

import org.openqa.selenium.By;

import yokwe.finance.account.Secret;
import yokwe.finance.util.WebBrowser;

public class WebBrowserNikko extends WebBrowser {
	private static final Target LOGIN_A = new Target.GetImpl("https://trade.smbcnikko.co.jp/Login/0/login/ipan_web/hyoji/", "ログイン");
	private static final Target LOGIN_B = new Target.ClickImpl(By.name("logIn"), "トップ");

	private static final Target LOGOUT  = new Target.ClickImpl(By.name("btn_logout"), "ログアウト");
	
	private static final Target BALANSE = new Target.ClickImpl(By.name("menu04"), "口座残高");
	private static final Target TRADE   = new Target.ClickImpl(By.name("menu03"), "お取引");
	
	private static final Target LIST_STOCK_US     = new Target.ClickImpl(By.linkText("米国株式"), "米国株式 - 取扱銘柄一覧");
	private static final Target LIST_FOREIGN_BOND = new Target.ClickImpl(By.linkText("外国債券"), "外国債券 - 取扱銘柄一覧");
	private static final Target NEXT_30_ITEMS     = new Target.ClickImpl(By.linkText("次の30件"));
	

	public void login() {
		var secret = Secret.read().nikko;
		login(secret.branch, secret.account, secret.password);
	}
	public void login(String branch, String account, String password) {
		LOGIN_A.action(this);
		
		sendKey(By.name("koza1"),  branch);
		sendKey(By.name("koza2"),  account);
		sendKey(By.name("passwd"), password);
		
		LOGIN_B.action(this);
	}
	public void logout() {
		LOGOUT.action(this);
	}
	public void balance() {
		BALANSE.action(this);
	}
	public void trade() {
		TRADE.action(this);
	}
	public void listStockUS() {
		LIST_STOCK_US.action(this);
	}
	public void listForeignBond() {
		LIST_FOREIGN_BOND.action(this);
	}
	public void next30Items() {
		NEXT_30_ITEMS.action(this);
	}
}

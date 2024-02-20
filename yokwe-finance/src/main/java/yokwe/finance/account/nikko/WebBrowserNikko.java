package yokwe.finance.account.nikko;

import java.io.File;

import org.openqa.selenium.By;

import yokwe.finance.account.Secret;
import yokwe.finance.util.webbrowser.Target;
import yokwe.finance.util.webbrowser.WebBrowser;

public class WebBrowserNikko extends WebBrowser {
	private static final Target LOGIN_A = new Target.Get("https://trade.smbcnikko.co.jp/Login/0/login/ipan_web/hyoji/", "ログイン");
	private static final Target LOGIN_B = new Target.Click(By.name("logIn"), "トップ");

	private static final Target LOGOUT  = new Target.Click(By.name("btn_logout"), "ログアウト");
	
	private static final Target BALANCE      = new Target.Click(By.name("menu04"), "口座残高");
	private static final Target BALANCE_BANK = new Target.Click(By.linkText("銀行・証券残高一覧"), "銀行・証券残高一覧");
	
	private static final Target TRADE                   = new Target.Click(By.name("menu03"), "お取引");
	private static final Target TRADE_LIST_STOCK_US     = new Target.Click(By.linkText("米国株式"), "米国株式 - 取扱銘柄一覧");
	private static final Target TRADE_LIST_FOREIGN_BOND = new Target.Click(By.linkText("外国債券"), "外国債券 - 取扱銘柄一覧");
	
	private static final Target NEXT_30_ITEMS     = new Target.Click(By.linkText("次の30件"));
	
	private static final Target TRADE_HISTORY           = new Target.Click(By.linkText("お取引履歴"), "お取引履歴 - 検索");
	private static final Target TRADE_HISTORY_3_MONTH   = new Target.Click(By.xpath("//input[@id='term02']"));
	private static final Target TRADE_HISTORY_DOWNLOAD  = new Target.Click(By.xpath("//input[@id='dlBtn']"));

	
	public WebBrowserNikko(File file) {
		super(file);
	}
	public WebBrowserNikko() {
		super();
	}
	
	
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
		BALANCE.action(this);
	}
	public void balanceBank() {
		BALANCE_BANK.action(this);
	}
	public void trade() {
		TRADE.action(this);
	}
	public void listStockUS() {
		TRADE_LIST_STOCK_US.action(this);
	}
	public void listForeignBond() {
		TRADE_LIST_FOREIGN_BOND.action(this);
	}
	public void next30Items() {
		NEXT_30_ITEMS.action(this);
	}
	public void tradeHistory() {
		TRADE_HISTORY.action(this);
	}
	public void tradeHistoryDownload() {
		TRADE_HISTORY_3_MONTH.action(this);
		TRADE_HISTORY_DOWNLOAD.action(this);
	}
}

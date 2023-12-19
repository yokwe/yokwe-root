package yokwe.finance.account.rakuten;

import java.io.File;

import org.openqa.selenium.By;

import yokwe.finance.account.Secret;
import yokwe.finance.util.webbrowser.Target;
import yokwe.finance.util.webbrowser.WebBrowser;

public class WebBrowserRakuten extends WebBrowser {
	private static final Target LOGIN_A = new Target.Get("https://www.rakuten-sec.co.jp/ITS/V_ACT_Login.html", "総合口座ログイン | 楽天証券");
	private static final Target LOGIN_B = new Target.Click(By.id("login-btn"), "ホーム");
	private static final Target LOGOUT  = new Target.Javascript("logoutDialog()");
	
	private static final Target MY_MENU         = new Target.Click(By.xpath("//span[@class='pcm-gl-g-header-mymenu-btn']"));	
	private static final Target MY_MENU_BALANCE = new Target.Click(By.xpath("//a[text()='保有商品一覧']"),    "保有商品一覧-すべて");
	
	private static final Target SAVE_AS_CSV = new Target.Click(By.xpath("//img[@alt='CSVで保存']"));
		
	public WebBrowserRakuten(File file) {
		super(file);
	}
	
	public void login() {
		var secret = Secret.read().rakuten;
		login(secret.account, secret.password);
	}
	public void login(String account, String password) {
		LOGIN_A.action(this);
		
		sendKey(By.name("loginid"), account);
		sendKey(By.name("passwd"),  password);
		
		LOGIN_B.action(this);
		sleepRandom();
	}
	
	public void logout() {
		LOGOUT.action(this);
		wait.untilAlertIsPresent().accept();
		sleepRandom();
	}
	
	public void balance() {
		MY_MENU.action(this);
		MY_MENU_BALANCE.action(this);
		// pause before return
		sleepRandom(1500);
	}
	public void saveAsCSV() {
		SAVE_AS_CSV.action(this);
		sleepRandom();
	}
}

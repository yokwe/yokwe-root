package yokwe.finance.account.nikko;

import java.time.Duration;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import yokwe.finance.util.SeleniumUtil;

public class AccountNikko {
	public static final String URL_LOGIN = "https://trade.smbcnikko.co.jp/Login/0/login/ipan_web/hyoji/";
	
	public static void login(WebDriver driver, String branch, String account, String password) {
		// <input name="koza1" type="text" size="10" maxlength="3" tabindex="1" onfocus="change_focus(0)" id="padInput0" class="padInput">
		// <input name="koza2" type="text" size="10" maxlength="6" tabindex="2" onfocus="change_focus(1)" id="padInput1" class="padInput">
		// <input type="password" onfocus="change_focus(2)" name="passwd" id="padInput2" maxlength="16" tabindex="3" value="" autocomplete="on" class="padInput">
		// <input type="image" src="/common_2015/img/login_help_btn_001.gif" alt="ログイン" name="logIn" tabindex="4">s		
		driver.get(URL_LOGIN);
		SeleniumUtil.waitUntilTitleContains(driver, "ログイン");
		
		var elementBranch   = SeleniumUtil.waitUntilPresence(driver, By.name("koza1"));
		var elementAccount  = SeleniumUtil.waitUntilPresence(driver, By.name("koza2"));
		var elementPassword = SeleniumUtil.waitUntilPresence(driver, By.name("passwd"));
		var elementButton   = SeleniumUtil.waitUntilPresence(driver, By.name("logIn"));
		
		elementBranch.sendKeys(branch);
		sleepShort();
		elementAccount.sendKeys(account);
		sleepShort();
		elementPassword.sendKeys(password);
		sleepShort();
		
		elementButton.click();
		SeleniumUtil.waitUntilTitleContains(driver, "トップ");
		sleepShort();
	}
	public static void logout(WebDriver driver) {
		SeleniumUtil.waitUntilPresence(driver, By.name("btn_logout")).click();
		SeleniumUtil.waitUntilTitleContains(driver, "ログアウト");
		sleepShort();
	}
	
	public static void firstPageUSStock(WebDriver driver) {		
		SeleniumUtil.waitUntilPresence(driver, By.name("menu03")).click();
		SeleniumUtil.waitUntilTitleContains(driver, "お取引");
		sleepShort();

		SeleniumUtil.waitUntilPresence(driver, By.linkText("米国株式")).click();
		SeleniumUtil.waitUntilTitleContains(driver, "米国株式 - 取扱銘柄一覧");
		sleepShort();
	}
	public static boolean nextPageUSStock(WebDriver driver) {		
		var page = driver.getPageSource();
		if (page.contains("次の30件")) {
			SeleniumUtil.waitUntilPresence(driver, By.linkText("次の30件")).click();
			sleepShort();
			return true;
		} else {
			return false;
		}
	}
	
	public static void balance(WebDriver driver) {
		SeleniumUtil.waitUntilPresence(driver, By.name("menu04")).click();
		SeleniumUtil.waitUntilTitleContains(driver, "口座残高");
		sleepShort();
	}

	private static Random random = new Random(System.currentTimeMillis());
	
	public static void sleepShort() {
		long milli = (long)(random.nextDouble() * 3000 + 1000);
		SeleniumUtil.sleep(Duration.ofMillis(milli));
	}
	public static void sleepLong() {
		long milli = (long)(random.nextDouble() * 5000 + 5000);
		SeleniumUtil.sleep(Duration.ofMillis(milli));
	}
}

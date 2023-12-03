package yokwe.finance.account.nikko;

import java.io.File;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.By;

import yokwe.finance.Storage;
import yokwe.finance.account.Secret;
import yokwe.finance.util.webbrowser.Target;
import yokwe.finance.util.webbrowser.WebBrowser;
import yokwe.util.FileUtil;

public class WebBrowserNikko extends WebBrowser {
	private static final Storage STORAGE = Storage.account.nikko;
	
	public static final File DOWNLOAD_DIR  = STORAGE.getFile("download");
	public static final File TORIREKI_FILE = STORAGE.getFile("Torireki.csv");
	
	private static final Charset FILE_CHARSET = Charset.forName("SJIS");
	private static void convertCharset(File fileRead, File fileWrite, Charset charset) {
		String string = FileUtil.read().withCharset(charset).file(fileRead);
		FileUtil.write().file(fileWrite, string);
	}
	private static final String TIMESTAMP  = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now());
	public static File getDownloadCSVFile(String name) {
		var path = String.format("%s/%s%s.csv", DOWNLOAD_DIR.getAbsoluteFile(), name, TIMESTAMP);
		return new File(path);
	}
	
	
	private static final Target LOGIN_A = new Target.Get("https://trade.smbcnikko.co.jp/Login/0/login/ipan_web/hyoji/", "ログイン");
	private static final Target LOGIN_B = new Target.Click(By.name("logIn"), "トップ");

	private static final Target LOGOUT  = new Target.Click(By.name("btn_logout"), "ログアウト");
	
	private static final Target BALANCE      = new Target.Click(By.name("menu04"), "口座残高");
	private static final Target BALANCE_BANK = new Target.Click(By.linkText("銀行・証券残高一覧"), "銀行・証券残高一覧");
	
	private static final Target TRADE                   = new Target.Click(By.name("menu03"), "お取引");
	private static final Target TRADE_LIST_STOCK_US     = new Target.Click(By.linkText("米国株式"), "米国株式 - 取扱銘柄一覧");
	private static final Target TRADE_LIST_FOREIGN_BOND = new Target.Click(By.linkText("外国債券"), "外国債券 - 取扱銘柄一覧");
	
	private static final Target TRADE_HISTORY             = new Target.Click(By.linkText("お取引履歴"), "お取引履歴 - 検索");
	private static final Target TRADE_HISTORY_DURATION_M1 = new Target.Click(By.id("term01"));
	private static final Target TRADE_HISTORY_DURATION_M3 = new Target.Click(By.id("term02"));
	private static final Target TRADE_HISTORY_DURATION_Y1 = new Target.Click(By.id("term03"));
	private static final Target TRADE_HISTORY_DURATION_Y3 = new Target.Click(By.id("term04"));
	private static final Target TRADE_HISTORY_DOWNLOAD    = new Target.Click(By.xpath("//input[@alt='CSVダウンロード']"));
	
	private static final Target NEXT_30_ITEMS     = new Target.Click(By.linkText("次の30件"));
	
	public WebBrowserNikko() {
		super(DOWNLOAD_DIR);
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
	public void tradeHistoryM1() {
		TRADE_HISTORY_DURATION_M1.action(this);
	}
	public void tradeHistoryM3() {
		TRADE_HISTORY_DURATION_M3.action(this);
	}
	public void tradeHistoryY1() {
		TRADE_HISTORY_DURATION_Y1.action(this);
	}
	public void tradeHistoryY3() {
		TRADE_HISTORY_DURATION_Y3.action(this);
	}
	public void tradeHistoryDownload() {
		var downloadFile = getDownloadCSVFile("Torireki");
		if (downloadFile.exists()) downloadFile.delete();
		
		TRADE_HISTORY_DOWNLOAD.action(this);
		wait.untilDownloadFinish(downloadFile);
		
		// covert to utf8
		convertCharset(downloadFile, TORIREKI_FILE, FILE_CHARSET);
	}
}

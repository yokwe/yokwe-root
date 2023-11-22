package yokwe.finance.util.webbrowser;

import java.util.ArrayList;

import org.openqa.selenium.WebDriver;

//
// WindowInfo
//
public class WindowInfo {
	public static class Entry {
		public final String handle;
		public final String title;
		public final String url;
		
		public Entry(WebDriver driver) {
			handle = driver.getWindowHandle();
			title  = driver.getTitle();
			url    = driver.getCurrentUrl();
		}
		
		public boolean titleContains(String string) {
			return title.contains(string);
		}
		public boolean urlContains(String string) {
			return url.contains(string);
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %s}", handle, title, url);
		}
	}

	private final WindowInfo.Entry[]   array;
	
	public WindowInfo(WebDriver driver) {
		var list = new ArrayList<WindowInfo.Entry>();
		{
			// save current window
			String save = driver.getWindowHandle();

			for(var e: driver.getWindowHandles()) {
				driver.switchTo().window(e);
				list.add(new Entry(driver));
			}
			// restore current window
			driver.switchTo().window(save);
		}
		array = list.stream().toArray(WindowInfo.Entry[]::new);
	}
	
	public boolean titleContaisn(String string) {
		for(var e: array) {
			if (e.titleContains(string)) return true;
		}
		return false;
	}
	public boolean urlContaisn(String string) {
		for(var e: array) {
			if (e.urlContains(string)) return true;
		}
		return false;
	}
	public String getHandleByTitleContains(String string) {
		for(var e: array) {
			if (e.titleContains(string)) {
				return e.handle;
			}
		}
		return null;
	}
	public String getHandleByURLContains(String string) {
		for(var e: array) {
			if (e.urlContains(string)) {
				return e.handle;
			}
		}
		return null;
	}
}
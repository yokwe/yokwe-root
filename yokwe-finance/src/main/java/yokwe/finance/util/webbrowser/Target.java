package yokwe.finance.util.webbrowser;

import org.openqa.selenium.By;

import yokwe.util.UnexpectedException;

public interface Target {
	public String getString();
	public By     getLocator();
	
	public boolean hasTitle();
	public String  getTitle();
	
	public void    action(WebBrowser browser);
	
	public static abstract class Impl implements Target {
		protected final String string;
		protected final By     locator;
		protected final String title;
		protected final long   sleep;
		
		protected Impl(String string, By locator, String title, long sleep) {
			this.string  = string;
			this.locator = locator;
			this.title   = title;
			this.sleep   = sleep;
			checkSleep();
		}
		protected Impl(String string, String title, long sleep) {
			this(string, null, title, sleep);
			checkString();
		}
		protected Impl(String string, long sleep) {
			this(string, null, null, sleep);
			checkString();
		}
		protected Impl(By locator, String title, long sleep) {
			this(null, locator, title, sleep);
			checkLocator();
		}
		protected Impl(By locator, long sleep) {
			this(null, locator, null, sleep);
			checkLocator();
		}
		
		protected void checkString() {
			if (string == null) {
				logger.error("string is null");
				logger.error("  {}", toString());
				throw new UnexpectedException("string is null");
			}
		}
		protected void checkLocator() {
			if (locator == null) {
				logger.error("locator is null");
				logger.error("  {}", toString());
				throw new UnexpectedException("locator is null");
			}
		}
		protected void checkTitle() {
			if (title == null) {
				logger.error("title is null");
				logger.error("  {}", toString());
				throw new UnexpectedException("title is null");
			}
		}
		protected void checkSleep() {
			if (sleep < 0) {
				logger.error("sleep is negative");
				logger.error("  {}", toString());
				throw new UnexpectedException("sleep is negative");
			}
		}
		
		@Override
		public String toString() {
			if (string != null)  return String.format("{string  {}  {}  {}", string, title, sleep);
			if (locator != null) return String.format("{locator {}  {}  {}", locator, title, sleep);
			logger.error("  {}", toString());
			throw new UnexpectedException("Unexpected");
		}
		
		@Override
		public String getString() {
			checkString();
			return string;
		}
		@Override
		public By getLocator() {
			checkLocator();
			return locator;
		}
		@Override
		public boolean hasTitle() {
			return title != null;
		}
		@Override
		public String getTitle() {
			checkTitle();
			return title;
		}
	}
	
	public static class Get extends Target.Impl {
		public Get(String string, String title, long sleep) {
			super(string, null, title, sleep);
		}
		public Get(String string, String title) {
			this(string, title, WebBrowser.DEFAULT_SLEEP_MILLS);
		}
		public Get(String string, long sleep) {
			super(string, null, null, sleep);
		}
		public Get(String string) {
			this(string, WebBrowser.DEFAULT_SLEEP_MILLS);
		}
		
		@Override
		public void action(WebBrowser browser) {
			browser.get(getString());
			if (hasTitle()) browser.wait.untilTitleContains(getTitle());
			if (0 < sleep) browser.sleepRandom(sleep);
		}
	}
	public static class Click extends Target.Impl {
		public Click(By locator, String title, long sleep) {
			super(null, locator, title, sleep);
		}
		public Click(By locator, String title) {
			this(locator, title, WebBrowser.DEFAULT_SLEEP_MILLS);
		}
		public Click(By locator, long sleep) {
			super(null, locator, null, sleep);
		}
		public Click(By locator) {
			this(locator, WebBrowser.DEFAULT_SLEEP_MILLS);
		}
		
		@Override
		public void action(WebBrowser browser) {
			if (hasTitle()) {
				browser.wait.untilPresenceOfElement(getLocator()).click();
				browser.wait.untilTitleContains(getTitle());
			} else {
				var page = browser.getPage();
				browser.wait.untilPresenceOfElement(getLocator()).click();
				browser.wait.untilPageUpdate(page);
			}
			if (0 < sleep) browser.sleepRandom(sleep);
		}
	}
	public static class Javascript extends Target.Impl {
		public Javascript(String string, String title, long sleep) {
			super(string, null, title, sleep);
		}
		public Javascript(String string, String title) {
			this(string, title, WebBrowser.DEFAULT_SLEEP_MILLS);
		}
		public Javascript(String string, long sleep) {
			super(string, null, null, sleep);
		}
		public Javascript(String string) {
			this(string, WebBrowser.DEFAULT_SLEEP_MILLS);
		}
		
		@Override
		public void action(WebBrowser browser) {
			browser.javaScript(getString());
			if (hasTitle()) browser.wait.untilTitleContains(getTitle());
			if (0 < sleep) browser.sleepRandom(sleep);
		}
	}
}
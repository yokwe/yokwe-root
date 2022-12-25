package yokwe.stock.jp.xbrl.edinet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.xbrl.XBRL;
import yokwe.util.ListUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.xml.QValue;

public class Label implements Comparable<Label> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static class Key implements Comparable<Key> {
		final String namespace;
		final String label;
		final String role;
		final String lang;
		
		public Key(String namespace, String label, String role, String lang) {
			this.namespace = namespace;
			this.label     = label;
			this.role      = role;
			this.lang      = lang;
		}

		@Override
		public int compareTo(Key that) {
			int ret = this.namespace.compareTo(that.namespace);
			if (ret == 0) ret = this.label.compareTo(that.label);
			if (ret == 0) ret = this.role.compareTo(that.role);
			if (ret == 0) ret = this.lang.compareTo(that.lang);
			return ret;
		}
		
		@Override
		public String toString() {
			return String.format("{%s %s %s %s}", namespace, label, role, lang);
		}
	}
	private static Map<Key, String> cache = new TreeMap<>();

	private static final String PATH_DATA_FILE = Storage.XBRL.EDINET.getPath("label.csv");
	public static final String getPath() {
		return PATH_DATA_FILE;
	}
	public static List<Label> load() {
		return ListUtil.load(Label.class, getPath());
	}
	public static void save(Collection<Label> collection) {
		ListUtil.save(Label.class, getPath(), collection);
	}
	
	private static void fillCache() {
		List<Label> list = load();
		if (list == null) {
			logger.warn("no data file {}", PATH_DATA_FILE);
			list = new ArrayList<>();
		}
		for(Label e: list) {
			final String namespace = e.namespace;
			final String label     = e.label;
			final String role      = e.role;
			final String lang      = e.lang;
			final String value     = e.value;
			
			Key key = new Key(namespace, label, role, lang);

			if (cache.containsKey(key)) {
				logger.error("Duplicate entry {} {} {} {} \"{}\"", namespace, label, role, lang, value);
				throw new UnexpectedException("Duplicate entry");	
			} else {
				cache.put(key, value);
			}
		}
	}

	public static String getValueJA(QValue qValue) {
		return getValueJA(qValue.namespace, qValue.value);
	}
	public static String getValueJA(String namespace, String label) {
		return getValueJA(namespace, label, XBRL.ROLE_LABLE);
	}
	public static String getValueJA(String namespace, String label, String role) {
		return getValue(namespace, label, role, XBRL.LANG_JA);
	}
	public static String getValue(String namespace, String label, String role, String lang) {
		if (cache.isEmpty()) fillCache();
		Key key = new Key(namespace, label, role, lang);
		if (cache.containsKey(key)) {
			return cache.get(key);
		} else {
			logger.warn("Unknown namespace {} {} {} {}", namespace, label, role, lang);
		}
		return null;
	}
	
	
	public String namespace;
	public String label;
	public String role;
	public String lang;
	public String value;
	
	public Label(String namespace, String label, String role, String lang, String value) {
		this.namespace = namespace;
		this.label = label;
		this.role  = role;
		this.lang  = lang;
		this.value = value;
	}
	public Label() {
		this(null, null, null, null, null);
	}
	
	@Override
	public String toString() {
		return String.format("{%s %s %s %s \"%s\"}", namespace, label, role, lang, value);
	}

	@Override
	public int compareTo(Label that) {
		int ret = this.namespace.compareTo(that.namespace);
		if (ret == 0) ret = this.label.compareTo(that.label);
		if (ret == 0) ret = this.role.compareTo(that.role);
		if (ret == 0) ret = this.lang.compareTo(that.lang);
		if (ret == 0) ret = this.value.compareTo(that.value);
		return ret;
	}
}

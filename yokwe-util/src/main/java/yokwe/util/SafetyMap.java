package yokwe.util;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class SafetyMap<K, V> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private Map<K, V> map = new TreeMap<>();
	
	public void put(K key, V value) {
		if (map.containsKey(key)) {
			logger.error("Duplicate key  {}  {}", key, value);
			throw new UnexpectedException("Duplicate key");
		} else {
			map.put(key, value);
		}
	}
	public V get(K key) {
		if (map.containsKey(key)) {
			return map.get(key);
		} else {
			logger.error("Unexpected key  {}", key);
			throw new UnexpectedException("Unexpected key");
		}
	}
	public boolean containsKey(K key) {
		return map.containsKey(key);
	}
	public Collection<V> values() {
		return map.values();
	}
}
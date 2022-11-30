package yokwe.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

public final class ListUtil {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ListUtil.class);

	// To support List and Set, use Collection here.
	public static <E, K> SortedMap<K, E> checkDuplicate(Collection<E> collection, Function<E, K> getKey) {
		var map = new TreeMap<K, E>();
		if (collection != null) {
			for(var e: collection) {
				var key = getKey.apply(e);
				var old = map.put(key, e);
				if (old != null) {
					logger.warn("Duplicate key");
					logger.warn("  key {}", key);
					logger.warn("  old {}", old);
					logger.warn("  new {}", e);
					// throw new UnexpectedException("Duplicate symbol");
				}
			}
		}
				
		return map;
	}
	
	// To support List and Set, use Collection here.
	public static <E> SortedSet<E> checkDuplicate(Collection<E> collection) {
		var set = new TreeSet<E>();
		if (collection != null) {
			for(var e: collection) {
				if (set.add(e)) {
					// new entry
				} else {
					// existing entry
					logger.error("Duplicate value");
					logger.error("  value {}", e.toString());
					throw new UnexpectedException("Duplicate value");
				}
			}
		}
		return set;
	}
	
	public static <E extends Comparable<E>, K> void save(Class<E> clazz, String path, Collection<E> collection) {
		save(clazz, path, new ArrayList<>(collection));
	}
	public static <E extends Comparable<E>, K> void save(Class<E> clazz, String path, List<E> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(clazz).file(path, list);
	}
	public static <E extends Comparable<E>, K> List<E> load(Class<E> clazz, String path) {
		return CSVUtil.read(clazz).file(path);
	}
	public static <E extends Comparable<E>, K> List<E> getList(Class<E> clazz, String path) {
		var ret = load(clazz, path);
		return ret == null ? new ArrayList<>() : ret;
	}
}

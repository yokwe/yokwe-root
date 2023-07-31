package yokwe.util;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

public final class ListUtil {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

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
	
	public static <E extends Comparable<E>> void save(Class<E> clazz, String path, Collection<E> collection) {
		save(clazz, path, new ArrayList<>(collection));
	}
	public static <E extends Comparable<E>> void save(Class<E> clazz, String path, List<E> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(clazz).file(path, list);
	}
	public static <E extends Comparable<E>> List<E> load(Class<E> clazz, String path) {
		return CSVUtil.read(clazz).file(path);
	}
	public static <E extends Comparable<E>> List<E> load(Class<E> clazz, Reader reader) {
		return CSVUtil.read(clazz).file(reader);
	}
	public static <E extends Comparable<E>> List<E> getList(Class<E> clazz, String path) {
		var ret = load(clazz, path);
		return ret == null ? new ArrayList<>() : ret;
	}
	public static <E extends Comparable<E>> List<E> getList(Class<E> clazz, Reader reader) {
		var ret = load(clazz, reader);
		return ret == null ? new ArrayList<>() : ret;
	}
}

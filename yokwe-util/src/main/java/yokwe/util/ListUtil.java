package yokwe.util;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ListUtil {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	// To support List and Set, use Collection here.
	public static <E, K> Map<K, E> checkDuplicate(Collection<E> collection, Function<E, K> getKey) {
		var map = new HashMap<K, E>(collection.size());
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
	
	//
	// Reader reader
	//
	public static <E extends Comparable<E>> List<E> load(Class<E> clazz, Reader reader) {
		return CSVUtil.read(clazz).file(reader);
	}
	public static <E extends Comparable<E>> List<E> getList(Class<E> clazz, Reader reader) {
		var ret = load(clazz, reader);
		return ret == null ? new ArrayList<>() : ret;
	}
	
	//
	// String path
	//
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
	public static <E extends Comparable<E>> List<E> getList(Class<E> clazz, String path) {
		var ret = load(clazz, path);
		return ret == null ? new ArrayList<>() : ret;
	}
	
	//
	// File file
	//
	public static <E extends Comparable<E>> void save(Class<E> clazz, File file, Collection<E> collection) {
		save(clazz, file, new ArrayList<>(collection));
	}
	public static <E extends Comparable<E>> void save(Class<E> clazz, File file, List<E> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(clazz).file(file, list);
	}
	public static <E extends Comparable<E>> List<E> load(Class<E> clazz, File file) {
		return CSVUtil.read(clazz).file(file);
	}
	public static <E extends Comparable<E>> List<E> getList(Class<E> clazz, File file) {
		var ret = load(clazz, file);
		return ret == null ? new ArrayList<>() : ret;
	}
}

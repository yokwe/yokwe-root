package yokwe.finance;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public interface Storage {
	static org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public interface LoadSave <E extends Comparable<E>, K extends Comparable<K>> {
		public Class<E>  getClazz();
		public K         getKey(E that);
		public String    getPath();
		
		default void save(List<E> list) {
			Collections.sort(list);
			CSVUtil.write(getClazz()).file(getPath(), list);
		}
		default void save(Collection<E> collection) {
			save(new ArrayList<E>(collection));
		}
		default List<E> load() {
			return CSVUtil.read(getClazz()).file(getPath());
		}
		default List<E> load(Reader reader) {
			return CSVUtil.read(getClazz()).file(reader);
		}
		default List<E> getList() {
			var list = load();
			return list != null ? list : new ArrayList<>();
		}
		default List<E> getList(Reader reader) {
			var list = load(reader);
			return list != null ? list : new ArrayList<>();
		}
		
		default Map<K, E> checkDuplicate(Collection<E> collection) {
			var map = new HashMap<K, E>(collection.size());
			if (collection != null) {
				for(var e: collection) {
					var key = getKey(e);
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
		default Map<K, E> getMap() {
			return checkDuplicate(getList());
		}
		default Map<K, E> getMap(Reader reader) {
			return checkDuplicate(getList(reader));
		}
		
		
		public class Impl <E extends Comparable<E>, K extends Comparable<K>> implements LoadSave<E, K> {
			private final Class<E>       clazz;
			private final Function<E, K> getKey;
			private final String         path;
			
			public Impl(Class<E> clazz, Function<E, K> getKey, Storage storage, String name) {
				this.clazz  = clazz;
				this.getKey = getKey;
				this.path   = storage.getPath(name);
			}
			
			@Override
			public Class<E> getClazz() {
				return clazz;
			}
			@Override
			public String getPath() {
				return path;
			}
			@Override
			public K getKey(E that) {
				return this.getKey.apply(that);
			}
		}
	}
	public interface LoadSave2 <E extends Comparable<E>, K extends Comparable<K>> {
		public Class<E>  getClazz();
		public K         getKey(E that);
		public String    getPath(String name);
		public String    getPath();
		public String    getPathDelist();

		default void    save(String name, List<E> list) {
			Collections.sort(list);
			CSVUtil.write(getClazz()).file(getPath(name), list);
		}
		default void    save(String name, Collection<E> list) {
			save(name, new ArrayList<>(list));
		}
		default List<E> load(String name) {
			return CSVUtil.read(getClazz()).file(getPath(name));
		}
		default List<E> load(Reader reader) {
			return CSVUtil.read(getClazz()).file(reader);
		}
		default List<E> getList(String name) {
			var list = load(name);
			return list != null ? list : new ArrayList<>();
		}
		default List<E> getList(Reader reader) {
			var list = load(reader);
			return list != null ? list : new ArrayList<>();
		}

		default Map<K, E> checkDuplicate(Collection<E> collection) {
			var map = new HashMap<K, E>(collection.size());
			if (collection != null) {
				for(var e: collection) {
					var key = getKey(e);
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
		default Map<K, E> getMap(String name) {
			return checkDuplicate(getList(name));
		}
		default Map<K, E> getMap(Reader reader) {
			return checkDuplicate(getList(reader));
		}
		
		
		public class Impl <E extends Comparable<E>, K extends Comparable<K>> implements LoadSave2<E, K> {
			private final Class<E>                 clazz;
			private final Function<E, K>           getKey;
			private final Storage                  storage;
			private final String                   prefix;
			private final Function<String, String> getName;
			
			public Impl(Class<E> clazz, Function<E, K> getKey, Storage storage, String prefix, Function<String, String> getName) {
				this.clazz   = clazz;
				this.getKey  = getKey;
				this.storage = storage;
				this.prefix  = prefix;
				this.getName = getName;
			}
			
			@Override
			public Class<E> getClazz() {
				return clazz;
			}
			@Override
			public K getKey(E that) {
				return this.getKey.apply(that);
			}
			@Override
			public String getPath(String name) {
				return storage.getPath(prefix, getName.apply(name));
			}
			@Override
			public String getPath() {
				return storage.getPath(prefix);
			}
			@Override
			public String getPathDelist() {
				return storage.getPath(prefix + "-delist");
			}
		}
	}
	
	
	public static final String DATA_PATH_FILE = "data/DataPathLocation";
	
	public static void initialize() {}
	
	public static  String getDataPath() {
		org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
		logger.info("DATA_PATH_FILE  !{}!", DATA_PATH_FILE);
		// Sanity check
		if (!FileUtil.canRead(DATA_PATH_FILE)) {
			throw new UnexpectedException("Cannot read file");
		}
		
		String dataPath = FileUtil.read().file(DATA_PATH_FILE);
		logger.info("DATA_PATH       !{}!", dataPath);
		// Sanity check
		if (dataPath.isEmpty()) {
			logger.error("Empty dataPath");
			throw new UnexpectedException("Empty dataPath");
		}		
		if (!FileUtil.isDirectory(dataPath)) {
			logger.error("Not directory");
			throw new UnexpectedException("Not directory");
		}		
		return dataPath;
	}
	public static final String DATA_PATH = getDataPath();

	public String getPath();
	public String getPath(String path);
	public String getPath(String prefix, String path);
	
	public class Impl implements Storage {
		org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

		private final String basePath;
		
		public Impl(String basePath) {			
			if (!FileUtil.isDirectory(basePath)) {
				logger.error("Not directory");
				logger.error("  basePath  {}!", basePath);
				throw new UnexpectedException("Not directory");
			}

			this.basePath = basePath;
		}
		public Impl(String parent, String prefix) {
			this(parent + "/" + prefix);
		}
		public Impl(Storage parent, String prefix) {
			this(parent.getPath(), prefix);
		}
		
		@Override
		public String getPath() {
			return basePath;
		}

		@Override
		public String getPath(String path) {
			return basePath + "/" + path;
		}

		@Override
		public String getPath(String prefix, String path) {
			return basePath + "/" + prefix + "/" + path;
		}
	}
	
	public static Storage root            = new Impl(DATA_PATH);
	
	public static Storage provider        = new Impl(root, "provider");
	public static Storage stock           = new Impl(root, "stock");
	public static Storage fund            = new Impl(root, "fund");
	public static Storage report          = new Impl(root, "report");
	
	public static void main(String[] args) {
		org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
		logger.info("START");
		logger.info("DATA_PATH       {}", DATA_PATH);
		
		logger.info("root            {}", Storage.root.getPath());
		logger.info("fund            {}", Storage.fund.getPath());
		logger.info("stock           {}", Storage.stock.getPath());
		logger.info("provider        {}", Storage.provider.getPath());
				
		logger.info("STOP");
	}
}

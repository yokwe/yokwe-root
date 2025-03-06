package yokwe.finance;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public interface Storage {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void initialize() {}
	
	public interface LoadSave <E extends Comparable<E>, K extends Comparable<K>> {
		public Class<E>  getClazz();
		public K         getKey(E that);
		public String    getPath();
		
		default File getFile() {
			return new File(getPath());
		}
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
		public String	 getFileName(String name);
		public String    getPath(String name);
		public String    getPath();
		public String    getPathDelist();
		
		default File    getFile(String name) {
			return new File(getPath(name));
		}
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
		default void delistUnknownFile(Set<String> validNameSet) {
			delistUnknownFile(validNameSet, false);
		}
		default void delistUnknownFile(Set<String> validNameSet, boolean dryRun) {
			Set<String> validFileNameSet = validNameSet.stream().map(o -> getFileName(o)).collect(Collectors.toSet());
			FileUtil.moveUnknownFile(validFileNameSet, getPath(), getPathDelist(), dryRun);
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
			public String getFileName(String name) {
				return getName.apply(name);
			}
			@Override
			public String getPath(String name) {
				return storage.getPath(prefix, getFileName(name));
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
	public interface LoadSaveList <E extends Comparable<E>> {
		public Class<E>  getClazz();
		public String    getPath();
		
		default File getFile() {
			return new File(getPath());
		}
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
		
		public class Impl <E extends Comparable<E>> implements LoadSaveList<E> {
			private final Class<E>       clazz;
			private final String         path;
			
			public Impl(Class<E> clazz, Storage storage, String name) {
				this.clazz  = clazz;
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
		}
	}
	public interface LoadSaveText2 {
		public String  getFileName(String name);
		public String  getPath(String name);
		public String  getPath();
		public String  getPathDelist();
		
		default File   getFile(String name) {
			return new File(getPath(name));
		}
		default void   save(String name, String text) {
			FileUtil.write().file(getFile(name), text);
		}
		default String load(String name) {
			return FileUtil.read().file(getFile(name));
		}
		default void delistUnknownFile(Set<String> validNameSet) {
			delistUnknownFile(validNameSet, false);
		}
		default void delistUnknownFile(Set<String> validNameSet, boolean dryRun) {
			Set<String> validFileNameSet = validNameSet.stream().map(o -> getFileName(o)).collect(Collectors.toSet());
			FileUtil.moveUnknownFile(validFileNameSet, getPath(), getPathDelist(), dryRun);
		}
		
		public class Impl implements LoadSaveText2 {
			private final Storage                  storage;
			private final String                   prefix;
			private final Function<String, String> getName;
			
			public Impl(Storage storage, String prefix, Function<String, String> getName) {
				this.storage = storage;
				this.prefix  = prefix;
				this.getName = getName;
			}
			
			@Override
			public String getFileName(String name) {
				return getName.apply(name);
			}
			@Override
			public String getPath(String name) {
				return storage.getPath(prefix, getFileName(name));
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
	private static String getDataPath() {		
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
	
	default public File getFile() {
		return new File(getPath());
	}
	default public File getFile(String path) {
		return new File(getPath(path));
	}
	default public File getFile(String prefix, String path) {
		return new File(getPath(prefix, path));
	}
	
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
	
	
	public static final Storage root     = new Impl(DATA_PATH);
	
	public static final Storage chart    = new Impl(root, "chart");
	public static final Storage fund     = new Impl(root, "fund");
	public static final Storage fx       = new Impl(root, "fx");
	public static final Storage report   = new Impl(root, "report");
	public static final Storage stock    = new Impl(root, "stock");
	
	public static final class account {
		public static final Storage base = new Impl(root, "account");
		
		public static final Storage nikko    =  new Storage.Impl(base, "nikko");
		public static final Storage prestia  =  new Storage.Impl(base, "prestia");
		public static final Storage rakuten  =  new Storage.Impl(base, "rakuten");
		public static final Storage smtb     =  new Storage.Impl(base, "smtb");
		public static final Storage sony     =  new Storage.Impl(base, "sony");
	}
	
	public static final class provider {
		public static final Storage base = new Impl(root, "provider");

		public static final Storage bats    =  new Storage.Impl(base, "bats");
		public static final Storage jita    =  new Storage.Impl(base, "jita");
		public static final Storage jpx     =  new Storage.Impl(base, "jpx");
		public static final Storage jreit   =  new Storage.Impl(base, "jreit");
		public static final Storage manebu  =  new Storage.Impl(base, "manebu");
		public static final Storage mizuho  =  new Storage.Impl(base, "mizuho");
		public static final Storage nasdaq  =  new Storage.Impl(base, "nasdaq");
		public static final Storage nikkei  =  new Storage.Impl(base, "nikkei");
		public static final Storage nikko   =  new Storage.Impl(base, "nikko");
		public static final Storage nyse    =  new Storage.Impl(base, "nyse");
		public static final Storage prestia =  new Storage.Impl(base, "prestia");
		public static final Storage rakuten =  new Storage.Impl(base, "rakuten");
		public static final Storage smtb    =  new Storage.Impl(base, "smtb");
		public static final Storage sony    =  new Storage.Impl(base, "sony");
		public static final Storage yahoo   =  new Storage.Impl(base, "yahoo");
	}
	
	
	public static void main(String[] args) {
		org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
		logger.info("START");
		logger.info("DATA_PATH  {}", DATA_PATH);
		
		logger.info("root       {}", Storage.root.getPath());
		logger.info("account    {}", Storage.account.base.getPath());
		logger.info("fund       {}", Storage.fund.getPath());
		logger.info("provider   {}", Storage.provider.base.getPath());
		logger.info("report     {}", Storage.report.getPath());
		logger.info("stock      {}", Storage.stock.getPath());
		
		logger.info("STOP");
	}
}

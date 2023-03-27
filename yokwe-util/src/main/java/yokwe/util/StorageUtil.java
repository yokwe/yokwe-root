package yokwe.util;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StorageUtil {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final String PATH_STORAGE_UTIL = "/yokwe/util/storage-util.csv";
	
	static class Data {
		String hostname;
		String type;
		String path;
				
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	static Map<String, String> map;
	//         type    path
		
	static {
		String hostname;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}

		List<Data> list = CSVUtil.read(Data.class).file(Data.class, PATH_STORAGE_UTIL, StandardCharsets.UTF_8);
//		logger.info("list {}", list.size());
		if (list.isEmpty()) {
			logger.error("list is empty");
			logger.error("  path {}", PATH_STORAGE_UTIL);
			logger.error("  list {}", list);
			throw new UnexpectedException("list is empty");
		}
		map = list.stream().filter(o -> o.hostname.equals(hostname)).collect(Collectors.toMap(o -> o.type, o -> o.path));
//		logger.info("map {}", map.size());
		if (map.isEmpty()) {
			logger.error("map is empty");
			logger.error("  path {}", PATH_STORAGE_UTIL);
			logger.error("  list {}", list);
			logger.error("  hosts {}", hostname);
			throw new UnexpectedException("map is emptyss");
		}
		
		// sanity check
		for(var entry: map.entrySet()) {
			String type = entry.getKey();
			String path = entry.getValue();
			
			File dir = new File(path);
			if (!dir.exists()) {
				logger.error("Path does not exist");
				logger.error("  type {}", type);
				logger.error("  path {}", path);
				throw new UnexpectedException("Path does not exist");
			}
			if (!dir.isDirectory()) {
				logger.error("Path is not directory");
				logger.error("  type {}", type);
				logger.error("  map  {}", map);
				logger.error("  path {}", path);
				throw new UnexpectedException("Path is not directory");
			}
		}
	}

	public static String getPath() {
		Class<?> clazz = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		return getPath(clazz);
	}
	
	public static String getPath(Class<?> clazz) {
		String type = clazz.getTypeName();
		if (map.containsKey(type)) {
			return map.get(type);
		} else {
			logger.error("Unexpected type");
			logger.error("  type {}", type);
			logger.error("  map  {}", map);
			throw new UnexpectedException("Unexpected type");
		}
	}
}

package yokwe.util;

import java.util.HashMap;
import java.util.Map;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class EnumUtil {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static class EnumInfo {
		private static Map<String, EnumInfo> map = new HashMap<>();
		
		private static EnumInfo get(Class<?> clazz) {
			String key = clazz.getName();
			if (map.containsKey(key)) {
				return map.get(key);
			} else {
				EnumInfo enumInfo = new EnumInfo(clazz);
				map.put(key, enumInfo);
				return enumInfo;
			}
		}
		
		private Map<String, Enum<?>> enumMap;
		EnumInfo(Class<?> clazz) {
			enumMap = new HashMap<>();
			
			@SuppressWarnings("unchecked")
			Class<Enum<?>> enumClazz = (Class<Enum<?>>)clazz;
			for(Enum<?> e: enumClazz.getEnumConstants()) {
				String key = e.toString();
				if (enumMap.containsKey(key)) {
					logger.error("Unexpected value");
					logger.error("  clazz {}", clazz.toString());
					logger.error("  key   {}", key);
					throw new UnexpectedException("Unexpected value");
				} else {
					enumMap.put(key, e);
				}
			}
		}
	}
	
	public static <E extends Enum<?>> E getInstance(Class<E> clazz, String value) {
		EnumInfo enumInfo = EnumInfo.get(clazz);
		if (enumInfo.enumMap.containsKey(value)) {
			Object o = enumInfo.enumMap.get(value);
			@SuppressWarnings("unchecked")
			E e = (E)o;
			return e;
		} else {
			logger.error("Unexpected value");
			logger.error("  clazz {}", clazz.toString());
			logger.error("  value {}", value);
			throw new UnexpectedException("Unexpected value");
		}
	}
	
	public static class GenericXmlAdapter extends XmlAdapter<String, Enum<?>> {
		private final Class<? extends Enum<?>> enumClass;
		protected GenericXmlAdapter(Class<? extends Enum<?>> enumClass) {
			this.enumClass = enumClass;
		}
		
		public String marshal(Enum<?> type) {
			return type.toString();
		}
		public Enum<?> unmarshal(String string) {
			return EnumUtil.getInstance(this.enumClass, string);
		}
	}
}

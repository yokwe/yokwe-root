package yokwe.finance.type;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import yokwe.util.UnexpectedException;

public class DailyValueMap implements Map<LocalDate, BigDecimal> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private final TreeMap<LocalDate, BigDecimal> map = new TreeMap<>();
	
	public DailyValueMap(Collection<DailyValue> collection) {
		for(var e: collection) {
			put(e);
		}
	}
	public DailyValueMap(DailyValue[] array) {
		for(var e: array) {
			put(e);
		}
	}
	public void put(DailyValue newValue) {
		put(newValue.date, newValue.value);
	}
	public BigDecimal get(LocalDate date) {
		// sanity check
		if (map.isEmpty()) {
			logger.error("map is empty");
			logger.error("  date      {}", date);
			throw new UnexpectedException("map is empty");
		}
		
		var firstKey = map.firstKey();
		if (date.isBefore(firstKey)) {
//			logger.warn("get ZERO for  {}  --  {}", date, firstKey);
			return null;
		}

		for(;;) {
			var ret = map.get(date);
			if (ret != null) return ret;
			
			if (date.isBefore(firstKey)) break;
			// try previous date
			date = date.minusDays(1);
		}
		logger.error("Unexpected date");
		logger.error("  date      {}", date);
		logger.error("  firstKey  {}", firstKey);
		throw new UnexpectedException("Unexpected date");
	}
	
	public LocalDate firstKey() {
		return map.firstKey();
	}
	public LocalDate lastKey() {
		return map.lastKey();
	}
	
	//
	// implements Map
	//
	@Override
	public int size() {
		return map.size();
	}
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}
	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}
	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}
	@Override
	public BigDecimal get(Object key) {
		return map.get(key);
	}
	@Override
	public BigDecimal put(LocalDate key, BigDecimal value) {
		return map.put(key, value);
	}
	@Override
	public BigDecimal remove(Object key) {
		return map.remove(key);
	}
	@Override
	public void putAll(Map<? extends LocalDate, ? extends BigDecimal> m) {
		map.putAll(m);
	}
	@Override
	public void clear() {
		map.clear();
	}
	@Override
	public Set<LocalDate> keySet() {
		return map.keySet();
	}
	@Override
	public Collection<BigDecimal> values() {
		return map.values();
	}
	@Override
	public Set<Entry<LocalDate, BigDecimal>> entrySet() {
		return map.entrySet();
	}
}

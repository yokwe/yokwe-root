package yokwe.finance.type;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import yokwe.util.UnexpectedException;

public class LocalDateMap<V> implements Map<LocalDate, V>, NavigableMap<LocalDate, V> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static class PreviousDay<V> extends LocalDateMap<V> {
		public PreviousDay() {
			super(o -> o.minusDays(1));
		}
	}
	public static class NextDay<V> extends LocalDateMap<V> {
		public NextDay() {
			super(o -> o.plusDays(1));
		}
	}
	
	public static class DailyValueMap extends PreviousDay<BigDecimal>{
		public DailyValueMap(List<DailyValue> list) {
			list.stream().forEach(o -> put(o.date, o.value));
		}
	}
	
	
	private final TreeMap<LocalDate, V>          map;
	private final Function<LocalDate, LocalDate> updateKey;
	
	private LocalDateMap(Function<LocalDate, LocalDate> updateKey) {
		this.map       = new TreeMap<>();
		this.updateKey = updateKey;
	}
		
	@Override
	public V put(LocalDate key, V value) {
		return map.put(key, value);
	}
	
	@Override
	public V get(Object o) {
		{
			var ret = map.get(o);
			if (ret != null) return ret;
		}
		
		// sanity check
		if (o == null) {
			logger.error("o is null");
			throw new UnexpectedException("o is null");
		}
		if (!(o instanceof LocalDate)) {
			logger.error("o is not LocalDate");
			logger.error("  o  {}", o.getClass().getTypeName());
			throw new UnexpectedException("o is not LocalDate");
		}
		if (isEmpty()) {
			logger.error("map is empty");
			throw new UnexpectedException("map is empty");
		}
		
		{
			var key      = (LocalDate)o;
			var firstKey = map.firstKey();
			var lastKey  = map.lastKey();
			
			for(;;) {
				if (key.isBefore(firstKey) || key.isAfter(lastKey)) {
					return null;
				}
				
				var ret = map.get(key);
				if (ret != null) return ret;
				
				// update key for next iteration
				key = updateKey.apply(key);
			}
		}
	}
	
	public LocalDate firstKey() {
		return map.firstKey();
	}
	public LocalDate lastKey() {
		return map.lastKey();
	}
	
	//
	// implements Map<LocalDate, V>
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
	public V remove(Object key) {
		return map.remove(key);
	}
	@Override
	public void putAll(Map<? extends LocalDate, ? extends V> m) {
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
	public Collection<V> values() {
		return map.values();
	}
	@Override
	public Set<Entry<LocalDate, V>> entrySet() {
		return map.entrySet();
	}
	
	
	//
	// implements NavigableMap<LocalDate,V>
	//
	@Override
	public Comparator<? super LocalDate> comparator() {
		return map.comparator();
	}

	@Override
	public Entry<LocalDate, V> lowerEntry(LocalDate key) {
		return map.lastEntry();
	}

	@Override
	public LocalDate lowerKey(LocalDate key) {
		return map.lastKey();
	}

	@Override
	public Entry<LocalDate, V> floorEntry(LocalDate key) {
		return map.floorEntry(key);
	}

	@Override
	public LocalDate floorKey(LocalDate key) {
		return map.floorKey(key);
	}

	@Override
	public Entry<LocalDate, V> ceilingEntry(LocalDate key) {
		return map.ceilingEntry(key);
	}

	@Override
	public LocalDate ceilingKey(LocalDate key) {
		return map.ceilingKey(key);
	}

	@Override
	public Entry<LocalDate, V> higherEntry(LocalDate key) {
		return map.higherEntry(key);
	}

	@Override
	public LocalDate higherKey(LocalDate key) {
		return map.higherKey(key);
	}

	@Override
	public Entry<LocalDate, V> firstEntry() {
		return map.firstEntry();
	}

	@Override
	public Entry<LocalDate, V> lastEntry() {
		return map.lastEntry();
	}

	@Override
	public Entry<LocalDate, V> pollFirstEntry() {
		return map.pollFirstEntry();
	}

	@Override
	public Entry<LocalDate, V> pollLastEntry() {
		return map.pollLastEntry();
	}

	@Override
	public NavigableMap<LocalDate, V> descendingMap() {
		return map.descendingMap();
	}

	@Override
	public NavigableSet<LocalDate> navigableKeySet() {
		return map.navigableKeySet();
	}

	@Override
	public NavigableSet<LocalDate> descendingKeySet() {
		return map.descendingKeySet();
	}

	@Override
	public NavigableMap<LocalDate, V> subMap(LocalDate fromKey, boolean fromInclusive, LocalDate toKey, boolean toInclusive) {
		return map.subMap(fromKey, fromInclusive, toKey, toInclusive);
	}

	@Override
	public NavigableMap<LocalDate, V> headMap(LocalDate toKey, boolean inclusive) {
		return map.headMap(toKey, inclusive);
	}

	@Override
	public NavigableMap<LocalDate, V> tailMap(LocalDate fromKey, boolean inclusive) {
		return map.tailMap(fromKey, inclusive);
	}

	@Override
	public SortedMap<LocalDate, V> subMap(LocalDate fromKey, LocalDate toKey) {
		return map.subMap(fromKey, toKey);
	}

	@Override
	public SortedMap<LocalDate, V> headMap(LocalDate toKey) {
		return map.headMap(toKey);
	}

	@Override
	public SortedMap<LocalDate, V> tailMap(LocalDate fromKey) {
		return map.tailMap(fromKey);
	}
}

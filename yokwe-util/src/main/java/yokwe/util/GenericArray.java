package yokwe.util;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class GenericArray {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	//
	// create array from another type of array using map and op
	//
	public static final class Generator<R> implements IntFunction<R[]> {
		private final Class<R> clazz;
		
		public Generator(Class<R> clazz) {
			this.clazz = clazz;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public R[] apply(int value) {
			return (R[]) Array.newInstance(clazz, value);
		}
	}
	public static <T, R> R[] toArray(T[] array, int startIndex, int stopIndexPlusOne, Function<T, R> map, Function<R, R> op, Class<R> clazz) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		IntFunction<R[]> generator = new Generator<R>(clazz);
		return Arrays.stream(array, startIndex, stopIndexPlusOne).map(map).map(op).toArray(generator);
	}
	
	
	//
	// create single value from array using collect
	//
	public interface CollectImpl extends Consumer<BigDecimal>, Supplier<BigDecimal> {
		@Override
		public abstract void accept(BigDecimal value);
		@Override
		public abstract BigDecimal get();
	}
	public static class Collect implements Collector<BigDecimal, CollectImpl, BigDecimal> {
		private Supplier<CollectImpl> builder;
		
		public Collect(Supplier<CollectImpl> builder) {
			this.builder = builder;
		}
		
		@Override
		public Supplier<CollectImpl> supplier() {
			return builder;
		}
		
		private static final BiConsumer<CollectImpl, BigDecimal> accumulator = (c, v) -> c.accept(v);
		@Override
		public BiConsumer<CollectImpl, BigDecimal> accumulator() {
			return accumulator;
		}

		private static final BinaryOperator<CollectImpl> combiner = (a, b) -> {
			logger.error("Unexpected");
			throw new UnexpectedException("Unexpected");
		};
		
		@Override
		public BinaryOperator<CollectImpl> combiner() {
			return combiner;
		}

		private static final Function<CollectImpl, BigDecimal> finisher = c -> c.get();
		@Override
		public Function<CollectImpl, BigDecimal> finisher() {
			return finisher;
		}

		private static final Set<Characteristics> characteristics = Set.of();
		@Override
		public Set<Characteristics> characteristics() {
			return characteristics;
		}
	}
	public static <T> BigDecimal collect(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> map, Collect collector) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		return Arrays.stream(array, startIndex, stopIndexPlusOne).map(map).collect(collector);
	}

	
	//
	// check index consistency with array
	//
	public static <T> void checkIndex(T[] array, int startIndex, int stopIndexPlusOne) {
		if (array == null) {
			logger.error("array == null");
			throw new UnexpectedException("array == null");
		}
		if (array.length == 0 && startIndex == 0 && stopIndexPlusOne == 0) return;
		
		if (!(0 <= startIndex && startIndex < array.length)) {
			logger.error("  array.length      {}", array.length);
			logger.error("  startIndex        {}", startIndex);
			logger.error("  stopIndexPlusOne  {}", stopIndexPlusOne);
			throw new UnexpectedException("offset is out of range");
		}
		if (!(startIndex < stopIndexPlusOne && stopIndexPlusOne <= array.length)) {
			logger.error("  array.length      {}", array.length);
			logger.error("  startIndex        {}", startIndex);
			logger.error("  stopIndexPlusOne  {}", stopIndexPlusOne);
			throw new UnexpectedException("offset is out of range");
		}
	}
	
}

package yokwe.util;

import java.lang.reflect.Array;
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
	public interface CollectImpl<T> extends Consumer<T>, Supplier<T> {
		@Override
		public abstract void accept(T value);
		@Override
		public abstract T get();
	}
	public static class Collect<T> implements Collector<T, CollectImpl<T>, T> {
		private final Supplier<CollectImpl<T>> builder;
		
		public Collect(Supplier<CollectImpl<T>> builder) {
			this.builder = builder;
		}
		
		@Override
		public Supplier<CollectImpl<T>> supplier() {
			return builder;
		}
		
		@Override
		public BiConsumer<CollectImpl<T>, T> accumulator() {
			return accumulator;
		}
		private final BiConsumer<CollectImpl<T>, T> accumulator = (c, v) -> c.accept(v);
		
		@Override
		public BinaryOperator<CollectImpl<T>> combiner() {
			return combiner;
		}
		private final BinaryOperator<CollectImpl<T>> combiner = (a, b) -> {
			logger.error("Unexpected");
			throw new UnexpectedException("Unexpected");
		};

		@Override
		public Function<CollectImpl<T>, T> finisher() {
			return finisher;
		}
		private final Function<CollectImpl<T>, T> finisher = c -> c.get();

		@Override
		public Set<Characteristics> characteristics() {
			return characteristics;
		}
		private static final Set<Characteristics> characteristics = Set.of();
	}
	public static <T,U> U collect(T[] array, int startIndex, int stopIndexPlusOne, Function<T, U> map, Collect<U> collector) {
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

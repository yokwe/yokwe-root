package yokwe.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;
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

	///////////////////////////////////////////////////////////////////////////
	// check index consistency with array
	///////////////////////////////////////////////////////////////////////////
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
	

	///////////////////////////////////////////////////////////////////////////
	// T[] to R[] with map and op
	///////////////////////////////////////////////////////////////////////////
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
	///////////////////////////////////////////////////////////////////////////
	// T[] to R[] with map
	///////////////////////////////////////////////////////////////////////////
	public static <T, R> R[] toArray(T[] array, int startIndex, int stopIndexPlusOne, Function<T, R> map, Class<R> clazz) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		IntFunction<R[]> generator = new Generator<R>(clazz);
		return Arrays.stream(array, startIndex, stopIndexPlusOne).map(map).toArray(generator);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// T[] to R using collect
	///////////////////////////////////////////////////////////////////////////
	public interface CollectImpl<R> extends Consumer<R>, Supplier<R> {
		@Override
		public abstract void accept(R value);
		@Override
		public abstract R get();
	}
	public static final class Collect<R> implements Collector<R, CollectImpl<R>, R> {
		private final Supplier<CollectImpl<R>> builder;
		
		public Collect(Supplier<CollectImpl<R>> builder) {
			this.builder = builder;
		}
		
		@Override
		public Supplier<CollectImpl<R>> supplier() {
			return builder;
		}
		
		@Override
		public BiConsumer<CollectImpl<R>, R> accumulator() {
			return accumulator;
		}
		private final BiConsumer<CollectImpl<R>, R> accumulator = (c, v) -> c.accept(v);
		
		@Override
		public BinaryOperator<CollectImpl<R>> combiner() {
			return combiner;
		}
		private final BinaryOperator<CollectImpl<R>> combiner = (a, b) -> {
			logger.error("Unexpected");
			throw new UnexpectedException("Unexpected");
		};

		@Override
		public Function<CollectImpl<R>, R> finisher() {
			return finisher;
		}
		private final Function<CollectImpl<R>, R> finisher = c -> c.get();

		@Override
		public Set<Characteristics> characteristics() {
			return characteristics;
		}
		private static final Set<Characteristics> characteristics = Set.of();
	}
	public static <T,R> R collect(T[] array, int startIndex, int stopIndexPlusOne, Function<T, R> map, Collect<R> collector) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		return Arrays.stream(array, startIndex, stopIndexPlusOne).map(map).collect(collector);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// T[] to R using reduce
	///////////////////////////////////////////////////////////////////////////
	public static <T, R> R reduce(T[] array, int startIndex, int stopIndexPlusOne, Function<T, R> map, BinaryOperator<R> op, R identity) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		return Arrays.stream(array, startIndex, stopIndexPlusOne).map(map).reduce(identity, op);
	}
	public static <T, R> R reduce(T[] array, int startIndex, int stopIndexPlusOne, Function<T, R> map, BinaryOperator<R> op) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		Optional<R> opt = Arrays.stream(array, startIndex, stopIndexPlusOne).map(map).reduce(op);
		if (opt.isPresent()) return opt.get();
		logger.error("opt is empty");
		throw new UnexpectedException("opt is empty");
	}
	
}

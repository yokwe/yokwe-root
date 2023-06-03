package yokwe.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public final class GenericArray {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	//
	// create array from another type of array using Function
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
		IntFunction<R[]> generator = new Generator<R>(clazz);
		
		return Arrays.stream(array, startIndex, stopIndexPlusOne).map(map).map(op).toArray(generator);
	}
	
	
	//
	// create single value from array
	//
	public interface ToValueImpl<T, R> extends Function<T,R>, Consumer<R>, Supplier<R> {
		@Override
	    public R apply(T value);

		@Override
	    public void accept(R value);

		@Override
		public R get();
	}
	public static abstract class ToValueBase<T, R> implements ToValueImpl<T, R> {
		private Function<T,R> function;
		
		public ToValueBase(Function<T,R> function) {
			this.function = function;
		}
		
		@Override
		public R apply(T value) {
			return function.apply(value);
		}
		
		@Override
		public abstract void accept(R value);
		
		@Override
		public abstract R get();
	}
	public static <T, R> R toValue(T[] array, int startIndex, int stopIndexPlusOne, ToValueImpl<T, R> impl) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			impl.accept(impl.apply(array[i]));
		}
		
		return impl.get();
	}
	public interface ConsumerSupplier<T> extends Consumer<T>, Supplier<T> {
		@Override
	    public void accept(T value);

		@Override
		public T get();
	}
	public static <T, R> R toValue(T[] array, int startIndex, int stopIndexPlusOne, Function<T, R> map, ConsumerSupplier<R> consumerSupplier) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			consumerSupplier.accept(map.apply(array[i]));
		}
		
		return consumerSupplier.get();
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

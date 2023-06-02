package yokwe.util;

import java.lang.reflect.Array;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class GenericArray {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	//
	// create array from another type of array using Function
	//
	public static <T, R> R[] toArray(T[] array, int startIndex, int stopIndexPlusOne, Function<T, R> function, Class<R> clazz) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		@SuppressWarnings("unchecked")
		R[] ret = (R[]) Array.newInstance(clazz, stopIndexPlusOne - startIndex);
		
		for(int i = startIndex, j = 0; i < stopIndexPlusOne; i++, j++) {
			ret[j] = function.apply(array[i]);
		}
		return ret;
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

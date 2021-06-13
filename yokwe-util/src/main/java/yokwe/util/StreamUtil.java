package yokwe.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtil {
	public static <T> Stream<T> asStream(Enumeration<T> e) {
		Iterator<T> iterator = new Iterator<T>() {
			public T next() {
				return e.nextElement();
			}
			public boolean hasNext() {
				return e.hasMoreElements();
			}
		};
		
		Spliterator<T> splitIterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
		
		return StreamSupport.stream(splitIterator, false);
	}
}

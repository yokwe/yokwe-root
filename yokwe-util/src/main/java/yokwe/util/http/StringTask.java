package yokwe.util.http;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;

public class StringTask extends Task {
	static final Logger logger = LoggerFactory.getLogger(StringTask.class);

	private static class MyConsumer implements Consumer<Result> {		
		private Consumer<String> consumer;
		private Charset charset;
		
		public MyConsumer(Consumer<String> consumer, Charset charset) {
			this.consumer = consumer;
			this.charset = charset;
		}
		
		@Override
		public void accept(Result result) {
			Charset myCharset = result.charset;
			
			if (myCharset == null) {
				if (charset == null) {
					logger.error("charset is null");
					logger.error("  uri         {}", result.task.uri);
					logger.error("  contentType {}", result.contentType);
					throw new UnexpectedException("charset is null");
				} else {
					myCharset = charset;
				}
			}
			String page = new String(result.body, myCharset);
			consumer.accept(page);
		}
	}
	
	private StringTask(URI uri, Consumer<Result> consumer) {
		super(uri, consumer);
	}
	private StringTask(String uriString, Consumer<Result> consumer) {
		super(uriString, consumer);
	}

	public static StringTask text(URI uri, Consumer<String> consumer, Charset charset) {
		return new StringTask(uri, new MyConsumer(consumer, charset));
	}
	public static StringTask text(String uriString, Consumer<String> consumer, Charset charset) {
		return new StringTask(uriString, new MyConsumer(consumer, charset));
	}
	
	public static StringTask text(URI uri, Consumer<String> consumer) {
		return new StringTask(uri, new MyConsumer(consumer, null));
	}
	public static StringTask text(String uriString, Consumer<String> consumer) {
		return new StringTask(uriString, new MyConsumer(consumer, null));
	}
}
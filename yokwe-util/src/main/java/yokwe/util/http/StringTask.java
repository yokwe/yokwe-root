package yokwe.util.http;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.Consumer;

import org.apache.hc.core5.http.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;

public class StringTask {
	static final Logger logger = LoggerFactory.getLogger(StringTask.class);

	private static class MyConsumer implements Consumer<Result> {		
		private Consumer<String> consumer;
		private Charset          defaultCharset;
		
		public MyConsumer(Consumer<String> consumer, Charset defaultCharset) {
			this.consumer        = consumer;
			this.defaultCharset  = defaultCharset;
		}
		
		@Override
		public void accept(Result result) {
			String page;
			if (result.body == null) {
				page = "";
			} else {
				Charset myCharset = result.charset;
				
				if (myCharset == null) {
					if (defaultCharset == null) {
						logger.error("defaultCharset is null");
						logger.error("  uri         {}", result.task.uri);
						logger.error("  header {}", Arrays.asList(result.head.getHeaders()));
						throw new UnexpectedException("defaultCharset is null");
					} else {
						myCharset = defaultCharset;
					}
				}
				page = new String(result.body, myCharset);
			}
			consumer.accept(page);
		}
	}
	
	
	public static Task post(String uriString, Consumer<String> consumer, String content, String contentTypeString) {
		return Task.post(new MyConsumer(consumer, null), URI.create(uriString), content, ContentType.parse(contentTypeString));
	}
	
	private static final ContentType CONTENT_TYPE_WWW_FORM = ContentType.parse("application/x-www-form-urlencoded; charset=UTF-8");
	public static Task post(String uriString, Consumer<String> consumer, String content) {
		return Task.post(new MyConsumer(consumer, null), URI.create(uriString), content, CONTENT_TYPE_WWW_FORM);
	}

	public static Task get(String uriString, Consumer<String> consumer, Charset defaultCharset) {
		return Task.get(new MyConsumer(consumer, defaultCharset), URI.create(uriString));
	}
	
	public static Task get(String uriString, Consumer<String> consumer) {
		return Task.get(new MyConsumer(consumer, null), URI.create(uriString));
	}
}
package yokwe.util.test;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.nio.support.BasicResponseConsumer;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.impl.nio.bootstrap.H2RequesterBootstrap;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yokwe.UnexpectedException;

public class T101 {
	private static final Logger logger = LoggerFactory.getLogger(T101.class);
	
	public static class Result {
		public static Map<String, Charset> charsetMap = new TreeMap<>();
		static {
			charsetMap.put("application/xml",  StandardCharsets.UTF_8);
			charsetMap.put("application/json", StandardCharsets.UTF_8);
		}
		
		public final Task                          task;
		public final Message<HttpResponse, byte[]> message;
		
		// Derived fields from message
		public final HttpResponse head;
		public final byte[]       body;
		
		public final ProtocolVersion version;
		public final int             code;
		
		public final ContentType     contentType;
		public final Charset         charset;    // derived charset from content type
		
		public Result(Task task, Message<HttpResponse, byte[]> message) {
			this.task          = task;
			
			this.message      = message;
			
			this.head     = message.getHead();
			this.body     = message.getBody();
			
			this.version      = head.getVersion();
			this.code         = head.getCode();
			
			{
				Header contentTypeHeader = head.getFirstHeader("Content-Type");
				if (contentTypeHeader != null) {
					this.contentType  = ContentType.parse(contentTypeHeader.getValue());
					String mimeType = contentType.getMimeType();
					Charset charset = contentType.getCharset();
					if (charset == null) {
						if (charsetMap.containsKey(mimeType)) {
							this.charset = charsetMap.get(mimeType);
						} else {
							if (mimeType.startsWith("text/")) {
								logger.warn("assume charset UTF_8 for contet type of text/*");
								this.charset = StandardCharsets.UTF_8;
							} else {
//								logger.warn("assume charset null for contet type of {}!", mimeType);
								this.charset = null;
							}
						}
					} else {
						this.charset = charset;
					}
				} else {
					this.contentType = null;
					this.charset     = null;
				}
			}
		}
	}


	public static abstract class Task {
		public final URI uri;
		public final Consumer<Result> consumer;
		
		public Task(URI uri, Consumer<Result> consumer) {
			this.uri      = uri;
			this.consumer = consumer;
		}
		
		public abstract void beforeProdess(Task task);
		public          void process(Result result) {
			consumer.accept(result);
		}
		public abstract void afterProcess(Task task);
	}
	public static class BasicTask extends Task {
		public BasicTask(URI uri, Consumer<Result> consumer) {
			super(uri, consumer);
		}
		
		public void beforeProdess(Task task) {
//			logger.info("beforeProcess {}", task.uri);
		}
		public void afterProcess(Task task) {
//			logger.info("afterProcess  {}", task.uri);
		}
	}
	
	private static class MyFutureCallback implements FutureCallback<Message<HttpResponse, byte[]>> {
		private final AsyncClientEndpoint clientEndpoint;
		private final Task                task;

		public MyFutureCallback(AsyncClientEndpoint clientEndpoint, Task task) {
			this.clientEndpoint = clientEndpoint;
			this.task           = task;
		}
		
        @Override
        public void completed(final Message<HttpResponse, byte[]> message) {
            clientEndpoint.releaseAndReuse();
            
            Result result = new Result(task, message);
            task.beforeProdess(task);
            task.process(result);
            task.afterProcess(task);
        }

        @Override
        public void failed(final Exception e) {
            clientEndpoint.releaseAndDiscard();
            logger.warn("failed {}", task.uri);
			String exceptionName = e.getClass().getSimpleName();
			logger.warn("{} {}", exceptionName, e);
        }

        @Override
        public void cancelled() {
            clientEndpoint.releaseAndDiscard();
            logger.warn("cancelled {}", task.uri);
        }
	}

		
	public static final class TaskProcessor implements Runnable {
		private static HttpAsyncRequester requester = null;
		
    	private static HttpVersionPolicy httpVersionPolicy = HttpVersionPolicy.NEGOTIATE;
    	public static void setVersionPolicy(HttpVersionPolicy newValue) {
    		httpVersionPolicy = newValue;
        }
    	private static int maxTotal = 50;
    	public static void setMaxTotal(int newValue) {
    		maxTotal = newValue;
        }
    	private static int defaultMaxPerRoute = 20;
    	public static void setDefaultMaxPerRoute(int newValue) {
    		defaultMaxPerRoute = newValue;
        }
    	public static void startRequester() {
    		logger.info("httpVersionPolicy  {}", httpVersionPolicy);
    		logger.info("maxTotal           {}", maxTotal);
    		logger.info("defaultMaxPerRoute {}", defaultMaxPerRoute);
    		
            final H2Config h2Config = H2Config.custom()
                    .setPushEnabled(false)
                    .build();

    		requester = H2RequesterBootstrap.bootstrap()
                    .setH2Config(h2Config)
                    .setVersionPolicy(httpVersionPolicy)
                    .setMaxTotal(maxTotal)
                    .setDefaultMaxPerRoute(defaultMaxPerRoute)
                    .create();
    		
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                	logger.info("{}", "HTTP requester shutting down");
                    requester.close(CloseMode.GRACEFUL);
               }
            });
            
            requester.start();
    	}
    	
		private static final ConcurrentLinkedQueue<Task> taskQueue = new ConcurrentLinkedQueue<Task>();
		public static void addTask(Task task) {
			taskQueue.add(task);
		}
		
		private static final List<Header> headerList = new ArrayList<>();
		public static void addHeader(String name, String value) {
			headerList.add(new BasicHeader(name, value));
		}
		public static void addReferer(String value) {
			addHeader("Referer", value);
		}
		public static void addUserAgent(String value) {
			addHeader("User-Agent", value);
		}
		
		private static int threadc = 1;
		public static void setThreadC(int newValue) {
			threadc = newValue;
		}
		public static void processQueue() {
			if (requester == null) {
				logger.error("Need to call TaskProcessor.startRequester()");
				throw new UnexpectedException("Need to call TaskProcessor.startRequester()");
			}
			
			try {
				logger.info("threadc {}", threadc);
				ExecutorService executor = Executors.newFixedThreadPool(threadc);
				
				for(int i = 0; i < threadc; i++) {
					TaskProcessor taskProcessor = new TaskProcessor();
					executor.execute(taskProcessor);
				}

				executor.shutdown();
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.warn("{} {}", exceptionName, e);
			}
		}
		
		@Override
		public void run() {
			if (requester == null) {
				logger.error("Need to call TaskProcessor.startRequester()");
				throw new UnexpectedException("Need to call TaskProcessor.startRequester()");
			}

			for(;;) {
				Task task = taskQueue.poll();
				if (task == null) break;
				
	            try {
					URI      uri = task.uri;
					HttpHost target = new HttpHost(uri.getHost());
					
					AsyncClientEndpoint clientEndpoint = requester.connect(target, Timeout.ofSeconds(30)).get();
					
					String pathString;
					{
						String path  = uri.getPath();
						String query = uri.getQuery();
						
						if (query != null) {
							pathString = String.format("%s?%s", path, query);
						} else {
							pathString = path;
						}
					}
					
//					logger.info("pathString {}", pathString);
					
		            HttpRequest request = new BasicHttpRequest(Method.GET, target, pathString);
		            headerList.forEach(o -> request.addHeader(o));
		            
		            AsyncRequestProducer                                 requestProducer  = new BasicRequestProducer(request, null);
		            AsyncResponseConsumer<Message<HttpResponse, byte[]>> responseConsumer = new BasicResponseConsumer<>(new BasicAsyncEntityConsumer());
		            FutureCallback<Message<HttpResponse, byte[]>>        futureCallback   = new MyFutureCallback(clientEndpoint, task);

		            clientEndpoint.execute(requestProducer, responseConsumer, futureCallback);
				} catch (InterruptedException | ExecutionException e) {
					String exceptionName = e.getClass().getSimpleName();
					logger.warn("{} {}", exceptionName, e);
				}
			}
		}
	}

	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		logger.info("START");
		// Configure Requester
		TaskProcessor.setVersionPolicy(HttpVersionPolicy.NEGOTIATE);
		TaskProcessor.setMaxTotal(50);
		TaskProcessor.setDefaultMaxPerRoute(50);
		TaskProcessor.startRequester();
		
		// Configure custom header
		TaskProcessor.addUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit");
		TaskProcessor.addReferer("https://www.jpx.co.jp/");
		
		// Configure TaskProcessor
		TaskProcessor.setThreadC(50);
		
//		Consumer<Result> consumer = o -> logger.info("XXX {}  {}  {}", o.task.uri, o.body.length, new String(o.body, o.charset));
//		Consumer<Result> consumer = o -> logger.info("XXX {}  {}", o.task.uri, o.body.length);
		Consumer<Result> consumer = o -> {};
		
		{
			List<Stock> stockList = Stock.getList();
			Collections.shuffle(stockList);
			
			for(Stock stock: stockList) {
				String stockCode4 = Stock.toStockCode4(stock.stockCode);
				String path = String.format("https://quote.jpx.co.jp/jpx/template/quote.cgi?F=tmp/stock_detail&MKTN=T&QCODE=%s", stockCode4);
				URI uri = URI.create(path);
				Task task = new BasicTask(uri, consumer);
				TaskProcessor.addTask(task);
			}
			
			logger.info("BEFORE RUN");
			TaskProcessor.processQueue();
			logger.info("AFTER  RUN");
		}

		
		logger.info("STOP");
	}

}

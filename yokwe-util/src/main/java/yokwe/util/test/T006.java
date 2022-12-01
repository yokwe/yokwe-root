package yokwe.util.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.AsyncEntityConsumer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.impl.nio.bootstrap.H2RequesterBootstrap;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class T006 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static class MyBasicResponseConsumer<T> implements AsyncResponseConsumer<Message<HttpResponse, T>> {

	    private final Supplier<AsyncEntityConsumer<T>> dataConsumerSupplier;
	    private final AtomicReference<AsyncEntityConsumer<T>> dataConsumerRef;

	    public MyBasicResponseConsumer(final Supplier<AsyncEntityConsumer<T>> dataConsumerSupplier) {
	        this.dataConsumerSupplier = Args.notNull(dataConsumerSupplier, "Data consumer supplier");
	        this.dataConsumerRef = new AtomicReference<>(null);
	    }

	    public MyBasicResponseConsumer(final AsyncEntityConsumer<T> dataConsumer) {
	        this(new Supplier<AsyncEntityConsumer<T>>() {

	            @Override
	            public AsyncEntityConsumer<T> get() {
	                return dataConsumer;
	            }

	        });
	    }

	    @Override
	    public void consumeResponse(
	            final HttpResponse response,
	            final EntityDetails entityDetails,
	            final HttpContext httpContext, final FutureCallback<Message<HttpResponse, T>> resultCallback) throws HttpException, IOException {
	        logger.info("XX consumeResponse {} {}", Thread.currentThread().getId(), Thread.currentThread().getName());
            logger.info("  response {} {}", response.getCode(), response.getReasonPhrase());
            logger.info("  version {}", response.getVersion());
			Header header = response.getFirstHeader("Content-Type");
			ContentType contentType = ContentType.parse(header.getValue());
        	logger.info("  contentType {} - {}", contentType.getMimeType(), contentType.getCharset());

	        Args.notNull(response, "Response");

	        if (entityDetails != null) {
	            final AsyncEntityConsumer<T> dataConsumer = dataConsumerSupplier.get();
	            if (dataConsumer == null) {
	                throw new HttpException("Supplied data consumer is null");
	            }
	            dataConsumerRef.set(dataConsumer);
	            dataConsumer.streamStart(entityDetails, new FutureCallback<T>() {

	                @Override
	                public void completed(final T body) {
	        	        logger.info("  XX completed");
	                    final Message<HttpResponse, T> result = new Message<>(response, body);
	                    if (resultCallback != null) {
	                        resultCallback.completed(result);
	                    }
	        	        logger.info("  YY completed");
	                }

	                @Override
	                public void failed(final Exception ex) {
	        	        logger.info("  XX failed");
	                    if (resultCallback != null) {
	                        resultCallback.failed(ex);
	                    }
	        	        logger.info("  YY failed");
	                }

	                @Override
	                public void cancelled() {
	        	        logger.info("  XX cancelled");
	                    if (resultCallback != null) {
	                        resultCallback.cancelled();
	                    }
	        	        logger.info("  YY cancelled");
	                }

	            });
	        } else {
	            final Message<HttpResponse, T> result = new Message<>(response, null);
	            if (resultCallback != null) {
	                resultCallback.completed(result);
	            }
	        }
	        logger.info("YY consumeResponse");
	    }

	    @Override
	    public void informationResponse(final HttpResponse response, final HttpContext httpContext) throws HttpException, IOException {
	        logger.info("informationResponse");
	    }

	    @Override
	    public void updateCapacity(final CapacityChannel capacityChannel) throws IOException {
	        logger.info("updateCapacity");
	        final AsyncEntityConsumer<T> dataConsumer = dataConsumerRef.get();
	        dataConsumer.updateCapacity(capacityChannel);
	    }

	    @Override
	    public void consume(final ByteBuffer src) throws IOException {
	        logger.info("consume {}", src.limit());
	        final AsyncEntityConsumer<T> dataConsumer = dataConsumerRef.get();
	        dataConsumer.consume(src);
	    }

	    @Override
	    public void streamEnd(final List<? extends Header> trailers) throws HttpException, IOException {
	        logger.info("XX streamEnd");
	        final AsyncEntityConsumer<T> dataConsumer = dataConsumerRef.get();
	        dataConsumer.streamEnd(trailers);
	        logger.info("YY streamEnd");
	    }

	    @Override
	    public void failed(final Exception cause) {
	        logger.info("failed");
	        final AsyncEntityConsumer<T> dataConsumer = dataConsumerRef.get();
	        if (dataConsumer != null) {
	            dataConsumer.failed(cause);
	        }
	        releaseResources();
	    }

	    @Override
	    public void releaseResources() {
	        logger.info("releaseResources");
	        final AsyncEntityConsumer<T> dataConsumer = dataConsumerRef.getAndSet(null);
	        if (dataConsumer != null) {
	            dataConsumer.releaseResources();
	        }
	    }

	}

	private static class MyFutureCallback implements FutureCallback<Message<HttpResponse, byte[]>> {
		AsyncClientEndpoint clientEndpoint;
		CountDownLatch      latch;
		String              path;
		
		public MyFutureCallback(AsyncClientEndpoint clientEndpoint, CountDownLatch latch, String path) {
			this.clientEndpoint = clientEndpoint;
			this.latch          = latch;
			this.path           = path;
		}
		
        @Override
        public void completed(final Message<HttpResponse, byte[]> message) {
        	logger.info("XX completed");
            clientEndpoint.releaseAndReuse();
            final HttpResponse response = message.getHead();
            logger.info("response {} {}", response.getCode(), response.getReasonPhrase());
            logger.info("version {}", response.getVersion());
			Header header = response.getFirstHeader("Content-Type");
			ContentType contentType = ContentType.parse(header.getValue());
        	logger.info("contentType {} - {}", contentType.getMimeType(), contentType.getCharset());
            final byte[] body = message.getBody();
            logger.info("body-length {}", body.length);
            
//            if (contentType.getMimeType().startsWith("text/")) {
//            	if (contentType.getCharset() == null) {
//                	String string = new String(body, StandardCharsets.UTF_8);
//                	logger.info("TEXT {}", string);
//            	} else {
//                	String string = new String(body, contentType.getCharset());
//                	logger.info("TEXT {}", string);
//            	}
//            } else if (contentType.getMimeType().equals("application/json")) {
//                	String string = new String(body, StandardCharsets.UTF_8);
//                	logger.info("TEXT {}", string);
//            } else {
//            	logger.info("BIN {}", StringUtil.toHexString(body));
//            }
            
        	logger.info("YY completed");
            latch.countDown();
        }

        @Override
        public void failed(final Exception ex) {
        	logger.info("XX failed");
            clientEndpoint.releaseAndDiscard();
            logger.info("{}", path + "->" + ex);
        	logger.info("YY failed");
            latch.countDown();
        }

        @Override
        public void cancelled() {
        	logger.info("XX cancelled");
            clientEndpoint.releaseAndDiscard();
            logger.info("{}", path + " cancelled");
        	logger.info("YY cancelled");
            latch.countDown();
        }
	}
	

    public static class Request {
    	protected static final HttpAsyncRequester requester;
    	static {
            final H2Config h2Config = H2Config.custom()
                    .setPushEnabled(false)
                    .build();

    		requester = H2RequesterBootstrap.bootstrap()
                    .setH2Config(h2Config)
                    .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
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
    	
    	public final HttpHost     target;
    	public final List<Header> headerList = new ArrayList<>();
    	public final List<String> pathList   = new ArrayList<>();
    	
    	public Request(String hostname) {
    		this.target = new HttpHost(URIScheme.HTTPS.id, hostname);
    	}
    	public void addHeader(String name, String value) {
    		addHeader(new BasicHeader(name, value));
    	}
    	public void addHeader(Header header) {
    		this.headerList.add(header);
    	}
    	public void addPath(String path) {
    		this.pathList.add(path);
    	}
    	public void addPath(String... pathArray) {
    		for(String path: pathArray) addPath(path);
    	}
    	
    	public void execute() throws InterruptedException, ExecutionException {
            final CountDownLatch latch = new CountDownLatch(pathList.size());

            for(String path: pathList) {
                execute(latch, path);
            }
            latch.await();
    	}
    	
    	public void execute(CountDownLatch latch, String path) throws InterruptedException, ExecutionException {
            AsyncClientEndpoint clientEndpoint = requester.connect(target, Timeout.ofSeconds(5)).get();
            HttpRequest request = new BasicHttpRequest(Method.GET, target, path);
            // build header
            headerList.stream().forEach(o -> request.addHeader(o));

            AsyncRequestProducer                                 requestProducer  = new BasicRequestProducer(request, null);
            AsyncResponseConsumer<Message<HttpResponse, byte[]>> responseConsumer = new MyBasicResponseConsumer<>(new BasicAsyncEntityConsumer());
            FutureCallback<Message<HttpResponse, byte[]>>        futureCallback   = new MyFutureCallback(clientEndpoint, latch, path);

            clientEndpoint.execute(requestProducer, responseConsumer, futureCallback);
    	}
    	
    }

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		logger.info("START");
		
//		Request request = new Request("nghttp2.org");
//		request.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit");
//		request.addPath("/httpbin/ip", "/httpbin/user-agent", "/httpbin/headers");
		Request request = new Request("www.monex.co.jp");
		request.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit");
		request.addPath("/mst/servlet/ITS/ucu/UsEvaluationRateGST", "/mst/servlet/ITS/ucu/UsEvaluationRateGST");
		
		request.execute();
		
		logger.info("STOP");
	}

}

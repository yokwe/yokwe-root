package yokwe.util.test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.Method;
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

import yokwe.util.StringUtil;

public class T005 {
	private static final Logger logger = LoggerFactory.getLogger(T005.class);
	
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
            clientEndpoint.releaseAndReuse();
            final HttpResponse response = message.getHead();
            logger.info("response {} {}", response.getCode(), response.getReasonPhrase());
            logger.info("version {}", response.getVersion());
			Header header = response.getFirstHeader("Content-Type");
			ContentType contentType = ContentType.parse(header.getValue());
        	logger.info("contentType {} - {}", contentType.getMimeType(), contentType.getCharset());
            final byte[] body = message.getBody();
            logger.info("body-length {}", body.length);
            
            if (contentType.getMimeType().startsWith("text/")) {
            	if (contentType.getCharset() == null) {
                	String string = new String(body, StandardCharsets.UTF_8);
                	logger.info("TEXT {}", string);
            	} else {
                	String string = new String(body, contentType.getCharset());
                	logger.info("TEXT {}", string);
            	}
            } else if (contentType.getMimeType().equals("application/json")) {
                	String string = new String(body, StandardCharsets.UTF_8);
                	logger.info("TEXT {}", string);
            } else {
            	logger.info("BIN {}", StringUtil.toHexString(body));
            }
            
            latch.countDown();
        }

        @Override
        public void failed(final Exception ex) {
            clientEndpoint.releaseAndDiscard();
            logger.info("{}", path + "->" + ex);
            latch.countDown();
        }

        @Override
        public void cancelled() {
            clientEndpoint.releaseAndDiscard();
            logger.info("{}", path + " cancelled");
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
    		this.target = new HttpHost(hostname);
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
            AsyncResponseConsumer<Message<HttpResponse, byte[]>> responseConsumer = new BasicResponseConsumer<>(new BasicAsyncEntityConsumer());
            FutureCallback<Message<HttpResponse, byte[]>>        futureCallback   = new MyFutureCallback(clientEndpoint, latch, path);

            clientEndpoint.execute(requestProducer, responseConsumer, futureCallback);
    	}
    	
    }

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		logger.info("START");
		
		Request request = new Request("nghttp2.org");
		request.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit");
		request.addPath("/httpbin/ip", "/httpbin/user-agent", "/httpbin/headers");
		
		request.execute();
		
		logger.info("STOP");
	}

}

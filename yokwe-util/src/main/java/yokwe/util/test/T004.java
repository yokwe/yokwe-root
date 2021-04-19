package yokwe.util.test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

public class T004 {
	private static final Logger logger = LoggerFactory.getLogger(T003.class);
		
	private static class MyFutureCallback implements FutureCallback <Message<HttpResponse, byte[]>> {
		private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
		
		static Map<String, Charset> mimeTypeCharsetMap = new TreeMap<>();
		static {
			mimeTypeCharsetMap.put("application/json", StandardCharsets.UTF_8);
			mimeTypeCharsetMap.put("application/xml",  StandardCharsets.UTF_8);
		}

		private final String              requestUri;
		private final AsyncClientEndpoint clientEndpoint;
		private final CountDownLatch      latch;
		
		public MyFutureCallback(String requestUri, AsyncClientEndpoint clientEndpoint, CountDownLatch latch) {
			this.requestUri     = requestUri;
			this.clientEndpoint = clientEndpoint;
			this.latch          = latch;
		}
		
        @Override
        public void completed(final Message<HttpResponse, byte[]> message) {
            clientEndpoint.releaseAndReuse();
            final HttpResponse response = message.getHead();
            logger.info("response {} {}", response.getVersion(), response.getCode(), response.getReasonPhrase());
			Header header = response.getFirstHeader("Content-Type");
			ContentType contentType = ContentType.parse(header.getValue());
         	String mimeType = contentType.getMimeType();
        	Charset charset = contentType.getCharset();
        	if (charset == null) {
        		charset = mimeTypeCharsetMap.get(mimeType);
        	}
        	if (charset == null) {
        		if (mimeType.startsWith("text/")) {
        			// supply default charset
        			charset = DEFAULT_CHARSET;
        		}
        	}
           	logger.info("contentType {} - {} ({})", mimeType, charset, contentType.getCharset());

           	final byte[] body = message.getBody();
           	if (charset == null) {
                logger.info("body DATA {}", body.length);
 //               logger.info("body DATA {}", StringUtil.toHexString(body));
           	} else {
                logger.info("body TEXT {}", body.length);
//                logger.info("body TEXT {}", new String(body, charset));
           	}
                               
            latch.countDown();
        }

        @Override
        public void failed(final Exception ex) {
            clientEndpoint.releaseAndDiscard();
            logger.info("{}", requestUri + "->" + ex);
            latch.countDown();
        }

        @Override
        public void cancelled() {
            clientEndpoint.releaseAndDiscard();
            logger.info("{}", requestUri + " cancelled");
            latch.countDown();
        }

    };

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		logger.info("START");
		
        // Create and start requester
        final H2Config h2Config = H2Config.custom()
                .setPushEnabled(false)
                .build();

		final HttpAsyncRequester requester = H2RequesterBootstrap.bootstrap()
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

//        final HttpHost target = new HttpHost(URIScheme.HTTPS.id, "www.monex.co.jp");
//        final String[] requestUris = new String[] {"/mst/servlet/ITS/ucu/UsEvaluationRateGST"};
//        final HttpHost target = new HttpHost(URIScheme.HTTPS.id, "iexcloud.io");
//        final String[] requestUris = new String[] {"/index.html"};
//        final HttpHost target = new HttpHost(URIScheme.HTTPS.id, "sandbox.iexapis.com");
//        final String[] requestUris = new String[] {"/v1/status"};
//        final HttpHost target = new HttpHost(URIScheme.HTTPS.id, "www.youtube.com");
//        final String[] requestUris = new String[] {"index.html"};
        final HttpHost target = new HttpHost("nghttp2.org");
        final String[] requestUris = new String[] {"/httpbin/ip", "/httpbin/user-agent", "/httpbin/headers"};

        BasicHeader header = new BasicHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit");

        final CountDownLatch latch = new CountDownLatch(requestUris.length);
        for (final String requestUri: requestUris) {
            final Future<AsyncClientEndpoint> future = requester.connect(target, Timeout.ofSeconds(5));
            final AsyncClientEndpoint clientEndpoint = future.get();
            
            HttpRequest request = new BasicHttpRequest(Method.GET, target, requestUri);
            request.addHeader(header);
            
            AsyncRequestProducer                                 requestProducer  = new BasicRequestProducer(request, null);
            AsyncResponseConsumer<Message<HttpResponse, byte[]>> responseConsumer = new BasicResponseConsumer<>(new BasicAsyncEntityConsumer());
            FutureCallback<Message<HttpResponse, byte[]>>        futureCallback   = new MyFutureCallback(requestUri, clientEndpoint, latch);

            clientEndpoint.execute(requestProducer, responseConsumer, futureCallback);
        }

        latch.await();
//        logger.info("{}", "Shutting down I/O reactor");
//        requester.initiateShutdown();
		logger.info("STOP");
		
//		System.exit(0);
	}

}

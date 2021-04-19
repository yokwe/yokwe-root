package yokwe.util.test;

import java.nio.charset.StandardCharsets;
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
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
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

public class T003 {
	private static final Logger logger = LoggerFactory.getLogger(T004.class);
	
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

        final HttpHost target = new HttpHost(URIScheme.HTTPS.id, "www.monex.co.jp");
        final String[] requestUris = new String[] {"/mst/servlet/ITS/ucu/UsEvaluationRateGST"};
//        final HttpHost target = new HttpHost(URIScheme.HTTPS.id, "iexcloud.io");
//        final String[] requestUris = new String[] {"/index.html"};
//        final HttpHost target = new HttpHost(URIScheme.HTTPS.id, "sandbox.iexapis.com");
//        final String[] requestUris = new String[] {"/v1/status"};
//        final HttpHost target = new HttpHost(URIScheme.HTTPS.id, "www.youtube.com");
//        final String[] requestUris = new String[] {"index.html"};
//        final HttpHost target = new HttpHost("nghttp2.org");
//        final String[] requestUris = new String[] {"/httpbin/ip", "/httpbin/user-agent", "/httpbin/headers"};

        BasicHeader header = new BasicHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit");

        final CountDownLatch latch = new CountDownLatch(requestUris.length);
        for (final String requestUri: requestUris) {
            final Future<AsyncClientEndpoint> future = requester.connect(target, Timeout.ofSeconds(5));
            final AsyncClientEndpoint clientEndpoint = future.get();
            HttpRequest request = new BasicHttpRequest(Method.GET, target, requestUri);
            request.addHeader(header);
            
            clientEndpoint.execute(
                    new BasicRequestProducer(request, null),
                    new BasicResponseConsumer<>(new BasicAsyncEntityConsumer()),
                    new FutureCallback<Message<HttpResponse, byte[]>>() {

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
                            logger.info("{}", requestUri + "->" + ex);
                            latch.countDown();
                        }

                        @Override
                        public void cancelled() {
                            clientEndpoint.releaseAndDiscard();
                            logger.info("{}", requestUri + " cancelled");
                            latch.countDown();
                        }

                    });
        }

        latch.await();
//        logger.info("{}", "Shutting down I/O reactor");
//        requester.initiateShutdown();
		logger.info("STOP");
		
//		System.exit(0);
	}

}

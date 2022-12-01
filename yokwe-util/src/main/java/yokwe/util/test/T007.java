package yokwe.util.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.RequestChannel;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.impl.nio.bootstrap.H2RequesterBootstrap;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class T007 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static class MyAsyncClientExchangeHandler implements AsyncClientExchangeHandler {
		private static Set<String> utf8Set = new TreeSet<>();
		static {
			utf8Set.add("application/xml");
			utf8Set.add("application/json");
		}
		
		private static class Context {
			int         length;
			ContentType contentType;
			Charset     charset;
			
			ByteArrayOutputStream baos;
			WritableByteChannel channel;

			Context() {
				this.length      = 0;
				this.contentType = null;
				this.charset     = null;
				
				this.baos    = null;
				this.channel = null;

			}
		}
		private final HttpRequest    request;
		private final CountDownLatch latch;
		
		private final Context context;
		
		MyAsyncClientExchangeHandler(HttpRequest request, CountDownLatch latch) {
			this.request = request;
			this.latch   = latch;
			this.context = new Context();
		}
		
		//
		// AsyncClientExchangeHandler
		//
		@Override
		public void produceRequest(RequestChannel channel, HttpContext context) throws HttpException, IOException {
			logger.info("produceRequest");
			
	        channel.sendRequest(request, null, context);
		}
		@Override
		public void consumeResponse(HttpResponse response, EntityDetails entityDetails, HttpContext context)
				throws HttpException, IOException {
			logger.info("consumeResponse");
            logger.info("  response {} {} {}", response.getCode(), response.getReasonPhrase(), response.getVersion());
        	
        	this.context.length   = (int)entityDetails.getContentLength();
        	this.context.baos     = new ByteArrayOutputStream(this.context.length);
        	this.context.channel  = Channels.newChannel(this.context.baos);
            this.context.contentType = ContentType.parse(entityDetails.getContentType());
            
        	String  mimeType = this.context.contentType.getMimeType();
        	Charset charset  = this.context.contentType.getCharset();
        	
        	if (charset == null) {
        		if (utf8Set.contains(mimeType)) {
        			charset = StandardCharsets.UTF_8;
        		}
        	}
        	if (charset == null) {
        		if (mimeType.startsWith("text/")) {
        			charset = StandardCharsets.UTF_8;
        		}
        	}
        	this.context.charset  = charset;

        	logger.info("  contentType {}", this.context.contentType);
        	logger.info("  charset     {}", this.context.charset);
        	logger.info("  length      {}", this.context.length);
		}
		@Override
		public void consumeInformation(HttpResponse response, HttpContext context) throws HttpException, IOException {
			logger.info("consumeInformation");			
		}
		@Override
		public void cancel() {
			logger.info("cancel");
		}
		

		//
		// AsyncDataExchangeHandler
		//
		@Override
		public void failed(Exception cause) {
			logger.info("failed");
		}
		

		//
		// AsyncDataConsumer
		//
		@Override
		public void updateCapacity(CapacityChannel capacityChannel) throws IOException {
			logger.info("updateCapacity");
		}
		@Override
		public void consume(ByteBuffer src) throws IOException {
			logger.info("consume");
			this.context.channel.write(src);
			logger.info("  {} / {}", this.context.baos.size(), this.context.length);
		}
		@Override
		public void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
			logger.info("streamEnd");
			if (context.charset == null) {
				logger.info("data {}  {}", this.request.toString(), context.baos.size());
			} else {
				String string = new String(context.baos.toByteArray(), this.context.charset);
				logger.info("text {}  {}", this.request.getRequestUri(), string.length());
			}

            latch.countDown();
		}

		//
		// DataHolder
		//
		@Override
		public void releaseResources() {
			logger.info("releaseResources");
		}

		//
		// AsyncDataProducer
		//
		@Override
		public int available() {
			logger.info("available");
			return 0;
		}
		@Override
		public void produce(DataStreamChannel channel) throws IOException {
			logger.info("produce");
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

            final AsyncClientExchangeHandler exchangeHandler = new MyAsyncClientExchangeHandler(request, latch);
            final HttpContext context = HttpCoreContext.create();

            clientEndpoint.execute(exchangeHandler, context);
    	}
    	
    }

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		logger.info("START");
		
//		Request request = new Request("nghttp2.org");
//		request.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit");
//		request.addPath("/httpbin/ip", "/httpbin/user-agent", "/httpbin/headers");
		Request request = new Request("www.monex.co.jp");
		request.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit");
		request.addPath("/mst/servlet/ITS/ucu/UsEvaluationRateGST", "/pc/static/img/btn/btn_search_off.gif");
		
		request.execute();
		
		logger.info("STOP");
	}

}

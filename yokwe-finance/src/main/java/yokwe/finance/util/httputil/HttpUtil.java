package yokwe.finance.util.httputil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.impl.bootstrap.HttpRequester;
import org.apache.hc.core5.http.impl.bootstrap.RequesterBootstrap;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.HttpEntities;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.Timeout;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class HttpUtil {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final HttpRequester requester;
	static {
		SocketConfig socketConfig =
				SocketConfig.custom()
                .setSoTimeout(30, TimeUnit.SECONDS)
                .build();
		
		requester =
				RequesterBootstrap.bootstrap()
                .setSocketConfig(socketConfig)
                .setMaxTotal(100)
                .setDefaultMaxPerRoute(50)
                .create();
		
		Runnable runnable = () -> requester.close(CloseMode.GRACEFUL);
		
        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
	}
	
    private static final HttpCoreContext httpContext = HttpCoreContext.create();
    
    public static final class Post {
    	public static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    	public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded;charset=UTF-8";
    	
    	public static Post Json(String value) {
    		return new Post(CONTENT_TYPE_JSON, value);
    	}
    	public static Post Form(String value) {
    		return new Post(CONTENT_TYPE_FORM, value);
    	}
    	
    	public final ContentType contentType;
    	public final String      body;
    	
    	public Post(String contentType, String body) {
    		this(ContentType.parse(contentType), body);
    	}
    	public Post(ContentType contentType, String body) {
    		this.contentType = contentType;
    		this.body        = body;
    	}
    }
    
	public static class Builder {
		private static final String  DEFAULT_TRACE_DIR  = "tmp/http";
		private static final String  DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit";
		private static final String  DEFAULT_CONNECTION = "keep-alive";

		private File                traceDir       = null;
		private Charset             defaultCharset = null;
		private Map<String, String> headerMap      = new TreeMap<>();

    	public Builder() {
    		setUserAgent(DEFAULT_USER_AGENT);
    		setConnection(DEFAULT_CONNECTION);
    	}
    	
    	public HttpUtil build() {
    		return new HttpUtil(traceDir, defaultCharset, headerMap);
    	}
    	
    	public Builder setTraceDir() {
    		return setTraceDir(DEFAULT_TRACE_DIR);
    	}
       	public Builder setTraceDir(String newValue) {
       		return setTraceDir(new File(newValue));
    	}
       	public Builder setTraceDir(File newValue) {
    		traceDir = newValue;
    		return this;
    	}
    	public Builder setDefaultCharset(Charset newValue) {
    		defaultCharset = newValue;
    		return this;
    	}
    	
    	//
    	// header
    	//
    	public Builder setReference(String newValue) {
    		return setHeader("Referer", newValue);
    	}
       	public Builder setUserAgent(String newValue) {
    		return setHeader("User-Agent", newValue);
    	}
       	public Builder setCookie(String newValue) {
    		return setHeader("Cookie", newValue);
    	}
       	public Builder setConnection(String newValue) {
    		return setHeader("Connection", newValue);
    	}
    	public Builder setHeader(String name, String value) {
    		headerMap.put(name, value);
    		return this;
    	}
    }

	public static class Result {
		private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

		public final String       timestamp;
		public final String       path;
		
		public final URI          uri;
		public final byte[]       content;
		public final Charset      charset;
		
		public final HttpResponse response;
		
		public Result (File traceDir, URI uri, byte[] content, Charset charset, HttpResponse response) {
			if (traceDir != null) {
				timestamp = LocalDateTime.now(ZoneId.systemDefault()).format(DATE_TIME_FORMATTER);
				path      = String.format("%s/%s", traceDir.getPath(), timestamp);
				FileUtil.rawWrite().file(new File(path), content);
			} else {
				timestamp = null;
				path      = null;
			}
			
			this.uri      = uri;
			this.content  = content;
			this.charset  = charset;
			this.response = response;
		}
		
		@Override
		public String toString() {
			return String.format("{%d  %s  %s  %d  %s}", response.getCode(), response.getReasonPhrase(), response.getVersion(), (content != null ? content.length : -1), uri);
		}
		
		public Result check() {
			if (content == null) {
				logger.error("content is null");
				logger.error("  {}", this.toString());
				throw new UnexpectedException("content is null");
			}
			return this;
		}
		public byte[] asByteArray() {
			return content;
		}
		public String asString() {
			if (charset == null) {
				logger.error("charset is null");
				logger.error("  {}", this.toString());
				throw new UnexpectedException("charset is null");
			}
			return new String(content, charset);
		}
	}
	
	
	private final File                traceDir;
	private final Charset             defaultCharset;
	private final Map<String, String> headerMap;
	
	private HttpUtil(File traceDir, Charset defaultCharset, Map<String, String> headerMap) {
		this.traceDir       = traceDir;
		this.defaultCharset = defaultCharset;
		this.headerMap      = headerMap;
	}
	
	
	public Result postJson(String url, String value) {
		return post(url, Post.Json(value));
	}
	public Result postForm(String url, String value) {
		return post(url, Post.Form(value));
	}
	public Result post(String url, Post post) {
		var uri     = URI.create(url);
        var request = new BasicClassicHttpRequest(Method.POST, uri);
        
		for(var e: headerMap.entrySet()) {
			request.setHeader(e.getKey(), e.getValue());
		}
		
		request.setEntity(HttpEntities.create(post.body, post.contentType));
        
		return getResult(uri, request);
	}
	public Result get(String url) {
		var uri     = URI.create(url);
		var request = new BasicClassicHttpRequest(Method.POST, uri);
        
		for(var e: headerMap.entrySet()) {
			request.setHeader(e.getKey(), e.getValue());
		}
		
		return getResult(uri, request);
	}
	
	private static Set<Integer> RETRY_SET = new TreeSet<>();
	static {
		RETRY_SET.add(HttpStatus.SC_TOO_MANY_REQUESTS);
		RETRY_SET.add(HttpStatus.SC_FORBIDDEN);
		RETRY_SET.add(HttpStatus.SC_SERVICE_UNAVAILABLE);
	}
	private static Set<Integer> RETURN_SET = new TreeSet<>();
	static {
		RETURN_SET.add(HttpStatus.SC_NOT_FOUND);
		RETURN_SET.add(HttpStatus.SC_BAD_REQUEST);
		RETURN_SET.add(HttpStatus.SC_MOVED_TEMPORARILY);
		RETURN_SET.add(HttpStatus.SC_SERVER_ERROR);
		RETURN_SET.add(HttpStatus.SC_UNAUTHORIZED);
	}

	private Result getResult(URI uri, ClassicHttpRequest request) {
		var target            = HttpHost.create(uri);
		var connectionTimeout = Timeout.ofSeconds(5);
		
		int retryCount = 0;
		for(;;) {
			try {
				MyResponse   myResponse   = requester.execute(target, request, connectionTimeout, httpContext, o -> new MyResponse(o));
				HttpResponse response     = myResponse.response;
		        int          code         = response.getCode();
		        
		        if (RETRY_SET.contains(code)) {
					retryCount++;
					logger.warn("retry {} {} {}  {}", retryCount, code, response.getReasonPhrase(), uri);
					Thread.sleep(1000 * retryCount * retryCount); // sleep 1 * retryCount * retryCount sec
					continue;
		        }
		        
				retryCount = 0;
				if (code == HttpStatus.SC_OK) {
					// 		public Result (File traceDir, URI uri, byte[] content, Charset charset, HttpResponse response) {
					Charset charset = null;
					{
						var contentType = myResponse.contentType;
						if (contentType != null) {
							charset = contentType.getCharset();
						}
						if (charset == null) charset = defaultCharset;
					}
					
	    			Result ret = new Result(traceDir, uri, myResponse.content, charset, response);
					
					if (ret.path != null) {
						logger.info(String.format("%s %7d %s", ret.timestamp, ret.content.length, ret.uri));
					}
					return ret;
				}
				if (RETURN_SET.contains(code)) {
					logger.warn("error {} {} {}  {}", retryCount, code, response.getReasonPhrase(), uri);
					
					if (code == HttpStatus.SC_MOVED_TEMPORARILY) {
						Header location = response.getHeader("Location");
						if (location != null) {
							logger.warn("  {} {}!", location.getName(), location.getValue());
						}
					}
					
					return new Result(traceDir, uri, null, null, response);
				}
		        
				// Other code
				logger.error("code    {}", code);
				logger.error("reason  {}", response.getReasonPhrase());
				logger.error("version {}", response.getVersion());
				{
					if (myResponse.content == null) {
						logger.error("content  null");
					} else {
						logger.error("content  {}", myResponse.content.length);
					}
				}
				throw new UnexpectedException("execute");
			} catch (SocketTimeoutException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.warn("{}", exceptionName);
				return null;
			} catch (NoHttpResponseException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.warn("{}", exceptionName);
				return null;
			} catch (IOException | HttpException | InterruptedException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}
		}
	}
	
	
	private static class MyResponse {
		byte[]       content;
		ContentType  contentType;
		HttpResponse response;
		
		MyResponse(ClassicHttpResponse response) {
			this.response = response;
			
			HttpEntity entity = response.getEntity();
//			logger.debug("entity       {}", entity);			
//			logger.debug("entiry contentTyppe {}", entity.getContentEncoding());
//			logger.debug("entiry contentLenth {}", entity.getContentLength());
//			logger.debug("entiry contentType  {}", entity.getContentType());
//			logger.debug("response code {}", response.getCode());
//			for(var e: response.getHeaders()) {
//				logger.debug("response header       {} {}!", e.getName(), e.getValue());
//			}
			
			if (entity == null) {
				this.content = null;
			} else if (entity.isChunked() == false && entity.getContentLength() == 0 && entity.getContentType() == null) {
				this.content = null;
			} else {
				{
					String string = entity.getContentType();
					if (string != null) {
						this.contentType = ContentType.parse(string);
					}
				}
				
				InputStream is = null;

				try {
					String contentEncoding = entity.getContentEncoding();
					if (contentEncoding == null) {
						is = entity.getContent();
					} else if (contentEncoding.equalsIgnoreCase("gzip")){
						is = new GZIPInputStream(entity.getContent());
					} else if (contentEncoding.equalsIgnoreCase("defalte")){
						is = new DeflaterInputStream(entity.getContent());
					} else {
						logger.error("Unexpected contentEncoding");
						logger.error("  entity {}", entity);
						throw new UnexpectedException("Unexpected contentEncoding");
					}
					
					this.content = is.readAllBytes();
					is.close();
					is = null;
				} catch (IOException e) {
					String exceptionName = e.getClass().getSimpleName();
					logger.error("{} {}", exceptionName, e);
					throw new UnexpectedException(exceptionName, e);
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							String exceptionName = e.getClass().getSimpleName();
							logger.error("{} {}", exceptionName, e);
							throw new UnexpectedException(exceptionName, e);
						}
					}
				}
			}
//			logger.info("response {} {}", charset, content.length);
		}
	}
}

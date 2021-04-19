package yokwe.util.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;

public class Result {
	static final Logger logger = LoggerFactory.getLogger(Result.class);

	public static Map<String, Charset> charsetMap = new TreeMap<>();
	static {
		charsetMap.put("application/json", StandardCharsets.UTF_8);
	}
	
	public final Task         task;
	
	// Derived fields from message
	public final HttpResponse head;
	public final byte[]       body;
	
	public final ProtocolVersion version;
	public final int             code;
	
	public final ContentType     contentType;
	public final Charset         charset;    // derived charset from content type
	
	public Result(Task task, Message<HttpResponse, byte[]> message) {
		this.task    = task;
		
		this.head    = message.getHead();
		this.body    = message.getBody();
		
		this.version = head.getVersion();
		this.code    = head.getCode();
		
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
						// don't assume charset
						this.charset = null;
					}
				} else {
					this.charset = charset;
				}
			} else {
				logger.warn("no Content-Type header");
				this.contentType = null;
				this.charset     = null;
			}
		}
	}
	
	public Result(Task task, ClassicHttpResponse response) {
		this.task    = task;
//		this.message = message;
		
		this.head    = response;
		
		{
			byte[] byteArray;
			
			HttpEntity entity = response.getEntity();
			if (entity == null) {
				byteArray = null;
			} else {
				try {
					byteArray = EntityUtils.toByteArray(entity);
				} catch (IOException e) {
					byteArray = null;
				}
			}
			this.body = byteArray;
		}
		
		this.version = head.getVersion();
		this.code    = head.getCode();
		
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
				logger.warn("no Content-Type header");
				this.contentType = null;
				this.charset     = null;
			}
		}
	}

	public String getBodyAsString() {
		if (charset == null) {
			logger.error("charset is null");
			throw new UnexpectedException("charset is null");
		} else {
			return new String(body, charset);
		}
	}
}
package yokwe.finance.util.httputil;

import java.io.File;
import java.nio.charset.Charset;
import java.util.function.Supplier;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public final class HttpHelper {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
	public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded;charset=UTF-8";
	public enum ContentType {
		JSON(CONTENT_TYPE_JSON),
		FORM(CONTENT_TYPE_FORM);
		
		public final String value;
		ContentType(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
	
	public static final class Post {
		public static Post Json(String value) {
			return new Post(ContentType.JSON, value);
		}
		public static Post Form(String value) {
			return new Post(ContentType.FORM, value);
		}
		
		public final String contentType;
		public final String body;
		
		public Post(ContentType contentType, String body) {
			this.contentType = contentType.value;
			this.body        = body;
		}
	}
		
	
	public static Supplier<HttpUtil.Result> getSupplierString(String url) {
		return () -> HttpUtil.getInstance().download(url);
	}
	public static Supplier<HttpUtil.Result> getSupplierString(String url, Charset charset) {
		return () -> HttpUtil.getInstance().withCharset(charset).download(url);
	}
	public static Supplier<HttpUtil.Result> getSupplierString(String url, String referer) {
		return () -> HttpUtil.getInstance().withReferer(referer).download(url);
	}
	public static Supplier<HttpUtil.Result> getSupplierString(String url, Charset charset, String referer) {
		return () -> HttpUtil.getInstance().withCharset(charset).withReferer(referer).download(url);
	}
	public static Supplier<HttpUtil.Result> getSupplierString(String url, Post post) {
		return () -> HttpUtil.getInstance().withPost(post.body, post.contentType).download(url);
	}
	public static Supplier<HttpUtil.Result> getSupplierString(String url, Post post, String referer) {
		return () -> HttpUtil.getInstance().withPost(post.body, post.contentType).withReferer(referer).download(url);
	}
	//
	//
	//
	public static Supplier<HttpUtil.Result> getSupplierByteArray(String url) {
		return () -> HttpUtil.getInstance().withRawData(true).download(url);
	}
	public static Supplier<HttpUtil.Result> getSupplierByteArray(String url, Charset charset) {
		return () -> HttpUtil.getInstance().withRawData(true).withCharset(charset).download(url);
	}
	public static Supplier<HttpUtil.Result> getSupplierByteArray(String url, String referer) {
		return () -> HttpUtil.getInstance().withRawData(true).withReferer(referer).download(url);
	}
	public static Supplier<HttpUtil.Result> getSupplierByteArray(String url, Charset charset, String referer) {
		return () -> HttpUtil.getInstance().withRawData(true).withCharset(charset).withReferer(referer).download(url);
	}
	public static Supplier<HttpUtil.Result> getSupplierByteArray(String url, Post post) {
		return () -> HttpUtil.getInstance().withRawData(true).withPost(post.body, post.contentType).download(url);
	}
	public static Supplier<HttpUtil.Result> getSupplierByteArray(String url, Post post, String referer) {
		return () -> HttpUtil.getInstance().withRawData(true).withPost(post.body, post.contentType).withReferer(referer).download(url);
	}
	
	
	//
	// downloadString
	//
	public static String downloadString(Supplier<HttpUtil.Result> supplier) {
		HttpUtil.Result result = supplier.get();
		// sanity check
		if (result == null || result.result == null) {
			logger.error("Unexpected");
			logger.error("  result  {}", result);
			throw new UnexpectedException("Unexpected");
		}
		return result.result;
	}
	public static String downloadString(String url, Charset charset) {
		Supplier<HttpUtil.Result> supplier = getSupplierString(url, charset);
		return downloadString(supplier);
	}
	public static String downloadString(String url, String referer) {
		Supplier<HttpUtil.Result> supplier = getSupplierString(url, referer);
		return downloadString(supplier);
	}
	public static String downloadString(String url, Charset charset, String referer) {
		Supplier<HttpUtil.Result> supplier = getSupplierString(url, charset, referer);
		return downloadString(supplier);
	}
	public static String downloadString(String url, Post post) {
		Supplier<HttpUtil.Result> supplier = getSupplierString(url, post);
		return downloadString(supplier);
	}
	public static String downloadString(String url, Post post, String referer) {
		Supplier<HttpUtil.Result> supplier = getSupplierString(url, post, referer);
		return downloadString(supplier);
	}

	//
	// downloadString with file cache
	//
	public static String downloadString(Supplier<HttpUtil.Result> supplier, File file, boolean useFile) {
		if (useFile && file.exists()) {
			return FileUtil.read().file(file);
		} else {
			String string = downloadString(supplier).replace("\r", "\n"); // fix end of line  -- server is windows
			FileUtil.write().file(file, string);
			return string;
		}
	}
	public static String downloadString(String url, File file, boolean useFile) {
		Supplier<HttpUtil.Result> supplier = getSupplierString(url);
		return downloadString(supplier, file, useFile);
	}
	public static String downloadString(String url, Charset charset, File file, boolean useFile) {
		Supplier<HttpUtil.Result> supplier = getSupplierString(url, charset);
		return downloadString(supplier, file, useFile);
	}
	public static String downloadString(String url, Charset charset, String referer, File file, boolean useFile) {
		Supplier<HttpUtil.Result> supplier = getSupplierString(url, charset, referer);
		return downloadString(supplier, file, useFile);
	}
	public static String downloadString(String url, String referer, File file, boolean useFile) {
		Supplier<HttpUtil.Result> supplier = getSupplierString(url, referer);
		return downloadString(supplier, file, useFile);
	}
	public static String downloadString(String url, Post post, File file, boolean useFile) {
		Supplier<HttpUtil.Result> supplier =getSupplierString(url, post);
		return downloadString(supplier, file, useFile);
	}
	public static String downloadString(String url, Post post, String referer, File file, boolean useFile) {
		Supplier<HttpUtil.Result> supplier =getSupplierString(url, post, referer);
		return downloadString(supplier, file, useFile);
	}
	
	
	//
	// downloadByteArray
	//
	public static byte[] downloadByteArray(Supplier<HttpUtil.Result> supplier) {
		HttpUtil.Result result = supplier.get();
		// sanity check
		if (result == null || result.rawData == null) {
			logger.error("Unexpected result");
			logger.error("  result  {}", result);
			throw new UnexpectedException("Unexpected result");
		}
		return result.rawData;
	}
	public static byte[] downloadByteArray(String url) {
		Supplier<HttpUtil.Result> supplier = getSupplierByteArray(url);
		return downloadByteArray(supplier);
	}
	public static byte[] downloadByteArray(String url, String referer) {
		Supplier<HttpUtil.Result> supplier = getSupplierByteArray(url, referer);
		return downloadByteArray(supplier);
	}
	public static byte[] downloadByteArray(String url, Post post, File file) {
		Supplier<HttpUtil.Result> supplier = getSupplierByteArray(url, post);
		return downloadByteArray(supplier);
	}
	public static byte[] downloadByteArray(String url, Post post, String referer) {
		Supplier<HttpUtil.Result> supplier = getSupplierByteArray(url, post, referer);
		return downloadByteArray(supplier);
	}

	//
	// downloadByteArray with file cache
	//
	public static byte[] downloadByteArray(Supplier<HttpUtil.Result> supplier, File file, boolean useFile) {
		if (useFile && file.exists()) {
			return FileUtil.rawRead().file(file);
		} else {
			var result = downloadByteArray(supplier);
			FileUtil.rawWrite().file(file, result);
			return result;
		}
	}
	public static byte[] downloadByteArray(String url, File file, boolean useFile) {
		Supplier<HttpUtil.Result> supplier = getSupplierByteArray(url);
		return downloadByteArray(supplier, file, useFile);
	}
	public static byte[] downloadByteArray(String url, String referer, File file, boolean useFile) {
		Supplier<HttpUtil.Result> supplier = getSupplierByteArray(url, referer);
		return downloadByteArray(supplier, file, useFile);
	}
	public static byte[] downloadByteArray(String url, Post post, File file, boolean useFile) {
		Supplier<HttpUtil.Result> supplier = getSupplierByteArray(url, post);
		return downloadByteArray(supplier, file, useFile);
	}
	public static byte[] downloadByteArray(String url, Post post, String referer, File file, boolean useFile) {
		Supplier<HttpUtil.Result> supplier = getSupplierByteArray(url, post, referer);
		return downloadByteArray(supplier, file, useFile);
	}
	
}

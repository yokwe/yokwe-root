package yokwe.finance.util.httputil;

import java.io.File;
import java.nio.charset.Charset;
import java.util.function.Supplier;

import yokwe.util.FileUtil;

public class FileCache {
	public static String readString(Supplier<String> action, File file, boolean useFile) {
		if (useFile && file.canRead()) {
			return FileUtil.read().file(file);
		}
		var result = action.get();
		FileUtil.write().file(file, result);
		return result;
	}
	public static byte[] readByteArray(Supplier<byte[]> action, File file, boolean useFile) {
		if (useFile && file.canRead()) {
			return FileUtil.rawRead().file(file);
		}
		var result = action.get();
		FileUtil.rawWrite().file(file, result);
		return result;
	}
	public static String readString(Supplier<byte[]> action, Charset charset, File file, boolean useFile) {
		if (useFile && file.canRead()) {
			return FileUtil.read().file(file);
		}
		var result = action.get();
		var string = new String(result, charset);
		FileUtil.write().file(file, string);
		return string;
	}
}

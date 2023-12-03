package yokwe.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;


public class FileUtil {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final int BUFFER_SIZE = 65536;
	
	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	
	private static final char BOM_UTF_8 = '\uFEFF';

	private static class Context {
		private Charset charset = DEFAULT_CHARSET;		
	}
	
	public static Read read() {
		return new Read();
	}
	public static class Read {
		private final Context context;
		
		private Read() {
			context = new Context();
		}
		public Read withCharset(String newValue) {
			withCharset(Charset.forName(newValue));
			return this;
		}
		public Read withCharset(Charset newValue) {
			context.charset = newValue;
			return this;
		}
		
		public String file(File file) {
			char[] buffer = new char[BUFFER_SIZE];		
			try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), context.charset), buffer.length)) {
				StringBuilder ret = new StringBuilder();
				
				for(;;) {
					int len = br.read(buffer);
					if (len == -1) break;
					
					ret.append(buffer, 0, len);
				}
				// remove BOM_UTF_8
				if (0 < ret.length() && ret.charAt(0) == BOM_UTF_8) {
					return ret.substring(1);
				} else {
					return ret.toString();
				}
			} catch (IOException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}
		}
		public String file(String path) {
			return file(new File(path));
		}
	}
	
	public static RawRead rawRead() {
		return new RawRead();
	}
	public static class RawRead {
		private RawRead() {
		}
		
		public byte[] file(File file) {			
			byte[] buffer = new byte[BUFFER_SIZE];
			try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), buffer.length)) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				for(;;) {
					int len = bis.read(buffer);
					if (len == -1) break;
					
					baos.write(buffer, 0, len);
				}
				return baos.toByteArray();
			} catch (IOException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}
		}
		public byte[] file(String path) {
			return file(new File(path));
		}
	}
	
	public static Write write() {
		return new Write();
	}
	public static class Write {
		private final Context context;
		
		private Write() {
			context = new Context();
		}
		public Write withCharset(String newValue) {
			withCharset(Charset.forName(newValue));
			return this;
		}
		public Write withCharset(Charset newValue) {
			context.charset = newValue;
			return this;
		}
		
		public void file(File file, String content) {
			// Make parent directory if necessary.
			{
				File parent = file.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
			}
						
			try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), context.charset), BUFFER_SIZE)) {
				// remove BOM_UTF_8 
				bw.append(content.substring(content.charAt(0) == BOM_UTF_8 ? 1 : 0));
			} catch (IOException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}
		}
		public void file(String path, String content) {
			file (new File(path), content);
		}
	}
	
	public static RawWrite rawWrite() {
		return new RawWrite();
	}
	public static class RawWrite {
		private RawWrite() {
		}
		public void file(File file, InputStream is) {
			byte[] buffer = new byte[BUFFER_SIZE];
			
			// Make parent directory if necessary.
			{
				File parent = file.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
			}
			try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file), buffer.length)) {
				for(;;) {
					int len = is.read(buffer);
					if (len == -1) break;
					bos.write(buffer, 0, len);
				}
			} catch (IOException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}
		}
		public void file(File file, byte[] content) {
			InputStream is = new ByteArrayInputStream(content);
			file(file, is);
		}
		public void file(String path, byte[] content) {
			file (new File(path), content);
		}
	}
	
	
	//
	// listFile
	//
	public static List<File> listFile(File dir) {
		List<File> ret = new ArrayList<>();
		
		if (dir.isDirectory()) {
			for(File file: dir.listFiles()) {
				final String name = file.getName();
				// Skip special files -- assume we run on Unix
				if (name.equals("."))  continue;
				if (name.equals("..")) continue;

				if (file.isDirectory()) {
					ret.addAll(listFile(file));
				}
				if (file.isFile()) {
					ret.add(file);
				}
			}
		}
		
		return ret;
	}
	public static List<File> listFile(String dirPath) {
		return listFile(new File(dirPath));
	}
	
	
	//
	// md5FileMap md5Set
	//
	public static Map<File, String> md5FileMap(List<File> list) {
		Map<File, String> ret = new TreeMap<>();
		for(File file: list) {
			String md5 = StringUtil.toHexString(HashCode.getHashCode(file));
			ret.put(file, md5);
		}
		return ret;
	}
	public static Set<String> md5Set(List<File> list) {
		Map<File, String> map = md5FileMap(list);
		Set<String> ret = new TreeSet<>(map.values());
		return ret;
	}

	
	//
	// touch
	//
	public static void touch(File file) {
		try {
			if (file.exists()) {
				file.setLastModified(System.currentTimeMillis());
			} else {
				file.createNewFile();
			}
		} catch (IOException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	public static void touch(String path) {
		File file = new File(path);
		touch(file);
	}
	
	// delete
	public static void delete(File file) {
		if (file.isFile()) {
			file.delete();
		} else if (file.isDirectory()) {
			try (Stream<Path> walk = Files.walk(file.toPath())) {
			    walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			} catch (IOException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}
		} else {
			throw new UnexpectedException("Unexpected");
		}
	}
	
	//
	// move unknown file
	//
	public static void moveUnknownFile(Set<String> validNameSet, String dirString, String delistDirString) {
		moveUnknownFile(validNameSet, new File(dirString), new File(delistDirString));
	}
	public static void moveUnknownFile(Set<String> validNameSet, File dir, File delistDir) {
		delistDir.mkdir();
		Path delistDirPath = delistDir.toPath();

		for(var file: dir.listFiles()) {
			if (file.isDirectory()) continue;
			
			String name = file.getName();
			if (validNameSet.contains(name)) continue;

			try {
				logger.info("move unknonw file {} to {}", file.getPath(), delistDir.getPath());
				Files.move(file.toPath(), delistDirPath.resolve(name), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}
		}
	}
	public static void deleteUnknownFile(Set<String> validNameSet, String dirString) {
		deleteUnknownFile(validNameSet, new File(dirString));
	}
	public static void deleteUnknownFile(Set<String> validNameSet, File dir) {
		for(var file: dir.listFiles()) {
			if (file.isDirectory()) continue;
			
			String name = file.getName();
			if (validNameSet.contains(name)) continue;

			logger.info("delete unknown file {}", file.getPath());
			file.delete();
		}
	}

	//
	// convenience methods from File
	//
	public static boolean isDirectory(String stringPath) {
		Path path = Path.of(stringPath);
		return Files.isDirectory(path);
	}
	public static boolean canRead(String stringPath) {
		Path path = Path.of(stringPath);
		return Files.isReadable(path);
	}
	public static Instant getLastModified(String stringPath) {		
		try {
			Path path = Path.of(stringPath);
			return Files.getLastModifiedTime(path).toInstant();
		} catch (IOException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
}
